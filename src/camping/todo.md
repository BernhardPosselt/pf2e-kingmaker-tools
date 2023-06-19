## available actors:
* should give GM a hint that camping activity result has been set
* should show which activities (excluding prepare camp) have been chosen

## activities
* Setting 'Prepare Campsite'
  * Critical Success should increase encounter dc
  * Failure should apply a -2 to all camping effect on all actors
  * Anything else than critical failure shows all other activities:
    * pre-selected effects like Bolster Confidence are automatically applied and synced to actors
* unsetting an actor should clear the success drop down
* activities without checks should not show a degree of success nor check dropdown
* Dropping characters on activities that don't satisfy proficiency requirements shows an error message and does not set the actor
* changing a degree of success dropdown should remove all effects except the current one and roll a random encounter check if noted
* clicking the roll button should
  * if activity has no predefined dc, show a dc popup, then: 
  * show the check popup
  * rolling the check should auto set the degree of success dropdown
  * rolled check should have the actors name
  * if action was hunt and gather:
    * should add ingredients to PC's inventory 
    * should display a message how many ingredients were added including an undo button to remove said quantities in case of a re-roll
  * if action was learn a recipe:
    * show learn a recipe popup
    * popup should list all common recipes learnable in zone
    * 3 choices: add, learn
    * learn check button is greyed out if more recipe requires more ingredients than available across actors; clicking ok should add the recipe to current recipes and subtract ingredients
    * add adds the recipe without check
  * figure out how to deal with Relax (probably not at all)

## Eating
* Favorite Meal should be taken from known recipes and persisted
* Subsistence/Magical Subsistence should be persisted
* Current row:
  * Meals should be set to equal servings further down
* Consumed row:
  * Every meal choice except nothing will subtract 1 from columns in the following order:
    * Magical Subsistence
    * Subsistence
    * Rations
  * Basic & Special Ingredients should be calculated and filled in based on recipe and servings
  * Meals is set to number of all meal drop downs in actor choices
  * If any number is higher than in the current column, highlight cell in red
* Servings and Recipe should be persisted
* Recipe dropdown includes known recipes
* Cook button performs the check on the actor that selected the "Cook Meal" camping activity and change the degree outcome
* Changing the degree outcome should:
  * delete all previous meal effects
  * apply effects of meal to all actors that choose "Meal" in their dropdown; if favorite meal matches, apply that one as well
* Consume ingredients button is greyed out for users
* Consume ingredients button reduces ingredient quantities across all actors by recipe * servings amount

## Rest
* Configuration exists to set:
  * number of guns to clean
  * if: 1 encounter should be rolled per night or 1 every 4 hours
* Rest duration + daily preps duration should be shown in the timeline
* Cursor on timeline should show current time
* Clicking on Begin Rest button:
  * rolls x random encounters until: 1 is positive or none is left
  * if a check is a success determine a point in time randomly between rest start and end date
  * Determine a random actor that was watching during that point in time and auto roll a secret perception check
  * Advance time in .5s intervals until either point in time or end of rest is reached
  * If random point is reached, Begin Rest button turns into: Continue Rest; clicking that button advances to the end
  * Run daily preps
  * Remove all camping activity effects
  * Reset daily preps counter to 0
  * Reset all degree of success dropdowns to empty

## Miscellaneous
* Blend into the night should increase encounter dc between 6pm and 6am
