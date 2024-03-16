package jmxtest;

import com.sun.jdmk.comm.HtmlAdaptorServer;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * 启动类
 *         <dependency>
 *             <groupId>org.glassfish.external</groupId>
 *             <artifactId>opendmk_jdmkrt_jar</artifactId>
 *             <version>1.0-b01-ea</version>
 *         </dependency>
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-02-07 17:35
 **/
public class HelloAgent {

    public static void main(String[] args) throws JMException, Exception {
        //// 通过工厂类获取MBeanServer，用来做MBean的容器
        //MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        //// 创建一个ObjectName对象，注意取名规范
        //// 格式为：“域名：name=MBean名称”，域名和MBean的名称可以任意
        //ObjectName helloName = new ObjectName("jmxBean:name=hello");
        //// 将Hello这个类注入到MBeanServer中
        //server.registerMBean(new Hello(), helloName);
        //Thread.sleep(60 * 60 * 1000);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName helloName = new ObjectName("jmxBean:name=hello");
        //create mbean and register mbean
        server.registerMBean(new Hello(), helloName);

        ObjectName adapterName = new ObjectName("HelloAgent:name=htmladapter,port=8082");
        // 注册工具页面访问适配器，不难发现这也是个MBean
        HtmlAdaptorServer adapter = new HtmlAdaptorServer();
        server.registerMBean(adapter, adapterName);
        adapter.start();
    }

}
