package org.powbot.krulvis.chompy.tree.branch

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.chompy.ChompyBird
import org.powbot.krulvis.chompy.tree.leaf.KillBird

class BirdSpawned(script:ChompyBird) :Branch<ChompyBird>(script, "BirdSpawned?"){
    override val failedComponent: TreeComponent<ChompyBird> = HasToad(script)
    override val successComponent: TreeComponent<ChompyBird> = KillBird(script)

    override fun validate(): Boolean {
        script.currentTarget =  Npcs.stream().name("Chompy bird").action("Attack").nearest().first()
        return script.currentTarget.valid() && me.interacting() != script.currentTarget
    }
}

//class IsKilling(script:ChompyBird) :Branch<ChompyBird>(script, "Should kill"){
//    override val failedComponent: TreeComponent<ChompyBird>
//        get() = TODO("Not yet implemented")
//    override val successComponent: TreeComponent<ChompyBird> = KillBird(script)
//
//    override fun validate(): Boolean {
//        return script.currentTarget.healthBarVisible() || me.interacting() == script.currentTarget
//    }
//}

