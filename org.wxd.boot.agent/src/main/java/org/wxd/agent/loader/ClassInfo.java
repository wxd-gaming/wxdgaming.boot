package org.wxd.agent.loader;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-06 17:10
 **/
public class ClassInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Class<?> loadClass;
    private byte[] loadClassBytes;

    public String getLoadClassSimpleName() {
        return loadClass.getSimpleName();
    }

    public String getLoadClassClassName() {
        return loadClass.getName();
    }

    public Class<?> getLoadClass() {
        return loadClass;
    }

    public ClassInfo setLoadClass(Class<?> loadClass) {
        this.loadClass = loadClass;
        return this;
    }

    public byte[] getLoadClassBytes() {
        return loadClassBytes;
    }

    public ClassInfo setLoadClassBytes(byte[] loadClassBytes) {
        this.loadClassBytes = loadClassBytes;
        return this;
    }

    /**
     * 判断是否是继承关系
     *
     * @param clazz
     * @return
     */
    public boolean isAssignableFrom(Class<?> clazz) {
        return clazz.isAssignableFrom(this.loadClass);
    }

    /**
     * 获取加载类的实例对象
     *
     * @param <R>
     * @return
     * @throws Exception
     */
    public <R> R newInstance() throws Exception {
        return (R) this.loadClass.newInstance();
    }
}
