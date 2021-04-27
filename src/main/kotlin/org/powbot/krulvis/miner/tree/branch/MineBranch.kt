package org.powbot.krulvis.miner.tree.branch

import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Ore.Companion.getOre
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.Mine
import org.powbot.krulvis.miner.tree.leaf.WalkToSpot
import org.powerbot.script.Tile

class AtSpot(script: Miner) : Branch<Miner>(script, "AtSpot") {

    override fun validate(): Boolean {
        return script.profile.center.distance() < script.profile.radius
    }

    override val successComponent: TreeComponent<Miner> = IsMining(script)
    override val failedComponent: TreeComponent<Miner> = WalkToSpot(script)
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
        val facingRocks = ctx.objects.toStream().at(facingTile()).name("Rocks").findFirst()
        return me.animation() > 0 && facingRocks.isPresent && facingRocks.get().getOre() != null
    }

    override val successComponent: TreeComponent<Miner> = SimpleLeaf(script, "Chilling") {}
    override val failedComponent: TreeComponent<Miner> = Mine(script)
}