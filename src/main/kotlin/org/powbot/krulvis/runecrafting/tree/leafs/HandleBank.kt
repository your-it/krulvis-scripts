package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.Notifications
import org.powbot.api.requirement.RunePowerRequirement
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.api.rt4.magic.RunePower
import org.powbot.api.rt4.magic.Staff
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.*
import org.powbot.mobile.script.ScriptManager
import kotlin.random.Random

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Handling Bank") {
    override fun execute() {
        val bankPouches = EssencePouch.inBank()
        if (script.usePouches && bankPouches.isNotEmpty()) {
            bankPouches.forEach { it.withdrawExact(1) }
        }
        val invPouches = EssencePouch.inInventory()
        if (script.altar == RuneAltar.ZMI) depositAllExcept(keep)//Immediately deposit runes
        if (invPouches.all { it.filled() } && Bank.depositAllExcept(*keep) && Inventory.isFull()) {
            script.logger.info("Closing bank...")
            Bank.close()
        } else if (Bank.stream().name(script.essence).count() <= 0) {
            Notifications.showNotification("Out of essence, stopping script")
            ScriptManager.stop()
        } else if (invPouches.any { !it.filled() }) {
            fillEssencePouch(invPouches)
        } else if (Bank.depositAllExcept(*keep)) {
            withdrawEssence()
        }
    }


    fun fillEssencePouch(pouches: List<EssencePouch>) {
        val fillTimer = Timer(5000)
        do {
            if (!Inventory.isFull() && withdrawEssence()) {
                waitFor { Inventory.isFull() }
            }
            pouches.forEach { it.fill() }
        } while (!fillTimer.isFinished() && pouches.any { !it.filled() })

        if (!Inventory.isFull() && withdrawEssence()) {
            waitFor { Inventory.isFull() }
        }
    }

    fun depositAllExcept(keep: Array<String>): Boolean {
        val interactDelay = Random.nextInt(60, 150)
        val depositables = findDepositables(keep)
        if (depositables.isEmpty()) return true
        if (depositables.size == Inventory.occupiedSlotCount()) return Bank.depositInventory()
        return depositables.all {
            sleep(interactDelay)
            it.interact("Deposit-All")
        } && waitFor { findDepositables(keep).isEmpty() }
    }

    fun findDepositables(keep: Array<String>): List<Item> {
        val lowercased = keep.map { it.lowercase() }
        return Inventory.get { it.name().lowercase() !in lowercased }.sortedBy { it.inventoryIndex }
    }

    val keep: Array<String> by lazy {
        val list = mutableListOf(
            "Rune pouch",
            "Small pouch",
            "Medium pouch",
            "Large pouch",
            "Giant pouch",
            "Colossal pouch",
            RUNE_ESSENCE,
            PURE_ESSENCE,
            DAEYALT_ESSENCE
        )
        val bankTeleport = script.bankTeleport
        val runes = bankTeleport?.runes()?.toMutableList() ?: mutableListOf()

        if (script.vileVigour) {
            runes.addAll(Magic.ArceuusSpell.VILE_VIGOUR.runes())
            runes.addAll(Magic.LunarSpell.SPELL_BOOK_SWAP.runes())
        }
        if (EssencePouch.inInventory().isNotEmpty()) {
            runes.addAll(Magic.LunarSpell.NPC_CONTACT.runes())
        }
        when (script.altar) {
            RuneAltar.ZMI -> list.add("${script.zmiPayment} rune")//Always keep rune to open bank with in inventory
            else -> {}
        }
        val staffPowers = Staff.equippedPowers()
        val pouchPowers = RunePouch.runes().flatMap { it.first.runePowers.toList() }
        list.addAll(runes.filterNot { it in staffPowers || it in pouchPowers }.map { "${Rune.getForPower(it)} rune" })
        script.logger.info("KEEP=${list.distinct().joinToString()}")
        list.distinct().toTypedArray()
    }

    private fun Magic.MagicSpell.runes(): List<RunePower> {
        return requirements.filterIsInstance<RunePowerRequirement>().map { it.power }
    }

    private fun withdrawEssence(): Boolean = Bank.withdraw(script.essence, Bank.Amount.ALL)
}