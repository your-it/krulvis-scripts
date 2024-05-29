package org.powbot.krulvis.api.extensions

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance

object House {
	//
	val PARENT = 370

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


	fun getPool(): GameObject = Objects.stream().name(*allPools).first()

	private const val ornatePool = "Ornate pool of Rejuvenation"
	private const val restorationPool = "Pool of Restoration"
	private const val revitalisationPool = "Pool of Revitalisation"
	private const val rejuvenationPool = "Pool of Rejuvenation"
	private const val fancyPool = "Fancy pool of Rejuvenation"

	private val allPools =
		arrayOf(ornatePool, restorationPool, restorationPool, revitalisationPool, rejuvenationPool, fancyPool)
}
