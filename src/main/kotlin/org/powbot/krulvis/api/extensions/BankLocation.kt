package org.powbot.krulvis.api.extensions

import org.powbot.api.Filter
import org.powbot.api.MenuCommand
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.WebWalkingResult
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.rt4.walking.local.Utils.getWalkableNeighbor
import org.powbot.api.rt4.walking.model.GameObjectInteraction
import org.powbot.api.rt4.walking.model.NamedEntityInteraction
import org.powbot.api.rt4.walking.model.NpcInteraction
import org.powbot.api.rt4.walking.toRegularTile
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.distanceM
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.requirements.Requirement
import org.powbot.mobile.script.ScriptManager
import org.powbot.mobile.service.WebWalkingService


enum class BankType {
    BOOTH, CHEST, NPC, DEPOSIT_BOX
}


enum class BankLocation(
    val tile: Tile,
    val type: BankType,
    private vararg val requirements: Requirement
) {
    ARDOUGNE_NORTH_BANK(Tile(2615, 3332, 0), BankType.BOOTH),
    ARDOUGNE_SOUTH_BANK(Tile(2655, 3283, 0), BankType.BOOTH),
    AL_KHARID_BANK(Tile(3269, 3167, 0), BankType.BOOTH),
    BURTHORPE_BANK(
        Tile(3047, 4974, 1), BankType.NPC
    ),
    CANIFIS_BANK(
        Tile(3512, 3480, 0), BankType.BOOTH
    ),
    CATHERBY_BANK(Tile(2809, 3441, 0), BankType.BOOTH),
    CASTLE_WARS_BANK(
        Tile(2443, 3083, 0), BankType.CHEST
    ),
    DRAYNOR_BANK(Tile(3092, 3245, 0), BankType.BOOTH),
    EDGEVILLE_BANK(Tile(3094, 3491, 0), BankType.BOOTH),
    FALADOR_WEST_BANK(Tile(2947, 3368, 0), BankType.BOOTH),
    FALADOR_EAST_BANK(Tile(3013, 3355, 0), BankType.BOOTH),
    FARMING_GUILD_85(
        Tile(1248, 3758, 0), BankType.BOOTH,
    ),
    FARMING_GUILD_65(
        Tile(1253, 3741, 0), BankType.CHEST,
    ),
    GRAND_EXCHANGE(
        Tile(3167, 3489, 0), BankType.NPC,
    ),
    HOSIDIUS_BEST_BANK_SPOT(
        Tile(1676, 3615, 0), BankType.CHEST,
    ),
    MISCELLANIA_BANK(
        Tile(2618, 3895, 0), BankType.NPC,
    ),
    LUMBRIDGE_TOP(Tile(3208, 3221, 2), BankType.BOOTH),
    LUMBRIDGE_CASTLE_BANK(Tile(3208, 3220, 2), BankType.BOOTH),
    VARROCK_WEST_BANK(Tile(3185, 3436, 0), BankType.BOOTH),
    VARROCK_EAST_BANK(Tile(3253, 3420, 0), BankType.BOOTH),
    GNOME_STRONGHOLD_BANK(Tile(2445, 3424, 1), BankType.BOOTH),
    FISHING_GUILD_BANK(
        Tile(2586, 3419, 0), BankType.BOOTH,
    ),
    MINING_GUILD(
        Tile(3013, 9718, 0), BankType.CHEST,
    ),
    MOTHERLOAD_MINE(
        Tile(3760, 5666, 0), BankType.CHEST,
    ),
    MOTHERLOAD_MINE_DEPOSIT(
        Tile(3758, 5664, 0), BankType.DEPOSIT_BOX,
    ),
    PRIFIDDINAS(
        Tile(3256, 6108, 0), BankType.BOOTH,
    ),
    PORT_SARIM_DB(
        Tile(3045, 3235, 0), BankType.DEPOSIT_BOX,
    ),
    SEERS_BANK(
        Tile(2727, 3493, 0), BankType.BOOTH,
    ),
    SHANTAY_PASS_BANK(
        Tile(3308, 3120, 0), BankType.CHEST,
    ),
    TZHAAR_BANK(
        Tile(2446, 5178, 0), BankType.NPC,
    ),
    WOODCUTTING_GUILD(
        Tile(1592, 3476, 0), BankType.CHEST,
    ),
    WARRIORS_GUILD(
        Tile(2843, 3543, 0), BankType.BOOTH,
        requirements = arrayOf(object : Requirement {
            override fun hasRequirement(): Boolean {
                return Skills.realLevel(Constants.SKILLS_STRENGTH) + Skills.realLevel(Constants.SKILLS_ATTACK) >= 130
            }
        })
    ),
    WINTERTODT(
        Tile(1640, 3944, 0), BankType.CHEST,
    ),
    YANILLE_BANK(Tile(2613, 3094, 0), BankType.BOOTH),
    ;

    /**
     * Opens the bank at the location
     */
    fun open(): Boolean {
        val log = ScriptManager.script()?.log ?: return false
        log.info("Banking at: $this")
        if (Bank.opened()) {
            return true
        } else if (CollectionBox.opened()) {
            CollectionBox.close()
        } else if (me.floor() != tile.floor || tile.distance() >= 14) {
            WebWalking.moveTo(tile, false, { false }, 1, 100, false)
            return false
        }
        return Bank.open()
    }

    /**
     *
     * @return true if all the requirements are met to access this bank
     */
    fun canUse(): Boolean {
        return requirements.all { it.hasRequirement() }
    }

    override fun toString(): String = "$name, tile=$tile, type=$type"

    companion object {

        private fun getAllowedBanks(): List<BankLocation> =
            values().filter { it.requirements.isEmpty() || it.requirements.all { req -> req.hasRequirement() } }

        /**
         * @return the nearest bank according to the geographical distance
         */
        fun Bank.getNearestBank(includeDepositBox: Boolean = false, enableTeleports: Boolean = true): BankLocation {
            val player = WebWalking.playerState.player(false)
            if (player != null) {
                try {
                    val path = WebWalkingService.getPathToNearestBank(
                        player, enableTeleports
                    ).filterNotNull()
                    if (path.isNotEmpty()) {
                        val to = path.last().to.toRegularTile()
                        return getAllowedBanks()
                            .filter { (includeDepositBox || it.type != BankType.DEPOSIT_BOX) && it.canUse() }
                            .minByOrNull { it.tile.distanceM(to) }!!
                    }
                } catch (e: Exception) {
                    ScriptManager.script()?.log?.severe(e.stackTraceToString())
                }
            }
            return getNearestBankWithoutWeb(includeDepositBox)
        }

        fun Bank.getNearestBankWithoutWeb(includeDepositBox: Boolean = false): BankLocation {
            val me = Players.local()
            return getAllowedBanks()
                .filter { (includeDepositBox || it.type != BankType.DEPOSIT_BOX) && it.canUse() }
                .minByOrNull { it.tile.distanceM(me) }!!
        }

        fun Bank.openNearestBank(includeDepositBox: Boolean = false): Boolean {
            if (opened()) {
                return true
            }
            val nearest = nearest()
            if (nearest.distance() > 30 || !nearest.reachable()) {
                ScriptManager.script()?.log?.info("nearest=$nearest, distance=${nearest.distance()} is not reachable!")
                val localPath = LocalPathFinder.findPath(nearest.getWalkableNeighbor())
                if (localPath.isNotEmpty()) {
                    localPath.traverseUntilReached()
                } else {
                    try {
                        Movement.moveToBank()
                    } catch (e: Exception) {
                        ScriptManager.script()?.log?.info("Failed to move to bank!")
                        ScriptManager.script()?.log?.info(e.stackTraceToString())
                    }
                }
            }
            return open()
        }
    }


}

fun main() {
    println(
        "[\"${
            BankLocation.values().map {
                it.name
            }.joinToString("\", \"")
        }\"]"
    )
}