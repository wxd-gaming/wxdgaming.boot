package org.wxd.boot.holder;

import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.loader.ClassDirLoader;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * 容器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-01-27 15:36
 **/
public class Holder {

    public static void main(String[] args) throws Exception {
        new Thread(() -> {
            loadJar(new String[]{"target/s1"});
        }).start();
        new Thread(() -> {
            loadJar(new String[]{"target/s2"});
        }).start();

        new Thread(() -> {
            loadJar(new String[]{"target/s3"});
        }).start();

        new Thread(() -> {
            loadJar(new String[]{"target/s4"});
        }).start();

    }

    static void loadJar(String[] args) {
        try {
            String domain = "domain-" + args[0];
            MBeanServer server = MBeanServerFactory.createMBeanServer(domain);
            ObjectName instance = ObjectName.getInstance(domain, "user.s", domain);
            server.createMBean("javax.management.loading.MLet", instance);
            server.getClassLoader(instance);
            /** 父 load null 表示需要做到类的绝对隔离 */
            ClassDirLoader classDirLoader = new ClassDirLoader((ClassLoader) null);
            /**把jar 需要的引用 lib 里面的jar包全部加载 */
            FileUtil.walk("target/lib", ".jar").forEach(jar -> {
                try {
                    classDirLoader.addURL(jar.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            });

            classDirLoader.addURL(new File("target/classes").toURI().toURL());
            classDirLoader.addURL(new File("target/test-classes").toURI().toURL());
            classDirLoader.addURL(new File("src/test/resources").toURI().toURL());

            Class<?> loadClass = classDirLoader.loadClass("code.LogOut");
            Method method = loadClass.getMethod("main", String[].class);
            method.invoke(null, (Object) args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
