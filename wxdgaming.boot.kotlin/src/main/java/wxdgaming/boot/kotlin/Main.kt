package wxdgaming.boot.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wxdgaming.boot.core.timer.MyClock
import wxdgaming.boot.net.http.client.url.HttpBuilder


fun main() {
    T2().sum(1, 2)
    repeat(4) {
        var nowOfMills = MyClock.millis()
        javaLaunch2(100)
        println("请求：" + (MyClock.millis() - nowOfMills) + " ms")
    }
}

fun javaLaunch2(count: Int) {
    var exec = 0;
    val obj = Object()
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    repeat(count) {
        val launch = coroutineScope.launch {
            try {
                var url = "";
                url = "https://www.baidu.com"
//                url = "https://wapi.xitwxi.com/user/verifyAccount?uid=1&token=1"
//                url = "http://center.xiaw.net:18800/sjcq/wanIp"
                val ret = HttpBuilder.get(url).request().bodyString();
                println("" + Thread.currentThread() + " - " + MyClock.nowString() + " - " + ret)
            } finally {
                synchronized(obj) {
                    exec++;
                }
            }
        }
    }
    while (exec < count) {
//        println(exec)
        Thread.sleep(1)
    }
}
