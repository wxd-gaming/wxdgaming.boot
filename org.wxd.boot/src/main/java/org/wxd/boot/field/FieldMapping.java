package org.wxd.boot.field;

import com.alibaba.fastjson.annotation.JSONField;
import org.wxd.agent.exception.Throw;
import org.wxd.boot.field.extend.FieldAnn;
import org.wxd.boot.lang.ConvertUtil;
import org.wxd.boot.str.json.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * 反射解析 class 的属性字段
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-14 11:54
 **/
public class FieldMapping {

    /*字段*/
    private Field field;
    /*字段注解*/
    private FieldAnn fieldAnn;
    private Type jsonFieldType = null;
    private Class<?> fieldType;
    /*字段的get属性*/
    private Method getMethod;
    /*字段的set属性*/
    private Method setMethod;

    public FieldMapping() {
    }

    public void copy(FieldMapping fieldMapping) {
        this.field = fieldMapping.field;
        this.fieldAnn = fieldMapping.fieldAnn;
        this.fieldType = fieldMapping.fieldType;
        this.getMethod = fieldMapping.getMethod;
        this.setMethod = fieldMapping.setMethod;
    }

    /**
     * 是否是静态字段
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isStaticField() {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * 获取是否是最终字段
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isFinalField() {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * 字段名字
     */
    @JSONField(serialize = true)
    public String getFieldName() {
        return getField().getName();
    }

    public Field getField() {
        return field;
    }

    public FieldMapping setField(Field field) {
        this.field = field;
        this.fieldType = field.getType();
        return this;
    }

    public FieldAnn getFieldAnn() {
        return fieldAnn;
    }

    public FieldMapping setFieldAnn(FieldAnn fieldAnn) {
        this.fieldAnn = fieldAnn;
        return this;
    }

    /**
     * 设置反射可调用字段
     */
    public void setAccessible(boolean flag) {
        this.field.setAccessible(flag);
    }

    public String typeName() {
        Type tmp = jsonFieldType();
        if (tmp instanceof Class) {
            return ((Class<?>) tmp).getSimpleName();
        }
        return tmp.getTypeName();
    }

    /**
     * json反序列化使用
     *
     * @return
     */
    public Type jsonFieldType() {
        if (jsonFieldType == null) {
            if (getField() != null) {
                jsonFieldType = ParameterizedTypeImpl.genericFieldTypes(getField());
            } else {
                jsonFieldType = fieldType;
            }
        }
        return jsonFieldType;
    }

    public Class<?> getFieldType() {
        if (getField() != null) {
            return getField().getType();
        }
        return fieldType;
    }

    public FieldMapping setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    /**
     * getter方法
     *
     * @return
     */
    public Method getGetMethod() {
        return getMethod;
    }

    /**
     * getter方法
     *
     * @param getMethod
     * @return
     */
    public FieldMapping setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
        return this;
    }

    /**
     * setter方法
     *
     * @return
     */
    public Method getSetMethod() {
        return setMethod;
    }

    /**
     * setter方法
     *
     * @param setMethod
     * @return
     */
    public FieldMapping setSetMethod(Method setMethod) {
        this.setMethod = setMethod;
        return this;
    }

    /**
     * 获取属性值
     *
     * @param source
     * @param value
     */
    @JSONField(serialize = false, deserialize = false)
    public void setFieldValue(Object source, Object value) {
        try {
            if (value == null) {
                return;
            }
            if (getSetMethod() != null) {
                /*如果有set函数，调用set函数赋值*/
                getSetMethod().invoke(source, value);
            } else {
                /*没有set函数，直接赋值 需要设置反射赋值禁用 公开属性开关*/
                getField().set(source, value);
            }
        } catch (Throwable e) {
            throw Throw.as("字段：" + getField().getName() + ", setter ", e);
        }
    }


    /**
     * 获取属性值
     *
     * @param source
     * @return
     */
    @JSONField(serialize = false, deserialize = false)
    public Object getFieldValue(Object source) {
        try {
            if (getGetMethod() != null) {
                /*如果有get函数，调用get函数赋值*/
                return getGetMethod().invoke(source);
            } else {
                return getField().get(source);
            }
        } catch (Throwable e) {
            throw Throw.as("字段：" + getField().getName() + ", getter ", e);
        }
    }

    public Double getDouble(Object source) {
        Object invoke = getFieldValue(source);
        Double toDouble = ConvertUtil.toDouble(invoke);
        if (toDouble == null) {
            return 0D;
        } else {
            return toDouble;
        }
    }

    @Override
    public String toString() {
        return "field=" + field + ", fieldType=" + fieldType + ", getter=" + getMethod + ", setter=" + setMethod;
    }
}
