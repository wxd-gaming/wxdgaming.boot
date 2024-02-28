# 项目框架
## 简介
此框架可以理解为“spring spring boot”作为快速搭建应用的一些辅助实现;
<p>其中包括数据库操作，tcp,http,websocket rpc等网络服务应用

<br>org.wxd.boot.agent   -- 用于字节码更新 java 文件编译，class文件加载
<br>org.wxd.boot.assist  -- 用于运行耗时统计，通过 assist 字节码增强方式对调用链路增加耗时统计代码
<br>org.wxd.boot.batis   -- batis，数据库操作基类
<br>org.wxd.boot.batis.mongodb -- 操作mongodb
<br>org.wxd.boot.batis.redis   -- 操作redis
<br>org.wxd.boot.batis.sql     -- 关系式数据库基类
<br>org.wxd.boot.batis.sql.mysql -- mysql
<br>org.wxd.boot.batis.sql.sqlite -- 本地文件数据库
<br>org.wxd.boot.core             -- 基类，辅助，一些数据结构，线程管理，任务队列管理
<br>org.wxd.boot.jscript          -- 用来执行js代码块的，之前某些项目有需求运用到了
<br>org.wxd.boot.net              -- 和网络相关的支持，基于netty实现
<br>org.wxd.boot.net.http         -- 实现了http协议的一些基础类
<br>org.wxd.boot.net.http.client.apache  -- apache 引用httpclient 二次使用封装
<br>org.wxd.boot.net.http.client.jdk     -- 基于jdk17过后原生代码实现的httpclient但是它有点傻线程跳转逻辑不好，多段式提交支持不好
<br>org.wxd.boot.net.http.client.url     -- 基于socket原生模拟httpclient ，性能最高，目前已经实现虚拟线程的异步化请求，实现了cookie，和多段式提交以及文件上传
<br>org.wxd.boot.net.http.service.simple -- 测试用的基于jdk原生代码实现的http服务器，简单的http服务支持很友好 这个很独立
<br>org.wxd.boot.starter                 -- 上面的项目库都是基础辅助库，这里是容器化项目，启动项目，可以理解为springboot的启动
<br>org.wxd.boot.holder                  -- 和 [奇衡三]() 讨论的结果，这里实现的是隔离化加载方案，可以达到共享内存；单进程多服模式，