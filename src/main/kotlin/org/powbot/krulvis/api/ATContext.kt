package org.powbot.krulvis.api

import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.walking.PBWebWalkingService
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.Locatable
import org.powerbot.script.Nameable
import org.powerbot.script.Tile
import org.powerbot.script.rt4.*
import java.awt.Point
import kotlin.math.abs


object ATContext {

    val ctx: ClientContext get() = org.powerbot.script.ClientContext.ctx()

    val me: Player get() = ctx.players.local()

    var nextRun = Random.nextInt(2, 5)

    val walkDelay = DelayHandler(2500, OddsModifier(), "Walking delay")

    var debugComponents: Boolean = true

    fun debug(msg: String) {
        if (debugComponents) {
            println(msg)
        }
    }

    fun turnRunOn(): Boolean {
        if (ctx.movement.running()) {
            return true
        }
        if (ctx.client().runPercentage >= Random.nextInt(1, 5) && ctx.movement.running(true)) {
            return true
        }
        return false
    }

    fun Movement.moving(): Boolean = ctx.movement.destination() != Tile.NIL

    fun walk(position: Tile?, enableRun: Boolean = true, forceMinimap: Boolean = false): Boolean {
        if (position == null || position == Tile.NIL) {
            return true
        }
        val position = position.getWalkableNeighbor() ?: return false
        if (ctx.players.local().tile() == position) {
            return true
        }
        if (enableRun && !ctx.movement.running() && ctx.client().runPercentage > nextRun) {
            ctx.movement.running(true)
            nextRun = Random.nextInt(1, 5)
        }
        if (!ctx.movement.moving() || walkDelay.isFinished()) {
            if (forceMinimap && position.onMap()) {
                //PowBot single tile interaction method (map)
                ctx.movement.step(position)
            } else {
                val localPath = LocalPathFinder.findPath(position)
                if (localPath.isNotEmpty()) {
                    localPath.traverse()
                } else {
                    debug("Using powbot method to walk")
                    //Powbot method
                    PBWebWalkingService.walkTo(position, false)
                }
            }
            walkDelay.resetTimer()
        }
        return false
    }

    /**
     * Custom interaction function
     */
    fun interact(
        target: Interactive?,
        action: String,
        alwaysWalk: Boolean = false,
        allowWalk: Boolean = true,
        selectItem: Int = -1
    ): Boolean {
        val t = target ?: return false
        val pos = (t as Locatable).tile()
        val destination = ctx.movement.destination()
        turnRunOn()
        debug("Interacting with: ${(target as Nameable).name()} at: $pos")
        if (!t.inViewport()
            || (destination != pos && pos.distanceTo(if (destination == Tile.NIL) me else destination) > (if (alwaysWalk) 4 else 12))
        ) {
            if (allowWalk) {
                debug("Walking before interacting... in viewport: ${t.inViewport()}")
                if (pos.matrix(ctx).onMap()) {
                    ctx.movement.step(pos)
                } else {
                    walk(pos)
                }
            }
        }
        if (selectItem > -1) {
            val item = ctx.inventory.firstOrNull { it.id() == selectItem } ?: return false
            item.click()
        }
        return t.interact(action)
    }


    fun Locatable.distance(): Int =
        tile().distanceTo(ctx.players.local()).toInt()

    fun Tile.distanceM(dest: Locatable): Int {
        return abs(dest.tile().x() - x()) + abs(dest.tile().y() - y())
    }

    fun Locatable.onMap(): Boolean = tile().matrix(ctx).onMap()

    fun Locatable.mapPoint(): Point = ctx.game.tileToMap(tile())

    /**
     * Returns: [Tile] nearest neighbor or self as  which is walkable
     */
    fun Locatable.getWalkableNeighbor(
        diagonalTiles: Boolean = false,
        filter: (Tile) -> Boolean = { true }
    ): Tile? {
        val walkableNeighbors = getWalkableNeighbors(diagonalTiles)
        return walkableNeighbors.filter(filter).minByOrNull { it.distance() }
    }

    fun Locatable.getWalkableNeighbors(
        diagonalTiles: Boolean = false
    ): MutableList<Tile> {

        val t = tile()
        val x = t.x()
        val y = t.y()
        val f = t.floor()
        val cm = ctx.client().collisionMaps[f]

        val n = Tile(x, y + 1, f)
        val e = Tile(x + 1, y, f)
        val s = Tile(x, y - 1, f)
        val w = Tile(x - 1, y, f)
        val straight = listOf(n, e, s, w)
        val ne = Tile(x + 1, y + 1, f)
        val se = Tile(x + 1, y - 1, f)
        val sw = Tile(x - 1, y - 1, f)
        val nw = Tile(x - 1, y + 1, f)
        val diagonal = listOf(ne, se, sw, nw)

        val walkableNeighbors = mutableListOf<Tile>()
        walkableNeighbors.addAll(straight.filter { !it.blocked(cm) })

        if (diagonalTiles) {
            walkableNeighbors.addAll(diagonal.filter { !it.blocked(cm) })
        }
        return walkableNeighbors
    }

    fun Tile.toRegionTile(): Tile {
        val mos = ctx.game.mapOffset()
        return Tile(x() - mos.x(), y() - mos.y(), floor())
    }

    fun Tile.getFlag(collisionMap: ICollisionMap): Int {
        val regionTile = toRegionTile()
        val localX = regionTile.x()
        val localY = regionTile.y()

        return try {
            collisionMap.flags[localX][localY]
        } catch (e: ArrayIndexOutOfBoundsException) {
            0
        }
    }

    fun Tile.blocked(flags: ICollisionMap, blockFlag: Int = Flag.BLOCKED): Boolean {
        val flag = getFlag(flags)
        if (flag == 0) {
            return false
        }
        return flag and (Flag.WATER or Flag.BLOCKED or Flag.BLOCKED2 or Flag.BLOCKED4 or blockFlag) != 0
    }


    fun Inventory.containsOneOf(vararg ids: Int): Boolean = toStream().anyMatch { it.id() in ids }
    fun Inventory.emptyExcept(vararg ids: Int): Boolean = !toStream().filter { it.id() !in ids }.findFirst().isPresent

    fun Inventory.emptySlots(): Int = (28 - toStream().count()).toInt()
    fun Inventory.getCount(vararg ids: Int): Int = getCount(false, *ids)
    fun Inventory.getCount(countStacks: Boolean, vararg ids: Int): Int {
        val items = toStream().id(*ids)
        return if (countStacks) items.count(true).toInt() else items.count().toInt()
    }

    fun Int.getItemDef() = CacheItemConfig.load(ctx.bot().cacheWorker, this)
    fun Item.getItemDef() = CacheItemConfig.load(ctx.bot().cacheWorker, id())

    fun withdrawExact(id: Int, amount: Number, wait: Boolean = true): Boolean {
        return ctx.bank.withdrawExact(id, amount, wait)
    }

    fun Bank.withdrawExact(amount: Int, id: Number, wait: Boolean = true): Boolean {
        val id = id.toInt()
        if (id <= 0) {
            return false
        }
        debug("WithdrawExact: $id, $amount")
        val currentAmount = ctx.inventory.getCount(true, id)
        if (currentAmount < amount) {
            if (ctx.bank.none { it.id() == id }) {
                return false
            } else if (amount - currentAmount >= ctx.bank.toStream().id(id).count(true)) {
                ctx.bank.withdraw(id, Bank.Amount.ALL)
            } else if (amount - currentAmount >= ctx.inventory.emptySlots() && !id.getItemDef().stackable) {
                debug("Withdrawing all: $id, since there's just enough space")
                ctx.bank.withdraw(id, Bank.Amount.ALL)
            } else if (!ctx.bank.withdraw(amount - currentAmount, id)) {
                return false
            }
        } else if (currentAmount > amount) {
            ctx.bank.deposit(id, Bank.Amount.ALL)
            if (wait) waitFor { !ctx.inventory.containsOneOf(id) }
            return false
        }
        return if (wait) waitFor(5000) { ctx.inventory.getCount(true, id) == amount } else true
    }


//    fun Menu.close(): Boolean {
//        val ma = menu.area
//        val width = 763
//        val height = 499
//        val x = if (ma.centerX > width / 2.0) Random.nextInt(ma.x - 1) else Random.nextInt(ma.x + ma.width, width)
//        val y =
//            if (ma.centerY > height / 2.0) Random.nextInt(ma.y - 1) else Random.nextInt(ma.y + ma.height, height)
//        val pointToGetRid = Point(x, y)
//        mouse.move(pointToGetRid)
//        return waitFor { !menu.isOpen }
//    }


}