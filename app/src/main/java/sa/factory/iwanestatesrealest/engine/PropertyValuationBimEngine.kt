package sa.factory.iwanestatesrealest.engine

import java.util.concurrent.ConcurrentHashMap

enum class SaudiDistrictZone(
    val districtNameAr: String,
    val city: String,
    val basePricePerSqMSar: Double
) {
    AL_MALQA_RIYADH("حي الملقا الراقي", "الرياض", 8500.0),
    HITTIN_RIYADH("حي حطين النموذجي", "الرياض", 9800.0),
    AL_NAKHEEL_RIYADH("حي النخيل الدبلوماسي", "الرياض", 11000.0),
    AL_SHATI_JEDDAH("حي الشاطئ الذهبي", "جدة", 8900.0),
    AL_KHOZAMA_RIYADH("حي الخزامى الفاخر", "الرياض", 10200.0)
}

enum class PropertyFinishTier(val multiplier: Double, val tierLabelAr: String) {
    SUPER_DELUXE(1.35, "تشطيب سوبر ديلوكس رخامي"),
    ULTRA_LUXURY_SMART(1.60, "قصر ذكي فائق الفخامة"),
    PREMIUM_MODERN(1.20, "تشطيب مودرن حديث")
}

data class PropertyValuationAssessment(
    val valuationId: String,
    val title: String,
    val district: SaudiDistrictZone,
    val finishTier: PropertyFinishTier,
    val totalAreaSqM: Double,
    val averageCeilingHeightM: Double,
    val estimatedMarketValueSar: Double,
    val estimatedAnnualRentalSar: Double,
    val grossRentalYieldPct: Double,
    val lidarConfidenceIndex: Double
)

class PropertyValuationBimEngine {

    private val valuations = ConcurrentHashMap<String, PropertyValuationAssessment>()

    init {
        // Seed default luxury property valuations for demonstration
        assessProperty(
            valuationId = "VAL-MALQA-01",
            title = "فيلا إيوان الملقا المودرن",
            district = SaudiDistrictZone.AL_MALQA_RIYADH,
            finishTier = PropertyFinishTier.ULTRA_LUXURY_SMART,
            totalAreaSqM = 480.0,
            averageCeilingHeightM = 4.0,
            lidarConfidenceIndex = 98.5
        )
        assessProperty(
            valuationId = "VAL-HITTIN-02",
            title = "قصر حطين البانورامي",
            district = SaudiDistrictZone.HITTIN_RIYADH,
            finishTier = PropertyFinishTier.SUPER_DELUXE,
            totalAreaSqM = 750.0,
            averageCeilingHeightM = 4.2,
            lidarConfidenceIndex = 99.0
        )
    }

    fun assessProperty(
        valuationId: String,
        title: String,
        district: SaudiDistrictZone,
        finishTier: PropertyFinishTier,
        totalAreaSqM: Double,
        averageCeilingHeightM: Double,
        lidarConfidenceIndex: Double = 98.0
    ): PropertyValuationAssessment {
        val basePricePerSqM = district.basePricePerSqMSar
        val tierMultiplier = finishTier.multiplier

        // High ceiling premium: +3% per 0.5m above 3.0m baseline
        val ceilingBonus = if (averageCeilingHeightM > 3.0) {
            1.0 + ((averageCeilingHeightM - 3.0) * 0.06)
        } else {
            1.0
        }

        val effectivePricePerSqM = basePricePerSqM * tierMultiplier * ceilingBonus
        val marketValue = Math.round(totalAreaSqM * effectivePricePerSqM * 100.0) / 100.0

        // Estimated annual rental in Saudi prime markets is typically 6.0% - 7.5% of asset value
        val grossRentalYieldPct = 6.8
        val annualRent = Math.round(marketValue * (grossRentalYieldPct / 100.0) * 100.0) / 100.0

        val assessment = PropertyValuationAssessment(
            valuationId = valuationId,
            title = title,
            district = district,
            finishTier = finishTier,
            totalAreaSqM = totalAreaSqM,
            averageCeilingHeightM = averageCeilingHeightM,
            estimatedMarketValueSar = marketValue,
            estimatedAnnualRentalSar = annualRent,
            grossRentalYieldPct = grossRentalYieldPct,
            lidarConfidenceIndex = lidarConfidenceIndex
        )

        valuations[valuationId] = assessment
        return assessment
    }

    fun getAssessment(valuationId: String): PropertyValuationAssessment? = valuations[valuationId]

    fun getTotalPortfolioValueSar(): Double = valuations.values.sumOf { it.estimatedMarketValueSar }

    fun getValuationCount(): Int = valuations.size
}
