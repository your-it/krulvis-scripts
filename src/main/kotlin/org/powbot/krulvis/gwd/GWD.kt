package org.powbot.krulvis.gwd

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Widgets

object GWD {

    val SARADOMIN_ENCAMPMENT = Area(Tile(2882, 5280), Tile(2931, 5254))
    val ANCIENT_PRISON = Area(Tile(2847, 5232), Tile(2944, 5186))
    val GOD_WARS_AREA = Area(Tile(0, 0), Tile(0, 0))

    const val KC_ROOT = 406

    fun nearAltar(): Boolean {
        val altar = Objects.stream().nameContains("Altar").first()
        return altar.valid()
    }

    enum class God(
        val area: Area,
        val doorTile: Tile,
        val kcLocation: Tile,
        val generalName: String,
        val mageName: String,
        val rangeName: String,
        val meleeName: String,
        vararg val kcNames: String
    ) {
        Saradomin(
            Area(Tile(2887, 5276, 0), Tile(2908, 5257, 0)),
            Tile(2909, 5265, 0),
            Tile(2910, 5262, 0),
            "Commander Zilyana",
            "Growler",
            "Bree",
            "Starlight",
            "Spiritual warrior", "Spiritual mage", "Knight of Saradomin", "Saradomin priest", "Spiritual ranger"
        ),
        Zamorak(
            Area(Tile(2917, 5332, 2), Tile(2937, 5316, 2)),
            Tile(0, 0, 0),
            Tile(0, 0, 0),
            "K'ril Tsutsaroth",
            "Balfrug Kreeyath",
            "Zakl'n Gritch",
            "Tstanon Karlak",
            "Imp"
        ),
        Bandos(
            Area(Tile(2862, 5370, 2), Tile(2877, 5349, 0)),
            Tile(0, 0, 0),
            Tile(0, 0, 0),
            "General Graardor",
            "Sergeant Steelwill",
            "Sergeant Grimspike",
            "Sergeant Strongstack",
            "Goblin"
        ),
        Armadyl(
            Area(Tile(2823, 5309, 2), Tile(2843, 5295, 2)),
            Tile(0, 0, 0), Tile(0, 0, 0),
            "Kree'arra",
            "Wingman Skree",
            "Flockleader Geerin",
            "Flight Kilisa"
        ),
        ;

        fun getGeneral() = Npcs.stream().name(generalName).first()
        fun getMage() = Npcs.stream().name(mageName).first()
        fun getRange() = Npcs.stream().name(rangeName).first()
        fun getMelee() = Npcs.stream().name(meleeName).first()

        fun killCount(): Boolean {
            val widget = Widgets.widget(KC_ROOT)
            if (!widget.valid()) return false
            val comps = widget.components()
            val index = comps.firstOrNull { it.text() == name }?.index() ?: -1
            return !comps[index + 5].text().contains("<col=ff4747>")
        }
    }

    val lootNames = arrayOf(
        "saradomin sword",
        "saradomin's light",
        "armadyl crossbow",
        "saradomin hilt",
        "godsword shard",
        "adamant platebody",
        "rune kiteshield",
        "rune plateskirt",
        "prayer potion",
        "super restore",
        "saradomin brew",
        "coins",
        "ranarr seed",
        "grimy ranarr weed",
        "magic seed",
        "law rune",
        "dragon med helm",
        "dragon spear",
        "clue scroll",
        "rune dart"
    )
}