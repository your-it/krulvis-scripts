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
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.runecrafting.tree.branches.ShouldCastNPCContact
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
	name = "krul Runecrafter", version = "1.0.5",
	description = "Supports Abyss, Astral & ZMI/Ourania, Repairs pouches, restores energy",
	scriptId = "329bdd0e-3813-4c39-917b-d943e79a0f47",
	markdownFileName = "Runecrafter.md",
	category = ScriptCategory.Runecrafting
)
@ScriptConfiguration.List([
	ScriptConfiguration(name = METHOD_CONFIGURATION, description = "Which method of runecrafting?", optionType = OptionType.STRING, allowedValues = arrayOf(ABYSS, ALTAR), defaultValue = ABYSS),
	ScriptConfiguration(name = RUNE_ALTAR_CONFIGURATION, description = "Which altar to make runes at?", optionType = OptionType.STRING, allowedValues = arrayOf(COSMIC, NATURE, LAW, CHAOS, DEATH, BLOOD, SOUL), defaultValue = NATURE),
	ScriptConfiguration(name = VILE_VIGOUR_CONFIG, description = "Cast vile vigour?", optionType = OptionType.BOOLEAN, defaultValue = "false", visible = false),
	ScriptConfiguration(name = ZMI_PAYMENT_RUNE_CONFIG, description = "Payment rune?", optionType = OptionType.STRING, allowedValues = arrayOf(MIND, AIR, WATER, EARTH, FIRE), defaultValue = MIND, visible = false),
	ScriptConfiguration(name = ZMI_PROTECT_CONFIG, description = "Protection prayer?", optionType = OptionType.STRING, allowedValues = arrayOf("NONE", "PROTECT_FROM_MAGIC", "PROTECT_FROM_MISSILES", "PROTECT_FROM_MELEE"), defaultValue = "NONE", visible = false),
	ScriptConfiguration(name = USE_POUCHES_OPTION, description = "Use pouches?", optionType = OptionType.BOOLEAN, defaultValue = "true"),
	ScriptConfiguration(name = ESSENCE_TYPE_CONFIGURATION, description = "Which essence to use?", optionType = OptionType.STRING, allowedValues = arrayOf(RUNE_ESSENCE, PURE_ESSENCE, DAEYALT_ESSENCE), defaultValue = DAEYALT_ESSENCE),
	ScriptConfiguration(name = FOOD_CONFIGURATION, description = "Which food to use?", optionType = OptionType.STRING, allowedValues = arrayOf(SALMON, TUNA, LOBSTER, BASS, KARAMBWAN), defaultValue = BASS),
	ScriptConfiguration(name = EAT_AT_CONFIG, description = "Eat below HP", optionType = OptionType.INTEGER, defaultValue = "70")
])
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
	val bankTeleport by lazy { if (method == ABYSS) Magic.Spell.TELEPORT_TO_HOUSE else altar.bankTeleport }
	override fun createPainter(): ATPaint<*> = RCPainter(this)

	fun getBank(): InteractableEntity {
		val eniola = Npcs.stream().name("Eniola").first()
		if (eniola.valid()) return eniola
		return Objects.stream().type(GameObject.Type.INTERACTIVE).name("Bank booth").action("Bank").nearest().first()
	}


	var chaosAltar = GameObject.Nil
	fun findChaosAltar() = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Chaos altar").action("Pray-at").first()

	override val rootComponent: TreeComponent<*> = object : Branch<Runecrafter>(this, "Should logout?") {
		override val failedComponent: TreeComponent<Runecrafter> = ShouldCastNPCContact(script)//Pouch repair branches
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

	@ValueChanged(METHOD_CONFIGURATION)
	fun onMethodChange(method: String) {
		val altars = if (method == ABYSS) {
			arrayOf(COSMIC, NATURE, LAW, CHAOS, DEATH, BLOOD, SOUL)
		} else {
//            arrayOf(ZMI, COSMIC, NATURE, LAW, CHAOS, ASTRAL, DEATH, BLOOD, SOUL)
			arrayOf(ZMI, ASTRAL)
		}
		updateAllowedOptions(RUNE_ALTAR_CONFIGURATION, altars)
	}

	@ValueChanged(RUNE_ALTAR_CONFIGURATION)
	fun onAltarChange(altar: String) {
		updateVisibility(VILE_VIGOUR_CONFIG, altar == ZMI)
		updateVisibility(ZMI_PAYMENT_RUNE_CONFIG, altar == ZMI)
		updateVisibility(ZMI_PROTECT_CONFIG, altar == ZMI)
		if (altar != ZMI) {
			updateOption(VILE_VIGOUR_CONFIG, false, OptionType.BOOLEAN)
			updateOption(ZMI_PROTECT_CONFIG, "NONE", OptionType.STRING)
		}
	}
}

fun main() {
	Runecrafter().startScript("127.0.0.1", "GIM", false)
}
