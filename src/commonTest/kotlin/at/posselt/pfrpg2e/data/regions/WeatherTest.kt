package at.posselt.pfrpg2e.data.regions

import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherTest {
    @Test
    fun weatherType() {
        assertEquals(WeatherType.SNOWY, findWeatherType(isCold = true, hasPrecipitation = true))
        assertEquals(WeatherType.RAINY, findWeatherType(isCold = false, hasPrecipitation = true))
        assertEquals(WeatherType.COLD, findWeatherType(isCold = true, hasPrecipitation = false))
        assertEquals(WeatherType.SUNNY, findWeatherType(isCold = false, hasPrecipitation = false))
    }
}