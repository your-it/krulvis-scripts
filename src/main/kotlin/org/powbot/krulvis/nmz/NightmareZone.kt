package org.powbot.krulvis.nmz

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.nmz.tree.leaf.EnterDream
import org.powbot.krulvis.nmz.tree.leaf.SetupInventory
import org.powbot.mobile.script.ScriptManager
import kotlin.random.Random

@ScriptManifest(
        name = "krul NMZ",
        author = "Krulvis",
        version = "1.0.1",
        description = "Nightmarezone: Sips all potions, guzzles rock cake and flicks rapid heal"
)
@ScriptConfiguration.List(
        [
            ScriptConfiguration(
                    name = "Extra info",
                    description = "When you have pray pots, this script will active quick prayer. \n So make sure you have the quick prayer set to the prayers you want to use.",
                    optionType = OptionType.INFO,
                    defaultValue = "A"
            ),
            ScriptConfiguration(
                    name = "Extra info pots",
                    description = "If a potion is not sipped, it is not supported. Just tag me in discord to add it.",
                    optionType = OptionType.INFO,
                    defaultValue = "A"
            ),
            ScriptConfiguration(
                    name = "Inventory",
                    description = "Inventory setup",
                    optionType = OptionType.INVENTORY
            ),
            ScriptConfiguration(
                    name = "StopAfterNMZ",
                    description = "Stop when outside of NMZ",
                    optionType = OptionType.BOOLEAN,
                    defaultValue = "true"
            ),
        ]
)
class NightmareZone : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return NMZPainter(this)
    }

    override val rootComponent: TreeComponent<*> = OutsideNMZ(this)
    val inventoryItems by lazy { getOption<Map<Int, Int>>("Inventory") }
    val stopOutside by lazy { getOption<Boolean>("StopAfterNMZ") }
    var nextFlick = Timer(1)

    fun outsideNMZ() = Npcs.stream().name("Dominic Onion").isNotEmpty()

    class OutsideNMZ(script: NightmareZone) : Branch<NightmareZone>(script, "Is outside nmz") {
        override val failedComponent: TreeComponent<NightmareZone> = ShouldSipPotion(script, ShouldPray(script))
        override val successComponent: TreeComponent<NightmareZone> = HasCorrectInventory(script)

        override fun validate(): Boolean {
            val outside = script.outsideNMZ()
            if (outside && script.stopOutside) {
                script.log.info("Stopping because outside of NMZ")
                ScriptManager.stop()
            }
            return outside
        }
    }

    class HasCorrectInventory(script: NightmareZone) : Branch<NightmareZone>(script, "Has correct inventory") {
        override val failedComponent: TreeComponent<NightmareZone> = SetupInventory(script)
        override val successComponent: TreeComponent<NightmareZone> = EnterDream(script)

        override fun validate(): Boolean {
            val inv = Inventory.items()
            return script.inventoryItems.all { wi -> wi.value == inv.filter { it.id == wi.key }.sumOf { it.stack } }
        }
    }

    class ShouldPray(script: NightmareZone) : Branch<NightmareZone>(script, "Should pray") {
        override val failedComponent: TreeComponent<NightmareZone> = ShouldDrinkOverload(script)
        override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Flick pray") {
            Prayer.quickPrayer(true)
        }

        override fun validate(): Boolean {
            return Potion.PRAYER.hasWith() && !Prayer.quickPrayer()
        }
    }

    class ShouldDrinkOverload(script: NightmareZone) : Branch<NightmareZone>(script, "Should Overload") {
        override val failedComponent: TreeComponent<NightmareZone> = ShouldRockCake(script)
        override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Drink overload") {
            Potion.OVERLOAD.drink()
        }

        override fun validate(): Boolean {
            return Potion.OVERLOAD.needsRestore(90)
        }
    }

    class ShouldRockCake(script: NightmareZone) : Branch<NightmareZone>(script, "Should RockCake") {
        override val failedComponent: TreeComponent<NightmareZone> = ShouldFlickRapidHeal(script)
        override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Eating Stone") {
            val rockCake = Inventory.stream().id(7510).firstOrNull()
            val action = if (currentHP() in 10 downTo 3) "Eat" else "Guzzle"
            rockCake?.interact(action)
        }

        override fun validate(): Boolean {
            val hp = currentHP()
            return hp > 1 && Potion.OVERLOAD.lastSip.getElapsedTime() > 10000
        }
    }

    class ShouldFlickRapidHeal(script: NightmareZone) : Branch<NightmareZone>(script, "Should Flick Rapid Heal") {
        override val failedComponent: TreeComponent<NightmareZone> = ShouldDrinkAbsorption(script)
        override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Flick RapidHeal") {
            Prayer.prayer(Prayer.Effect.RAPID_HEAL, true)
            sleep(Random.nextInt(50, 150))
            Prayer.prayer(Prayer.Effect.RAPID_HEAL, false)
            script.nextFlick = Timer(Random.nextInt(10, 40) * 1000)
        }

        override fun validate(): Boolean {
            return script.nextFlick.isFinished()
        }
    }

    class ShouldDrinkAbsorption(script: NightmareZone) : Branch<NightmareZone>(script, "Should Drink Absorption") {
        override val failedComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Chill") {}
        override val successComponent: TreeComponent<NightmareZone> = SimpleLeaf(script, "Drink absorption") {
            Potion.ABSORPTION.drink()
        }

        var nextTop = 950

        override fun validate(): Boolean {
            return Potion.getAbsorptionRemainder() <= 50 && Potion.ABSORPTION.hasWith()
        }
    }
}

class NMZPainter(script: NightmareZone) : ATPaint<NightmareZone>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
                .addString("Absorption: ") { Potion.getAbsorptionRemainder().toString() }
                .addString("NextFlick: ") { script.nextFlick.getRemainderString() }
                .trackSkill(Skill.Attack)
                .trackSkill(Skill.Strength)
                .trackSkill(Skill.Defence)
                .trackSkill(Skill.Hitpoints)
                .trackSkill(Skill.Slayer)
                .trackSkill(Skill.Magic)
                .trackSkill(Skill.Ranged)
                .build()
    }
}

fun main() {
    NightmareZone().startScript(useDefaultConfigs = true)
}