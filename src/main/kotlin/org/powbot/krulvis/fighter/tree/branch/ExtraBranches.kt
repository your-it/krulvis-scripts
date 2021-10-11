package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.rt4.Magic.component
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter


class ShouldEat(script: Fighter) : Branch<Fighter>(script, "Should eat?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Eating") {
        val count = script.food?.getInventoryCount() ?: -1
        if (script.food?.eat() == true) {
            nextEatExtra = Random.nextInt(1, 8)
            waitFor { script.food!!.getInventoryCount() < count }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldSip(script)

    var nextEatExtra = Random.nextInt(1, 8)

    override fun validate(): Boolean {
        return script.food?.inInventory() == true && (script.needFood() || script.canEat(nextEatExtra))
    }
}

class ShouldSip(script: Fighter) : Branch<Fighter>(script, "Should Sip Pot??") {

    var potion: Potion? = null
    var nextRestore = Random.nextInt(30, 60)

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Sip Potion") {
        val doses = potion!!.doses()
        if (potion!!.drink()) {
            nextRestore = Random.nextInt(30, 60)
            waitFor { potion!!.doses() < doses }
            sleep(1000)
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldEquipAmmo(script)

    override fun validate(): Boolean {
        potion = script.potions.firstOrNull { it.first.inInventory() && it.first.needsRestore(nextRestore) }?.first
        return potion != null
    }
}

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
        script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.equip()
        waitFor { script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.inInventory() != true }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script)

    override fun validate(): Boolean {
        val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
        return ammo != null && ammo.inInventory()
                && (Inventory.isFull()
                || (ammo.getInvItem()?.stack ?: -1) > 5
                || !ammo.inEquipment()
                )
    }
}

class ShouldHighAlch(script: Fighter) : Branch<Fighter>(script, "Should high alch?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "High alch") {
        if (!Magic.casting(spell)) {
            if (Magic.cast(spell)) {
                waitFor { Magic.casting(spell) }
            }
        }
        if (Magic.casting(spell)) {
            val count = Inventory.stream().id(alchable!!.id).count()
            alchable?.interact("Cast")
            waitFor { Inventory.stream().id(alchable!!.id).count() != count }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldBurryBones(script)

    val spell = Magic.Spell.HIGH_ALCHEMY
    var alchable: Item? = null

    fun alchable(): Item? {
        val lootIds = script.painter.paintBuilder.items
            .filter { row -> row.any { it is InventoryItemPaintItem } }
            .map { row -> (row.first { it is InventoryItemPaintItem } as InventoryItemPaintItem).itemId }
            .toIntArray()
        return Inventory.stream().id(*lootIds).firstOrNull {
            val value = it.value()
            value > 300 && it.value() / GrandExchange.getItemPrice(it.id).toDouble() > .9
        }
    }

    override fun validate(): Boolean {
        if (!script.highAlch) return false
        alchable = alchable()
        return alchable != null && Game.tab(Game.Tab.MAGIC) && component(spell).textureId() != spell.texture()
    }
}

class ShouldBurryBones(script: Fighter) : Branch<Fighter>(script, "Should Bury bones?") {

    var bones = emptyList<Item>()

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Bury bones") {
        bones.forEachIndexed { i, item ->
            val count = Inventory.getCount(item.id)
            if (item.interact("Bury")) {
                waitFor { count > Inventory.getCount(item.id) }
                if (i < bones.size - 1)
                    sleep(1500)
            }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldExitRoom(script)

    override fun validate(): Boolean {
        if (!script.buryBones) return false
        bones = Inventory.stream().filtered { it.name().contains("Bones") }.list()
        return bones.isNotEmpty()
    }
}