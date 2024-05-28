package org.powbot.krulvis.test

import org.powbot.krulvis.mta.rooms.AlchemyRoom

class Test(val id: Int, val amount: Int)

fun main() {

	val items = listOf(
		AlchemyRoom.Alchable.LEATHER_BOOTS,
		AlchemyRoom.Alchable.ADAMANT_KITESHIELD,
		AlchemyRoom.Alchable.ADAMANT_HELM,
		AlchemyRoom.Alchable.EMERALD,
		AlchemyRoom.Alchable.RUNE_LONGSWORD,
		AlchemyRoom.Alchable.NONE
	)

	val foundItem = AlchemyRoom.Alchable.LEATHER_BOOTS

	val knownIndex = foundItem.ordinal
	val cupboardIndex = 1
	val offsetNoModulo = knownIndex - cupboardIndex + items.size
	val offset = offsetNoModulo % items.size

	println("cupboardIndex=$cupboardIndex, itemIndex=$knownIndex, offsetNoModulo=$offsetNoModulo, offset=$offset")
	val newList = items.drop(offset) + items.take(offset)
	println(newList.joinToString())

}
