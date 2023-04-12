package org.powbot.krulvis.test

class WithNullableField {

    val active: Boolean = true
}

fun main() {
    var nullable: WithNullableField? = null
    println(!(nullable?.active ?: false))


}