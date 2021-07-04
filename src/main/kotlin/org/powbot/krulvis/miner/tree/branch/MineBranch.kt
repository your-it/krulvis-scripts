package org.powbot.krulvis.miner.tree.branch

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.extensions.items.Ore.Companion.getOre
import org.powbot.krulvis.api.extensions.items.Ore.Companion.hasOre
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.Mine
import org.powbot.krulvis.miner.tree.leaf.WalkToSpot
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.util.*

class AtSpot(script: Miner) : Branch<Miner>(script, "AtSpot") {

    override fun validate(): Boolean {
        return script.profile.center.distance() < script.profile.radius
    }

    override val successComponent: TreeComponent<Miner> = ShouldHop(script)
    override val failedComponent: TreeComponent<Miner> = WalkToSpot(script)
}

class ShouldHop(script: Miner) : Branch<Miner>(script, "ShouldHop") {

    val hopDelay = DelayHandler(2000, script.oddsModifier, "Hop delay")
    override fun validate(): Boolean {
        if (!script.profile.hopFromPlayers) {
            return false
        }
        val nearByPlayers = ctx.players.filter {
            it.name() != ctx.players.local().name() && it.tile()
                .distanceTo(script.profile.center) <= script.profile.radius
        }
        if (nearByPlayers.isNotEmpty()) {
            if (hopDelay.isFinished()) {
                return true
            }
        } else {
            hopDelay.resetTimer()
        }
        return false
    }

    override val successComponent: TreeComponent<Miner> = SimpleLeaf(script, "Hopping") {
        val worlds = ctx.worlds.joinable().get()
        worlds[Random.nextInt(0, worlds.size)].hop()
    }
    override val failedComponent: TreeComponent<Miner> = IsMining(script)
}

class IsMining(script: Miner) : Branch<Miner>(script, "IsMining") {

    fun facingTile(): Tile {
        val orientation: Int = ctx.players.local().orientation()
        val t: Tile = ctx.players.local().tile()
        when (orientation) {
            4 -> return Tile(t.x(), t.y() + 1, t.floor())
            6 -> return Tile(t.x() + 1, t.y(), t.floor())
            0 -> return Tile(t.x(), t.y() - 1, t.floor())
            2 -> return Tile(t.x() - 1, t.y(), t.floor())
        }
        return Tile.NIL
    }


    override fun validate(): Boolean {
        return me.animation() > 0 && ctx.objects.toStream().at(facingTile()).anyMatch { it.hasOre() }
    }

    override val successComponent: TreeComponent<Miner> =
        SimpleLeaf(script, "Chilling") {
            waitFor(1500) {
                ctx.objects.toStream().at(facingTile()).noneMatch { it.hasOre() }
            }
        }
    override val failedComponent: TreeComponent<Miner> = Mine(script)
}