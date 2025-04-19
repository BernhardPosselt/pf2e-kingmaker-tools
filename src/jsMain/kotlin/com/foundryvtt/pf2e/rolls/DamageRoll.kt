package com.foundryvtt.pf2e.rolls

import com.foundryvtt.core.dice.Roll


@Suppress("NAME_CONTAINS_ILLEGAL_CHARS")
@JsName("foundryvttKotlinPatches.rolls.DamageRoll")
external class DamageRoll(
    formula: String,
) : Roll