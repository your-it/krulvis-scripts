package org.powbot.krulvis.api.utils.trackers

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.utils.Timer
import java.awt.Graphics2D

class SkillTracker(val script: ATScript) {


    private var started = false

    private val skills = mutableListOf<Skill>()
    private val startLvl = mutableListOf<Int>()
    private val startXp = mutableListOf<Int>()
    private val currentXp = mutableListOf<Int>()
    private val currentLvl = mutableListOf<Int>()
    private val gainedXp = mutableListOf<Int>()
    private val gainedLvl = mutableListOf<Int>()

    /**
     * Gets called automatically, don't bother
     */
    fun track() {
        if (started) {
            skills.forEachIndexed { index, skill ->
                currentLvl[index] = script.ctx.skills.realLevel(skill.index)
                currentXp[index] = script.ctx.skills.experience(skill.index)

                gainedLvl[index] = currentLvl[index] - startLvl[index]
                gainedXp[index] = currentXp[index] - startXp[index]
            }
        } else if (skills.sumBy { script.ctx.skills.experience(it.index) } > 0) {
            startTracker()
            started = true
        }
    }

    /**
     * Call once to make sure that the added skill gets trackedl
     */
    fun addSkill(vararg skills: Skill) {
        this.skills.addAll(skills)
        for (i in 0..skills.size) {
            this.startLvl.add(0)
            this.startXp.add(0)
            this.currentLvl.add(0)
            this.currentXp.add(0)
            this.gainedXp.add(0)
            this.gainedLvl.add(0)
        }
    }

    private fun startTracker() {
        skills.forEachIndexed { index, skill ->
            startLvl[index] = script.ctx.skills.realLevel(skill.index)
            startXp[index] = script.ctx.skills.experience(skill.index)
        }
    }

    fun draw(g: Graphics2D, x: Int, y: Int, t: Timer = script.timer): Int {
        var y = y
        skills.forEachIndexed { index, skill ->
            val gainedXp = this.gainedXp[index]
            val gainedLvl = this.gainedLvl[index]
            if (gainedXp > 0) {
                val gainedHr = t.getPerHour(gainedXp)
                val gains = if (gainedXp > 10000) ATPainter.formatAmount(gainedXp) else "" + gainedXp
                val gainshr = if (gainedHr > 10000) ATPainter.formatAmount(gainedHr) else "" + gainedHr
                y += 13
                script.painter!!.drawSplitText(
                    g,
                    "$skill XP: ",
                    "$gains, $gainshr/hr",
                    x,
                    y
                )
                y += 13
                val ttnl = t.getTimeToNextLevel(
                    gainedHr,
                    currentLvl[index],
                    currentXp[index].toLong()
                )
                script.painter.drawSplitText(
                    g,
                    "$skill Lvl: ",
                    "${currentLvl[index]}, ($gainedLvl) TTNL: $ttnl",
                    x,
                    y
                )
            }
        }
        return y + 13
    }

    /**
     * For ProgressHandler
     */
    fun getProgress(): Map<String, Int> {
        return skills.map { Pair(it.name + "xp", gainedXp[skills.indexOf(it)]) }.toMap()
    }

    /**
     * For ProgressHandler
     */
    fun getProgress(skill: Skill): Pair<String, Int> {
        val trackingSkill = skills.firstOrNull { it == skill }?.ordinal ?: -1
        return Pair(skill.name + "xp", if (trackingSkill != -1) gainedXp[trackingSkill] else 0)
    }

    /**
     * For Activity tracker
     */
    fun getTotalXPGained(): Int {
        return gainedXp.sumBy { it }
    }

}