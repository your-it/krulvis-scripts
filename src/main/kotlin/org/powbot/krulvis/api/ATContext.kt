package org.powbot.krulvis.api

import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.short
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.walking.PBWebWalkingService
import org.powerbot.script.*
import org.powerbot.script.rt4.*
import org.powerbot.script.rt4.ClientContext
import org.powerbot.script.rt4.Interactive
import java.awt.Point
import java.awt.Rectangle
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
        if (ctx.movement.energyLevel() >= Random.nextInt(1, 5)) {
            return ctx.movement.running(true)
        }
        return false
    }

    fun Movement.moving(): Boolean = ctx.movement.destination() != Tile.NIL

    fun walk(position: Tile?, enableRun: Boolean = true, forceMinimap: Boolean = false): Boolean {
        if (position == null || position == Tile.NIL) {
            return true
        }
        val flags = ctx.client().collisionMaps[position.floor()].flags
        val position = if (!position.blocked(flags)) position else position.getWalkableNeighbor() ?: return false
        if (ctx.players.local().tile() == position) {
            debug("Already on tile: $position")
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
                    debug("Using localwalker")
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
        val name = (t as Nameable).name()
        val pos = (t as Locatable).tile()
        val destination = ctx.movement.destination()
        turnRunOn()
        debug("Interacting with: $name at: $pos")
        if (ctx.menu.opened() && ctx.menu.contains {
                it.action.equals(action, true) && it.option.contains(
                    name,
                    true
                )
            }) {
            debug("Clicking directly on opened menu")
            return handleMenu(action, name)
        }
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
        val selectedId = ctx.inventory.selectedItem().id()
        if (selectedId != selectItem) {
            ctx.game.tab(Game.Tab.INVENTORY)
            if (selectItem > -1) {
                ctx.inventory.toStream().id(selectItem).findFirst().ifPresent {
                    it.interact("Use")
                }
            } else {
                ctx.inventory.toStream().id(selectedId).findFirst().get().click()
            }

        }
        return waitFor(short()) { ctx.inventory.selectedItem().id() == selectItem } && t.interact(action)
    }

    /**
     * Requires menu to be open
     */
    fun handleMenu(action: String, name: String): Boolean {
        if (!ctx.client().isMenuOpen) {
            return false
        }
        if (!ctx.menu.contains {
                it.action.equals(action, true) && it.option.contains(
                    name,
                    true
                )
            }) {
            debug("Closing menu")
            clickMenu(Menu.filter("Cancel"))
            waitFor { !ctx.client().isMenuOpen }
            return false
        }
        return clickMenu(
            Menu.filter(
                action,
                name
            )
        )
    }

    fun clickMenu(filter: Filter<in MenuCommand>): Boolean {
        val slot = ctx.menu.indexOf(filter)
        val headerOffset = if (ctx.client().isMobile) 29 else 19
        val itemOffset = if (ctx.client().isMobile) 24 else 15

        val rectangle = Rectangle(
            ctx.client().menuX,
            ctx.client().menuY + headerOffset + slot * itemOffset,
            ctx.client().menuWidth,
            itemOffset
        )
        val point = Point(
            Random.nextInt(rectangle.x + 3, rectangle.x + rectangle.width - 3),
            Random.nextInt(rectangle.y + 2, rectangle.y + rectangle.height - 2)
        )
        Condition.sleep(org.powerbot.script.Random.hicks(slot) / 2)
//        if (!ctx.input.move(
//                org.powerbot.script.Random.nextInt(rectangle.x, rectangle.x + rectangle.width),
//                org.powerbot.script.Random.nextInt(rectangle.y, rectangle.y + rectangle.height)
//            ) || !ctx.client().isMenuOpen
//        ) {
//            return false
//        }
        return ctx.input.click(point, true)
    }

    fun Locatable.distance(): Int =
        tile().distanceTo(ctx.players.local()).toInt()

    fun Tile.distanceM(dest: Locatable): Int {
        return abs(dest.tile().x() - x()) + abs(dest.tile().y() - y())
    }

    fun Locatable.onMap(): Boolean = tile().matrix(ctx).onMap()

    fun Locatable.mapPoint(): Point = ctx.game.tileToMap(tile())

    /**
     * Returns: [Tile] nearest neighbor or self as which is walkable
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
        val cm = ctx.client().collisionMaps[f].flags

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

    /**
     * Only useful for mobile
     */
    fun closeOpenHUD(): Boolean {
        val tab = ctx.game.tab()
        if (!ctx.client().isMobile || tab == Game.Tab.NONE) {
            return true
        }
        val c: Component = ctx.widgets.widget(601).firstOrNull { it.textureId() in tab.textures } ?: return true
        return c.click(true)
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