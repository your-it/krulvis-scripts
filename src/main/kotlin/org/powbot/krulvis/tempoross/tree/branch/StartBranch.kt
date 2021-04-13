package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.EnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.Leave
import org.powerbot.script.rt4.Npc

class ShouldEnterBoat(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        if (game.clientState() != 30) {
            return !waitFor(10000) { script.getEnergy() > -1 }
        }
        return script.getEnergy() == -1 && !BOAT_AREA.contains(me.tile())
                && npcs.toStream().name("Ammunition crate").findFirst().isEmpty
                && npcs.toStream().noneMatch { it.actions().contains("Leave") }
    }

    override fun onSuccess(): TreeComponent {
        return EnterBoat(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldChill(script)
    }
}

class ShouldChill(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        if (script.side == Tempoross.Side.UNKNOWN) {
            if (npcs.toStream().name("Ammunition crate").findFirst().isPresent) {
                println("Getting Side of minigame")
                val mast = objects.toStream().name("Mast").nearest().first()
                println("Mast found: $mast, orientation: ${mast.orientation()}")
                script.side = if (mast.orientation() == 4) Tempoross.Side.SOUTH else Tempoross.Side.NORTH
                script.mastLocation = mast.tile()
            } else {
                return true
            }
        }
        return false
    }

    override fun onSuccess(): TreeComponent {
        return object : Leaf<Tempoross>(script, "Waiting for game to start..") {
            override fun loop() {
                waitFor(60000) { script.getEnergy() > -1 }
            }
        }
    }

    override fun onFailure(): TreeComponent {
        return ShouldLeave(script)
    }
}

class ShouldLeave(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        return npcs.toStream().anyMatch { it.actions().contains("Leave") }
    }

    override fun onSuccess(): TreeComponent {
        return Leave(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldUntether(script)
    }
}