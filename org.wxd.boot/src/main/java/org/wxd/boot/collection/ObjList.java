package org.wxd.boot.collection;

import com.alibaba.fastjson.util.TypeUtils;
import org.wxd.boot.format.data.Data2Json;
import org.wxd.boot.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 通用类型list
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-06-14 23:35
 **/
public class ObjList extends ArrayList<Object> implements Serializable, List<Object>, Data2Json {

    public static ObjList build() {return new ObjList();}

    public static ObjList build(int initialCapacity) {return new ObjList(initialCapacity);}

    public static ObjList build(Collection c) {return new ObjList(c);}

    public ObjList() {
    }

    public ObjList(int initialCapacity) {
        super(initialCapacity);
    }

    public ObjList(Collection c) {
        super(c);
    }

    public ObjList append(Object obj) {
        super.add(obj);
        return this;
    }

    public ObjList getObjList(int index) {
        final Object o = get(index);
        if (o == null) return null;
        if (o instanceof ObjList) return (ObjList) o;
        if (o instanceof List) return new ObjList((List) o);
        if (o instanceof String) return FastJsonUtil.parse((String) o, ObjList.class);
        throw new UnsupportedOperationException("无法转化类型：" + o.getClass() + FastJsonUtil.toJson(o));
    }

    public <R> R parseObject(int key, Class<R> rClass) {
        final String o = getString(key);
        if (o == null) return null;
        return FastJsonUtil.parse(o, rClass);
    }

    public <R> R getObject(int index) {
        return (R) get(index);
    }

    protected <R> R getObject(int index, Class<R> rClass) {
        final Object o = get(index);
        if (o == null) return null;
        return TypeUtils.castToJavaBean(o, rClass);
    }

    protected <R> R getObjectDefault(int index, Class<R> rClass) {
        final Object o = get(index);
        return TypeUtils.castToJavaBean(o, rClass);
    }

    public <R> R getObject(int index, Function<String, R> function) {
        final String string = getString(index);
        if (string == null) return null;
        return function.apply(string);
    }

    public <R> R getObject(int index, Function<String, R> function, R defaultValue) {
        final R object = getObject(index, function);
        if (object == null) return defaultValue;
        return object;
    }

    public String getString(int index) {
        return getObject(index, String.class);
    }

    public Boolean getBoolean(int index) {
        return getObject(index, Boolean.class);
    }

    public boolean getBooleanValue(int index) {
        return getObjectDefault(index, boolean.class);
    }

    public Byte getByte(int index) {
        return getObject(index, Byte.class);
    }

    public byte getByteValue(int index) {
        return getObjectDefault(index, byte.class);
    }

    public Short getShort(int index) {
        return getObject(index, Short.class);
    }

    public short getShortValue(int index) {
        return getObjectDefault(index, short.class);
    }

    public Integer getInteger(int index) {
        return getObject(index, Integer.class);
    }

    public int getIntValue(int index) {
        return getObjectDefault(index, int.class);
    }

    public Long getLong(int index) {
        return getObject(index, Long.class);
    }

    public long getLongValue(int index) {
        return getObjectDefault(index, long.class);
    }

    public Float getFloat(int index) {
        return getObject(index, Float.class);
    }

    public float getFloatValue(int index) {
        return getObjectDefault(index, float.class);
    }

    public Double getDouble(int index) {
        return getObject(index, Double.class);
    }

    public double getDoubleValue(int index) {
        return getObjectDefault(index, double.class);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
