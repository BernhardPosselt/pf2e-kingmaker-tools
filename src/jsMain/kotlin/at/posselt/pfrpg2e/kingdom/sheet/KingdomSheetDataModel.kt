package at.posselt.pfrpg2e.kingdom.sheet

import at.posselt.pfrpg2e.data.kingdom.KingdomSkill
import at.posselt.pfrpg2e.data.kingdom.Relations
import at.posselt.pfrpg2e.data.kingdom.leaders.Leader
import at.posselt.pfrpg2e.data.kingdom.leaders.LeaderType
import com.foundryvtt.core.AnyObject
import com.foundryvtt.core.abstract.DataModel
import com.foundryvtt.core.abstract.DocumentConstructionContext
import com.foundryvtt.core.data.dsl.buildSchema

@JsExport
class KingdomSheetDataModel(
    value: AnyObject,
    options: DocumentConstructionContext?
) : DataModel(value, options) {
    companion object {
        @JsStatic
        fun defineSchema() = buildSchema {
            string("bonusFeat", nullable = true)
            string("ongoingEvent", nullable = true)
            string("name")
            boolean("atWar")
            schema("fame") {
                int("now")
                int("next")
                string("type")
            }
            int("level")
            int("xpThreshold")
            int("xp")
            int("size")
            int("unrest")
            enum<Leader>("activeLeader", nullable = true)
            schema("resourcePoints") {
                int("now")
                int("next")
            }
            schema("resourceDice") {
                int("now")
                int("next")
            }
            stringArray("initialProficiencies")
            schema("workSites") {
                schema("farmlands") {
                    int("resources")
                    int("quantity")
                }
                schema("lumberCamps") {
                    int("resources")
                    int("quantity")
                }
                schema("mines") {
                    int("resources")
                    int("quantity")
                }
                schema("quarries") {
                    int("resources")
                    int("quantity")
                }
                schema("luxurySources") {
                    int("resources")
                    int("quantity")
                }
            }
            schema("consumption") {
                int("now")
                int("next")
                int("armies")
            }
            int("supernaturalSolutions")
            int("creativeSolutions")
            schema("commodities") {
                schema("now") {
                    int("food")
                    int("lumber")
                    int("luxuries")
                    int("ore")
                    int("stone")
                }
                schema("next") {
                    int("food")
                    int("lumber")
                    int("luxuries")
                    int("ore")
                    int("stone")
                }
            }
            schema("ruin") {
                schema("corruption") {
                    int("value")
                    int("threshold")
                    int("penalty")
                }
                schema("crime") {
                    int("value")
                    int("threshold")
                    int("penalty")
                }
                schema("decay") {
                    int("value")
                    int("threshold")
                    int("penalty")
                }
                schema("strife") {
                    int("value")
                    int("threshold")
                    int("penalty")
                }
            }
            string("activeSettlement", nullable = true)
            schema("notes") {
                string("gm")
                string("public")
            }
            schema("leaders") {
                Leader.entries.forEach {
                    schema(it.value) {
                        boolean("invested")
                        boolean("vacant")
                        enum<LeaderType>("type")
                        string("uuid", nullable = true)
                    }
                }
            }
            schema("charter") {
                string("type", nullable = true)
                schema("abilityBoosts") {
                    boolean("culture")
                    boolean("economy")
                    boolean("loyalty")
                    boolean("stability")
                }
            }
            schema("heartland") {
                string("type", nullable = true)
            }
            schema("government") {
                string("type", nullable = true)
                enum<Leader>("featSupportedLeader", nullable = true)
                array("featRuinThresholdIncreases") {
                    schema {
                        schema("crime") {
                            int("value")
                            boolean("increase")
                        }
                        schema("corruption") {
                            int("value")
                            boolean("increase")
                        }
                        schema("strife") {
                            int("value")
                            boolean("increase")
                        }
                        schema("decay") {
                            int("value")
                            boolean("increase")
                        }
                    }
                }
                schema("abilityBoosts") {
                    boolean("culture")
                    boolean("economy")
                    boolean("loyalty")
                    boolean("stability")
                }
            }
            schema("abilityBoosts") {
                boolean("culture")
                boolean("economy")
                boolean("loyalty")
                boolean("stability")
            }
            array("features") {
                schema {
                    string("id")
                    enum<Leader>("supportedLeader", nullable = true)
                    enum<KingdomSkill>("skillIncrease", nullable = true)
                    schema("abilityBoosts", nullable = true) {
                        boolean("culture")
                        boolean("economy")
                        boolean("loyalty")
                        boolean("stability")
                    }
                    string("featId", nullable = true)
                    schema("ruinThresholdIncreases", nullable = true) {
                        schema("crime") {
                            int("value")
                            boolean("increase")
                        }
                        schema("corruption") {
                            int("value")
                            boolean("increase")
                        }
                        schema("strife") {
                            int("value")
                            boolean("increase")
                        }
                        schema("decay") {
                            int("value")
                            boolean("increase")
                        }
                    }
                    array("featRuinThresholdIncreases") {
                        schema {
                            schema("crime") {
                                int("value")
                                boolean("increase")
                            }
                            schema("corruption") {
                                int("value")
                                boolean("increase")
                            }
                            schema("strife") {
                                int("value")
                                boolean("increase")
                            }
                            schema("decay") {
                                int("value")
                                boolean("increase")
                            }
                        }
                    }
                }
            }
            array("bonusFeats") {
                schema {
                    string("id")
                    enum<Leader>("supportedLeader", nullable = true)
                    array("ruinThresholdIncreases") {
                        schema {
                            schema("crime") {
                                int("value")
                                boolean("increase")
                            }
                            schema("corruption") {
                                int("value")
                                boolean("increase")
                            }
                            schema("strife") {
                                int("value")
                                boolean("increase")
                            }
                            schema("decay") {
                                int("value")
                                boolean("increase")
                            }
                        }
                    }
                }
            }
            array("groups") {
                schema {
                    string("name")
                    int("negotiationDC")
                    boolean("atWar")
                    boolean("preventPledgeOfFealty")
                    enum<Relations>("relations")
                }
            }
            schema("skillRanks") {
                int("agriculture")
                int("arts")
                int("boating")
                int("defense")
                int("engineering")
                int("exploration")
                int("folklore")
                int("industry")
                int("intrigue")
                int("magic")
                int("politics")
                int("scholarship")
                int("statecraft")
                int("trade")
                int("warfare")
                int("wilderness")
            }
            schema("abilityScores") {
                int("economy")
                int("stability")
                int("loyalty")
                int("culture")
            }
            array("milestones") {
                schema {
                    string("id")
                    boolean("completed")
                    boolean("enabled")
                }
            }
        }
    }
}