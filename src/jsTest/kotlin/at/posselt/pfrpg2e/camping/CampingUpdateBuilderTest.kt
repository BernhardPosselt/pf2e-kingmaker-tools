package at.posselt.pfrpg2e.camping

import js.objects.recordOf
import js.objects.unsafeJso
import kotlin.test.Test
import kotlin.test.assertEquals

class CampingUpdateBuilderTest {

    @Test
    fun testEmpty() {
        val expected = unsafeJso<Any>()
        val result = buildCampingUpdate {

        }
        assertEquals(JSON.stringify(expected), JSON.stringify(result))
    }

    @Test
    fun testDelete() {
        val expected = recordOf(
            "actorUuids" to arrayOf("test"),
            "-=currentRegion" to null,
        )
        val result = buildCampingUpdate {
            actorUuids.set(arrayOf("test"))
            currentRegion.set("test")
            currentRegion.delete()
        }
        assertEquals(JSON.stringify(expected), JSON.stringify(result))
    }

    @Test
    fun testActorMeals() {
        val expected = recordOf(
            "cooking.actorMeals" to recordOf(
                "actor" to ActorMeal(
                    chosenMeal = "meal",
                )
            )
        )
        val result = buildCampingUpdate {
            cooking.actorMeals.set(
                recordOf(
                    "actor" to ActorMeal(
                        chosenMeal = "meal",
                    )
                )
            )
        }
        assertEquals(JSON.stringify(expected), JSON.stringify(result))
    }

    @Test
    fun testNestedCooking() {
        val expected = recordOf(
            "cooking.actorMeals" to recordOf(
                "actor" to ActorMeal(
                    chosenMeal = "meal",
                )
            )
        )
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "actor" to ActorMeal(
                            chosenMeal = "meal",
                        )
                    )
                )
            }
        }
        assertEquals(JSON.stringify(expected), JSON.stringify(result))
    }

    @Test
    fun deletingParentPropertyDeletesNested() {
        val expected = recordOf(
            "-=cooking" to null
        )
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "actor" to ActorMeal(
                            chosenMeal = "meal",
                        )
                    )
                )
            }
            cooking.delete()
        }
        assertEquals(JSON.stringify(expected), JSON.stringify(result))
    }
}