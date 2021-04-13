package org.powbot.krulvis.tempoross

import org.powbot.krulvis.api.script.ScriptSettings


data class TemporossProfile(
    val cook: Boolean = true,
    val shootAfterTethering: Boolean = true,
    val minFishToForceShoot: Int = 15,
    val minFishToCook: Int = 15
) : ScriptSettings