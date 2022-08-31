package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.EnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.Leave

class ShouldEnterBoat(script: Tempoross) : Branch<Tempoross>(script, "Should enter boat") {
    override fun validate(): Boolean {
        Game.closeOpenTab()
        if (Game.clientState() != 30) {
            return !waitFor(10000) { script.getEnergy() > -1 }
        }
        return script.getEnergy() == -1 && !BOAT_AREA.contains(me.tile())
                && Npcs.stream().name("Ammunition crate").firstOrNull() == null
                && Npcs.stream().noneMatch { it.actions().contains("Leave") }
    }

    override val successComponent: TreeComponent<Tempoross> = EnterBoat(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldChill(script)
}

class ShouldChill(script: Tempoross) : Branch<Tempoross>(script, "Should Chill") {
    override fun validate(): Boolean {
        if (script.side == Side.UNKNOWN) {
            if (Npcs.stream().name("Ammunition crate").findFirst().isPresent) {
                script.log.info("Getting Side of minigame")
                val mast = Objects.stream().name("Mast").nearest().first()
                script.log.info("Mast found: $mast, orientation: ${mast.orientation()}")
                script.side = if (mast.orientation() == 4) Side.SOUTH else Side.NORTH
                script.side.mastLocation = mast.tile()
            } else {
                script.log.info("Couldn't find ammunition crate")
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
        return Npcs.stream().anyMatch { it.actions().contains("Leave") }
    }

    override val successComponent: TreeComponent<Tempoross> = Leave(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldUntether(script)
}