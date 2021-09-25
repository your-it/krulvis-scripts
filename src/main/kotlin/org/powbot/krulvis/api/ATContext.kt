package org.powbot.krulvis.api

import org.powbot.api.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.short
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.smither.Smithable
import org.powbot.mobile.script.ScriptManager
import kotlin.math.abs


object ATContext {


    val me: Player get() = Players.local()

    var nextRun = Random.nextInt(2, 5)

    val walkDelay = DelayHandler(2500, OddsModifier(), "Walking delay")

    var debugComponents: Boolean = true

    fun debug(msg: String) {
        if (debugComponents) {
            ScriptManager.script()?.log?.info(msg)
        }
    }

    fun turnRunOn(): Boolean {
        if (Movement.running()) {
            return true
        }
        if (Movement.energyLevel() >= Random.nextInt(1, 5)) {
            return Widgets.widget(Constants.MOVEMENT_MAP).component(Constants.MOVEMENT_RUN_ENERGY - 1).click()
        }
        return false
    }

    fun Movement.moving(): Boolean = Movement.destination() != Tile.Nil

    fun walk(position: Tile?, enableRun: Boolean = true, forceMinimap: Boolean = false): Boolean {
        if (position == null || position == Tile.Nil) {
            return true
        }
        val flags = Movement.collisionMap(position.floor()).flags()
        val position = if (!position.blocked(flags)) position else position.getWalkableNeighbor() ?: return false
        if (Players.local().tile() == position) {
            debug("Already on tile: $position")
            return true
        }
        if (enableRun && !Movement.running() && Movement.energyLevel() > nextRun) {
            Movement.running(true)
            nextRun = Random.nextInt(1, 5)
        }
        if (!Movement.moving() || walkDelay.isFinished()) {
            if (forceMinimap && position.onMap()
                && LocalPathFinder.findWalkablePath(Players.local().tile(), position).isNotEmpty()
            ) {
                Movement.step(position)
            } else {
                Movement.walkTo(position)
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
        selectItem: Int = -1,
        useMenu: Boolean = true
    ): Boolean {
        val t = target ?: return false
        val name = (t as Nameable).name()
        val pos = (t as Locatable).tile()
        val destination = Movement.destination()
        turnRunOn()
        debug("Interacting with: $name at: $pos")
        if (Menu.opened() && Menu.contains {
                it.action.equals(action, true) && (name == null || it.option.contains(
                    name,
                    true
                ))
            }) {
            debug("Clicking directly on opened menu")
            return handleMenu(action, name)
        }
        if (!t.inViewport()
            || (destination != pos && pos.distanceTo(if (destination == Tile.Nil) me else destination) > (if (alwaysWalk) 4 else 12))
        ) {
            if (allowWalk) {
                debug("Walking before interacting... in viewport: ${t.inViewport()}")
                if (pos.matrix().onMap()) {
                    Movement.step(pos)
                } else {
                    walk(pos)
                }
            }
        }
        val selectedId = Inventory.selectedItem().id()
        if (selectedId != selectItem) {
            Game.tab(Game.Tab.INVENTORY)
            if (selectItem > -1) {
                Inventory.stream().id(selectItem).firstOrNull()?.interact("Use")
            } else {
                Inventory.stream().id(selectedId).firstOrNull()?.click()
            }

        }
        val interactBool =
            if (name == null || name == "null" || name.isEmpty()) t.interact(action, useMenu) else t.interact(
                action,
                name,
                useMenu
            )
        return waitFor(short()) {
            Inventory.selectedItemIndex() == -1 || Inventory.selectedItem().id() == selectItem
        } && interactBool
    }

    /**
     * Requires menu to be open
     */
    fun handleMenu(action: String, name: String?): Boolean {
        if (!Menu.opened()) {
            return false
        }
        if (!Menu.contains {
                it.action.equals(action, true) && (name == null || it.option.contains(
                    name,
                    true
                ))
            }) {
            debug("Closing menu in: handleMenu()")
            Menu.click { it.action == "Cancel" }
            waitFor { !Menu.opened() }
            return false
        }
        return Menu.click { it.action.contains(action, true) && (name == null || it.option.contains(name, true)) }
    }

    fun Locatable.distance(): Int =
        tile().distanceTo(Players.local()).toInt()

    fun Tile.distanceM(dest: Locatable): Int {
        return abs(dest.tile().x() - x()) + abs(dest.tile().y() - y())
    }

    fun Locatable.onMap(): Boolean = tile().matrix()?.onMap() == true

    fun Locatable.mapPoint(): org.powbot.api.Point = Game.tileToMap(tile())

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
        val cm = Movement.collisionMap(f).flags()

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
        val mos = Game.mapOffset()
        return Tile(x() - mos.x(), y() - mos.y(), floor())
    }

    fun Equipment.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
    fun Bank.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
    fun Inventory.containsOneOf(vararg ids: Int): Boolean = stream().anyMatch { it.id() in ids }
    fun Inventory.emptyExcept(vararg ids: Int): Boolean = stream().firstOrNull { it.id() !in ids } == null

    fun Inventory.emptySlots(): Int = (28 - stream().count()).toInt()
    fun Inventory.getCount(vararg ids: Int): Int = getCount(true, *ids)
    fun Inventory.getCount(countStacks: Boolean, vararg ids: Int): Int {
        val items = stream().id(*ids).list()
        return if (countStacks) items.sumOf { it.stack } else items.count()
    }

    fun Int.getItemDef() = CacheItemConfig.load(this)
    fun Item.getItemDef() = CacheItemConfig.load(id())

    fun Bank.withdrawExact(id: Number, amount: Int, wait: Boolean = true): Boolean {
        val id = id.toInt()
        if (id <= 0) {
            return false
        }
        debug("WithdrawExact: $id, $amount")
        val currentAmount = Inventory.getCount(true, id)
        if (currentAmount < amount) {
            if (!containsOneOf(id)) {
                debug("No: ${CacheItemConfig.load(id).name} with id=$id in bank")
                return false
            } else if (amount - currentAmount >= stream().id(id).count(true)) {
                withdraw(id, Bank.Amount.ALL)
            } else if (amount - currentAmount >= Inventory.emptySlots() && !id.getItemDef().stackable) {
                debug("Withdrawing all: $id, since there's just enough space")
                withdraw(id, Bank.Amount.ALL)
            } else if (!withdraw(id, amount - currentAmount)) {
                return false
            }
        } else if (currentAmount > amount) {
            deposit(id, Bank.Amount.ALL)
            if (wait) waitFor { !Inventory.containsOneOf(id) }
            return false
        }
        return if (wait) waitFor(5000) { Inventory.getCount(true, id) == amount } else true
    }

    /**
     * Only useful for mobile
     */
    fun closeOpenHUD(): Boolean {
        val tab = Game.tab()
        if (tab == Game.Tab.NONE) {
            return true
        }
        val c: Component = Widgets.widget(601).firstOrNull { (it?.textureId() ?: -1) in tab.textures } ?: return true
        return c.click()
    }

    fun currentHP(): Int = Skills.level(Constants.SKILLS_HITPOINTS)
    fun maxHP(): Int = Skills.realLevel(Constants.SKILLS_HITPOINTS)
    fun missingHP(): Int = maxHP() - currentHP()


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