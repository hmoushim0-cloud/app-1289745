package sa.factory.iwanestatesrealest.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.iwanestatesrealest.engine.LidarSpatialMeshProtocolEngine
import sa.factory.iwanestatesrealest.engine.PropertyValuationBimEngine

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver(
    val lidarEngine: LidarSpatialMeshProtocolEngine = LidarSpatialMeshProtocolEngine(),
    val valuationEngine: PropertyValuationBimEngine = PropertyValuationBimEngine()
) {
    private val peripheral = "360 LiDAR Spatial Scanner + Matterport Pro3"

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "LiDAR Controller: Optical transceiver & IMU calibration initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "360 LiDAR Spatial Scanner + Matterport Pro3: ONLINE | READY"))

        val sampleRooms = listOf("ROOM-MAJLIS-01", "ROOM-SUITE-02", "ROOM-SALON-03")
        var frameIndex = 0

        while (true) {
            delay(1500)
            frameIndex++

            val roomId = sampleRooms[frameIndex % sampleRooms.size]
            val room = lidarEngine.getRoom(roomId)
            val totalScannedArea = lidarEngine.getTotalScannedAreaSqM()
            val totalPortfolioSar = valuationEngine.getTotalPortfolioValueSar()

            val payload = "LIDAR: ACTIVE [Room: ${room?.roomType ?: "صالون"}] | Area: ${room?.floorAreaSqM ?: 0.0}m2 | Height: ${room?.ceilingHeightM ?: 0.0}m | Total Mapped: ${String.format("%.1f", totalScannedArea)}m2 | Portfolio: ${String.format("%.0f", totalPortfolioSar)} SAR"

            emit(HardwareTelemetryFrame(
                peripheralName = peripheral,
                connectionState = "ACTIVE_STREAMING",
                livePayload = payload
            ))
        }
    }
}
