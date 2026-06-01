package at.posselt.pfrpg2e.camping

import at.posselt.pfrpg2e.data.actor.Lore
import at.posselt.pfrpg2e.data.actor.Skill
import at.posselt.pfrpg2e.data.checks.DegreeOfSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the meal system: RecipeData cooking costs, FoodAmount arithmetic,
 * ParsedRecipeResult DCs, eating-state data structures (ActorMeal, CookingResult),
 * and RecipeData field validation.
 */
class MealChoiceTest {

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
    // RecipeData.cookingCost() tests
    // ==========================================

    @Test
    fun testCookingCostIncludesOneRation() {
        val recipe = testRecipe(basicIngredients = 3, specialIngredients = 1)
        val cost = recipe.cookingCost()
        assertEquals(1, cost.rations, "Every cooked meal consumes 1 ration")
    }

    @Test
    fun testCookingCostIncludesBasicIngredients() {
        val recipe = testRecipe(basicIngredients = 4, specialIngredients = 0)
        val cost = recipe.cookingCost()
        assertEquals(4, cost.basicIngredients)
        assertEquals(0, cost.specialIngredients)
    }

    @Test
    fun testCookingCostIncludesSpecialIngredients() {
        val recipe = testRecipe(basicIngredients = 8, specialIngredients = 3)
        val cost = recipe.cookingCost()
        assertEquals(8, cost.basicIngredients)
        assertEquals(3, cost.specialIngredients)
    }

    @Test
    fun testCookingCostWithZeroSpecialIngredients() {
        val recipe = testRecipe(basicIngredients = 2, specialIngredients = 0)
        val cost = recipe.cookingCost()
        assertEquals(0, cost.specialIngredients)
    }

    // ==========================================
    // RecipeData.discoverCost() tests
    // ==========================================

    @Test
    fun testDiscoverCostIsDoubleCookingCost() {
        val recipe = testRecipe(basicIngredients = 3, specialIngredients = 1)
        val discoverCost = recipe.discoverCost()
        assertEquals(6, discoverCost.basicIngredients, "Discover doubles basic ingredients")
        assertEquals(2, discoverCost.specialIngredients, "Discover doubles special ingredients")
        assertEquals(2, discoverCost.rations, "Discover doubles rations")
    }

    @Test
    fun testDiscoverCostForSingleBasicIngredient() {
        val recipe = testRecipe(basicIngredients = 1, specialIngredients = 0)
        val discoverCost = recipe.discoverCost()
        assertEquals(2, discoverCost.basicIngredients)
        assertEquals(0, discoverCost.specialIngredients)
        assertEquals(2, discoverCost.rations)
    }

    // ==========================================
    // RecipeData.canBeFavoriteMeal() tests
    // ==========================================

    @Test
    fun testBasicMealCannotBeFavorite() {
        val recipe = testRecipe(id = "basic-meal")
        assertFalse(recipe.canBeFavoriteMeal(), "basic-meal should not be a favorite")
    }

    @Test
    fun testHeartyMealCanBeFavorite() {
        val recipe = testRecipe(id = "hearty-meal")
        assertTrue(recipe.canBeFavoriteMeal(), "hearty-meal should be a favorite")
    }

    @Test
    fun testSpecialRecipeCanBeFavorite() {
        val recipe = testRecipe(id = "black-linnorm-stew", isSpecialMeal = true)
        assertTrue(recipe.canBeFavoriteMeal(), "special meals should be favorites")
    }

    @Test
    fun testAllNonBasicMealsCanBeFavorite() {
        val ids = listOf("hearty-meal", "haggis", "owlbear-omelet", "sweet-pancakes", "black-linnorm-stew")
        ids.forEach { id ->
            val recipe = testRecipe(id = id)
            assertTrue(recipe.canBeFavoriteMeal(), "Recipe '$id' should be able to be favorite")
        }
    }

    // ==========================================
    // RecipeData level, rarity, cost, requirements
    // ==========================================

    @Test
    fun testRecipeLevelAccess() {
        val recipes = listOf(
            testRecipe(id = "basic-meal", level = 0),
            testRecipe(id = "haggis", level = 1),
            testRecipe(id = "black-linnorm-stew", level = 18),
        )
        recipes.forEach { recipe ->
            assertTrue(recipe.level >= 0, "Recipe '${recipe.id}' should have non-negative level")
        }
    }

    @Test
    fun testRecipeRarityValidation() {
        val validRarities = setOf("common", "uncommon", "rare", "unique")
        val recipes = listOf(
            testRecipe(id = "basic-meal", rarity = "common"),
            testRecipe(id = "monster-casserole", rarity = "uncommon"),
            testRecipe(id = "black-linnorm-stew", rarity = "rare"),
        )
        recipes.forEach { recipe ->
            assertTrue(recipe.rarity in validRarities, "Recipe '${recipe.id}' has invalid rarity '${recipe.rarity}'")
        }
    }

    @Test
    fun testRecipeCostAndRequirementsFieldsAccessible() {
        val recipe = testRecipe(
            level = 3,
            rarity = "uncommon",
            cost = RawCost(value = 8, currency = "gp"),
            requirements = "trained in Arcana",
        )
        assertEquals(3, recipe.level)
        assertEquals("uncommon", recipe.rarity)
        assertEquals(8, recipe.cost.value)
        assertEquals("gp", recipe.cost.currency)
        assertEquals("trained in Arcana", recipe.requirements)
    }

    @Test
    fun testRecipeWithoutRequirements() {
        val recipe = testRecipe(id = "basic-meal", requirements = null)
        assertNull(recipe.requirements)
    }

    // ==========================================
    // FoodAmount arithmetic tests
    // ==========================================

    @Test
    fun testFoodAmountPlus() {
        val a = FoodAmount(basicIngredients = 2, specialIngredients = 1, rations = 1)
        val b = FoodAmount(basicIngredients = 3, specialIngredients = 0, rations = 1)
        val result = a + b
        assertEquals(5, result.basicIngredients)
        assertEquals(1, result.specialIngredients)
        assertEquals(2, result.rations)
    }

    @Test
    fun testFoodAmountTimes() {
        val a = FoodAmount(basicIngredients = 2, specialIngredients = 1, rations = 1)
        val result = a * 3
        assertEquals(6, result.basicIngredients)
        assertEquals(3, result.specialIngredients)
        assertEquals(3, result.rations)
    }

    @Test
    fun testFoodAmountSum() {
        val amounts = listOf(
            FoodAmount(basicIngredients = 2, specialIngredients = 1, rations = 1),
            FoodAmount(basicIngredients = 3, specialIngredients = 0, rations = 1),
            FoodAmount(basicIngredients = 0, specialIngredients = 2, rations = 0),
        )
        val result = amounts.sum()
        assertEquals(5, result.basicIngredients)
        assertEquals(3, result.specialIngredients)
        assertEquals(2, result.rations)
    }

    @Test
    fun testFoodAmountEmpty() {
        assertTrue(FoodAmount().isEmpty())
        assertFalse(FoodAmount(basicIngredients = 1).isEmpty())
        assertFalse(FoodAmount(specialIngredients = 1).isEmpty())
        assertFalse(FoodAmount(rations = 1).isEmpty())
    }

    @Test
    fun testFoodAmountEmptyWithAllZero() {
        assertTrue(FoodAmount(basicIngredients = 0, specialIngredients = 0, rations = 0).isEmpty())
    }

    // ==========================================
    // ParsedRecipeResult dc tests
    // ==========================================

    @Test
    fun testParsedRecipeResultDcUsesSurvivalWhenSkillIsSurvival() {
        val recipe = testRecipe(cookingLoreDC = 18, survivalDC = 22)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Skill.SURVIVAL,
            degreeOfSuccess = null,
        )
        assertEquals(22, result.dc, "Survival skill should use survivalDC")
    }

    @Test
    fun testParsedRecipeResultDcUsesCookingLoreWhenSkillIsLore() {
        val recipe = testRecipe(cookingLoreDC = 18, survivalDC = 22)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Lore("cooking"),
            degreeOfSuccess = null,
        )
        assertEquals(18, result.dc, "Cooking Lore skill should use cookingLoreDC")
    }

    @Test
    fun testParsedRecipeResultDcWithNonCookingLore() {
        val recipe = testRecipe(cookingLoreDC = 18, survivalDC = 22)
        // Any skill that is NOT Survival should use cookingLoreDC
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Lore("nature"),
            degreeOfSuccess = null,
        )
        assertEquals(18, result.dc, "Non-survival lore should use cookingLoreDC")
    }

    @Test
    fun testParsedRecipeResultDcWithNonSurvivalSkill() {
        val recipe = testRecipe(cookingLoreDC = 18, survivalDC = 22)
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Skill.NATURE,
            degreeOfSuccess = null,
        )
        assertEquals(18, result.dc, "Non-survival skill should use cookingLoreDC")
    }

    @Test
    fun testParsedRecipeResultWithDegreeOfSuccess() {
        val recipe = testRecipe()
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Skill.SURVIVAL,
            degreeOfSuccess = DegreeOfSuccess.CRITICAL_SUCCESS,
        )
        assertEquals(DegreeOfSuccess.CRITICAL_SUCCESS, result.degreeOfSuccess)
    }

    @Test
    fun testParsedRecipeResultWithNullDegreeOfSuccess() {
        val recipe = testRecipe()
        val result = ParsedRecipeResult(
            recipe = recipe,
            selectedSkill = Skill.SURVIVAL,
            degreeOfSuccess = null,
        )
        assertNull(result.degreeOfSuccess)
    }

    // ==========================================
    // Eating state: ActorMeal data structure
    // ==========================================

    @Test
    fun testActorMealStoresAllFields() {
        val meal = ActorMeal(
            actorUuid = "actor-uuid-1",
            favoriteMeal = "haggis",
            chosenMeal = "owlbear-omelet",
        )
        assertEquals("actor-uuid-1", meal.actorUuid)
        assertEquals("haggis", meal.favoriteMeal)
        assertEquals("owlbear-omelet", meal.chosenMeal)
    }

    @Test
    fun testActorMealDefaultChosenMealIsNothing() {
        val meal = ActorMeal(
            actorUuid = "actor-uuid-1",
            favoriteMeal = null,
            chosenMeal = "nothing",
        )
        assertEquals("nothing", meal.chosenMeal)
        assertNull(meal.favoriteMeal)
    }

    @Test
    fun testActorMealSupportsRations() {
        val meal = ActorMeal(
            actorUuid = "actor-uuid-1",
            favoriteMeal = null,
            chosenMeal = "rationsOrSubsistence",
        )
        assertEquals("rationsOrSubsistence", meal.chosenMeal)
    }

    // ==========================================
    // CookingResult data structure
    // ==========================================

    @Test
    fun testCookingResultStoresDegreeOfSuccess() {
        val result = CookingResult(result = "criticalSuccess", skill = "survival")
        assertEquals("criticalSuccess", result.result)
        assertEquals("survival", result.skill)
    }

    @Test
    fun testCookingResultStoresNullDegree() {
        val result = CookingResult(result = null, skill = "survival")
        assertNull(result.result)
        assertEquals("survival", result.skill)
    }

    @Test
    fun testCookingResultWithCookingLoreSkill() {
        val result = CookingResult(result = "success", skill = "cooking")
        assertEquals("success", result.result)
        assertEquals("cooking", result.skill)
    }

    // ==========================================
    // RecipeData outcome presence tests
    // ==========================================

    @Test
    fun testRecipeHasCriticalSuccessOutcome() {
        val recipe = testRecipe(id = "test")
        // All recipes must have criticalSuccess, success, criticalFailure (required in schema)
        assertNull(recipe.criticalSuccess.message, "Default test recipe has null message")
    }

    @Test
    fun testRecipeFavoriteMealOutcomeOptional() {
        val favOutcome = CookingOutcome(effects = emptyArray(), chooseRandomly = null, message = "Favorite!")
        val withFavorite = testRecipe(id = "with-fav", favoriteMeal = favOutcome)
        assertEquals("Favorite!", withFavorite.favoriteMeal?.message)
    }
}
