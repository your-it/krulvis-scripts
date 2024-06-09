package org.powbot.krulvis.api.teleports

class TeleportMethod(val teleport: Teleport) {

    var executed = false

    fun execute(): Boolean {
        if (executed) return true
        executed = teleport.execute()
        return executed
    }
}