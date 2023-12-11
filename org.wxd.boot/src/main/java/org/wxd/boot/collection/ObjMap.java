package org.wxd.boot.collection;

import com.alibaba.fastjson.util.TypeUtils;
import org.wxd.boot.format.data.Data2Json;
import org.wxd.boot.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * 通用的hashmap
 * <p>
 * key和value均是object
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-11 14:09
 **/
public class ObjMap extends LinkedHashMap<Object, Object> implements Map<Object, Object>, Data2Json, Serializable {

    public static ObjMap parse(String json) {
        return FastJsonUtil.parse(json, ObjMap.class);
    }

    public static ObjMap build() {
        return new ObjMap();
    }

    public static ObjMap build(int initialCapacity) {
        return new ObjMap(initialCapacity);
    }

    public static ObjMap build(Map m) {
        return new ObjMap(m);
    }

    public static ObjMap build(Object key, Object value) {
        return new ObjMap().append(key, value);
    }

    public ObjMap() {
    }

    public ObjMap(Map m) {
        super(m);
    }

    public ObjMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ObjMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public TreeMap<Object, Object> toTreeMap() {
        return new TreeMap<>(this);
    }

    public ObjMap append(Object key, Object value) {
        put(key, value);
        return this;
    }

    public ObjMap getObjMap(Object key) {
        final Object o = super.get(key);
        if (o == null) return null;
        if (o instanceof ObjMap) return (ObjMap) o;
        if (o instanceof Map) return new ObjMap((Map) o);
        if (o instanceof String) return FastJsonUtil.parse(o.toString(), ObjMap.class);
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
    protected <R> R getObjectDefault(Object key, Class<R> rClass) {
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
        return getObject(key, String.class);
    }

    public Boolean getBoolean(Object key) {
        return getObject(key, Boolean.class);
    }

    public boolean getBooleanValue(Object key) {
        return getObject(key, Boolean::parseBoolean, false);
    }

    public Byte getByte(Object key) {
        return getObject(key, Byte.class);
    }

    public byte getByteValue(Object key) {
        return getObject(key, Byte::parseByte, (byte) 0);
    }

    public Short getShort(Object key) {
        return getObject(key, Short.class);
    }

    public short getShortValue(Object key) {
        return getObject(key, Short::parseShort, (short) 0);
    }

    public Integer getInteger(Object key) {
        return getObject(key, Integer.class);
    }

    public int getIntValue(Object key) {
        return getObject(key, Integer::parseInt, 0);
    }

    public Long getLong(Object key) {
        return getObject(key, Long.class);
    }

    public long getLongValue(Object key) {
        return getObject(key, Long::parseLong, 0L);
    }

    public Float getFloat(Object key) {
        return getObject(key, Float.class);
    }

    public float getFloatValue(Object key) {
        return getObject(key, Float::parseFloat, 0f);
    }

    public Double getDouble(Object key) {
        return getObject(key, Double.class);
    }

    public double getDoubleValue(Object key) {
        return getObject(key, Double::parseDouble, 0d);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
