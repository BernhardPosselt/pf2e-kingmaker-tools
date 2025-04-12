package at.posselt.pfrpg2e.data.regions

import at.posselt.pfrpg2e.data.ValueEnum
import at.posselt.pfrpg2e.localization.Translatable
import at.posselt.pfrpg2e.toCamelCase

enum class WeatherEffect: Translatable, ValueEnum {
    NONE,
    SNOW,
    RAIN,
    SUNNY,
    LEAVES,
    RAIN_STORM,
    FOG,
    BLIZZARD;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "weatherEffect.$value"
}

enum class Season: Translatable, ValueEnum {
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "season.$value"
}

enum class Month: Translatable, ValueEnum {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "month.$value"
}

data class Climate(
    val coldDc: Int? = null,
    val precipitationDc: Int? = null,
    val month: Month,
    val season: Season,
    val weatherEventDc: Int? = 18
)

enum class WeatherType: Translatable, ValueEnum {
    COLD,
    SNOWY,
    RAINY,
    SUNNY;

    override val value: String
        get() = toCamelCase()

    override val i18nKey: String
        get() = "weatherType.$value"
}

fun getMonth(index: Int): Month =
    Month.entries[index]

fun findWeatherType(isCold: Boolean, hasPrecipitation: Boolean) =
    if (isCold && hasPrecipitation) {
        WeatherType.SNOWY
    } else if (isCold) {
        WeatherType.COLD
    } else if (hasPrecipitation) {
        WeatherType.RAINY
    } else {
        WeatherType.SUNNY
    }
