package sa.factory.iwanestatesrealest

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import sa.factory.iwanestatesrealest.data.repository.IwanEstatesRepositoryImpl
import sa.factory.iwanestatesrealest.engine.*
import sa.factory.iwanestatesrealest.hardware.HardwareSensorDriver
import sa.factory.iwanestatesrealest.model.IwanEstatesItem

class IwanEstatesRegressionTest {

    @Test
    fun testCheckoutCalculationAndTax() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = IwanEstatesRepositoryImpl(sensorDriver)

        val item1 = IwanEstatesItem("t1", "خدمة تجريبية أ", "الوصف", 100.0, "معتمد", "عام")
        val item2 = IwanEstatesItem("t2", "خدمة تجريبية ب", "الوصف", 200.0, "معتمد", "عام")

        repository.addToCart(item1)
        repository.addToCart(item2)

        val order = repository.checkout("Mada EMV")

        assertEquals(300.0, order.subtotalSar, 0.01)
        assertEquals(45.0, order.vatSar, 0.01)
        assertEquals(345.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)
    }

    @Test
    fun testSphericalToCartesianConversion() {
        val engine = LidarSpatialMeshProtocolEngine()

        // Test straight ahead along X axis (azimuth 0, elevation 0, 5 meters)
        val pointAhead = engine.sphericalToCartesian(0.0, 0.0, 5.0, 200)
        assertEquals(5.0, pointAhead.xMeters, 0.01)
        assertEquals(0.0, pointAhead.yMeters, 0.01)
        assertEquals(0.0, pointAhead.zMeters, 0.01)
        assertEquals(200, pointAhead.intensity)

        // Test directly upward along Z axis (elevation 90 deg, 3 meters)
        val pointUp = engine.sphericalToCartesian(0.0, 90.0, 3.0, 180)
        assertEquals(0.0, pointUp.xMeters, 0.01)
        assertEquals(0.0, pointUp.yMeters, 0.01)
        assertEquals(3.0, pointUp.zMeters, 0.01)
    }

    @Test
    fun testEuclideanDistanceCalculation() {
        val engine = LidarSpatialMeshProtocolEngine()

        val p1 = LidarPoint(0.0, 0.0, 0.0, 100)
        val p2 = LidarPoint(3.0, 4.0, 0.0, 100)

        // 3-4-5 right triangle in XY plane
        val distance = engine.computeEuclideanDistance(p1, p2)
        assertEquals(5.0, distance, 0.001)
    }

    @Test
    fun testRoomBoundaryReconstructionFromPointCloud() {
        val engine = LidarSpatialMeshProtocolEngine()

        val samplePoints = listOf(
            LidarPoint(0.0, 0.0, 0.0, 220),
            LidarPoint(10.0, 0.0, 0.0, 210),
            LidarPoint(10.0, 8.0, 0.0, 205),
            LidarPoint(0.0, 8.0, 0.0, 215),
            LidarPoint(5.0, 4.0, 3.5, 230)
        )

        val room = engine.reconstructRoomBoundary("ROOM-TEST-01", "قاعة العرض العقاري", samplePoints)

        assertEquals("ROOM-TEST-01", room.roomId)
        assertEquals(80.0, room.floorAreaSqM, 0.01) // 10m * 8m = 80m2
        assertEquals(3.5, room.ceilingHeightM, 0.01) // 3.5m - 0.0m = 3.5m
        assertEquals(280.0, room.roomVolumeCuM, 0.01) // 80m2 * 3.5m = 280m3
        assertEquals(36.0, room.wallPerimeterM, 0.01) // 2 * (10 + 8) = 36m
        assertEquals(5, room.pointCount)
    }

    @Test
    fun testTotalScannedAreaAggregation() {
        val engine = LidarSpatialMeshProtocolEngine()

        // Seed rooms: Majlis (85.5m2) + Suite (64.0m2) + Salon (112.0m2) = 261.5m2
        val totalArea = engine.getTotalScannedAreaSqM()
        assertEquals(261.5, totalArea, 0.1)
    }

    @Test
    fun testPropertyValuationAssessmentHedonicCalculation() {
        val engine = PropertyValuationBimEngine()

        val assessment = engine.assessProperty(
            valuationId = "VAL-TEST-01",
            title = "بنتهاوس النخيل الفاخر",
            district = SaudiDistrictZone.AL_NAKHEEL_RIYADH,
            finishTier = PropertyFinishTier.PREMIUM_MODERN,
            totalAreaSqM = 300.0,
            averageCeilingHeightM = 3.0
        )

        // 300m2 * 11000 SAR/m2 * 1.20 tier * 1.0 ceiling bonus = 3,960,000 SAR
        assertEquals(3960000.0, assessment.estimatedMarketValueSar, 100.0)
        assertEquals(6.8, assessment.grossRentalYieldPct, 0.01)
        assertEquals(269280.0, assessment.estimatedAnnualRentalSar, 100.0)
    }

    @Test
    fun testCeilingHeightPremiumScaling() {
        val engine = PropertyValuationBimEngine()

        val standardCeiling = engine.assessProperty(
            valuationId = "VAL-STD-01",
            title = "فيلا عادية",
            district = SaudiDistrictZone.AL_MALQA_RIYADH,
            finishTier = PropertyFinishTier.PREMIUM_MODERN,
            totalAreaSqM = 200.0,
            averageCeilingHeightM = 3.0
        )

        val highCeiling = engine.assessProperty(
            valuationId = "VAL-HIGH-01",
            title = "فيلا ذات أسقف مرتفعة",
            district = SaudiDistrictZone.AL_MALQA_RIYADH,
            finishTier = PropertyFinishTier.PREMIUM_MODERN,
            totalAreaSqM = 200.0,
            averageCeilingHeightM = 4.0
        )

        // High ceiling has +6% bonus per meter above 3.0m
        assertTrue(highCeiling.estimatedMarketValueSar > standardCeiling.estimatedMarketValueSar)
        assertEquals(1.06, highCeiling.estimatedMarketValueSar / standardCeiling.estimatedMarketValueSar, 0.01)
    }

    @Test
    fun testPortfolioAggregation() {
        val engine = PropertyValuationBimEngine()

        val totalPortfolioSar = engine.getTotalPortfolioValueSar()
        assertTrue(totalPortfolioSar > 10000000.0) // Seeded portfolios exceed 10M SAR
        assertTrue(engine.getValuationCount() >= 2)
    }

    @Test
    fun testHardwareSensorDriverEmission() = runBlocking {
        val driver = HardwareSensorDriver()
        val frames = driver.observePeripheralStream().take(2).toList()

        assertEquals(2, frames.size)
        assertEquals("360 LiDAR Spatial Scanner + Matterport Pro3", frames[0].peripheralName)
        assertEquals("CONNECTING", frames[0].connectionState)
        assertEquals("ONLINE", frames[1].connectionState)
    }
}
