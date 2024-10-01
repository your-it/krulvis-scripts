package org.powbot.krulvis.mixology

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.MessageType
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.stripTags
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.mixology.Data.ALCO_AUGMENTOR
import org.powbot.krulvis.mixology.Data.ANTI_LEECH_LOTION
import org.powbot.krulvis.mixology.Data.AQUALUX_AMALGAM
import org.powbot.krulvis.mixology.Data.AZURE_AURA_MIX
import org.powbot.krulvis.mixology.Data.FALLBACK_OPTION
import org.powbot.krulvis.mixology.Data.LIPLACK_LIQUOR
import org.powbot.krulvis.mixology.Data.MAMMOTH_MIGHT_MIX
import org.powbot.krulvis.mixology.Data.MARLEYS_MOONLIGHT
import org.powbot.krulvis.mixology.Data.MEGALITE_LIQUID
import org.powbot.krulvis.mixology.Data.MIXALOT
import org.powbot.krulvis.mixology.Data.MYSTIC_MANA_AMALGAM
import org.powbot.krulvis.mixology.tree.branches.AtMixologyLab

@ScriptManifest(
	"krul Mixology",
	"Completes Mixology Minigame. Protip: Use krul Combiner to make the paste",
	"Krulvis",
	"1.0.5",
	category = ScriptCategory.Herblore,
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			FALLBACK_OPTION,
			"Which potion to make when order list is impossible?",
			OptionType.STRING,
			defaultValue = MIXALOT,
			allowedValues = [MAMMOTH_MIGHT_MIX, MYSTIC_MANA_AMALGAM, MARLEYS_MOONLIGHT, AZURE_AURA_MIX, ALCO_AUGMENTOR, AQUALUX_AMALGAM, MEGALITE_LIQUID, ANTI_LEECH_LOTION, LIPLACK_LIQUOR]
		)
	]
)
class Mixology : KrulScript() {
	override fun createPainter(): ATPaint<*> = MixologyPainter(this)

	override val rootComponent: TreeComponent<*> = AtMixologyLab(this)
	val fallbackPot by lazy { Data.MixPotion.forName(getOption(FALLBACK_OPTION))!! }
	var mixToMake = Pair(Data.MixPotion.Mammoth_might_mix, Data.Modifier.Homogenise)
	var modifierNpc = Npc.Nil

	var totals = arrayOf(0, 0, 0)
	var gained = arrayOf(0, 0, 0)


	@Subscribe
	fun onMessage(e: MessageEvent) {
		if (e.messageType != MessageType.Game) {
			return
		}
		val m = e.message
		if (m.contains("You are rewarded")) {
			val numbers = m.stripTags().parseNumbers()
			gained.indices.forEach { i ->
				gained[i] += numbers[i]
			}
			totals.indices.forEach { i ->
				totals[i] = numbers[i + 3]
			}
		}
	}


	@Subscribe
	fun onTick(e: TickEvent) {
		val modifier = mixToMake.second
		modifierNpc = Npcs.stream().at(modifier.machineTile).last()
	}

	private val NUMBER_PATTER = Regex("\\d+")
	private fun String.parseNumbers(): List<Int> = NUMBER_PATTER.findAll(this).map { it.value.toInt() }.toList()
}

fun main() {
	Mixology().startScript()
}