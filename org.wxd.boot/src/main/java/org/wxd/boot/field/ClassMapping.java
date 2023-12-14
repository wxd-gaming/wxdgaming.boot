package org.wxd.boot.field;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.function.ConsumerE2;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.field.extend.FieldAnn;
import org.wxd.boot.field.extend.FieldType;
import org.wxd.boot.lang.ConvertUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.str.json.ProtobufSerializer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 反射解析 class 结构体
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-14 11:57
 **/
@Slf4j
public class ClassMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<?> clazz;
    private final LinkedHashMap<String, FieldMapping> fieldMap = new LinkedHashMap<>();

    public ClassMapping(Class<?> clazz,
                        boolean filterFinal,
                        boolean filterTransient,
                        FieldType fieldType) {
        this.clazz = clazz;
        actionClass(this.clazz, filterFinal, filterTransient, fieldType);
    }

    /**
     * 如果 注解{@link FieldAnn}.alligator() = true 表示字段不参与计算忽律缓存
     */
    protected void actionClass(Class<?> clazz,
                               boolean filterFinal,
                               boolean filterTransient, FieldType fieldType) {
        if (Object.class.equals(clazz)) {
            return;
        }

        if (clazz.getSuperclass() != null) {
            actionClass(clazz.getSuperclass(), filterFinal, filterTransient, fieldType);
        }

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (filterFinal && Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            if (filterTransient && Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            FieldAnn fieldAnn = AnnUtil.ann(field, FieldAnn.class);
            if (fieldAnn != null) {
                if (fieldAnn.alligator()) {
                    /*忽律字段*/
                    continue;
                }
                if (!FieldUtil.checkFieldType(fieldAnn.fieldTypes(), fieldType)) {
                    /*忽律字段*/
                    continue;
                }
            } else if (fieldType != null) {
                /*需求不一致*/
                continue;
            }
            field.setAccessible(true);
            FieldMapping fieldValue = new FieldMapping();
            fieldValue.setField(field);
            fieldValue.setFieldAnn(fieldAnn);
            fieldValue.setGetMethod(findGetMethod(clazz, field));
            fieldValue.setSetMethod(findSetMethod(clazz, field));

            fieldMap.put(field.getName(), fieldValue);
        }
    }

    /** 查找get方法 */
    public Method findGetMethod(Class<?> clazz, Field field) {
        Method method = findMethod(clazz, field, "get", 0);
        if (method != null) return method;
        method = findMethod(clazz, field, "is", 0);
        if (method != null) return method;

        if (field.getType().getSimpleName().equalsIgnoreCase(boolean.class.getSimpleName())) {
            method = findMethod(clazz, field, "", 0);
            if (method != null) return method;
        }
        return null;
    }

    /** 查找set方法 */
    public Method findSetMethod(Class<?> clazz, Field field) {
        return findMethod(clazz, field, "set", 1);
    }

    /** 查找方法 */
    public Method findMethod(Class<?> clazz, Field field, String prefix, int parameterCount) {
        return findMethod(clazz, field, field.getName(), prefix, parameterCount);
    }

    public Method findMethod(Class<?> clazz, Field field, String fieldName, String prefix, int parameterCount) {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            FieldAnn mannotation = AnnUtil.ann(method, FieldAnn.class);
            if (mannotation != null) {
                if (mannotation.alligator()) {
                    /*忽律字段*/
                    continue;
                }
            }
            String methodName = method.getName();// 获取每一个方法名
            if (methodName.equalsIgnoreCase(prefix + fieldName)) {
                if (method.getParameterCount() != parameterCount) {
                    if (log.isDebugEnabled()) {
                        log.debug("类：" + clazz.getName() + " " + prefix + " method " + methodName + " 是否有同名方法 当前方法有参数", new RuntimeException());
                    }
                    continue;
                }
                if (parameterCount > 0) {
                    Class<?> parameterType = method.getParameterTypes()[0];
                    if (!field.getType().equals(parameterType)) {
                        if (log.isDebugEnabled()) {
                            log.debug("类：" + clazz.getName() + " set method " + methodName + " 是否有同名方法 当前参数类型不一致 字段参数类型：" + field.getType() + ", set 方法参数类型：" + parameterType, new RuntimeException());
                        }
                        continue;
                    }
                }
                method.setAccessible(true);
                return method;
            }
        }
        if (boolean.class.getSimpleName().equalsIgnoreCase(field.getType().getSimpleName().toLowerCase())) {
            if (fieldName.startsWith("is")) {
                return findMethod(clazz, field, fieldName.substring(2), prefix, parameterCount);
            }
        }
        return null;
    }

    /**
     * 根据字段名字解析
     *
     * @return
     */
    public Map<String, FieldMapping> getFieldMap() {
        return fieldMap;
    }

    /**
     * 设置属性
     */
    @JSONField(serialize = false, deserialize = false)
    public void setFieldValue(String fieldName, Object source, Object value) {
        FieldMapping field = fieldMap.get(fieldName);
        if (field != null) {
            FieldUtil.setFieldValue(source, field, value);
        } else {
            log.error("属性名字不存在：" + fieldName, new RuntimeException());
        }
    }

    /**
     * source1 = source2 的字段属性值 * (source3 的字段属性值 / 100)
     */
    public void setFieldValue1(Object source1, Object source2, Object source3) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping ms = entry.getValue();
            if (FieldUtil.isNumberFrom(ms.getField())) {
                Double value2 = ms.getDouble(source2);
                Double value3 = ms.getDouble(source3);
                if (value3 != 0) {
                    FieldUtil.setFieldValueByDouble(source1, ms, value2 * (value3 / 10000D));
                } else {
                    FieldUtil.setFieldValueByDouble(source1, ms, value2);
                }
            }
        }
    }

    /**
     * source1 的字段属性值 = source2 的字段属性值 * (source3 的字段属性值 / 100)
     */
    public void setFieldValue2(Object source1, Object source2, Object source3) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping ms = entry.getValue();
            if (FieldUtil.isNumberFrom(ms.getField())) {
                Double value2 = ms.getDouble(source2);
                Double value3 = ms.getDouble(source3);
                if (value3 != 0) {
                    FieldUtil.setFieldValueByDouble(source1, ms, value2 * (value3 / 100D));
                } else {
                    FieldUtil.setFieldValueByDouble(source1, ms, value2);
                }
            }
        }
    }

    /**
     * source1 字段属性 = source2 字段属性 * ratio
     */
    public void setFieldValueByFloat(Object source1, Object source2, float ratio) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping ms = entry.getValue();
            if (FieldUtil.isNumberFrom(ms.getField())) {
                double aDouble = ms.getDouble(source2);
                aDouble *= ratio;
                FieldUtil.setFieldValueByDouble(source1, ms, aDouble);
            }
        }
    }

    /**
     * 设置属性，累加属性
     */
    public void sumValue(String fieldName, Object source, Object value) {
        FieldMapping field = fieldMap.get(fieldName);
        if (field != null) {
            FieldUtil.sumFieldValue(source, field, value);
        } else {
            log.error("属性名字不存在：" + fieldName, new RuntimeException());
        }
    }

    /**
     * 第一个参数值 加上 第二个参数的属性值
     */
    public void sumValue(Object source1, Object source2) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*读取第二个参数的属性值*/
                Object fieldValue = FieldUtil.getFieldValue(source2, field);
                /*追加到抵押给参数的属性值*/
                FieldUtil.sumFieldValue(source1, field, fieldValue);
            }
        }
    }

    /**
     * 属性字段做减法操作
     * 非数值类型直接替换
     */
    public void subtractValue(String fieldName, Object source, Object value) {
        FieldMapping fieldValue = fieldMap.get(fieldName);
        if (fieldValue != null) {
            FieldUtil.subtractFieldValue(source, fieldValue, value);
        } else {
            log.error("属性名字不存在：" + fieldName, new RuntimeException());
        }
    }

    /**
     * 第一个参数值 减去 第二个参数的属性值
     */
    public void subtractValue(Object source1, Object source2) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*读取第二个参数的属性值*/
                Object fieldValue = FieldUtil.getFieldValue(source2, field);
                /*追加到抵押给参数的属性值*/
                FieldUtil.subtractFieldValue(source1, field, fieldValue);
            }
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     */
    public void multiplyValue(String fieldName, Object source, Object value) {
        FieldMapping field = fieldMap.get(fieldName);
        if (field != null) {
            FieldUtil.multiplyFieldValue(source, field, value);
        } else {
            log.error("属性名字不存在：" + fieldName, new RuntimeException());
        }
    }

    /**
     * s1.属性 * ratio
     *
     * @param source1
     * @param ratio
     */
    public void multiplyValue(Object source1, float ratio) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*追加到抵押给参数的属性值*/
                FieldUtil.multiplyValue(source1, field, ratio);
            }
        }
    }

    /**
     * 把对象的属性值对应相 乘
     * <p>
     * s1.属性 * s2.属性
     *
     * @param source1
     * @param source2
     */
    public void multiply(Object source1, Object source2) {
        multiply(source1, source2, 0);
    }

    /**
     * 把对象的属性值对应相 乘
     * <p>
     * s1.属性 * ( s2.属性 + ratio)
     *
     * @param source1
     * @param source2
     * @param ratio   对应属性增加值
     */
    public void multiply(Object source1, Object source2, float ratio) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                Double source2Value = FieldUtil.getFieldValueByDouble(source2, field);
                if (source2Value == null) {
                    source2Value = 0d;
                }
                source2Value += ratio;
                FieldUtil.multiplyValue(source1, field, source2Value.floatValue());
            }
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     */
    public void multiplyValue(Object source1, Object source2) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*读取第二个参数的属性值*/
                Object fieldValue = FieldUtil.getFieldValue(source2, field);
                /*追加到抵押给参数的属性值*/
                FieldUtil.multiplyFieldValue(source1, field, fieldValue);
            }
        }
    }

    /**
     * 乘以对应的值，如果是非数值类型，忽律
     * 按照万分比配置的数值进行 1+(value/10000);
     */
    public void multiplyFieldValueByRatio1(Object source1, Object source2) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*读取第二个参数的属性值*/
                Object fieldValue = FieldUtil.getFieldValue(source2, field);
                /*追加到抵押给参数的属性值*/
                FieldUtil.multiplyFieldValueByRatio1(source1, field, fieldValue);
            }
        }
    }

    /**
     * 乘以对于的值，如果是非数值类型，忽律
     * 按照百分比配置的数值进行 1+(value/100);
     */
    public void multiplyValueByRatio2(Object source1, Object source2) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*读取第二个参数的属性值*/
                Object fieldValue = FieldUtil.getFieldValue(source2, field);
                /*追加到抵押给参数的属性值*/
                FieldUtil.multiplyFieldValueByRatio2(source1, field, fieldValue);
            }
        }
    }

    /**
     * 获取所有数字类型字段的总和
     */
    public int getTotalValue(Object source) {
        int v = 0;
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                v += FieldUtil.getFieldValueByDouble(source, field);
            }
        }
        return v;
    }

    /**
     * clamp source的数字字段在min和max之间
     */
    public void clamp(Object source, Object min, Object max) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                Double i = FieldUtil.getFieldValueByDouble(source, field);
                Double t = i;
                if (min != null) {
                    Double mi = FieldUtil.getFieldValueByDouble(min, field);
                    t = t < mi ? mi : t;
                }
                if (min != null) {
                    Double ma = FieldUtil.getFieldValueByDouble(max, field);
                    t = t > ma ? ma : t;
                }
                if (!Objects.equals(i, t)) {
                    FieldUtil.setFieldValueByDouble(source, field, t);
                }
            }
        }
    }

    /**
     * 判断是否有任意一个匹配字段的值source大于target的
     */
    public boolean bigger(Object source, Object target) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                Double i = FieldUtil.getFieldValueByDouble(source, field);
                Double t = FieldUtil.getFieldValueByDouble(target, field);
                if (i > t) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 属性除以
     * <p>
     * s1.属性 / ratio
     *
     * @param source1
     * @param ratio
     */
    public void divideValue(Object source1, float ratio) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                /*追加到指定给参数的属性值*/
                FieldUtil.divideValue(source1, field, ratio);
            }
        }
    }

    /**
     * 如果有一个属性不为 0 就 返回 false
     */
    public boolean isValueZero(Object source) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField())) {
                if (FieldUtil.isZore(field, source) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 如果属性小于0.直接设置0;
     */
    public void clearValueZero(Object source) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField().getType()) && FieldUtil.isZore(field, source) < 0) {
                FieldUtil.setFieldValue(source, field, 0);
            }
        }
    }

    /**
     * 所有属性都归0；
     */
    public void clearZero(Object source) {
        for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
            FieldMapping field = entry.getValue();
            if (FieldUtil.isNumberFrom(field.getField().getType())) {
                FieldUtil.setFieldValue(source, field, 0);
            }
        }
    }

    /**
     * 把对象转化成 {@link Map<String, String>}
     *
     * @param object
     * @return
     */
    public String toStringMapJson(Object object) throws Exception {
        Map<String, String> stringStringMap = toStringMap(object);
        return FastJsonUtil.toJson(stringStringMap);
    }

    /**
     * 把对象的字段转化成map键值对
     */
    public Map<String, String> toStringMap(Object object) throws Exception {
        LinkedHashMap<String, String> stringHashMap = new LinkedHashMap<>();
        toStringMap(object, stringHashMap);
        return stringHashMap;
    }

    /**
     * 把对象的字段转化成map键值对
     */
    public void toStringMap(Object object, Map<String, String> retMap) throws Exception {
        toStringCall(object,
                (skey, svalue) -> {
                    retMap.put(skey, svalue);
                }
        );
    }

    /**
     * 原本解析成map对象的，直接回调，减少垃圾产生
     *
     * @param object
     * @param call
     */
    public void toStringCall(Object object, ConsumerE2<String, String> call) throws Exception {
        toMap(object,
                (String fieldName, Object fieldValue) -> {
                    if (fieldValue != null) {
                        String json = null;
                        if (fieldValue instanceof String) {
                            /*字符串类型*/
                            json = String.valueOf(fieldValue);
                        } else if (MessageOrBuilder.class.isAssignableFrom(fieldValue.getClass())) {
                            /* protobuff 消息*/
                            json = ProtobufSerializer.toJson((MessageOrBuilder) fieldValue);
                        } else if (ConvertUtil.isBaseType(fieldValue.getClass())) {
                            /*基础数据类型*/
                            json = FastJsonUtil.toJson(fieldValue);
                        } else {
                            /*非基础数据类型*/
                            json = FastJsonUtil.toJsonWriteType(fieldValue);
                        }
                        call.accept(fieldName, json);
                    }
                }
        );
    }

    /**
     * 把对象的字段转化成map键值对
     */
    public LinkedHashMap<String, Object> toMap(Object object) {
        LinkedHashMap<String, Object> retMap = new LinkedHashMap<>();
        toMap(object, retMap);
        return retMap;
    }

    /**
     * 把对象的字段转化成map键值对
     */
    public void toMap(Object object, Map<String, Object> retMap) {
        toMap(object,
                (String fieldName, Object fieldValue) -> {
                    if (fieldValue != null) {
                        retMap.put(fieldName, fieldValue);
                    }
                }
        );
    }

    public void toMap(Object object, ConsumerE2<String, Object> call) {
        try {
            for (Map.Entry<String, FieldMapping> entry : fieldMap.entrySet()) {
                String key = entry.getKey();
                FieldMapping field = entry.getValue();
                String fieldName = field.getField().getName();
                Object fieldValue = field.getFieldValue(object);
                call.accept(fieldName, fieldValue);
            }
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 根据属性重新设置自动的值
     *
     * @param object
     * @param ov
     */
    public void fromMap(Object object, Map<String, String> ov) {
        if (ov == null || ov.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : ov.entrySet()) {
            String key = entry.getKey();
            String jsonStr = entry.getValue();
            try {
                FieldMapping fv = fieldMap.get(key);
                if (fv == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("获取属性时，" + object.getClass() + " 找不到要赋值的属性：" + key);
                    }
                    continue;
                }
                Object jsonValue = jsonStr;
                if (Message.class.isAssignableFrom(fv.getField().getType())) {
                    /*基础数据类型*/
                    jsonValue = ProtobufSerializer.parse4Json(jsonStr, fv.getField().getType());
                } else if (Message.Builder.class.isAssignableFrom(fv.getField().getType())) {
                    /*基础数据类型*/
                    jsonValue = ProtobufSerializer.parse4Json(jsonStr, fv.getField().getType());
                } else if (!String.class.isAssignableFrom(fv.getField().getType())) {
                    /*基础数据类型*/
                    jsonValue = FastJsonUtil.parse(jsonStr, fv.jsonFieldType());
                }
                fv.setFieldValue(object, jsonValue);
            } catch (Throwable e) {
                throw Throw.as("值=" + jsonStr, e);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringAppend = new StringBuilder();
        stringAppend.append("class=").append(this.clazz.getName()).append("\n");

        for (FieldMapping fieldMapping : fieldMap.values()) {
            stringAppend.append("field = ").append(fieldMapping.getField().getName()).append(", ");
            stringAppend.append("setter = ");
            if (fieldMapping.getSetMethod() == null) {
                stringAppend.append("null");
            } else {
                stringAppend.append(fieldMapping.getSetMethod().getName()).append("(").append(fieldMapping.getField().getType().getName()).append(")");
            }
            stringAppend.append(", ");
            stringAppend.append("getter = ");
            if (fieldMapping.getGetMethod() == null) {
                stringAppend.append("null");
            } else {
                stringAppend.append(fieldMapping.getGetMethod().getName()).append("()");
            }
            stringAppend.append(", ").append("\n");
        }
        return stringAppend.toString();
    }

}
