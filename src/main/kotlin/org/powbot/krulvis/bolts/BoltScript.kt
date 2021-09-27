package org.powbot.krulvis.bolts

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep

@ScriptManifest(name = "Krul Bolts", version = "1.0.1", description = "", priv = true)
class BoltScript : ATScript() {
    override fun createPainter(): ATPaint<*> = BoltPainter(this)

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        val items = Inventory.stream().id(11876, 314).list()
        if (items.size == 2)
            items.forEach {
                it.click()
                sleep(Random.nextInt(20, 100))
            }

    }

}

class BoltPainter(script: BoltScript) : ATPaint<BoltScript>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackInventoryItem(11875)
            .trackSkill(Skill.Fletching)
            .build()
    }


}

fun main() {
    BoltScript().startScript(true)
}
