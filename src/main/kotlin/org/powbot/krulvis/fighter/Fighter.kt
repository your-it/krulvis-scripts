package org.powbot.krulvis.fighter

import org.powbot.api.Tile
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.selectors.NpcOption
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.tree.branch.ShouldBank
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(
    name = "krul Fighter",
    description = "Fights anything, anywhere",
    author = "Krulvis",
    version = "1.0.1",
    category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "food", "Choose your food", defaultValue = "TUNA",
            allowedValues = ["TROUT", "SALMON", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "MONKFISH", "SHARK", "KARAMBWAN"]
        ),
        ScriptConfiguration(
            "monsters",
            "Click the NPC's you want to kill",
            optionType = OptionType.NPCS,
            defaultValue = "[{\"id\":4821,\"interaction\":\"Attack\",\"level\":111,\"name\":\"Blue dragon\"}]"
        ),
        ScriptConfiguration(
            "radius", "Kill radius", optionType = OptionType.INTEGER, defaultValue = "5"
        ),
        ScriptConfiguration(
            "bank", "Choose bank", optionType = OptionType.STRING, defaultValue = "FALADOR_WEST_BANK",
            allowedValues = ["LUMBRIDGE_TOP", "FALADOR_WEST_BANK", "FALADOR_EAST_BANK", "LUMBRIDGE_CASTLE_BANK", "VARROCK_WEST_BANK", "VARROCK_EAST_BANK", "CASTLE_WARS_BANK", "EDGEVILLE_BANK", "DRAYNOR_BANK", "SEERS_BANK", "AL_KHARID_BANK", "SHANTAY_PASS_BANK", "CANIFIS_BANK", "CATHERBY_BANK", "YANILLE_BANK", "ARDOUGNE_NORTH_BANK", "ARDOUGNE_SOUTH_BANK", "MISCELLANIA_BANK", "GNOME_STRONGHOLD_BANK", "TZHAAR_BANK", "FISHING_GUILD_BANK", "BURTHORPE_BANK", "PORT_SARIM_DB", "MOTHERLOAD_MINE", "MINING_GUILD", "MOTHERLOAD_MINE_DEPOSIT", "FARMING_GUILD_85", "FARMING_GUILD_65"]
        )
    ]
)
class Fighter : ATScript() {
    override fun createPainter(): ATPaint<*> = FighterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    var safespot = Tile(2904, 9808, 0)
    val food by lazy { Food.valueOf(getOption<String>("food")!!) }
    val monsters by lazy {
        getOption<List<NpcOption>>("monsters")!!.map { it.name }
    }
    val radius by lazy { getOption<Int>("radius")!! }
    val bank by lazy { BankLocation.valueOf(getOption<String>("bank")!!) }

    fun canEat() = ATContext.missingHP() > food.healing
    fun needFood(): Boolean = ATContext.currentHP().toDouble() / ATContext.maxHP() < .4

    fun loot(): List<GroundItem> =
        GroundItems.stream().filtered { 1 * it.stackSize() >= 500 }.list()
//        GroundItems.stream().filtered { ItemPriceCache[it.id()] * it.stackSize() >= 500 }.list()

}

class FighterPainter(script: Fighter) : ATPaint<Fighter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder
            .trackSkill(Skill.Attack)
            .trackSkill(Skill.Strength)
            .trackSkill(Skill.Defence)
            .trackSkill(Skill.Hitpoints)
            .trackSkill(Skill.Prayer)
            .trackSkill(Skill.Magic)
            .trackSkill(Skill.Ranged)

        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
    }
}

fun main() {
    Fighter().startScript("127.0.0.1", "krullieman", false)
}