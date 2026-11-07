package sa.factory.iwanestatesrealest.engine

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LidarPoint(
    val xMeters: Double,
    val yMeters: Double,
    val zMeters: Double,
    val intensity: Int
)

data class RoomSpatialBoundary(
    val roomId: String,
    val roomType: String,
    val floorAreaSqM: Double,
    val ceilingHeightM: Double,
    val roomVolumeCuM: Double,
    val wallPerimeterM: Double,
    val pointCount: Int,
    val scanQualityScore: Double
)

data class LidarRawScanFrame(
    val frameIndex: Long,
    val azimuthDegrees: Double,
    val elevationDegrees: Double,
    val distanceMm: Int,
    val reflectivity: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class LidarSpatialMeshProtocolEngine {

    private val activeRooms = ConcurrentHashMap<String, RoomSpatialBoundary>()
    private var totalPointsProcessed: Long = 0

    init {
        // Seed default luxury estate spatial scans (Majlis, Master Suite, Grand Salon)
        registerRoomScan(
            RoomSpatialBoundary(
                roomId = "ROOM-MAJLIS-01",
                roomType = "مجلس الضيافة الرئيسي",
                floorAreaSqM = 85.5,
                ceilingHeightM = 4.2,
                roomVolumeCuM = 359.1,
                wallPerimeterM = 38.0,
                pointCount = 14200,
                scanQualityScore = 98.5
            )
        )
        registerRoomScan(
            RoomSpatialBoundary(
                roomId = "ROOM-SUITE-02",
                roomType = "جناح النوم الماستر",
                floorAreaSqM = 64.0,
                ceilingHeightM = 3.8,
                roomVolumeCuM = 243.2,
                wallPerimeterM = 32.0,
                pointCount = 9800,
                scanQualityScore = 96.0
            )
        )
        registerRoomScan(
            RoomSpatialBoundary(
                roomId = "ROOM-SALON-03",
                roomType = "الصالون البانورامي",
                floorAreaSqM = 112.0,
                ceilingHeightM = 4.5,
                roomVolumeCuM = 504.0,
                wallPerimeterM = 44.0,
                pointCount = 18500,
                scanQualityScore = 99.2
            )
        )
    }

    fun registerRoomScan(boundary: RoomSpatialBoundary) {
        activeRooms[boundary.roomId] = boundary
    }

    fun sphericalToCartesian(azimuthDeg: Double, elevationDeg: Double, distanceM: Double, intensity: Int): LidarPoint {
        val azRad = Math.toRadians(azimuthDeg)
        val elRad = Math.toRadians(elevationDeg)

        val x = distanceM * cos(elRad) * cos(azRad)
        val y = distanceM * cos(elRad) * sin(azRad)
        val z = distanceM * sin(elRad)

        return LidarPoint(
            xMeters = (Math.round(x * 1000.0) / 1000.0),
            yMeters = (Math.round(y * 1000.0) / 1000.0),
            zMeters = (Math.round(z * 1000.0) / 1000.0),
            intensity = intensity
        )
    }

    fun computeEuclideanDistance(p1: LidarPoint, p2: LidarPoint): Double {
        val dx = p1.xMeters - p2.xMeters
        val dy = p1.yMeters - p2.yMeters
        val dz = p1.zMeters - p2.zMeters
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun reconstructRoomBoundary(
        roomId: String,
        roomType: String,
        points: List<LidarPoint>
    ): RoomSpatialBoundary {
        require(points.isNotEmpty()) { "Point cloud cannot be empty for spatial reconstruction" }

        totalPointsProcessed += points.size

        val minX = points.minOf { it.xMeters }
        val maxX = points.maxOf { it.xMeters }
        val minY = points.minOf { it.yMeters }
        val maxY = points.maxOf { it.yMeters }
        val minZ = points.minOf { it.zMeters }
        val maxZ = points.maxOf { it.zMeters }

        val widthM = (maxX - minX).coerceAtLeast(0.1)
        val lengthM = (maxY - minY).coerceAtLeast(0.1)
        val ceilingHeightM = (maxZ - minZ).coerceAtLeast(0.1)

        val floorAreaSqM = Math.round(widthM * lengthM * 100.0) / 100.0
        val roomVolumeCuM = Math.round(floorAreaSqM * ceilingHeightM * 100.0) / 100.0
        val wallPerimeterM = Math.round(2.0 * (widthM + lengthM) * 100.0) / 100.0

        val scanQuality = (points.size.toDouble() / 100.0).coerceIn(80.0, 100.0)

        val boundary = RoomSpatialBoundary(
            roomId = roomId,
            roomType = roomType,
            floorAreaSqM = floorAreaSqM,
            ceilingHeightM = ceilingHeightM,
            roomVolumeCuM = roomVolumeCuM,
            wallPerimeterM = wallPerimeterM,
            pointCount = points.size,
            scanQualityScore = scanQuality
        )

        registerRoomScan(boundary)
        return boundary
    }

    fun getTotalScannedAreaSqM(): Double {
        return activeRooms.values.sumOf { it.floorAreaSqM }
    }

    fun getRoom(roomId: String): RoomSpatialBoundary? = activeRooms[roomId]

    fun getTotalPointsProcessed(): Long = totalPointsProcessed
}
