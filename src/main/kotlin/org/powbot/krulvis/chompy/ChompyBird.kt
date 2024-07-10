package org.powbot.krulvis.chompy

import org.powbot.api.rt4.Npc
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.chompy.tree.branch.BirdSpawned

class ChompyBird : ATScript(){
    override fun createPainter(): ATPaint<*> = ChompyBirdPainter(this)

    var currentTarget: Npc = Npc.Nil

    override val rootComponent: TreeComponent<*> = BirdSpawned(this)

}