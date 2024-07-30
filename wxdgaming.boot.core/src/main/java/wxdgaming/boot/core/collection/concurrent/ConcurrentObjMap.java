package wxdgaming.boot.core.collection.concurrent;

import com.alibaba.fastjson.util.TypeUtils;
import wxdgaming.boot.core.format.data.Data2Json;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * 通用的hashmap
 * <p>
 * key和value均是object
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-05-11 14:09
 **/
public class ConcurrentObjMap extends ConcurrentHashMap<Object, Object> implements Map<Object, Object>, ConcurrentMap<Object, Object>, Data2Json, Cloneable, Serializable {

    public static ConcurrentObjMap build() {
        return new ConcurrentObjMap();
    }

    public static ConcurrentObjMap build(Map m) {
        return new ConcurrentObjMap(m);
    }

    public static ConcurrentObjMap build(int initialCapacity) {
        return new ConcurrentObjMap(initialCapacity);
    }

    public static ConcurrentObjMap build(Object key, Object value) {
        return new ConcurrentObjMap().append(key, value);
    }

    public ConcurrentObjMap() {
    }

    public ConcurrentObjMap(Map m) {
        super(m);
    }

    public ConcurrentObjMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ConcurrentObjMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ConcurrentObjMap append(Object key, Object value) {
        put(key, value);
        return this;
    }

    public ConcurrentObjMap getObjMap(Object key) {
        final Object o = super.get(key);
        if (o == null) return null;
        if (o instanceof ConcurrentObjMap) return (ConcurrentObjMap) o;
        if (o instanceof Map) return new ConcurrentObjMap((Map) o);
        if (o instanceof String) return FastJsonUtil.parse(o.toString(), ConcurrentObjMap.class);
        throw new UnsupportedOperationException("无法转化类型：" + o.getClass() + FastJsonUtil.toJson(o));
    }

    public <R> R parseObject(Object key, Class<R> rClass) {
        final String o = getString(key);
        if (o == null) return null;
        return FastJsonUtil.parse(o, rClass);
    }

    public <R> R getObject(Object key) {
        return (R) super.get(key);
    }

    protected <R> R getObject(Object key, Class<R> rClass) {
        final Object o = get(key);
        if (o == null) return null;
        return TypeUtils.castToJavaBean(o, rClass);
    }

    /** 基础类型才会有默认值 */
    public <R> R getObjectDefault(Object key, Class<R> rClass) {
        final Object o = get(key);
        return TypeUtils.castToJavaBean(o, rClass);
    }

    public <R> R getObject(Object key, Function<String, R> function) {
        final String string = getString(key);
        if (string == null) return null;
        return function.apply(string);
    }

    public <R> R getObject(Object key, Function<String, R> function, R defaultValue) {
        final R object = getObject(key, function);
        if (object == null) return defaultValue;
        return object;
    }

    public String getString(Object key) {
        Object o = get(key);
        if (o == null) return null;
        return String.valueOf(o);
    }

    public Boolean getBoolean(Object key) {
        return getObject(key, Boolean.class);
    }

    public boolean getBooleanValue(Object key) {
        return getObjectDefault(key, boolean.class);
    }

    public Byte getByte(Object key) {
        return getObject(key, Byte.class);
    }

    public byte getByteValue(Object key) {
        return getObjectDefault(key, byte.class);
    }

    public Short getShort(Object key) {
        return getObject(key, Short.class);
    }

    public short getShortValue(Object key) {
        return getObjectDefault(key, short.class);
    }

    public Integer getInteger(Object key) {
        return getObject(key, Integer.class);
    }

    public int getIntValue(Object key) {
        return getObjectDefault(key, int.class);
    }

    public Long getLong(Object key) {
        return getObject(key, Long.class);
    }

    public long getLongValue(Object key) {
        return getObjectDefault(key, long.class);
    }

    public Float getFloat(Object key) {
        return getObject(key, Float.class);
    }

    public float getFloatValue(Object key) {
        return getObjectDefault(key, float.class);
    }

    public Double getDouble(Object key) {
        return getObject(key, Double.class);
    }

    public double getDoubleValue(Object key) {
        return getObjectDefault(key, double.class);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
