package org.wxd.boot.io;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 对象池
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-10 10:24
 **/
@Slf4j
@Getter
public class ObjectBox<E extends IObjectClear> implements Serializable {

    private final ReentrantLock relock = new ReentrantLock();
    private final LinkedList<E> objectPoolBeans = new LinkedList<>();
    private volatile int initSize;
    /** 核心数量 */
    private volatile int core;
    private volatile Class<E> beanClass;

    public ObjectBox(Class<E> beanClass) {
        this(beanClass, 32);
    }

    /**
     * @param beanClass
     * @param core      核心数量，一般是8的倍数
     */
    public ObjectBox(Class<E> beanClass, int core) {
        this.beanClass = beanClass;
        this.core = core;
        check(beanClass);
    }

    public int size() {
        return objectPoolBeans.size();
    }

    public void returnObject(E obj) {
        relock.lock();
        try {
            if (obj.getClass() != beanClass) {
                return;
            }
            clearBean(obj);
            if (objectPoolBeans.size() < core) {
                objectPoolBeans.add(obj);
            }
        } finally {
            relock.unlock();
        }
    }

    public E getObject(Class<E> clazz) {
        relock.lock();
        try {
            check(clazz);
            E obj = objectPoolBeans.poll();
            if (obj != null) {
                clearBean(obj);
            }
            return obj;
        } finally {
            relock.unlock();
        }
    }

    protected void clearBean(E obj) {
        obj.clear();
    }

    protected void check(Class<E> clazz) {
        if (clazz != beanClass) {
            this.objectPoolBeans.clear();
            this.beanClass = clazz;
            this.initSize = 0;
        }
        if (objectPoolBeans.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                try {
                    final E bean = newBean();
                    objectPoolBeans.add(bean);
                    initSize++;
                } catch (Exception e) {
                    throw new RuntimeException(beanClass.getName() + " - 缺少无参构造函数");
                }
            }
            log.info("初始化对象池：{}, 已初始化数量：{}", clazz.getName(), initSize);
        }
    }

    protected E newBean() throws Exception {
        return beanClass.getConstructor().newInstance();
    }

}
