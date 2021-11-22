package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Kill
import org.powbot.krulvis.fighter.tree.leaf.Loot
import org.powbot.mobile.script.ScriptManager

class IsKilling(script: Fighter) : Branch<Fighter>(script, "Is Killing?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Chillings") {
        val interacting = Players.local().interacting()
        Chat.clickContinue()
        if (script.hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0) {
            Prayer.quickPrayer(true)
        }
        if (waitFor { interacting.healthPercent() == 0 }) {
            waitFor(Random.nextInt(1500, 3000)) { script.loot().isNotEmpty() }
        } else
            sleep(Random.nextInt(1000, 1500))
    }
    override val failedComponent: TreeComponent<Fighter> = CanLoot(script)

    override fun validate(): Boolean {
        val player = Players.local()
        if (player.interacting().name in script.monsters) {
            return !script.useSafespot
                    || script.safespot == Players.local().tile()
                    || !Players.local().healthBarVisible()
        }
        return false
    }
}

class CanLoot(script: Fighter) : Branch<Fighter>(script, "Can loot?") {
    override val successComponent: TreeComponent<Fighter> = Loot(script)
    override val failedComponent: TreeComponent<Fighter> = AtSpot(script)

    override fun validate(): Boolean {
        val loot = script.loot()
        return loot.isNotEmpty() && loot.first().reachable()
    }
}


class AtSpot(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = Kill(script)
    override val failedComponent: TreeComponent<Fighter> =
        SimpleLeaf(script, "Walking") {
            val spot =
                if (script.warriorGuild && script.lastDefenderIndex >= 6) Tile(2915, 9966, 0) else script.safespot
            if (spot == Tile.Nil) {
                Notifications.showNotification("You have to select a centertile/safespot before starting the script")
                ScriptManager.stop()
                return@SimpleLeaf
            }
            Movement.walkTo(spot)
        }

    override fun validate(): Boolean {
        val myTile = Players.local().tile()
        return if (script.useSafespot) script.safespot == myTile
        else script.target()?.reachable() == true && myTile.distanceTo(script.safespot) <= script.radius
    }
}

