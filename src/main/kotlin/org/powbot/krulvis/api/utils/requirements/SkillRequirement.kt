package org.powbot.krulvis.api.utils.requirements

import org.powbot.api.rt4.Skills

class SkillRequirement(val skill: Int, val level: Int) : Requirement {
    override fun hasRequirement(): Boolean {
        return Skills.level(skill) >= level
    }
}