package org.wxd.boot.system;

import com.sun.tools.attach.VirtualMachine;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * 基于JMX在运行时查看codeCache使用情况
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-02-03 14:48
 **/
public class CodeCache {

    public static void main(String[] args) throws Exception {

        String pid = getPid(); // 先获取java程序的pid
        String codeCache = getCodeCache(pid); // 根据pid获取codeCache的使用情况
        System.out.println(codeCache);

    }

    /**
     * 获取java进程id
     *
     * @return
     */

    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.split("@")[0];

    }

    /**
     * 获取java应用的codeCache使用情况
     *
     * @param pid
     * @throws Exception
     */

    public static String getCodeCache(String pid) throws Exception {
        VirtualMachine vm = VirtualMachine.attach(pid);
        JMXConnector connector = null;
        try {
            String addr = "com.sun.management.jmxremote.localConnectorAddress";
            String property = vm.getAgentProperties().getProperty(addr);
            if (property == null) {
                String agent = vm.getSystemProperties().getProperty("java.home")
                        + File.separator
                        + "lib"
                        + File.separator
                        + "management-agent.jar";

                vm.loadAgent(agent);
                property = vm.getAgentProperties().getProperty(addr);
            }
            JMXServiceURL url = new JMXServiceURL(property);
            connector = JMXConnectorFactory.connect(url);
            MBeanServerConnection mbeanConn = connector.getMBeanServerConnection();
            ObjectName obj = new ObjectName("java.lang:type=MemoryPool,name=Code Cache");
            return mbeanConn.getAttribute(obj, "Usage").toString();
        } finally {
            if (connector != null) {
                connector.close();
            }
            vm.detach();
        }
    }

}
