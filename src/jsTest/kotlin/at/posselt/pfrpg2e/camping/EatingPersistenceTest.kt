package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import js.objects.recordOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for eating-state persistence: Cooking data structure defaults,
 * buildCampingUpdate cooking DSL round-trips, and ParsedRecipeResult DC logic.
 *
 * NOTE: After buildCampingUpdate, cooking.actorMeals and cooking.results are
 * stored as plain JS objects (dynamic). Access uses .asDynamic() to navigate
 * the nested property path since Kotlin/JS can't statically type-check arbitrary
 * property keys.
 */
class EatingPersistenceTest {

    private fun testRecipe(
        id: String = "test-recipe",
        name: String = "Test Recipe",
        level: Int = 2,
        rarity: String = "common",
        basicIngredients: Int = 2,
        specialIngredients: Int = 0,
        cookingLoreDC: Int = 15,
        survivalDC: Int = 17,
        cost: RawCost = RawCost(value = 5, currency = "gp"),
        isSpecialMeal: Boolean = false,
        isHomebrew: Boolean = false,
        requirements: String? = null,
        favoriteMeal: CookingOutcome? = null,
    ): RecipeData {
        val outcome = CookingOutcome(effects = emptyArray(), chooseRandomly = null, message = null)
        return RecipeData(
            id = id,
            name = name,
            level = level,
            rarity = rarity,
            basicIngredients = basicIngredients,
            specialIngredients = specialIngredients,
            cookingLoreDC = cookingLoreDC,
            survivalDC = survivalDC,
            uuid = "Compendium.test",
            icon = null,
            cost = cost,
            isSpecialMeal = isSpecialMeal,
            isHomebrew = isHomebrew,
            requirements = requirements,
            criticalSuccess = outcome,
            success = outcome,
            criticalFailure = outcome,
            favoriteMeal = favoriteMeal,
        )
    }

    // ==========================================
    // Cooking data structure defaults
    // ==========================================

    @Test
    fun testCookingDefaultKnownRecipesIsEmpty() {
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        assertEquals(0, cooking.knownRecipes.size)
    }

    @Test
    fun testCookingDefaultHomebrewMealsIsEmpty() {
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        assertEquals(0, cooking.homebrewMeals.size)
    }

    @Test
    fun testCookingDefaultMinimumSubsistenceIsZero() {
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        assertEquals(0, cooking.minimumSubsistence)
    }

    @Test
    fun testCookingStoresKnownRecipes() {
        val cooking = Cooking(
            knownRecipes = arrayOf("basic-meal", "hearty-meal", "haggis"),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        assertEquals(3, cooking.knownRecipes.size)
        assertTrue("basic-meal" in cooking.knownRecipes)
        assertTrue("hearty-meal" in cooking.knownRecipes)
        assertTrue("haggis" in cooking.knownRecipes)
    }

    @Test
    fun testCookingStoresMinimumSubsistence() {
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 3,
        )
        assertEquals(3, cooking.minimumSubsistence)
    }

    @Test
    fun testCookingStoresActorMealsByUuid() {
        val meals = recordOf(
            "actor-1" to ActorMeal(
                actorUuid = "actor-1",
                favoriteMeal = "haggis",
                chosenMeal = "hearty-meal",
            ),
            "actor-2" to ActorMeal(
                actorUuid = "actor-2",
                favoriteMeal = null,
                chosenMeal = "rationsOrSubsistence",
            ),
        )
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = meals,
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        assertEquals("hearty-meal", cooking.actorMeals["actor-1"]?.chosenMeal)
        assertEquals("haggis", cooking.actorMeals["actor-1"]?.favoriteMeal)
        assertEquals("rationsOrSubsistence", cooking.actorMeals["actor-2"]?.chosenMeal)
        assertNull(cooking.actorMeals["actor-2"]?.favoriteMeal)
    }

    @Test
    fun testCookingStoresResultsByRecipeId() {
        val results = recordOf(
            "hearty-meal" to CookingResult(result = "criticalSuccess", skill = "survival"),
            "haggis" to CookingResult(result = null, skill = "cooking"),
        )
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = results,
            minimumSubsistence = 0,
        )
        assertEquals("criticalSuccess", cooking.results["hearty-meal"]?.result)
        assertEquals("survival", cooking.results["hearty-meal"]?.skill)
        assertNull(cooking.results["haggis"]?.result)
        assertEquals("cooking", cooking.results["haggis"]?.skill)
    }

    @Test
    fun testCookingStoresHomebrewMeals() {
        val homebrew = arrayOf(
            testRecipe(id = "my-custom-stew", name = "My Custom Stew", isHomebrew = true),
            testRecipe(id = "secret-soup", name = "Secret Soup", isHomebrew = true),
        )
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = homebrew,
            results = recordOf(),
            minimumSubsistence = 0,
        )
        assertEquals(2, cooking.homebrewMeals.size)
        assertEquals("my-custom-stew", cooking.homebrewMeals[0].id)
        assertEquals("secret-soup", cooking.homebrewMeals[1].id)
    }

    @Test
    fun testCookingDefaultActorMealsKeyCount() {
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        val count = js("Object.keys(cooking.actorMeals).length") as Int
        assertEquals(0, count)
    }

    @Test
    fun testCookingDefaultResultsKeyCount() {
        val cooking = Cooking(
            knownRecipes = emptyArray(),
            actorMeals = recordOf(),
            homebrewMeals = emptyArray(),
            results = recordOf(),
            minimumSubsistence = 0,
        )
        val count = js("Object.keys(cooking.results).length") as Int
        assertEquals(0, count)
    }

    // ==========================================
    // ActorMeal data variants (the 3 meal choices)
    // ==========================================

    @Test
    fun testActorMealWithNothingChoice() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = null,
            chosenMeal = "nothing",
        )
        assertEquals("nothing", meal.chosenMeal)
        assertNull(meal.favoriteMeal)
    }

    @Test
    fun testActorMealWithRationsChoice() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = null,
            chosenMeal = "rationsOrSubsistence",
        )
        assertEquals("rationsOrSubsistence", meal.chosenMeal)
    }

    @Test
    fun testActorMealWithRecipeChoice() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = "haggis",
            chosenMeal = "black-linnorm-stew",
        )
        assertEquals("black-linnorm-stew", meal.chosenMeal)
        assertEquals("haggis", meal.favoriteMeal)
    }

    @Test
    fun testActorMealFavoriteCanBeNull() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = null,
            chosenMeal = "hearty-meal",
        )
        assertNull(meal.favoriteMeal)
        assertEquals("hearty-meal", meal.chosenMeal)
    }

    @Test
    fun testActorMealFavoriteCanDifferFromChosen() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = "black-linnorm-stew",
            chosenMeal = "hearty-meal",
        )
        assertEquals("black-linnorm-stew", meal.favoriteMeal)
        assertEquals("hearty-meal", meal.chosenMeal)
        assertFalse(meal.favoriteMeal == meal.chosenMeal)
    }

    // ==========================================
    // buildCampingUpdate cooking DSL round-trips
    // ==========================================

    @Test
    fun testBuildCookingUpdateSetsKnownRecipes() {
        val result = buildCampingUpdate {
            cooking.knownRecipes.set(arrayOf("basic-meal", "hearty-meal", "haggis"))
        }
        @Suppress("UNCHECKED_CAST")
        val recipes = result["cooking.knownRecipes"] as Array<String>
        assertEquals(3, recipes.size)
        assertEquals("basic-meal", recipes[0])
        assertEquals("hearty-meal", recipes[1])
        assertEquals("haggis", recipes[2])
    }

    @Test
    fun testBuildCookingUpdateSetsMinimumSubsistence() {
        val result = buildCampingUpdate {
            cooking.minimumSubsistence.set(5)
        }
        assertEquals(5, result["cooking.minimumSubsistence"])
    }

    @Test
    fun testBuildCookingUpdateSetsHomebrewMeals() {
        val homebrew = arrayOf(
            testRecipe(id = "custom-stew", name = "Custom Stew", isHomebrew = true),
        )
        val result = buildCampingUpdate {
            cooking.homebrewMeals.set(homebrew)
        }
        @Suppress("UNCHECKED_CAST")
        val stored = result["cooking.homebrewMeals"] as Array<RecipeData>
        assertEquals(1, stored.size)
        assertEquals("custom-stew", stored[0].id)
    }

    @Test
    fun testBuildCookingUpdateSetsHomebrewMealsEmpty() {
        val result = buildCampingUpdate {
            cooking.homebrewMeals.set(emptyArray())
        }
        @Suppress("UNCHECKED_CAST")
        val stored = result["cooking.homebrewMeals"] as Array<RecipeData>
        assertEquals(0, stored.size)
    }

    @Test
    fun testBuildCookingUpdateSetsActorMealViaNestedInvoke() {
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "actor-1" to ActorMeal(
                            actorUuid = "actor-1",
                            chosenMeal = "haggis",
                            favoriteMeal = "hearty-meal",
                        ),
                    ),
                )
            }
        }
        // Access nested dynamic JS object
        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("haggis", actorMeals["actor-1"].chosenMeal)
        assertEquals("hearty-meal", actorMeals["actor-1"].favoriteMeal)
        assertEquals("actor-1", actorMeals["actor-1"].actorUuid)
    }

    @Test
    fun testBuildCookingUpdateSetsActorMealChoiceNothing() {
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "fasting-actor" to ActorMeal(
                            actorUuid = "fasting-actor",
                            chosenMeal = "nothing",
                            favoriteMeal = null,
                        ),
                    ),
                )
            }
        }
        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("nothing", actorMeals["fasting-actor"].chosenMeal)
        assertEquals(null, actorMeals["fasting-actor"].favoriteMeal)
    }

    @Test
    fun testBuildCookingUpdateSetsActorMealChoiceRations() {
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "rations-actor" to ActorMeal(
                            actorUuid = "rations-actor",
                            chosenMeal = "rationsOrSubsistence",
                            favoriteMeal = "haggis",
                        ),
                    ),
                )
            }
        }
        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("rationsOrSubsistence", actorMeals["rations-actor"].chosenMeal)
        assertEquals("haggis", actorMeals["rations-actor"].favoriteMeal)
    }

    @Test
    fun testBuildCookingUpdateSetsActorMealChoiceRecipe() {
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "chef-actor" to ActorMeal(
                            actorUuid = "chef-actor",
                            chosenMeal = "black-linnorm-stew",
                            favoriteMeal = null,
                        ),
                    ),
                )
            }
        }
        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("black-linnorm-stew", actorMeals["chef-actor"].chosenMeal)
        assertEquals(null, actorMeals["chef-actor"].favoriteMeal)
    }

    @Test
    fun testBuildCookingUpdateSetsMultipleActorMeals() {
        val result = buildCampingUpdate {
            cooking {
                actorMeals.set(
                    recordOf(
                        "a1" to ActorMeal(
                            actorUuid = "a1",
                            chosenMeal = "hearty-meal",
                            favoriteMeal = "haggis",
                        ),
                        "a2" to ActorMeal(
                            actorUuid = "a2",
                            chosenMeal = "rationsOrSubsistence",
                            favoriteMeal = null,
                        ),
                        "a3" to ActorMeal(
                            actorUuid = "a3",
                            chosenMeal = "nothing",
                            favoriteMeal = "hearty-meal",
                        ),
                    ),
                )
            }
        }
        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("hearty-meal", actorMeals["a1"].chosenMeal)
        assertEquals("haggis", actorMeals["a1"].favoriteMeal)
        assertEquals("rationsOrSubsistence", actorMeals["a2"].chosenMeal)
        assertEquals(null, actorMeals["a2"].favoriteMeal)
        assertEquals("nothing", actorMeals["a3"].chosenMeal)
        assertEquals("hearty-meal", actorMeals["a3"].favoriteMeal)
    }

    @Test
    fun testBuildCookingUpdateSetsResultsViaNestedInvoke() {
        val result = buildCampingUpdate {
            cooking {
                results.set(
                    recordOf(
                        "hearty-meal" to CookingResult(result = "criticalSuccess", skill = "survival"),
                        "haggis" to CookingResult(result = null, skill = "cooking"),
                    ),
                )
            }
        }
        val results = result["cooking.results"].asDynamic()
        assertEquals("criticalSuccess", results["hearty-meal"].result)
        assertEquals("survival", results["hearty-meal"].skill)
        assertEquals(null, results["haggis"].result)
        assertEquals("cooking", results["haggis"].skill)
    }

    @Test
    fun testBuildCookingUpdateSetsMultipleResults() {
        val result = buildCampingUpdate {
            cooking {
                results.set(
                    recordOf(
                        "haggis" to CookingResult(result = "success", skill = "cooking"),
                        "hearty-meal" to CookingResult(result = "criticalSuccess", skill = "survival"),
                        "basic-meal" to CookingResult(result = null, skill = "cooking"),
                    ),
                )
            }
        }
        val results = result["cooking.results"].asDynamic()
        assertEquals("success", results["haggis"].result)
        assertEquals("cooking", results["haggis"].skill)
        assertEquals("criticalSuccess", results["hearty-meal"].result)
        assertEquals("survival", results["hearty-meal"].skill)
        assertEquals(null, results["basic-meal"].result)
        assertEquals("cooking", results["basic-meal"].skill)
    }

    @Test
    fun testBuildCookingUpdateFullRoundTrip() {
        val homebrew = arrayOf(
            testRecipe(id = "custom-stew", name = "Custom Stew", isHomebrew = true),
        )
        val result = buildCampingUpdate {
            cooking {
                knownRecipes.set(arrayOf("basic-meal", "hearty-meal", "haggis"))
                minimumSubsistence.set(2)
                actorMeals.set(
                    recordOf(
                        "actor-1" to ActorMeal(
                            actorUuid = "actor-1",
                            chosenMeal = "haggis",
                            favoriteMeal = "hearty-meal",
                        ),
                    ),
                )
                results.set(
                    recordOf(
                        "haggis" to CookingResult(result = "success", skill = "cooking"),
                    ),
                )
                homebrewMeals.set(homebrew)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val recipes = result["cooking.knownRecipes"] as Array<String>
        assertEquals(3, recipes.size)

        assertEquals(2, result["cooking.minimumSubsistence"])

        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("haggis", actorMeals["actor-1"].chosenMeal)
        assertEquals("hearty-meal", actorMeals["actor-1"].favoriteMeal)

        val results = result["cooking.results"].asDynamic()
        assertEquals("success", results["haggis"].result)
        assertEquals("cooking", results["haggis"].skill)

        @Suppress("UNCHECKED_CAST")
        val storedHomebrew = result["cooking.homebrewMeals"] as Array<RecipeData>
        assertEquals(1, storedHomebrew.size)
        assertEquals("custom-stew", storedHomebrew[0].id)
    }

    @Test
    fun testBuildCookingUpdateDelete() {
        val expected = recordOf("cooking" to js("_del"))
        val result = buildCampingUpdate {
            cooking {
                knownRecipes.set(arrayOf("test"))
            }
            cooking.delete()
        }
        assertEquals(JSON.stringify(expected), JSON.stringify(result))
    }

    @Test
    fun testBuildCookingUpdateTopLevelDirectSetActorMeals() {
        val result = buildCampingUpdate {
            cooking.actorMeals.set(
                recordOf(
                    "actor" to ActorMeal(
                        actorUuid = "actoruuid",
                        chosenMeal = "meal",
                        favoriteMeal = "fav",
                    ),
                ),
            )
        }
        val actorMeals = result["cooking.actorMeals"].asDynamic()
        assertEquals("meal", actorMeals["actor"].chosenMeal)
        assertEquals("fav", actorMeals["actor"].favoriteMeal)
    }

    // ==========================================
    // ParsedMeals data structure
    // ==========================================

    @Test
    fun testParsedMealsDefaults() {
        val parsedMeals = ParsedMeals(
            cook = null,
            skills = listOf(Skill.SURVIVAL, Lore("cooking")),
            results = emptyList(),
            meals = emptyList(),
        )
        assertNull(parsedMeals.cook)
        assertEquals(2, parsedMeals.skills.size)
        assertTrue(Skill.SURVIVAL in parsedMeals.skills)
        assertTrue(Lore("cooking") in parsedMeals.skills)
        assertEquals(0, parsedMeals.meals.size)
        assertEquals(0, parsedMeals.results.size)
    }

    @Test
    fun testParsedMealsWithResults() {
        val recipes = listOf(
            testRecipe(id = "r1", cookingLoreDC = 15, survivalDC = 17),
            testRecipe(id = "r2", cookingLoreDC = 20, survivalDC = 22),
        )
        val parsedResults = recipes.mapIndexed { index, recipe ->
            ParsedRecipeResult(
                recipe = recipe,
                selectedSkill = Skill.SURVIVAL,
                degreeOfSuccess = if (index == 0) DegreeOfSuccess.SUCCESS else null,
            )
        }
        val parsedMeals = ParsedMeals(
            cook = null,
            skills = listOf(Skill.SURVIVAL),
            results = parsedResults,
            meals = emptyList(),
        )
        assertEquals(2, parsedMeals.results.size)
        assertEquals(DegreeOfSuccess.SUCCESS, parsedMeals.results[0].degreeOfSuccess)
        assertNull(parsedMeals.results[1].degreeOfSuccess)
    }

    // ==========================================
    // ParsedRecipeResult DC logic
    // ==========================================

    @Test
    fun testParsedRecipeResultDcWithSurvival() {
        val recipe = testRecipe(cookingLoreDC = 15, survivalDC = 25)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Skill.SURVIVAL,
            degreeOfSuccess = null,
        )
        assertEquals(25, result.dc, "Survival skill should use survivalDC")
    }

    @Test
    fun testParsedRecipeResultDcWithCookingLore() {
        val recipe = testRecipe(cookingLoreDC = 15, survivalDC = 25)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Lore("cooking"),
            degreeOfSuccess = null,
        )
        assertEquals(15, result.dc, "Cooking lore should use cookingLoreDC")
    }

    @Test
    fun testParsedRecipeResultDcWithUnrelatedLore() {
        val recipe = testRecipe(cookingLoreDC = 15, survivalDC = 25)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Lore("nature"),
            degreeOfSuccess = null,
        )
        assertEquals(15, result.dc, "Non-survival lore should use cookingLoreDC")
    }

    @Test
    fun testParsedRecipeResultDcWithNonSurvivalSkill() {
        val recipe = testRecipe(cookingLoreDC = 15, survivalDC = 25)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Skill.NATURE,
            degreeOfSuccess = null,
        )
        assertEquals(15, result.dc, "Non-survival skill should use cookingLoreDC")
    }

    @Test
    fun testParsedRecipeResultDegreeOfSuccessVariants() {
        val recipe = testRecipe()
        val dosValues = listOf(
            DegreeOfSuccess.CRITICAL_SUCCESS,
            DegreeOfSuccess.SUCCESS,
            DegreeOfSuccess.FAILURE,
            DegreeOfSuccess.CRITICAL_FAILURE,
        )
        dosValues.forEach { dos ->
            val result = ParsedRecipeResult(
                recipe = recipe,
                selectedSkill = Skill.SURVIVAL,
                degreeOfSuccess = dos,
            )
            assertEquals(dos, result.degreeOfSuccess)
        }
    }

    @Test
    fun testParsedRecipeResultPreservesSelectedSkill() {
        val recipe = testRecipe()
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Lore("cooking"),
            degreeOfSuccess = DegreeOfSuccess.SUCCESS,
        )
        assertEquals(Lore("cooking"), result.selectedSkill)
        assertEquals(recipe, result.recipe)
    }

    // ==========================================
    // MealChoice constant string validation
    // ==========================================

    @Test
    fun testNothingStringConstant() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = null,
            chosenMeal = "nothing",
        )
        assertEquals("nothing", meal.chosenMeal)
        assertFalse(meal.chosenMeal == "Nothing")
        assertFalse(meal.chosenMeal == "NOTHING")
    }

    @Test
    fun testRationsOrSubsistenceStringConstant() {
        val meal = ActorMeal(
            actorUuid = "test",
            favoriteMeal = null,
            chosenMeal = "rationsOrSubsistence",
        )
        assertEquals("rationsOrSubsistence", meal.chosenMeal)
        assertFalse(meal.chosenMeal == "rations")
        assertFalse(meal.chosenMeal == "RationsOrSubsistence")
    }

    // ==========================================
    // RecipeData outcome field tests
    // ==========================================

    @Test
    fun testRecipeRequiresAllOutcomeFields() {
        val recipe = testRecipe(id = "test")
        assertEquals(0, recipe.criticalSuccess.effects?.size)
        assertEquals(0, recipe.success.effects?.size)
        assertEquals(0, recipe.criticalFailure.effects?.size)
    }

    @Test
    fun testRecipeFavoriteMealOutcomeIsOptional() {
        val favOutcome = CookingOutcome(
            effects = emptyArray(),
            chooseRandomly = null,
            message = "Delicious!",
        )
        val withFavorite = testRecipe(id = "with-fav", favoriteMeal = favOutcome)
        assertEquals("Delicious!", withFavorite.favoriteMeal?.message)

        val withoutFavorite = testRecipe(id = "without-fav")
        assertNull(withoutFavorite.favoriteMeal)
    }

    // ==========================================
    // FoodAmount cooking cost consistency
    // ==========================================

    @Test
    fun testCookingCostAlwaysIncludesOneRation() {
        val recipe = testRecipe(basicIngredients = 3, specialIngredients = 1)
        val cost = recipe.cookingCost()
        assertEquals(1, cost.rations)
    }

    @Test
    fun testCookingCostMatchesBasicIngredients() {
        val recipe = testRecipe(basicIngredients = 4, specialIngredients = 0)
        val cost = recipe.cookingCost()
        assertEquals(4, cost.basicIngredients)
        assertEquals(0, cost.specialIngredients)
    }

    @Test
    fun testDiscoverCostDoubles() {
        val recipe = testRecipe(basicIngredients = 3, specialIngredients = 1)
        val discoverCost = recipe.discoverCost()
        assertEquals(6, discoverCost.basicIngredients)
        assertEquals(2, discoverCost.specialIngredients)
        assertEquals(2, discoverCost.rations)
    }
}
