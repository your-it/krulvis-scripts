package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.EnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.Leave

class ShouldEnterBoat(script: Tempoross) : Branch<Tempoross>(script, "Should enter boat") {
    override fun validate(): Boolean {
        if (ctx.game.clientState() != 30) {
            return !waitFor(10000) { script.getEnergy() > -1 }
        }
        return script.getEnergy() == -1 && !BOAT_AREA.contains(me.tile())
                && !ctx.npcs.toStream().name("Ammunition crate").findFirst().isPresent
                && ctx.npcs.toStream().noneMatch { it.actions().contains("Leave") }
    }

    override val successComponent: TreeComponent<Tempoross> = EnterBoat(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldChill(script)
}

class ShouldChill(script: Tempoross) : Branch<Tempoross>(script, "Should Chill") {
    override fun validate(): Boolean {
        if (script.side == Tempoross.Side.UNKNOWN) {
            if (ctx.npcs.toStream().name("Ammunition crate").findFirst().isPresent) {
                println("Getting Side of minigame")
                val mast = ctx.objects.toStream().name("Mast").nearest().first()
                println("Mast found: $mast, orientation: ${mast.orientation()}")
                script.side = if (mast.orientation() == 4) Tempoross.Side.SOUTH else Tempoross.Side.NORTH
                script.mastLocation = mast.tile()
            } else {
                println("Couldn't find ammunition crate")
                return true
            }
        }
        return false
    }


    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Waiting for game to start...") {
        waitFor(60000) { script.getEnergy() > -1 }
    }

    override val failedComponent: TreeComponent<Tempoross> = ShouldLeave(script)
}

class ShouldLeave(script: Tempoross) : Branch<Tempoross>(script, "Should leave") {
    override fun validate(): Boolean {
        return ctx.npcs.toStream().anyMatch { it.actions().contains("Leave") }
    }

    override val successComponent: TreeComponent<Tempoross> = Leave(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldUntether(script)
}