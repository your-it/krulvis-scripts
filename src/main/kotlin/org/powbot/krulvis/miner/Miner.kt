package org.powbot.krulvis.miner

import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Varpbits
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.tree.branch.ShouldFixStrut

@ScriptManifest(
    name = "krul Miner",
    description = "Mines & banks anything, anywhere",
    version = "1.2.0",
    markdownFileName = "Miner.md",
    category = ScriptCategory.Mining
)
class Miner : ATScript() {


    var profile = MinerProfile()
    var lastPayDirtDrop = 0L

    override val rootComponent: TreeComponent<*> = ShouldFixStrut(this)

    /**
     * Used to get the amount of loot in the motherload sack
     */
    fun getMotherloadCount(): Int = (Varpbits.varpbit(375) xor 65544) / 256

    var shouldEmptySack = false

    /**
     * Get the sack in the motherload mine
     */
    fun getSack() = Objects.stream().name("Sack").action("Search").findFirst()

//    override fun mouseClicked(p0: MouseEvent?) {
//        if (!started) {
//            val p = p0!!.point
//            val obj =
//                Objects.stream().filter {
//                    it.name().isNotEmpty() && it.boundingModel()?.contains(Point(p.x, p.y)) == true
//                }.findFirst()
//            if (obj.isPresent) {
//                val rock = obj.get()
//                println(
//                    "Clicked on: ${rock.name()}, Mod Colors: ${rock.modifiedColors().joinToString()}"
//                )
//                val ore = rock.getOre()
//                if (ore != null) {
//                    println("Found ore: $ore")
//                } else {
//                    println("Could not recognize ore: $ore... If this rock is currently mineable, report the Mod Colors to Krulvis.")
//                }
////                gui?.addRock(rock.tile())
//
//            } else {
//                println("Clicked but couldn't find anything")
//            }
//        }
//    }
//
//    override fun mousePressed(p0: MouseEvent?) {
////        println("Pressed")
//    }
//
//    override fun mouseReleased(p0: MouseEvent?) {
////        println("Released")
//    }
//
//    override fun mouseEntered(p0: MouseEvent?) {
////        println("Entered")
//    }
//
//    override fun mouseExited(p0: MouseEvent?) {
////        println("Exited")
//    }

//    override fun onChange(evt: InventoryChangeEvent) {
//        val item = evt.itemId
//        if (item != Ore.PAY_DIRT.id && (item.getOre() != null || item == 21341) && !Bank.opened() && !ctx.depositBox.opened()) {
//            lootTracker.addLoot(item, evt.quantityChange)
//        }
//    }

}

fun main() {
    Miner().startScript()
}