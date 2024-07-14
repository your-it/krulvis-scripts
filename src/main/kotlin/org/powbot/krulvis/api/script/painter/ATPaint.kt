package org.powbot.krulvis.api.script.painter

import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.TrackInventoryOption
import org.powbot.krulvis.api.script.ATScript
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.rscache.loader.ItemLoader

abstract class ATPaint<S : ATScript>(val script: S, val x: Int = 110, val y: Int = 70) {

	val paintBuilder = PaintBuilder()

	init {
		paintBuilder.x(x)
		paintBuilder.y(y)
		paintBuilder.addString("Leaf: ") { script.lastLeaf.name }
	}

	fun PaintBuilder.trackInventoryItemQ(itemId: Int) = trackInventoryItem(itemId = itemId, textSize = null, TrackInventoryOption.QuantityChange)

	abstract fun buildPaint(paintBuilder: PaintBuilder): Paint

	open fun paintCustom(g: Rendering) {}

	fun perHourText(amount: Int) = "$amount, ${script.timer.getPerHour(amount)}/hr"

	fun trackItem(id: Int, amount: Int) {
		if (paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
			paintBuilder.trackInventoryItems(id)
			script.logger.info("Now tracking: ${ItemLoader.lookup(id)?.name()} adding $amount as start")
			paintBuilder.items.forEach { row ->
				val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
				if (item != null) (item as InventoryItemPaintItem).diff += amount
			}
		}
	}
}