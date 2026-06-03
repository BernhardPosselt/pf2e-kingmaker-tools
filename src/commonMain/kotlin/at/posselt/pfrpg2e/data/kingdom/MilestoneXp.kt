package at.posselt.pfrpg2e.data.kingdom

data class MilestoneXp(
    val xpAward: Int,
    val milestone: String,
)

val milestoneXpAwards = listOf(
    MilestoneXp(xpAward = 40, milestone = "Claim your first Landmark"),
    MilestoneXp(xpAward = 40, milestone = "Claim your first Refuge"),
    MilestoneXp(xpAward = 40, milestone = "Establish your first village"),
    MilestoneXp(xpAward = 40, milestone = "Reach kingdom Size 10"),
    MilestoneXp(xpAward = 60, milestone = "Establish diplomatic relations for the first time"),
    MilestoneXp(xpAward = 60, milestone = "Expand a village into your first town"),
    MilestoneXp(xpAward = 60, milestone = "All eight leadership roles are assigned"),
    MilestoneXp(xpAward = 60, milestone = "Reach kingdom Size 25"),
    MilestoneXp(xpAward = 80, milestone = "Establish your first trade agreement"),
    MilestoneXp(xpAward = 80, milestone = "Expand a town into your first city"),
    MilestoneXp(xpAward = 80, milestone = "Reach kingdom Size 50"),
    MilestoneXp(xpAward = 80, milestone = "Spend 100 RP during a Kingdom turn"),
    MilestoneXp(xpAward = 120, milestone = "Expand a city into your first metropolis"),
    MilestoneXp(xpAward = 120, milestone = "Reach kingdom Size 100"),
)
