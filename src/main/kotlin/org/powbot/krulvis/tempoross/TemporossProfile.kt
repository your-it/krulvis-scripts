package org.powbot.krulvis.tempoross

import org.powbot.krulvis.api.script.ScriptProfile


data class TemporossProfile(
    val shootAfterTethering: Boolean = true,
    val minFishToForceShoot: Int = 15,
    val minFishToCook: Int = 15
) : ScriptProfile