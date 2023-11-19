package org.powbot.krulvis.construction

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Plank
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.construction.tree.branch.InHouse

//TODO("This script is unfinished. Only started")
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

    var lastLeafCompleted = System.currentTimeMillis()

    override val rootComponent: TreeComponent<*> = InHouse(this)

    var plank = Plank.TEAK

    fun plankCount() = plank.getInventoryCount(false)

    fun buildSpace() = Objects.stream(40).type(GameObject.Type.WALL_DECORATION).name("Guild trophy space").firstOrNull()

    fun builtObj() = Objects.stream(40).type(GameObject.Type.WALL_DECORATION).name("Mythical cape").firstOrNull()

    fun hasCape() = Inventory.stream().id(22114).count(false) >= 1
    fun hasMats() = plankCount() >= 3 && hasCape()

}

fun main() {
    Construction().startScript("127.0.0.1", "GIM", true)
}