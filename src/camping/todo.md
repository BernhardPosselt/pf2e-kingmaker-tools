## activities
* pre-selected effects like Bolster Confidence are automatically applied and synced to actors
* changing a degree of success dropdown of an activity with an actor that is not hidden should remove all effects except the current one and roll a random encounter check if noted
* clicking the roll button should
  * if action was hunt and gather:
    * should display a message to chat to add ingredients
  * if action was learn a recipe:
    * should display a message to chat to learn recipe, pay ingredients and potentially apply crit fail effect

## Eating
* Changing the degree outcome should:
  * delete all previous meal effects
  * apply effects of meal to all actors that choose "Meal" in their dropdown; if favorite meal matches, apply that one as well
* implement meal effect increases to rest time, ration consumption and effect removal after rest

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
  * Remove all meal degrees that expire after resting

## TODO:
* sync sheet using imported actor
* think about recipe migration
* think about adding custom actions

