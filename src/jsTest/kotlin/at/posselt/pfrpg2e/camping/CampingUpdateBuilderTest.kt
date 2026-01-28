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
            "$campingPath.actorUuids" to arrayOf("test"),
            "$campingPath.-=currentRegion" to null,
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
            "$campingPath.cooking.actorMeals" to arrayOf(
                ActorMeal(
                    actorUuid = "actor",
                    chosenMeal = "meal",
                )
            )
        )
        val result = buildCampingUpdate {
            cooking.actorMeals.set(
                arrayOf(
                    ActorMeal(
                        actorUuid = "actor",
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
            "$campingPath.cooking.actorMeals" to arrayOf(
                ActorMeal(
                    actorUuid = "actor",
                    chosenMeal = "meal",
                )
            )
        )
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    arrayOf(
                        ActorMeal(
                            actorUuid = "actor",
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
            "flags.pf2e-kingmaker-tools.camping-sheet.-=cooking" to null
        )
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    arrayOf(
                        ActorMeal(
                            actorUuid = "actor",
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