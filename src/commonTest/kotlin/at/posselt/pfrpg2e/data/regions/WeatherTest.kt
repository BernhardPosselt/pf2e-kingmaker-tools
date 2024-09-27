package at.posselt.pfrpg2e.data.regions

import kotlin.test.Test
import kotlin.test.assertEquals

class WeatherTest {
    @Test
    fun weatherType() {
        assertEquals(WeatherType.SNOWY, findWeatherType(true, true))
        assertEquals(WeatherType.RAINY, findWeatherType(false, true))
        assertEquals(WeatherType.COLD, findWeatherType(true, false))
        assertEquals(WeatherType.SUNNY, findWeatherType(false, false))
    }
}