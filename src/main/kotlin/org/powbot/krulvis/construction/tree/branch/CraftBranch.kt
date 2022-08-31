package org.powbot.krulvis.construction.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.construction.Construction

class InHouse(script: Construction) : Branch<Construction>(script, "InHouse?") {
    override val failedComponent: TreeComponent<Construction>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Construction> = BuildingModeOn(script)

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }
}

class BuildingModeOn(script: Construction) : Branch<Construction>(script, "BuildingModeOn?") {
    override val failedComponent: TreeComponent<Construction>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Construction>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        return House.inBuildingMode()
    }
}