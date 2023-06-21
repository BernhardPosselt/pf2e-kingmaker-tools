## activities
* Setting 'Prepare Campsite'
  * Critical Success should increase encounter dc
  * Failure should apply a -2 to all camping effect on all actors
* pre-selected effects like Bolster Confidence are automatically applied and synced to actors
* changing a degree of success dropdown of an activity with an actor that is not hidden should remove all effects except the current one and roll a random encounter check if noted
* clicking the roll button should
  * if activity has no predefined dc, show a dc popup, then: 
  * show the check popup
  * rolling the check should auto set the degree of success dropdown
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
* Changing the degree outcome should:
  * delete all previous meal effects
  * apply effects of meal to all actors that choose "Meal" in their dropdown; if favorite meal matches, apply that one as well

## Rest
* Rest duration + daily preps duration should be shown in the timeline
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

## TODO:
* sync sheet using imported actor
* check how well recipes are implemented and document
* think about recipe migration
* think about adding custom actions
