package at.posselt.pfrpg2e.camping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecipeDataTest {

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
        )
    }

    @Test
    fun testRecipeHasLevel() {
        val recipe = testRecipe(level = 5)
        assertEquals(5, recipe.level)
    }

    @Test
    fun testRecipeHasRarity() {
        val common = testRecipe(rarity = "common")
        assertEquals("common", common.rarity)
        val uncommon = testRecipe(rarity = "uncommon")
        assertEquals("uncommon", uncommon.rarity)
        val rare = testRecipe(rarity = "rare")
        assertEquals("rare", rare.rarity)
        val unique = testRecipe(rarity = "unique")
        assertEquals("unique", unique.rarity)
    }

    @Test
    fun testRecipeHasCost() {
        val recipe = testRecipe(cost = RawCost(value = 10, currency = "gp"))
        assertEquals(10, recipe.cost.value)
        assertEquals("gp", recipe.cost.currency)
    }

    @Test
    fun testRecipeCostCurrencyValues() {
        val sp = testRecipe(cost = RawCost(value = 5, currency = "sp"))
        assertEquals("sp", sp.cost.currency)
        assertEquals(5, sp.cost.value)
    }

    @Test
    fun testSpecialMealFlag() {
        val special = testRecipe(isSpecialMeal = true)
        assertTrue(special.isSpecialMeal == true)
        val normal = testRecipe(isSpecialMeal = false)
        assertFalse(normal.isSpecialMeal == true)
    }

    @Test
    fun testHomebrewFlag() {
        val homebrew = testRecipe(isHomebrew = true)
        assertTrue(homebrew.isHomebrew == true)
        val official = testRecipe(isHomebrew = false)
        assertFalse(official.isHomebrew == true)
    }

    @Test
    fun testRecipeCostAccessibilityFields() {
        val recipe = testRecipe(
            level = 3,
            rarity = "uncommon",
            cost = RawCost(value = 8, currency = "gp")
        )
        assertEquals(3, recipe.level)
        assertEquals("uncommon", recipe.rarity)
        assertEquals(8, recipe.cost.value)
        assertEquals("gp", recipe.cost.currency)
    }

    @Test
    fun testAllRarityLevelsHaveDistinctValues() {
        val rarities = listOf("common", "uncommon", "rare", "unique")
        val recipes = rarities.mapIndexed { index, rarity ->
            testRecipe(
                id = "recipe-$rarity",
                rarity = rarity,
                level = index + 1
            )
        }
        recipes.forEachIndexed { index, recipe ->
            assertEquals(rarities[index], recipe.rarity)
            assertEquals(index + 1, recipe.level)
        }
    }

    @Test
    fun testRecipeWithDefaultSpecialIngredients() {
        val recipe = testRecipe(specialIngredients = 3)
        assertEquals(3, recipe.specialIngredients)
    }

    @Test
    fun testRecipeWithOptionalFieldsNull() {
        val recipe = testRecipe()
        assertEquals(null, recipe.icon)
    }

    @Test
    fun testRecipeRequirementsNullByDefault() {
        val recipe = testRecipe()
        assertEquals(null, recipe.requirements)
    }

    @Test
    fun testRecipeRequirementsWithValue() {
        val recipe = testRecipe(requirements = "master in Religion")
        assertEquals("master in Religion", recipe.requirements)
    }

    @Test
    fun testRecipeRequirementsWithEmptyString() {
        val recipe = testRecipe(requirements = "")
        assertEquals("", recipe.requirements)
    }
}
