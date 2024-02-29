# 项目框架
## 简介
此框架可以理解为“spring spring boot”作为快速搭建应用的一些辅助实现;
<p>其中包括数据库操作，tcp,http,websocket rpc等网络服务应用
<p>

| 项目模块                                 | 描述                                                                   |
|--------------------------------------|----------------------------------------------------------------------|
| org.wxd.boot.agent                   | 用于字节码更新 java 文件编译，class文件加载                                          |
| org.wxd.boot.agent                   | 用于字节码更新 java 文件编译，class文件加载                                          |
| org.wxd.boot.assist                  | 用于运行耗时统计，通过 assist 字节码增强方式对调用链路增加耗时统计代码                              |
| org.wxd.boot.batis                   | batis，数据库操作基类                                                        |
| org.wxd.boot.batis.mongodb           | 操作mongodb                                                            |
| org.wxd.boot.batis.redis             | 操作redis                                                              |
| org.wxd.boot.batis.sql               | 关系式数据库基类                                                             |
| org.wxd.boot.batis.sql.mysql         | mysql                                                                |
| org.wxd.boot.batis.sql.sqlite        | 本地文件数据库                                                              |
| org.wxd.boot.core                    | 基类，辅助，一些数据结构，线程管理，任务队列管理                                             |
| org.wxd.boot.jscript                 | 用来执行js代码块的，之前某些项目有需求运用到了                                             |
| org.wxd.boot.net                     | 和网络相关的支持，基于netty实现                                                   |
| org.wxd.boot.net.http                | 实现了http协议的一些基础类                                                      |
| org.wxd.boot.net.http.client.apache  | apache 引用httpclient 二次使用封装                                           |
| org.wxd.boot.net.http.client.jdk     | 基于jdk17过后原生代码实现的httpclient但是它有点傻线程跳转逻辑不好，多段式提交支持不好                   |
| org.wxd.boot.net.http.client.url     | 基于socket原生模拟httpclient ，性能最高，目前已经实现虚拟线程的异步化请求，实现了cookie，和多段式提交以及文件上传 |
| org.wxd.boot.net.http.service.simple | 测试用的基于jdk原生代码实现的http服务器，简单的http服务支持很友好 这个很独立                         |
| org.wxd.boot.starter                 | 上面的项目库都是基础辅助库，这里是容器化项目，启动项目，可以理解为springboot的启动                       |
| org.wxd.boot.holder                  | 和 [奇衡三]() 讨论的结果，这里实现的是隔离化加载方案，可以达到共享内存；单进程多服模式                       |

### 测试描述

org.wxd.boot.starter 模块的test目录下面有一个TestMain的文件

```java
/**
 * 启动项目
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-11 17:52
 **/
public class TestMain {

    public static void main(String[] args) {
        BootConfig bootConfig = new BootConfig();
        bootConfig.getHttp().getHeaders().add(new WebConfig.Header().setKey("1").setValue("2"));
        System.out.println(XmlUtil.toXml(bootConfig));
        Starter.startBoot(TestMain.class);
        Starter.start(true, 1, "test");
    }

}
```

启动后效果
```java
[2024-02-28 19:35:52.913] [DEBUG] [main] [o.w.b.s.a.ActionTimer.action() Line:63 ] - 
===============================================timer job=========================================================
class = org.wxd.boot.starter.webapi.PublicApi
[scheduled-job]org.wxd.boot.starter.webapi.PublicApi.s1
===============================================timer job=========================================================

[2024-02-28 19:35:52.914] [DEBUG] [main] [o.w.b.s.IocContext.getInstance() Line:38 ] - 1365163763 org.wxd.boot.starter.webapi.PublicApi
[2024-02-28 19:35:52.917] [DEBUG] [main] [o.w.b.s.IocContext.getInstance() Line:38 ] - 1365163763 org.wxd.boot.starter.webapi.ServerApi
[2024-02-28 19:35:52.920] [DEBUG] [main] [o.w.b.s.Starter.lambda$iocInitBean$11() Line:210] - bean init class org.wxd.boot.starter.webapi.ServerApi
[2024-02-28 19:35:52.920] [INFO ] [main] [o.w.b.s.Starter.startBoot() Line:125] - 主容器初始化完成：22790969
[2024-02-28 19:35:53.012] [INFO ] [main] [o.w.b.n.NioServer.open() Line:116] - class org.wxd.boot.starter.service.HsService http-server default http://127.0.0.1:19000 - 19000 服务器已启动
[2024-02-28 19:35:53.014] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/publicapi/index
[2024-02-28 19:35:53.014] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/publicapi/test0
[2024-02-28 19:35:53.014] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/publicapi/test1
[2024-02-28 19:35:53.015] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/publicapi/test2
[2024-02-28 19:35:53.015] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/publicapi/test3
[2024-02-28 19:35:53.015] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/serverapi/index
[2024-02-28 19:35:53.015] [DEBUG] [main] [o.w.b.n.w.h.HttpServer.lambda$open$0() Line:204] - http://127.0.0.1:19000/serverapi/sdk
[2024-02-28 19:35:53.016] [INFO ] [main] [o.w.b.n.NioServer.open() Line:116] - class org.wxd.boot.starter.service.TsService tcp-server default 127.0.0.1:17000 - 17000 服务器已启动
[2024-02-28 19:35:53.017] [INFO ] [main] [o.w.b.n.NioServer.open() Line:116] - class org.wxd.boot.starter.service.WsService web-server default - 18000 服务器已启动
```

## 项目最主要的配置 boot.xml

配置方案具体可以参考 org.wxd.boot.starter 模块的test目录 test/resources/boot.xml

```java
<!-- 虚拟线程池 -->
<vtExecutor coreSize="100" maxSize="200"/>
<!-- 默认线程池 -->
<defaultExecutor coreSize="2" maxSize="4"/>
<!-- 逻辑线程池 -->
<logicExecutor coreSize="8" maxSize="16"/>
```
start 项目启动 框架所支持的线程池配置，可以更改线程数量
```java
<server>
    <name>g1</name>
    <host>*</host>
    <wanIp>127.0.0.1</wanIp>
    <port>17000</port>
    <sslProtocolType></sslProtocolType>
    <jks></jks>
    <jksPwd></jksPwd>
</server>
<server1>
    <name>g2</name>
    <host>*</host>
    <wanIp>127.0.0.1</wanIp>
    <port>17001</port>
    <sslProtocolType></sslProtocolType>
    <jks></jks>
    <jksPwd></jksPwd>
</server1>
```
开启两个tcp端口的监听

## 更完整的解决方案落地实现请查阅 test-all/mmoarpg-j21 仓储代码

两个文件皆可以启动，注意自己配置数据库访问（test-all/mmoarpg-j21/pom.xml 配置自己的数据库访问）
<br>GameSrAppMain
<br>GameSrScriptMain