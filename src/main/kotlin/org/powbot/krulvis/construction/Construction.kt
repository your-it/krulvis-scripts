package org.powbot.krulvis.construction

import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.construction.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Construction",
    version = "1.0.0",
    category = ScriptCategory.Construction,
    description = "AIO Construction",
    author = "Krulvis",
    priv = true
)
class Construction : ATScript() {
    override fun createPainter(): ATPaint<*> = ConstructionPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBank(this)
}