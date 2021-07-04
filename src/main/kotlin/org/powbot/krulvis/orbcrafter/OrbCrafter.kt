package org.powbot.krulvis.orbcrafter

import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.orbcrafter.tree.branch.ShouldBank
import org.powerbot.script.Script

@Script.Manifest(
    name = "krul Orbs",
    description = "Fishing trawler minigame",
    version = "1.0",
    markdownFileName = "Orb.md",
    properties = "category=Farming;",
    mobileReady = true
)
class OrbCrafter : ATScript() {
    override val painter: ATPainter<*> = OrbPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    override fun startGUI() {
        TODO("Not yet implemented")
    }
}