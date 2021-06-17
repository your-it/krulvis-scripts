package org.powbot.krulvis.fighter

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.fighter.tree.branch.ShouldBank
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Npc

@Script.Manifest(
    name = "krul Fighter",
    description = "AIO Fighting script",
    version = "1.0",
    markdownFileName = "Fighter.md",
    properties = "category=Combat;",
    mobileReady = true
)
class Fighter : ATScript() {

    init {
        skillTracker.addSkill(Skill.HITPOINTS, Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.RANGED, Skill.MAGIC)
    }

    var forcedBanking = false
    var profile = FighterProfile()

    fun validTarget(npc: Npc): Boolean {
        return profile.names.contains(npc.name())
    }

    override val painter: ATPainter<*> = FighterPainter(this)
    override val rootComponent = ShouldBank(this)

    override fun startGUI() {
        started = true
    }
}