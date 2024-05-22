package org.powbot.krulvis.mta

import org.powbot.api.rt4.*

object AlchemyRoom {
    const val WIDGET_ID = 194
    const val ITEM_VALUE_START_ID = 12

    var bestItemName = ""
    var EMPTY_CUPBOARD = 23679

    fun getCupboard(): GameObject {
        return Objects.stream().name("Cupboard").action("Take-5")
            .filtered {
                it.id != EMPTY_CUPBOARD && GroundItems.stream().within(it.tile, 2).name(*Alchable.names.toTypedArray())
                    .count() < 20
            }.nearest().first()
    }

    fun getSortedCupboards(): List<GameObject> {
        val grouped = Objects.stream().type(GameObject.Type.INTERACTIVE)
            .name("Cupboard").action("Take-5")
            .groupBy { it.tile.x }
        val xs = grouped.keys.sorted()
        val entries = grouped[xs.first()]?.sortedBy { it.tile.y } ?: emptyList()
        return entries + (grouped[xs.last()]?.sortedByDescending { it.tile.y } ?: emptyList())
    }

    fun getItemsWorth(): List<Pair<Alchable, Int>> {
        val comps = Components.stream(WIDGET_ID).toList()
        val worth = comps.subList(ITEM_VALUE_START_ID, ITEM_VALUE_START_ID + 5).map { it.text().toInt() }
        return Alchable.values().zip(worth)
    }

    fun getBestItem(): Alchable = getItemsWorth().maxBy { it.second }.first

    fun getDroppables(): List<Item> =
        Inventory.stream().name(*(Alchable.names - bestItemName).toTypedArray()).toList()

    enum class Alchable(val itemName: String) {
        LEATHER_BOOTS("Leather boots"),
        ADAMANT_KITESHIELD("Adamant kiteshield"),
        ADAMANT_HELM("Adamant med helm"),
        EMERALD("Emerald"),
        RUNE_LONGSWORD("Rune longsword"),
        NONE("None")
        ;

        companion object {
            val names = values().map { it.itemName }
        }
    }

    enum class Pattern(val alchables: List<Alchable>) {
        PATTERN1(
            listOf(
                Alchable.RUNE_LONGSWORD,
                Alchable.NONE,
                Alchable.LEATHER_BOOTS,
                Alchable.EMERALD,
                Alchable.ADAMANT_HELM,
                Alchable.ADAMANT_KITESHIELD
            )
        ),
        PATTERN2(
            listOf(
                Alchable.EMERALD,
                Alchable.RUNE_LONGSWORD,
                Alchable.NONE,
                Alchable.ADAMANT_HELM,
                Alchable.ADAMANT_KITESHIELD,
                Alchable.LEATHER_BOOTS,
            )
        ),
        PATTERN3(
            listOf(
                Alchable.NONE,
                Alchable.LEATHER_BOOTS,
                Alchable.ADAMANT_KITESHIELD,
                Alchable.RUNE_LONGSWORD,
                Alchable.EMERALD,
                Alchable.ADAMANT_HELM,
            )
        ),
        PATTERN4(
            listOf(
                Alchable.ADAMANT_HELM,
                Alchable.EMERALD,
                Alchable.RUNE_LONGSWORD,
                Alchable.ADAMANT_KITESHIELD,
                Alchable.LEATHER_BOOTS,
                Alchable.NONE,
            )
        ),
        PATTERN5(
            listOf(
                Alchable.LEATHER_BOOTS,
                Alchable.ADAMANT_KITESHIELD,
                Alchable.ADAMANT_HELM,
                Alchable.NONE,
                Alchable.RUNE_LONGSWORD,
                Alchable.EMERALD,
            )
        ),
        PATTERN6(
            listOf(
                Alchable.ADAMANT_KITESHIELD,
                Alchable.ADAMANT_HELM,
                Alchable.EMERALD,
                Alchable.LEATHER_BOOTS,
                Alchable.NONE,
                Alchable.RUNE_LONGSWORD,
            )
        ),
    }

}