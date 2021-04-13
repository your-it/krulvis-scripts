package org.powbot.krulvis.api

import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.*
import org.powerbot.script.rt4.*
import org.powerbot.script.rt4.ClientContext
import org.powerbot.script.rt4.Interactive
import java.awt.Point


interface ATContext {

    val script: ATScript
    val ctx: ClientContext get() = script.ctx
    val controller: Script.Controller get() = ctx.controller
    val inventory: Inventory get() = ctx.inventory
    val bank: Bank get() = ctx.bank
    val npcs: Npcs get() = ctx.npcs
    val skills: Skills get() = ctx.skills
    val objects: Objects get() = ctx.objects
    val ge: GrandExchange get() = ctx.grandExchange
    val combat: Combat get() = ctx.combat
    val equipment: Equipment get() = ctx.equipment
    val me: Player get() = ctx.players.local()
    val game: Game get() = ctx.game
    val menu: Menu get() = ctx.menu
    val camera: Camera get() = ctx.camera
    val widgets: Widgets get() = ctx.widgets
    val input: Input get() = ctx.input
    val walking: Movement get() = ctx.movement
    val chat: Chat get() = ctx.chat

    val lpf: LocalPathFinder get() = script.lpf

    fun debug(msg: String) {
        if (script.debugComponents) {
            println(msg)
        }
    }


    fun turnRunOn(): Boolean {
        if (walking.running()) {
            return true
        }
        if (ctx.client().runPercentage >= Random.nextInt(1, 5) && walking.running(true)) {
            return true
        }
        return false
    }

    fun Movement.moving(): Boolean = walking.destination() != Tile.NIL

    fun walk(position: Tile?, enableRun: Boolean = true, forceMinimap: Boolean = false): Boolean {
        if (position == null || position == Tile.NIL) {
            return true
        }
        val position = position.getWalkableNeighbor() ?: return false
        if (me.tile() == position) {
            return true
        }
        if (enableRun && !walking.running() && ctx.client().runPercentage > script.nextRun) {
            walking.running(true)
            script.nextRun = Random.nextInt(1, 5)
        }
        if (!walking.moving() || script.walkDelay.isFinished()) {
            if (forceMinimap && position.onMap()) {
                //PowBot single tile interaction method (map)
                walking.step(position)
            } else {
                val localPath = lpf.findPath(position)
                if (localPath.isNotEmpty()) {
                    localPath.traverse()
                } else {
                    debug("Using powbot method to walk")
                    //Powbot method
                    walking.walkTo(position)
                }
            }
            script.walkDelay.resetTimer()
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
        val destination = walking.destination()
        turnRunOn()
        debug("Interacting with: ${(target as Nameable).name()} at: $pos")
        if (!t.inViewport()
            || (destination != pos && pos.distanceTo(if (destination == Tile.NIL) me else destination) > (if (alwaysWalk) 4 else 12))
        ) {
            if (allowWalk) {
                debug("Walking before interacting... in viewport: ${t.inViewport()}")
                if (pos.matrix(ctx).onMap()) {
                    walking.step(pos)
                } else {
                    walk(pos)
                }
            }
        }
        if (selectItem > -1) {
            val item = inventory.firstOrNull { it.id() == selectItem } ?: return false
            item.click()
        }
        return t.interact(action) && clicked()
    }

//    fun visible(target: Modelable): Boolean {
//        val randPoint = target.next
//        return randPoint.x > -1 && randPoint.y > -1
//    }
//
//    /**
//     * Custom interact function if Renderable is visible on screen
//     */
//    fun interactVisible(target: Interactive, action: String): Boolean {
//        var tries = if (walking.destination() != Tile.NIL) 3 else 1
//        val name = (target as Nameable).name().replace(Regex("<[^>]*>"), "")
//        // Move away if hovering on similar target with same action, name
//        if (!target.contains(input.location) && menu.contains(MenuOption(action, name))) {
//            input.move(target.basePoint())
//        }
//        while (tries > 0 && visible(target)) {
//            val timeout = Timer(Random.nextInt(600, 800))
//            while (!timeout.isFinished()
//                && menu.g(action, name) < 0
//                && !menu.isOpen && visible(target)
//            ) {
//                debug("Moving mouse")
//                mouse.move(target.randomPoint)
//                waitFor(100) { menu.contains(action) }
//            }
//            val index = menu.getIndex(action, name)
//            if (index == 0 && mouse.click(false)) {
//                if (clicked()) {
//                    debug("Left mouse-click")
//                    return true
//                }
//            } else if (index > 0 && openMenu(action)) {
//                if (menu.interact(action, name) && clicked()) {
//                    debug("Interacted with menu")
//                    return true
//                }
//            } else if (menu.isOpen) {
//                menu.close()
//            }
//
//            tries--
//        }
//        return false
//    }

    /**
     * Opens menu if necessary
     */
    fun openMenu(action: String): Boolean {
//        println("Opening menu")
        if (menu.opened()) {
            if (menu.containsAction(action)) {
                return true
            } else {
//                println("Closing menu")
                menu.close()
            }
        } else if (input.click(true)) {
            if (waitFor { menu.opened() }) {
                return menu.containsAction(action)
            }
        }

        return false
    }

    /**
     * Returns true if red mouse is found
     */
    fun clicked(): Boolean = waitFor(601) { ctx.game.crosshair() == Game.Crosshair.ACTION }

    fun Locatable.distance(): Int = tile().distanceTo(me).toInt()

    fun Locatable.onMap(): Boolean = tile().matrix(ctx).onMap()
    fun Locatable.mapPoint(): Point = game.tileToMap(tile())


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

    /**
     * Returns: [Tile] nearest neighbor or self as  which is walkable
     */
    fun Locatable.getWalkableNeighbor(
        excludeSelf: Boolean = false,
        diagonalTiles: Boolean = false,
        filter: (Tile) -> Boolean = { true }
    ): Tile? {
        val walkableNeighbors = getWalkableNeighbors(excludeSelf, diagonalTiles)
        return walkableNeighbors.filter(filter).minBy { it.distance() }
    }

    fun Locatable.getWalkableNeighbors(
        excludeSelf: Boolean = false,
        diagonalTiles: Boolean = false
    ): MutableList<Tile> {

        val x = tile().x()
        val y = tile().y()
        val f = tile().floor()
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
        val mos = game.mapOffset()
        return Tile(x() - mos.x(), y() - mos.y(), floor())
    }

    fun Tile.getFlag(collisionMap: ICollisionMap): Int {
        val regionTile = toRegionTile()
        val localX = regionTile.x()
        val localY = regionTile.y()
//        val offset = walking.getCollisionOffset(plane)

        val collX: Int = localX - collisionMap.offsetX
        val collY: Int = localY - collisionMap.offsetY
        return try {
            collisionMap.flags[collX][collY]
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


    fun Inventory.contains(vararg ids: Int): Boolean = toStream().anyMatch { it.id() in ids }
    fun Inventory.emptySlots(): Int = (28 - toStream().count()).toInt()

    fun Inventory.getCount(vararg ids: Int): Int = getCount(false, *ids)
    fun Inventory.getCount(countStacks: Boolean, vararg ids: Int): Int {
        val items = toStream().id(*ids)
        return if (countStacks) items.count(true).toInt() else items.count().toInt()
    }

    fun Int.getItemDef() = CacheItemConfig.load(ctx.bot().cacheWorker, this)
    fun Item.getItemDef() = CacheItemConfig.load(ctx.bot().cacheWorker, id())

    fun withdrawExact(id: Int, amount: Number, wait: Boolean = true): Boolean {
        return bank.withdrawExact(id, amount, wait)
    }

    fun Bank.withdrawExact(amount: Int, id: Number, wait: Boolean = true): Boolean {
        val id = id.toInt()
        if (id <= 0) {
            return false
        }
        debug("WithdrawExact: $id, $amount")
        val currentAmount = inventory.getCount(true, id)
        if (currentAmount < amount) {
            if (bank.none { it.id() == id }) {
                return false
            } else if (amount - currentAmount >= bank.toStream().id(id).count(true)) {
                bank.withdraw(id, Bank.Amount.ALL)
            } else if (amount - currentAmount >= inventory.emptySlots() && !id.getItemDef().stackable) {
                debug("Withdrawing all: $id, since there's just enough space")
                bank.withdraw(id, Bank.Amount.ALL)
            } else if (!bank.withdraw(amount - currentAmount, id)) {
                return false
            }
        } else if (currentAmount > amount) {
            bank.deposit(id, Bank.Amount.ALL)
            if (wait) waitFor { !inventory.contains(id) }
            return false
        }
        return if (wait) waitFor(5000) { inventory.getCount(true, id) == amount } else true
    }
}