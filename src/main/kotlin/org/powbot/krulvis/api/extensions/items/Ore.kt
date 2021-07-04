package org.powbot.krulvis.api.extensions.items

import org.powbot.krulvis.api.ATContext.ctx
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.util.*

enum class Ore(override val ids: IntArray, val miningLvl: Int, vararg val colors: Int) : Item {
    AMETHYST(intArrayOf(21347), 92, 6705),
    PAY_DIRT(intArrayOf(12011), 45, 6705),
    RUNITE(intArrayOf(451), 85, -31437),
    ADAMANTITE(intArrayOf(449), 70, 21662),
    MITHRIL(intArrayOf(447), 55, -22239),
    GOLD(intArrayOf(444), 40, 8885),
    COAL(intArrayOf(453), 30, 10508),
    SILVER(intArrayOf(442), 20, 74),
    IRON(intArrayOf(440), 15, 2576),
    COPPER(intArrayOf(436), 1, 4510, 4645, 8889),
    TIN(intArrayOf(438), 1, 53),
    CLAY(intArrayOf(434), 1, 6705);


    override fun hasWith(): Boolean {
        return getInventoryCount(false) > 0
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(false)
    }

    fun getNearestRock(): Optional<GameObject> {
        return ctx.objects.toStream().filter {
            it.hasOre(this)
        }.nearest().findFirst()
    }

    companion object {
        fun GameObject.hasOre(vararg ores: Ore = values()): Boolean {
            val name = name()
            return name == "Ore vein" || name == "Crystals" || ores.any { ore -> ore.colors.any { it.toShort() in modifiedColors() } }
        }

        fun GameObject.getOre(): Ore? {
            val name = name()
            //Amethyst crystals can always be mined, ore veins are static but can only be mined if called
            // "Ore vein"
            if (name.contains("vein")) {
                return PAY_DIRT
            } else if (name == "Crystals") {
                return AMETHYST
            }
            return values().firstOrNull { ore ->
                hasOre(ore)
            }
        }

        fun Tile.getOre(): Ore? {
            val rock = ctx.objects.toStream().at(this).name("Rock").findFirst()
            return if (rock.isPresent) {
                values().firstOrNull { ore ->
                    rock.get().hasOre(ore)
                }
            } else {
                null
            }
        }

        fun Int.getOre(): Ore? = values().firstOrNull { it.ids.contains(this) }

    }
}




