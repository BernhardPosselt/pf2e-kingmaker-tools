# Kingmaker Camping Implementation Brief

Sources:
- Kingmaker Companion Guide (KCG) pp. 107-120: Camping rules
- Kingmaker Companion Guide (KCG) pp. 113-119: Special Campsite Meals
- Kingmaker Companion Guide (KCG) p. 122: Random Weather Events
- Archives of Nethys: https://2e.aonprd.com/CampActivities.aspx
- Archives of Nethys: https://2e.aonprd.com/Rules.aspx?ID=1885 (Chapter 2: Camping)
- Archives of Nethys: https://2e.aonprd.com/Rules.aspx?ID=1902 (Weather Events)
- Archives of Nethys: https://2e.aonprd.com/CampMeals.aspx (Campsite Meals)

---

## 1. Camping Activity Rules

Source: KCG pp. 107-109; AoN Rules.aspx?ID=1885

### Core Timing Rules
- Each camping activity takes **2 hours** to complete.
- PCs can take up to **4 camping activities per day** (8 hours max downtime).
- Multiple PCs can perform different activities **simultaneously**.
- **No two PCs may attempt the same camping activity at the same time.**
- Once any PC achieves at least a **success** on a particular activity, that activity **cannot be attempted again** by any PC until the next camping session.
- Exception: **Relax** can be attempted multiple times.
- Exception: **Cook Special Meal** can be attempted multiple times with different recipes.
- Exception: **Provide Aid** can be attempted multiple times (bonuses don't stack).
- Other exploration activities (Identify Magic, Repair, Treat Wounds) can also be performed and are NOT limited to one success per session.

### Random Encounter Risk
- At the end of **each hour** that anyone undertakes a camping activity, attempt a flat check against the zone's Encounter DC.
- Each successive hour, the Encounter DC decreases by 1.
- After daily preparations or an encounter, the Encounter DC resets.
- If Prepare Campsite was a critical success, skip the first hour's flat check.

### Data Fields Per Activity
Each camping activity needs:
- `id: String` — unique identifier
- `name: String` — display name
- `skills: Array<CampingSkill>` — skills that can be used (name, proficiency, dcType, dc)
- `isSecret: Boolean` — hidden until discovered?
- `isLocked: Boolean` — can be disabled by GM?
- `isHomebrew: Boolean` — user-created?
- `modifyRandomEncounterDc: ModifyEncounterDc?` — day/night DC modifiers
- `effectUuids: Array<ActivityEffect>?` — linked effect items (uuid, target, doublesHealing)
- `criticalSuccess: ActivityOutcome?` — message, effectUuids, modifyRandomEncounterDc, checkRandomEncounter
- `success: ActivityOutcome?`
- `failure: ActivityOutcome?`
- `criticalFailure: ActivityOutcome?`
- `requiredCompanion: String?` — NPC companion required (for companion-specific activities)
- `oncePerSession: Boolean` — can only be attempted once per session regardless of result

---

## 2. Complete List of Camping Activities

Source: KCG pp. 109-112; AoN CampActivities.aspx

### Universal Activities (no companion required)

| Activity | ID | Skills | DC Type | Special Rules |
|---|---|---|---|---|
| **Camouflage Campsite** | `camouflage-campsite` | Stealth (trained req.) | Zone DC | Once per session. Crit Success: +2 Encounter DC + negate first encounter. Success: +1 Encounter DC. Crit Failure: -2 Encounter DC, crit on 19-20. |
| **Cook Basic Meal** | `cook-basic-meal` | Survival or Cooking Lore | Static DC 22 (Surv) / DC 18 (CL) | Expend 2 basic ingredients + 1 ration per serving. Crit Success: heal 2x Con mod x level, +1 status to saves. Success: +1 status to saves. Crit Failure: sickened 1. |
| **Cook Special Meal** | `cook-special-meal` | Survival or Cooking Lore | Varies by recipe | Requires recipe knowledge. Can be attempted multiple times with different recipes. Sickened characters can't eat. |
| **Discover Special Meal** | `discover-special-meal` | Cooking Lore (trained req.) | Varies by recipe | Choose common recipe <= zone level. Expend 2x ingredients. Crit Success: learn recipe + recover half ingredients. |
| **Hunt and Gather** | `hunt-and-gather` | Survival or Hunting Lore | Zone DC | Crit Success: 2x zone DC basic + 4 special (8 if zone >=7, 14 if >=14). Success: zone DC basic + 1d4 special (2d4 if >=7, 3d4 if >=14). Failure: zone DC basic only. Crit Failure: 1d4 basic + extra encounter check. |
| **Learn from a Companion** | `learn-from-companion` | Perception | Static DC 20 | Companion must be present and Friendly. See companion table below. Crit Success: learn activity permanently (if requirements met). Success: progress, improve next attempt by 1 degree. Crit Failure: can't retry this session. |
| **Organize Watch** | `organize-watch` | Perception (expert req.) | Zone DC | Crit Success: treat party as +1 for rest time, +2 status to Perception on watch. Success: +1 status to Perception on watch. Crit Failure: extra encounter check. |
| **Provide Aid** | `provide-aid` | GM-determined skill | Typical DC 20 | Can be attempted multiple times, bonuses don't stack. Crit Success: +2 circumstance bonus (+3 if master, +4 if legendary). Success: +1. Crit Failure: -1. |
| **Relax** | `relax` | None | None | +1 circumstance bonus to next camping activity check this session. Can be attempted unlimited times. Removes -1 penalty from Tell Campfire Story crit failure. |
| **Tell Campfire Story** | `tell-campfire-story` | Performance | Actor level DC | Allies who Relax get greatest benefit. Crit Success: +2 status to attacks/saves/skill checks in camp combat, 1 reroll (fortune). Success: +1 status, Relaxers get +2 but no reroll. Crit Failure: -1 to skill checks until Relax or daily prep. |

### Companion-Specific Activities

| Activity | ID | Required Companion | Companion Skill | Requirements to Learn |
|---|---|---|---|---|
| **Blend into the Night** | `blend-into-night` | Harrim | Religion (trained), worships Groetus | Religion trained, worships Groetus |
| **Bolster Confidence** | `bolster-confidence` | Linzi | Performance (expert) | Performance expert |
| **Camp Management** | `camp-management` | Jubilost | Survival (expert) | Survival expert |
| **Dawnflower's Blessing** | `dawnflowers-blessing` | Tristian | Religion (trained), worships Sarenrae | Religion trained, worships Sarenrae |
| **Enhance Campfire** | `enhance-campfire` | Kanerah | Nature (expert) | Nature expert |
| **Enhance Weapons** | `enhance-weapons` | Amiri | Crafting (expert) | Crafting expert |
| **Intimidating Posture** | `intimidating-posture` | Regongar | Intimidation (expert) | Intimidation expert |
| **Maintain Armor** | `maintain-armor` | Valerie | Crafting (expert) | Crafting expert |
| **Set Alarms** | `set-alarms` | Octavia | Arcana (expert) | Arcana expert |
| **Set Traps** | `set-traps` | Nok-Nok | Thievery (expert) | Thievery expert |
| **Undead Guardians** | `undead-guardians` | Jaethal | Religion (expert) | Religion expert |
| **Water Hazards** | `water-hazards` | Kalikke | Nature (expert) | Nature expert |
| **Wilderness Survival** | `wilderness-survival` | Ekundayo | Survival (expert) | Survival expert |

### Companion Activity Effects Summary

- **Blend into the Night**: Flat check DC for encounters increased by 2.
- **Bolster Confidence**: +1 circumstance bonus to all other camping activity checks (+2 if Linzi is master in Performance).
- **Camp Management**: Crit Success = each PC can do 2 activities next hour. Success = 1 PC can do 2. Crit Failure = -2 to all activity checks.
- **Dawnflower's Blessing**: Each character regains 2x normal HP from rest (not cumulative with Cook Basic Meal crit success).
- **Enhance Campfire**: +1 circumstance bonus to cooking activities (+2 if party >= level 11).
- **Enhance Weapons**: One melee weapon per PC gets +1 circumstance to damage for next encounter or 24 hours.
- **Intimidating Posture**: Low/Trivial encounters are treated as no encounter (XP still awarded).
- **Maintain Armor**: Armor grants temp HP = half level for next encounter. Or repair broken shield. Extra targets at 3rd level and every 2 levels.
- **Set Alarms**: First attack in 24 hours: sleeping PCs don't get -4 initiative penalty, awake PCs get +2 initiative.
- **Set Traps**: Attackers get -1 item penalty to attacks/skills first round. Enemies below Nok-Nok's Thievery DC are flat-footed round 2.
- **Undead Guardians**: Each round in camp combat, one PC gets +1 AC or +1 to melee Strikes for 1 round.
- **Water Hazards**: Attackers get -2 circumstance to Initiative (-3 at party level 11+).
- **Wilderness Survival**: +1 item bonus to Cook Meal or Treat Disease (+2 if party >= level 11).

---

## 3. Meal Options

Source: KCG pp. 110, 113-119; AoN CampMeals.aspx

### Basic Food Options (no cooking required)
- **Rations**: Standard, no special effect.
- **Subsist**: Use Survival check to find food, no special effect.
- **Magical sustenance**: create food, heroes' feast, ring of sustenance, etc.

### Cook Basic Meal (Activity)
- **ID**: `basic-meal`
- **Ingredients**: 2 basic + 1 ration per serving
- **DC**: Survival DC 22 or Cooking Lore DC 18
- **Crit Success**: Heal HP = Con mod (min 1) x 2 x level during rest; +1 status to all saves until daily prep
- **Success**: +1 status to all saves until daily prep
- **Failure**: No extra effect
- **Crit Failure**: Sickened 1 until rest + daily prep

### Hearty Meal (Special Meal, no recipe needed)
- **ID**: `hearty-meal`
- **Level**: 0
- **Ingredients**: 4 basic
- **DC**: Cooking Lore DC 14 or Survival DC 16
- **Favorite Meal**: Recover additional HP equal to level when resting
- **Crit Success**: +1 status to next 3 saves in 24 hours
- **Success**: +1 status to next save in 24 hours
- **Crit Failure**: -1 to initiative until rest + daily prep

### Special Campsite Meals (27 total)

All special meals:
- Must be eaten during the same camping session they're prepared
- Only one special meal effect at a time (first one eaten)
- Effects determined by cook's skill check (Survival or Cooking Lore)
- Effects last until next camp preparation or 24 hours (unless otherwise noted)
- Failure = no extra benefit, still prevents starvation
- A character can only have one **favorite meal** at a time
- Favorite meal: after experiencing success effect twice OR critical success once
- Changing favorite meal: requires 2 critical successes on new meal
- NPCs have fixed favorite meals (never change)

#### Recipe Data Fields
Each recipe needs:
- `id: String` — unique identifier
- `name: String` — display name
- `level: Int` — meal level
- `basicIngredients: Int` — count of basic ingredients per serving
- `specialIngredients: Int` — count of special ingredients per serving
- `cookingLoreDC: Int` — Cooking Lore check DC
- `survivalDC: Int` — Survival check DC
- `cost: {value: Int, currency: String}` — recipe purchase price
- `rarity: String` — common/uncommon/rare
- `requirements: String?` — special requirements (e.g., "ability to cast cold spell")
- `criticalSuccess: CookingOutcome` — effects + message
- `success: CookingOutcome` — effects + message
- `criticalFailure: CookingOutcome` — effects + message
- `favoriteMeal: CookingOutcome?` — bonus when favorite

#### Complete Recipe List

| Meal | Level | Basic | Special | CL DC | Surv DC | Rarity | Recipe Cost | Requirements | Favorite Meal Bonus |
|---|---|---|---|---|---|---|---|---|---|
| Baked Spider Legs | 5 | 4 | 1 | 20 | 22 | Common | 8 gp | — | +1 Stealth |
| Black Linnorm Stew | 18 | 8 | 3 | 43 | 45 | Rare | 1200 gp | Legendary Arcana/Nature | +2 Perception |
| Broiled Tuskwater Oysters | 3 | 2 | 1 | 20 | 22 | Common | 3 gp | — | +1 saves vs occult spells |
| Cheese Crostata | 5 | 4 | 0 | 22 | 24 | Common | 8 gp | — | +1 Religion |
| Chocolate Ice Cream | 4 | 2 | 1 | 19 | 21 | Common | 5 sp | Cold spell ability | +1 Lore RK |
| First World Mince Pie | 20 | 8 | 4 | 45 | 47 | Rare | 3500 gp | Cook in First World | +3 to random ability's skills |
| Fish-on-a-Stick | 1 | 2 | 0 | 17 | 19 | Common | 1 gp | — | 7h sleep in 8h |
| Galt Ragout | 4 | 4 | 0 | 20 | 22 | Common | 5 gp | — | +1 Acrobatics Tumble Through |
| Giant Scrambled Egg with Shambletus | 13 | 6 | 2 | 33 | 35 | Uncommon | 150 gp | — | +1 dmg 2-hand weapons |
| Grilled Silver Eel | 6 | 4 | 1 | 24 | 26 | Common | 13 gp | — | Free action: +5 Speed 1 min |
| Haggis | 1 | 2 | 0 | 15 | 17 | Common | 1 gp | — | +1 Will vs fear |
| Hearty Meal | 0 | 4 | 0 | 14 | 16 | Common | Free | — | +level HP on rest |
| Hearty Purple Soup | 16 | 6 | 3 | 40 | 42 | Rare | 500 gp | Legendary Nature | +4 vs poison/disease |
| Hunter's Roast | 6 | 4 | 0 | 22 | 24 | Common | 13 gp | — | +1 Nature |
| Jeweled Rice | 0 | 1 | 0 | 14 | 16 | Common | 5 sp | — | +1 Acrobatics Escape |
| Kameberry Pie | 10 | 3 | 2 | 27 | 29 | Uncommon | 50 gp | Master Religion | +1 Religion |
| Mastodon Steak | 14 | 4 | 3 | 34 | 36 | Uncommon | 225 gp | — | +1 Fortitude saves |
| Monster Casserole | 11 | 7 | 2 | 28 | 30 | Uncommon | 70 gp | — | +1 Athletics |
| Onion Soup | 8 | 2 | 1 | 24 | 26 | Common | 25 gp | — | +1 Arcana |
| Owlbear Omelet | 7 | 4 | 1 | 25 | 27 | Common | 18 gp | — | +1 Nature |
| Rice-n-Nut Pudding | 2 | 2 | 1 | 16 | 18 | Common | 2 gp | Trained Arcana | Refocus heals 1d8+level HP |
| Seasoned Wings and Thighs | 12 | 4 | 2 | 30 | 32 | Uncommon | 100 gp | Fire spell ability | Reaction: Ignite Magic (2d6 fire on spell crit fail) |
| Shepherd's Pie | 2 | 4 | 0 | 18 | 20 | Common | 2 gp | — | +1 HP per healing effect |
| Smoked Trout and Hydra Pate | 8 | 6 | 2 | 26 | 28 | Common | 25 gp | — | +1 Athletics |
| Succulent Sausages | 3 | 3 | 1 | 18 | 20 | Common | 3 gp | — | Reaction: Careful Casting (DC 15 vs disruption) |
| Sweet Pancakes | 7 | 2 | 2 | 23 | 25 | Common | 18 gp | — | +5 Speed first turn each combat |
| Whiterose Oysters | 9 | 3 | 2 | 26 | 28 | Uncommon | 35 gp | — | +2 Demoralize +2 Make Impression |

#### Recipe Availability
- **Common**: Purchase in any settlement
- **Uncommon**: Purchase in Town or larger settlements
- **Rare**: Quest rewards, discovered, taught by NPCs

#### Ingredient Sourcing
- **Basic ingredients**: Hunt and Gather activity (always finds some, even on crit failure); buy rations
- **Special ingredients**: Only from Hunt and Gather success; optionally 1 per party level per month as kingdom perk; harvest from creatures (Survival check vs creature level DC, 1 per creature, 10 min work)

---

## 4. NPC Favorite Meals

Source: KCG p. 113; AoN Rules.aspx?ID=1885

| Companion | Favorite Meal |
|---|---|
| Amiri | Monster Casserole |
| Ekundayo | Hunter's Roast |
| Harrim | Haggis |
| Jaethal | Jeweled Rice |
| Jubilost | Onion Soup |
| Kalikke | Chocolate Ice Cream |
| Kanerah | Seasoned Wings and Thighs |
| Linzi | Sweet Pancakes |
| Nok-Nok | Baked Spider Legs |
| Octavia | Rice-n-Nut Pudding |
| Regongar | Succulent Sausages |
| Tristian | Kameberry Pie |
| Valerie | Whiterose Oysters |

---

## 5. Companion Activity Availability (Grey-Out Logic)

Source: KCG pp. 111-112; AoN CampActivities.aspx

A companion-specific activity should be **greyed out / unavailable** when:
1. The required companion is **not present in camp** (not in `actorUuids`), AND
2. The party has **not yet learned** that activity via the "Learn from a Companion" activity

The "Learn from a Companion" activity:
- Requires the companion to be present and at least **Friendly**
- Takes 2 hours, DC 20 Perception check
- On success: any PC who meets the activity's requirements can perform it even when the companion is absent
- On failure with progress: next attempt improves by 1 degree of success

**Implementation approach**: Track learned companion activities in `CampingData` (e.g., `learnedCompanionActivities: Array<String>`). When determining if an activity is available:
- If companion is in camp → available
- If activity ID is in learned list → available
- Otherwise → greyed out

---

## 6. Random Weather Events Table

Source: KCG p. 122; AoN Rules.aspx?ID=1902

### Trigger
- At daily preparations, attempt a **DC 17 flat check**
- On success: roll on the Random Weather Events table
- On natural 20: potential secondary event (thematically linked, GM chooses)
- Reroll any hazard > 4 levels above party level

### Random Weather Events Table

| d20 Roll | Weather Event | Level |
|---|---|---|
| 1–3 | Fog | 0 |
| 4–7 | Heavy downpour | 0 |
| 8–9 | Cold snap | 1 |
| 10–12 | Windstorm | 1 |
| 13 | Hailstorm, severe | 2 |
| 14 | Blizzard | 6 |
| 15 | Supernatural storm | 6+ |
| 16 | Flash flood | 7 |
| 17 | Wildfire | 4 or 10 |
| 18 | Subsidence | 5 or 12 |
| 19 | Thunderstorm | 7 or 13 |
| 20 | Tornado | 12 or 17 |

### Data Fields for Weather Events
Each weather event needs:
- `id: String` — unique identifier
- `name: String` — display name
- `level: Int` — hazard level (some have two values for GM choice)
- `rollRange: String` — d20 range (e.g., "1-3", "4-7")
- `description: String` — effect description (not captured here; needs source text or summary)

### Additional Weather Rules (not in table)
- **Precipitation**: Flat check DC 20 (summer), 15 (spring/autumn), 8 (winter). Light rain/snow: 4h to fatigued, -1 visual Perception.
- **Temperature**: Mild Cold flat check DC 18 (Kuthona/Calistril) or DC 16 (Abadius). 4h to fatigued.
- **Weather event XP**: Characters gain XP for experiencing weather events (unless sheltered).

---

## 7. Implementation Notes for Existing Codebase

### Existing Data Structures (already in codebase)
- `CampingActivityData` — matches the activity schema well
- `RecipeData` — matches the recipe schema well
- `CampingData` — main camping state container
- `Cooking` — cooking state (knownRecipes, actorMeals, homebrewMeals, results)
- JSON schemas at `src/commonMain/resources/schemas/camping-activity.json` and `recipe.json`

### What Needs to Be Added/Updated

1. **Camping Activities JSON** (`camping-activities.json`):
   - Add `requiredCompanion` field to schema and data
   - Add `oncePerSession` field for Camouflage Campsite
   - Ensure all 23 activities are present with correct skills, DCs, and outcomes

2. **Recipes JSON** (`recipes.json`):
   - Ensure all 27 special meals + Hearty Meal are present
   - Add `requirements` field for meals with special requirements
   - Verify all DCs, ingredient counts, and effects

3. **Companion Learning**:
   - Add `learnedCompanionActivities: Array<String>` to `CampingData`
   - Update activity availability logic to check learned list
   - Implement "Learn from a Companion" activity resolution

4. **Weather Events**:
   - Create weather event data structure (table with 20 entries)
   - Add DC 17 flat check trigger at daily preparations
   - Add level cap check (reroll if > party level + 4)
   - The existing `Weather.kt` has basic weather types but NOT the random weather events table — this is a separate system

5. **Meal Effects**:
   - The existing `MealEffect` structure supports: uuid, removeWhenPreparingCampsite, changeRestDurationSeconds, doublesHealing, halvesHealing, healFormula, damageFormula, changeFatigueDurationSeconds, healMode, reduceConditions
   - Verify all meal effects can be represented with existing fields
   - Some meals grant actions/reactions (e.g., Grilled Silver Eel free action, Seasoned Wings reaction, Succulent Sausages reaction) — these may need special handling beyond simple effect items

6. **Favorite Meals**:
   - Already tracked per actor in `Cooking.actorMeals[favoriteMeal]`
   - NPC favorite meals need to be hardcoded (see table above)
   - PCs declare favorite meal after 2 successes or 1 critical success

### Missing Exact Wording (Flagged)
The following mechanics are described above based on AoN summaries but exact KCG wording was not directly accessed:
- Full weather event descriptions (Fog, Heavy downpour, Cold snap, Windstorm, Hailstorm, Blizzard, Supernatural storm, Flash flood, Wildfire, Subsidence, Thunderstorm, Tornado)
- Full meal effect descriptions for all 27 recipes (summarized from AoN)
- Exact XP values for weather events
- Full Undead Guardians combat mechanics

These should be verified against the physical KCG book before final implementation.
