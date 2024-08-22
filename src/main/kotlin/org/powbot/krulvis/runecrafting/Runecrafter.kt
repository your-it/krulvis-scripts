package org.powbot.krulvis.runecrafting

import org.powbot.api.InteractableEntity
import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.*
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.ItemRequirement
import org.powbot.krulvis.api.extensions.teleports.EDGEVILLE_GLORY
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FAIRY_RING_DLS
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.runecrafting.tree.branches.ShouldRepairPouches
import org.powbot.mobile.script.ScriptManager

//<editor-fold desc="ScriptManifest">
@ScriptManifest(
    name = "krul Runecrafter", version = "1.0.5",
    description = "Supports Abyss, Astral & ZMI/Ourania, Repairs pouches, restores energy",
    scriptId = "329bdd0e-3813-4c39-917b-d943e79a0f47",
    markdownFileName = "Runecrafter.md",
    category = ScriptCategory.Runecrafting
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = METHOD_CONFIGURATION,
            description = "Which method of runecrafting?",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(ABYSS, ALTAR),
            defaultValue = ABYSS
        ),
        ScriptConfiguration(
            name = RUNE_ALTAR_CONFIGURATION,
            description = "Which altar to make runes at?",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(COSMIC, NATURE, LAW, CHAOS, DEATH, BLOOD, SOUL),
            defaultValue = NATURE
        ),
        ScriptConfiguration(
            name = ALTAR_TELEPORT_CONFIG,
            description = "Teleport to Altar",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(FAIRY_RING_DLS),
            defaultValue = FAIRY_RING_DLS,
            visible = false
        ),
        ScriptConfiguration(
            name = BANK_TELEPORT_CONFIG,
            description = "Teleport to Bank",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(FAIRY_RING_DLS),
            defaultValue = FAIRY_RING_DLS,
            visible = true
        ),
        ScriptConfiguration(
            name = VILE_VIGOUR_CONFIG,
            description = "Cast vile vigour?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false",
            visible = false
        ),
        ScriptConfiguration(
            name = ZMI_PAYMENT_RUNE_CONFIG,
            description = "Payment rune?",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(MIND, AIR, WATER, EARTH, FIRE),
            defaultValue = MIND,
            visible = false
        ),
        ScriptConfiguration(
            name = ZMI_PROTECT_CONFIG,
            description = "Protection prayer?",
            optionType = OptionType.STRING,
            allowedValues = arrayOf("NONE", "PROTECT_FROM_MAGIC", "PROTECT_FROM_MISSILES", "PROTECT_FROM_MELEE"),
            defaultValue = "NONE",
            visible = false
        ),
        ScriptConfiguration(
            name = USE_POUCHES_OPTION,
            description = "Use pouches?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = ESSENCE_TYPE_CONFIGURATION,
            description = "Which essence to use?",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(RUNE_ESSENCE, PURE_ESSENCE, DAEYALT_ESSENCE),
            defaultValue = DAEYALT_ESSENCE
        ),
        ScriptConfiguration(
            name = FOOD_CONFIGURATION,
            description = "Which food to use?",
            optionType = OptionType.STRING,
            allowedValues = arrayOf(SALMON, TUNA, LOBSTER, BASS, KARAMBWAN),
            defaultValue = BASS
        ),
        ScriptConfiguration(
            name = EAT_AT_CONFIG,
            description = "Eat below HP (actual HP, not %)",
            optionType = OptionType.INTEGER,
            defaultValue = "70"
        ),
        ScriptConfiguration(EQUIPMENT_CONFIG, "What to wear?", OptionType.EQUIPMENT),
    ]
)
//</editor-fold>
class Runecrafter : ATScript() {

    val prayer by lazy { Prayer.Effect.values().firstOrNull { it.name == getOption<String>(ZMI_PROTECT_CONFIG) } }
    val altar by lazy { RuneAltar.valueOf(getOption(RUNE_ALTAR_CONFIGURATION)) }
    val method by lazy { getOption<String>(METHOD_CONFIGURATION) }
    val eatAt by lazy { getOption<Int>(EAT_AT_CONFIG) }
    val essence by lazy { getOption<String>(ESSENCE_TYPE_CONFIGURATION) }
    val food by lazy { Food.valueOf(getOption(FOOD_CONFIGURATION)) }
    val zmiPayment by lazy { Rune.valueOf(getOption(ZMI_PAYMENT_RUNE_CONFIG)) }
    val vileVigour by lazy { getOption<Boolean>(VILE_VIGOUR_CONFIG) }
    val usePouches by lazy { getOption<Boolean>(USE_POUCHES_OPTION) }
    val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_CONFIG))) }
    val equipment by lazy { EquipmentRequirement.forEquipmentOption(getOption(EQUIPMENT_CONFIG)) }
    val altarTeleport by lazy { TeleportMethod(Teleport.forName(getOption(ALTAR_TELEPORT_CONFIG))) }
    var spellBook: Magic.Book? = null
    val teleportItemRequirements by lazy {
        val bankTeleport = bankTeleport.teleport?.requirements ?: emptyList()
        val altarTeleport = altarTeleport.teleport?.requirements ?: emptyList()
        bankTeleport.filterIsInstance<ItemRequirement>() + altarTeleport.filterIsInstance<ItemRequirement>()
    }

    override fun createPainter(): ATPaint<*> = RCPainter(this)

    fun getBank(): InteractableEntity {
        val eniola = Npcs.stream().name("Eniola").first()
        if (eniola.valid()) return eniola
        return Objects.stream(25, GameObject.Type.INTERACTIVE).name("Bank booth", "Bank chest").action("Use", "Bank")
            .nearest().first()
    }


    var chaosAltar = GameObject.Nil
    fun findChaosAltar() =
        Objects.stream().type(GameObject.Type.INTERACTIVE).name("Chaos altar").action("Pray-at").first()

    override val rootComponent: TreeComponent<*> = object : Branch<Runecrafter>(this, "Should logout?") {
        override val failedComponent: TreeComponent<Runecrafter> = ShouldRepairPouches(script)//Pouch repair branches
        override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Logging out cuz death") {
            if (Game.logout()) {
                ScriptManager.stop()
            }
        }

        var died = false
        override fun validate(): Boolean {
            if (ATContext.currentHP() == 0) {
                died = true
            }
            return died
        }
    }

    fun getSpellBookAltar() =
        Objects.stream(20, GameObject.Type.INTERACTIVE).name("Lunar altar", "Altar of the Occult").first()

    @ValueChanged(METHOD_CONFIGURATION)
    fun onMethodChange(method: String) {
        val altars = if (method == ABYSS) {
            arrayOf(COSMIC, NATURE, LAW, CHAOS, DEATH, BLOOD, SOUL)
        } else {
            arrayOf(ZMI, ASTRAL, BLOOD)
        }
        updateAllowedOptions(RUNE_ALTAR_CONFIGURATION, altars)
        updateTeleports(method, getOption(RUNE_ALTAR_CONFIGURATION))
    }

    @ValueChanged(RUNE_ALTAR_CONFIGURATION)
    fun onAltarChange(altar: String) {
        if (getOption<String>(METHOD_CONFIGURATION) == ABYSS) return

        updateTeleports(getOption(METHOD_CONFIGURATION), altar)
        if (altar != ZMI) {
            updateOption(VILE_VIGOUR_CONFIG, false, OptionType.BOOLEAN)
            updateOption(ZMI_PROTECT_CONFIG, "NONE", OptionType.STRING)
        }
    }

    private fun updateTeleports(method: String, altar: String) {
        val runeAltar = RuneAltar.valueOf(altar)
        var bankTeleports = runeAltar.bankTeleports
        if (method == ABYSS) {
            updateVisibility(ALTAR_TELEPORT_CONFIG, false)
            updateOption(ALTAR_TELEPORT_CONFIG, "NONE", OptionType.STRING)
            bankTeleports = arrayOf(EDGEVILLE_MOUNTED_GLORY, EDGEVILLE_GLORY)
        } else if (method == ALTAR) {
            val altarTeleports = runeAltar.altarTeleports
            updateVisibility(ALTAR_TELEPORT_CONFIG, altarTeleports.isNotEmpty())
            updateAllowedOptions(ALTAR_TELEPORT_CONFIG, altarTeleports)
        }
        updateAllowedOptions(BANK_TELEPORT_CONFIG, bankTeleports)
    }
}

fun main() {
    Runecrafter().startScript("127.0.0.1", "GIM", false)
}
