package kcode

import org.junit.Test
import wxdgaming.boot.agent.LogbackUtil

data class PersonData(var name: String, var age: Int)

class K {

    val log = LogbackUtil.logger();

    @Test
    fun t1(): Unit {
        var pa = PersonData("a", 1)
        var pb = PersonData("a", 1)
        println(pa.toString() + pa.hashCode() + " - " + pb)
        println(pa == pb)
        println(pa === pb)
        LogbackUtil.logger().info("dd")
        log.info("{}", "s")
        pa.age = 12
        println(pa.toString() + pa.hashCode() + " - " + pb)
        println(pa == pb)
        println(pa === pb)
    }

}