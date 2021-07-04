package org.powbot.krulvis.miner

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.extensions.items.Ore.Companion.getOre
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.tree.branch.ShouldBank
import org.powerbot.script.InventoryChangeEvent
import org.powerbot.script.InventoryChangeListener
import org.powerbot.script.Script
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.SwingUtilities

@Script.Manifest(
    name = "krul Miner",
    description = "Mines & banks anything, anywhere",
    version = "1.1.2",
    markdownFileName = "Miner.md",
    properties = "category=Mining;",
    mobileReady = true
)
class Miner : ATScript(), MouseListener, InventoryChangeListener {

    var profile = MinerProfile()
    var gui: MinerGUI? = null

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    override val painter: ATPainter<*> = MinerPainter(this)

    override fun startGUI() {
        ctx.objects.toStream().name("Rock").nearest().findFirst()
        SwingUtilities.invokeLater { gui = MinerGUI(this) }
        skillTracker.addSkill(Skill.MINING)
    }

    /**
     * Used to get the amount of loot in the motherload sack
     */
    fun getMotherloadCount(): Int = ctx.varpbits.varpbit(375) / 256

    var shouldEmptySack = false

    /**
     * Get the sack in the motherload mine
     */
    fun getSack() = ctx.objects.toStream().name("Sack").action("Search").findFirst()

    override fun mouseClicked(p0: MouseEvent?) {
        if (!started) {
            val point = p0!!.point
            val obj =
                ctx.objects.toStream().filter {
                    it.name().isNotEmpty() && it.boundingModel().contains(point)
                }.findFirst()
            if (obj.isPresent) {
                val rock = obj.get()
                println(
                    "Clicked on: ${rock.name()}, Mod Colors: ${rock.modifiedColors().joinToString()}"
                )
                val ore = rock.getOre()
                if (ore != null) {
                    println("Found ore: $ore")
                } else {
                    println("Could not recognize ore: $ore... If this rock is currently mineable, report the Mod Colors to Krulvis.")
                }
                gui?.addRock(rock.tile())

            } else {
                println("Clicked but couldn't find anything")
            }
        }
    }

    override fun mousePressed(p0: MouseEvent?) {
//        println("Pressed")
    }

    override fun mouseReleased(p0: MouseEvent?) {
//        println("Released")
    }

    override fun mouseEntered(p0: MouseEvent?) {
//        println("Entered")
    }

    override fun mouseExited(p0: MouseEvent?) {
//        println("Exited")
    }

    override fun onChange(evt: InventoryChangeEvent) {
        val item = evt.itemId
        if (item != Ore.PAY_DIRT.id && (item.getOre() != null || item == 21341) && !ctx.bank.opened() && !ctx.depositBox.opened()) {
            lootTracker.addLoot(item, evt.quantityChange)
        }
    }

}