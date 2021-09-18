package org.powbot.krulvis.orbcrafter

import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.orbcrafter.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Orbs",
    description = "Craft orbs",
    author = "Krulvis",
    version = "1.0.1",
    markdownFileName = "Orb.md",
    category = ScriptCategory.Magic
)
class OrbCrafter : ATScript() {

    override fun createPainter(): ATPaint<*> = OrbPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

}