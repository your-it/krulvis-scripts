package org.powbot.krulvis.api.extensions

import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.distanceM
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.interactions.Interaction
import org.powbot.krulvis.api.utils.interactions.NpcInteraction
import org.powbot.krulvis.api.utils.interactions.ObjectInteraction
import org.powbot.krulvis.api.utils.requirements.Requirement
import org.powbot.krulvis.walking.PBWebWalkingService
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Bank
import org.powerbot.script.rt4.ClientContext


enum class BankType {
    BOOTH, CHEST, NPC, DEPOSIT_BOX
}


enum class BankLocation(
    val tile: Tile,
    val type: BankType,
    private val interaction: Interaction<*> = ObjectInteraction("Bank booth", "Bank"),
    private vararg val requirements: Requirement
) {
    LUMBRIDGE_TOP(Tile(3208, 3221, 2), BankType.BOOTH),
    FALADOR_WEST_BANK(Tile(2947, 3368, 0), BankType.BOOTH),
    FALADOR_EAST_BANK(Tile(3013, 3355, 0), BankType.BOOTH),
    LUMBRIDGE_CASTLE_BANK(Tile(3208, 3220, 2), BankType.BOOTH),
    VARROCK_WEST_BANK(Tile(3185, 3436, 0), BankType.BOOTH),
    VARROCK_EAST_BANK(Tile(3253, 3420, 0), BankType.BOOTH),
    CASTLE_WARS_BANK(
        Tile(2443, 3083, 0), BankType.CHEST,
        ObjectInteraction("Bank chest", "Use", Tile(2444, 3083, 0)),
    ),
    EDGEVILLE_BANK(Tile(3094, 3491, 0), BankType.BOOTH),
    DRAYNOR_BANK(Tile(3092, 3245, 0), BankType.BOOTH, ObjectInteraction("Bank booth", "Bank")),
    SEERS_BANK(
        Tile(2727, 3493, 0), BankType.BOOTH,
        ObjectInteraction("Bank booth", "Bank", Tile(2727, 3494, 0)),
    ),
    AL_KHARID_BANK(Tile(3269, 3167, 0), BankType.BOOTH),
    SHANTAY_PASS_BANK(
        Tile(3308, 3120, 0), BankType.CHEST,
        ObjectInteraction("Bank chest", "Use", Tile(3309, 3120, 0)),
    ),
    CANIFIS_BANK(
        Tile(3512, 3480, 0), BankType.BOOTH,
        ObjectInteraction("Bank booth", "Bank", Tile(3513, 3480, 0)),
    ),
    CATHERBY_BANK(Tile(2809, 3441, 0), BankType.BOOTH),
    YANILLE_BANK(Tile(2613, 3094, 0), BankType.BOOTH),
    ARDOUGNE_NORTH_BANK(Tile(2615, 3332), BankType.BOOTH),
    ARDOUGNE_SOUTH_BANK(Tile(2655, 3283, 0), BankType.BOOTH),
    MISCELLANIA_BANK(
        Tile(2618, 3895, 0), BankType.NPC,
        NpcInteraction("Banker", "Bank")
    ),
    GNOME_STRONGHOLD_BANK(Tile(2445, 3424, 1), BankType.BOOTH),
    TZHAAR_BANK(
        Tile(2446, 5178, 0), BankType.NPC,
        NpcInteraction("TzHaar-Ket-Zuh", "Bank")
    ),
    FISHING_GUILD_BANK(
        Tile(2586, 3419, 0), BankType.BOOTH,
        ObjectInteraction("Bank booth", "Bank", Tile(2585, 3419, 0)),
    ),
    BURTHORPE_BANK(
        Tile(3047, 4974, 1), BankType.NPC,
        NpcInteraction("Emerald Benedict", "Bank")
    ),
    PORT_SARIM_DB(
        Tile(3045, 3235, 0), BankType.DEPOSIT_BOX,
        ObjectInteraction("Bank deposit box", "Deposit", Tile(3045, 3234, 0))
    ),
    MOTHERLOAD_MINE(
        Tile(3760, 5666, 0), BankType.CHEST,
        ObjectInteraction("Bank chest", "Use", Tile(3761, 5666, 0)),
    )
    ;

    /**
     * Opens the bank at the location
     */
    fun open(): Boolean {
        val ctx = ClientContext.ctx()
        if (ctx.bank.opened()) {
            return true
        } else if (ctx.collectionBox.opened()) {
            ctx.collectionBox.close()
        } else if (tile.distanceM(me) >= 20) {
            debug("Using web to walk to bank")
            PBWebWalkingService.move(tile, false, {
                val b = interaction.getEntity()
                b.isPresent && b.get().inViewport()
            }, 1, 100)
            return false
        }
        return interaction.execute() && waitFor { ctx.bank.opened() }
    }

    /**
     *
     * @return true if all the requirements are met to access this bank
     */
    fun canUse(): Boolean {
        return requirements.all { it.hasRequirement() }
    }

    companion object {

        /**
         * @return the nearest bank according to the geographical distance
         */
        fun Bank.getNearestBank(includeDepositBox: Boolean = false): BankLocation {
            val me = ClientContext.ctx().players.local()
            return values()
                .filter { (includeDepositBox || it.type != BankType.DEPOSIT_BOX) && it.canUse() }
                .minByOrNull { it.tile.distanceM(me) }!!
        }

        fun Bank.openNearestBank(): Boolean {
            return getNearestBank().open()
        }
    }
}