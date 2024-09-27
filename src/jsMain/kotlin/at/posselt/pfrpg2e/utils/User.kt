package at.posselt.pfrpg2e.utils

import com.foundryvtt.core.Game

fun Game.isFirstGM() =
    users.activeGM?.id == user.id