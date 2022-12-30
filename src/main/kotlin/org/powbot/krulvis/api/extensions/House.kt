package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.utils.Utils.waitFor

object House {
    //
    val PARENT = 370

    //    val VARP = 738
//
//    val BUILDING_MODE = InterfaceAddress(Supplier {
//        Interfaces.get(PARENT).firstOrNull {
//            val hls = it.hoverListeners
//            hls != null && hls.any { hl -> hl.toString().contains("Toggle Building mode") }
//        }
//    })
//
//    fun hasHouse(): Boolean = Varpbits.varpbit(738) != 0
//
//    fun closeHouseOptions(): Boolean {
//        if (!houseOptionsOpen()) {
//            return true
//        } else {
//            return Interfaces.get(PARENT).firstOrNull { it.containsAction("Close") }?.interact("Close") == true
//        }
//    }
//
    fun houseOptionsOpen(): Boolean = Widgets.widget(PARENT).component(0).visible()

    //
    fun openHouseOptions(): Boolean {
        if (houseOptionsOpen()) {
            return true
        }
        if (!Game.tab(Game.Tab.SETTINGS)) {
            return false
        }
        val houseOptionButton = Components.stream(116).action("View House Options").firstOrNull()
        if (houseOptionButton != null) {
            houseOptionButton.click()
            return waitFor { houseOptionsOpen() }
        }
        return false
    }

    fun callButton() = Components.stream(PARENT)
        .action("Call Servant").firstOrNull()

    fun callButler(): Boolean {
        if (!openHouseOptions()) {
            return false
        }
        val bttn1 = Components.stream(PARENT)
            .action("Call Servant").firstOrNull()
        val bttn = Widgets.widget(PARENT).component(22)
        debug("Found with action=$bttn1, With index=$bttn, actions=[${bttn.actions().joinToString()}]")
        return bttn.click()
    }


    fun canCall(): Boolean = Objects.stream().action("Ring").isNotEmpty()

    fun isInside() = Objects.stream(40).type(GameObject.Type.INTERACTIVE)
        .name("Portal").action("Lock").isNotEmpty()

    //
    fun inBuildingMode(): Boolean = Varpbits.varpbit(780) == 1
//
//    fun enterBuildingMode(): Boolean {
//        if (inBuildingMode()) {
//            return true
//        } else if (openHouseOptions()) {
//            val button = Interfaces.get(PARENT)[BUILDING_MODE.resolve().index + 1]
//            return button?.interact(ActionOpcodes.INTERFACE_ACTION) == true
//        }
//        return false
//    }
//
//    fun setDoorRender(doorRender: DoorRender): Boolean {
//        return doorRender.setActive()
//    }
//
//    fun getDoorRender(): DoorRender {
//        return DoorRender.values().first { it.isActive() }
//    }
//
//    fun getPortalOutside(): SceneObject? = SceneObjects.getNearest { it.name == "Portal" && it.containsAction("Home") }
//
//    fun getPortalInHouse(): SceneObject? = SceneObjects.getNearest { it.name == "Portal" && it.containsAction("Lock") }
//
//    fun getEmptyHotspot(): SceneObject? {
//        val hotspots = SceneObjects.getLoaded { it.name == "Door hotspot" }
//        val singular = hotspots.filter { hs -> hotspots.count { it.distance(hs) <= 2 } <= 2 }
//        return singular.sortedBy { it.distance() }.firstOrNull()
//    }
//
//    enum class DoorRender(val bit: Int, val toolTip: String) {
//        OPEN(1, "initially open"),
//        CLOSED(0, "initially closed"),
//        NONE(2, "Do not render doors");
//
//        fun isActive(): Boolean = Varpbits.varpbit(6269) == bit
//
//        fun setActive(): Boolean {
//            if (isActive()) {
//                return true
//            }
//            return openHouseOptions() && Components.stream(PARENT)
//                .firstOrNull {
//                    val hls = it.hoverListeners
//                    hls != null && hls.any { hl -> hl.toString().contains(toolTip) }
//                }
//                ?.interact(ActionOpcodes.INTERFACE_ACTION) == true
//        }
//    }
}
