package org.powbot.krulvis.hunter

import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.hunter.tree.branch.ShouldDrop
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Constants

class Hunter : ATScript() {
    override val painter: ATPainter<*> = HunterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldDrop(this)


    override fun startGUI() {
        started = true
    }

    var trapTiles: MutableList<Tile> = mutableListOf()

    fun getFinishedTraps() =
        ctx.objects.toStream(25)
            .action("Check")
            .filter { it.tile() in trapTiles }
            .nearest().findFirst()

    fun maxTraps(): Int = ctx.skills.realLevel(Constants.SKILLS_HUNTER)
}