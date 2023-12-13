package org.powbot.krulvis.test

class Test {

    var i = 0


}

fun main() {
    val test = Test()
    var keeptrack = 0
    for (i in 0..10) {
        println("i=${i}, keeptrack=$keeptrack")
        println(keeptrack < i.also { keeptrack = i })
    }
}