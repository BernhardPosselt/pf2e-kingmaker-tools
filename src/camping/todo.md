# TODOs

## Activities

* changing a degree of success dropdown of an activity with an actor that is not hidden should remove all effects except
  the non expired applicable one and roll a random encounter check if noted
* pre-selected effects like Bolster Confidence are automatically applied and synced to actors
* Add a way to add and remove custom activities

## Eating

* Changing the degree outcome should:
    * delete all previous meal effects
    * apply effects of meal to all actors that choose "Meal" in their dropdown; if favorite meal matches, apply that one
      as well
* implement meal effect:
    * increases to rest time
    * increases ration consumption
* add a way to migrate previous known recipes

## Rest

* Clicking on Begin Rest button:
    * rolls x random encounters until: 1 is positive or none is left
    * if a check is a success determine a point in time randomly between rest start and end date
    * Determine a random actor that was watching during that point in time and auto roll a secret perception check
    * Advance time in .5s intervals until either point in time or end of rest is reached
    * If random point is reached, Begin Rest button turns into: Continue Rest; clicking that button advances to the end
    * Run daily preps
    * Remove all camping activity effects
    * Reset Adventuring time tracker to 0
    * Reset all degree of success dropdowns to not selected
    * Remove all meal degrees that expire after resting



