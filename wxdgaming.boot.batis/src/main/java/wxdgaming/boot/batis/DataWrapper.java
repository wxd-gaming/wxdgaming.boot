package wxdgaming.boot.batis;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.function.SLFunction0;
import wxdgaming.boot.agent.function.SLFunction1;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.enums.ColumnType;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.field.ClassMapping;
import wxdgaming.boot.core.field.FieldMapping;
import wxdgaming.boot.core.lang.ConvertUtil;
import wxdgaming.boot.core.lang.LoggerException;
import wxdgaming.boot.core.str.PatternStringUtil;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 数据映射装填器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-01-21 10:13
 **/
@Slf4j
public abstract class DataWrapper<DM extends EntityTable>
        implements
        Serializable, ToDbValue, FromDbValue {

    /** 因为无法处理final字段，会忽略，false 不增加警告 */
    public static boolean Final_Warning = true;
    /** 过滤最终字段 */
    public static boolean Filter_Transient = false;

    /** key 数据库表名 */
    private final ConcurrentSkipListMap<String, DM> dataTableMap = new ConcurrentSkipListMap<>();

    public DM dataTable(String tableName) {
        return dataTableMap.get(tableName);
    }

    public Collection<DM> dataTables() {
        return dataTableMap.values();
    }

    public Map<String, DM> getDataTableMap() {
        return dataTableMap;
    }

    public final boolean checkClazz(Class<?> clazz) {
        return checkClazz(clazz, false);
    }

    /**
     * 检查是否可以处理的类型，创建表的时候
     * <p>
     * 枚举，接口，匿名类，注解类，静态类，抽象类，忽略
     *
     * @param clazz
     * @return
     */
    public final boolean checkClazz(Class<?> clazz, boolean findSuperClass) {
        if (!ReflectContext.checked(clazz)) {
            return false;
        }
        DbTable annotation = AnnUtil.ann(clazz, DbTable.class, findSuperClass);
        if (annotation != null) {
            if (annotation.mappedSuperclass()) {
                return false;
            }
            if (annotation.alligator()) {
                return false;
            }
        }
        return true;
    }

    public final String tableName(Object model) {
        Class<?> clazz = model.getClass();
        if (!checkClazz(clazz)) {
            throw new UnsupportedOperationException("无法处理的类型：" + clazz + " ，请先调用 checkClazz() 方法检查类型");
        }
        return tableName(clazz);
    }

    public final String tableName(Class<?> clazz) {
        if (!checkClazz(clazz)) {
            throw new UnsupportedOperationException("无法处理的类型" + clazz + " ，请先调用 checkClazz() 方法检查类型");
        }
        // 判断指定类型的注释是否存在于此元素上
        DbTable annotation = AnnUtil.ann(clazz, DbTable.class, true);
        // 拿到对应的表格注解类型
        if (annotation == null || StringUtil.emptyOrNull(annotation.name())) {
            return clazz.getSimpleName().trim().toLowerCase();// 不存在就不需要获取其表名
        } else {
            return annotation.name().trim().toLowerCase();// 返回注解中的值，也就是表名
        }
    }

    public <R> String columnName(SLFunction0<R> columnFn) throws Exception {
        final Field field = columnFn.ofField();
        DbColumn dbColumn = AnnUtil.ann(field, DbColumn.class);
        return columnName(field, dbColumn);
    }

    public <T1, R> String columnName(SLFunction1<T1, R> columnFn) throws Exception {
        final Field field = columnFn.ofField();
        DbColumn dbColumn = AnnUtil.ann(field, DbColumn.class);
        return columnName(field, dbColumn);
    }

    public String columnName(Field field, DbColumn dbColumn) {
        String fieldName = field.getName();
        if (dbColumn != null) {
            if (StringUtil.notEmptyOrNull(dbColumn.name()))
                fieldName = dbColumn.name();
        }
        return fieldName;
    }

    /**
     * 数据映射类装填
     * 实体模型可以重写表名
     */
    public <R extends DM> R asEntityTable(Object model) {
        DM entityTable = null;
        if (checkClazz(model.getClass())) {
            entityTable = asEntityTable(model.getClass());
        }
        return (R) entityTable;
    }

    /**
     * 数据映射类装填
     */
    public <R extends DM> R asEntityTable(Class<?> clazz) {
        return asEntityTable(clazz, true);
    }

    public <R extends DM> R asEntityTable(Class<?> clazz, boolean checkColumnKey) {
        DbTable dbTable = AnnUtil.ann(clazz, DbTable.class, true);
        final String tableName = tableName(clazz);
        if (dbTable != null && dbTable.splitTable() > 1) {
            for (int i = 0; i < dbTable.splitTable(); i++) {
                asEntityTable(tableName + "_" + i, clazz, dbTable.comment() + "-" + (i + 1), checkColumnKey);
            }
        }
        return asEntityTable(tableName, clazz, null, checkColumnKey);
    }

    /**
     * 数据映射类装填
     */
    public final <R extends DM> R asEntityTable(String tableName, Class<?> clazz, String tableComment, boolean checkColumnKey) {
        return (R) dataTableMap.computeIfAbsent(tableName, k -> buildCreateEntityTable(tableName, clazz, tableComment, checkColumnKey));
    }

    public abstract DM createEntityTable();

    private DM buildCreateEntityTable(String tableName, Class<?> clazz, String tableComment, boolean checkColumnKey) {
        if (!checkClazz(clazz)) {
            return null;
        }
        DM entityTable = createEntityTable();
        entityTable
                .setTableName(tableName)
                .setEntityClass(clazz);

        DbTable dbTable = AnnUtil.ann(clazz, DbTable.class, true);

        if (dbTable != null) {
            entityTable.setTableComment(dbTable.comment());
            entityTable.setSplitNumber(dbTable.splitTable());
        }

        if (StringUtil.notEmptyOrNull(tableComment)) {
            entityTable.setTableComment(tableComment);
        }

        ClassMapping classMapping = new ClassMapping(clazz, false, false, null);
        Map<String, FieldMapping> fields = classMapping.getFieldMap();

        for (FieldMapping fieldMapping : fields.values()) {
            Field field = fieldMapping.getField();

            // 忽略字段，静态字段，最终字段，不会书写到数据库
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            DbColumn dbColumn = AnnUtil.ann(field, DbColumn.class);
            if (dbColumn != null && dbColumn.alligator()) {
                /*手动注册的忽律字段不考虑*/
                continue;
            }

            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            if (Modifier.isFinal(field.getModifiers())) {
                if (/*ConvertUtil.isBaseType(field.getType())*/
                        !Map.class.isAssignableFrom(field.getType())
                        && !List.class.isAssignableFrom(field.getType())
                        && !Set.class.isAssignableFrom(field.getType())
                ) {
                    if (Final_Warning) {
                        log.warn(
                                "警告 无法处理 final 字段：" + field.getName()
                                + " 实体类：" + entityTable.getLogTableName()
                        );
                    }
                    continue;
                }
            }

            EntityField entityField = new EntityField();
            entityField.copy(fieldMapping);
            entityField.setColumnName(columnName(field, dbColumn));
            entityField.setColumnComment("");
            if (dbColumn != null) {

                if (StringUtil.notEmptyOrNull(dbColumn.comment())) {
                    entityField.setColumnComment(dbColumn.comment().trim());
                }

                if (dbColumn.columnType() != ColumnType.None) {
                    entityField.setColumnType(dbColumn.columnType());
                }

                entityField.setDefaultValue(dbColumn.defaultValue());

                entityField.setColumnKey(dbColumn.key());
                /*是否可以 null */
                entityField.setColumnNullAble(dbColumn.nullable());
                /*索引*/
                entityField.setColumnIndex(dbColumn.index());
                /*设置索引类型*/
                entityField.setMysqlIndexType(dbColumn.mysqlIndexType());

                if (dbColumn.length() > 0) {
                    entityField.setColumnLength(dbColumn.length());
                }

            }

            if (entityField.isColumnKey()) {
                if (entityTable.getDataColumnKey() != null) {
                    throw new RuntimeException("不支持双主键："
                                               + entityTable.getLogTableName()
                                               + ", " + entityTable.getDataColumnKey().getFieldName()
                                               + ", " + entityField.getFieldName());
                }
                entityTable.setDataColumnKey(entityField);
            }

            if (entityField.getColumnType() == ColumnType.None) {
                String fieldTypeName = fieldMapping.getFieldType().getName();
                buildColumnType(entityField, fieldTypeName, false);
            }
            if (entityField.getColumnType() == ColumnType.None) {
                entityField.setColumnType(ColumnType.Json);
            }
            entityField.setFieldType(fieldMapping.getFieldType());

            if (entityField.getGetMethod() == null && entityField.getSetMethod() == null) {
                log.debug("实体类：" + entityTable.getLogTableName() + " 字段：" + field.getName() + " 没有 set And get Method", new LoggerException());
            } else if (entityField.getGetMethod() == null) {
                log.debug("实体类：" + entityTable.getLogTableName() + " 字段：" + field.getName() + " 没有 get Method", new LoggerException());
            } else if (entityField.getSetMethod() == null && !entityField.isFinalField()) {
                log.debug("实体类：" + entityTable.getLogTableName() + " 字段：" + field.getName() + " 没有 set Method", new LoggerException());
            }

            entityTable.getColumnMap().put(entityField.getColumnName(), entityField);
        }

        if (checkColumnKey && entityTable.getDataColumnKey() == null) {
            throw new RuntimeException("实体类：" + clazz.getName() + ", 缺少主键！！！");
        }

        return entityTable;
    }

    public void buildColumnType(EntityField entityField, String fieldTypeName, boolean client) {
        final String typeString = PatternStringUtil.typeString(fieldTypeName);
        switch (typeString.toLowerCase()) {
            case "bool":
            case "boolean":
                entityField.setColumnType(ColumnType.Bool);
                entityField.setFieldType(boolean.class);
                break;
            case "java.lang.boolean":
                entityField.setColumnType(ColumnType.Bool);
                entityField.setFieldType(Boolean.class);
                break;
            case "byte":
                entityField.setColumnType(ColumnType.Byte);
                entityField.setFieldType(byte.class);
                break;
            case "java.lang.byte":
                entityField.setColumnType(ColumnType.Byte);
                entityField.setFieldType(Byte.class);
                break;
            case "short":
                entityField.setColumnType(ColumnType.Short);
                entityField.setFieldType(short.class);
                break;
            case "java.lang.short":
                entityField.setColumnType(ColumnType.Short);
                entityField.setFieldType(Short.class);
                break;
            case "int":
                entityField.setColumnType(ColumnType.Int);
                entityField.setFieldType(int.class);
                break;
            case "java.lang.integer":
                entityField.setColumnType(ColumnType.Int);
                entityField.setFieldType(Integer.class);
                break;
            case "long":
                entityField.setColumnType(ColumnType.Long);
                entityField.setFieldType(long.class);
                break;
            case "java.lang.long":
                entityField.setColumnType(ColumnType.Long);
                entityField.setFieldType(Long.class);
                break;
            case "float":
                entityField.setColumnType(ColumnType.Float);
                entityField.setFieldType(float.class);
                break;
            case "java.lang.float":
                entityField.setColumnType(ColumnType.Float);
                entityField.setFieldType(Float.class);
                break;
            case "double":
                entityField.setColumnType(ColumnType.Double);
                entityField.setFieldType(double.class);
                break;
            case "java.lang.double":
                entityField.setColumnType(ColumnType.Double);
                entityField.setFieldType(Double.class);
                break;
            case "java.util.date":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Date.class);
                break;
            case "java.math.biginteger":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(BigInteger.class);
                break;
            case "java.math.bigdecimal":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(BigDecimal.class);
                break;
            case "string":
            case "java.lang.string":
                if (entityField.getColumnLength() < 5000) {
                    entityField.setColumnType(ColumnType.Varchar);
                } else if (entityField.getColumnLength() < 10000) {
                    entityField.setColumnType(ColumnType.Text);
                } else {
                    entityField.setColumnType(ColumnType.LongText);
                }
                entityField.setFieldType(String.class);
                break;
            case "string[]":
            case "java.lang.string[]":
                /*String[]*/
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(String[].class);
                entityField.setFieldTypeString("String[]");
                break;
            case "list<string>":
                /*String[]*/
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<String>");
                break;
            case "string[][]":
            case "java.lang.string[][]":
                /*String[][]*/
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(String[][].class);
                entityField.setFieldTypeString("String[][]");
                break;
            case "bool[]":
            case "boolean[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(boolean[].class);
                entityField.setFieldTypeString("boolean[]");
                break;
            case "java.lang.boolean[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Boolean[].class);
                entityField.setFieldTypeString("Boolean[]");
                break;
            case "list<bool>":
            case "list<boolean>":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<Boolean>");
                break;
            case "bool[][]":
            case "boolean[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(boolean[][].class);
                entityField.setFieldTypeString("boolean[][]");
                break;
            case "java.lang.boolean[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Boolean[][].class);
                entityField.setFieldTypeString("Boolean[][]");
                break;
            case "byte[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(byte[].class);
                entityField.setFieldTypeString("byte[]");
                break;
            case "list<byte>":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<Byte>");
                break;
            case "byte[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(byte[][].class);
                entityField.setFieldTypeString("byte[][]");
                break;
            case "java.lang.byte[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Byte[].class);
                entityField.setFieldTypeString("Byte[]");
                break;
            case "java.lang.byte[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Byte[][].class);
                entityField.setFieldTypeString("Byte[][]");
                break;
            case "int[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(int[].class);
                entityField.setFieldTypeString("int[]");
                break;
            case "java.lang.integer[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Integer[].class);
                entityField.setFieldTypeString("Integer[]");
                break;
            case "list":
            case "list<int>":
            case "list<integer>":
            case "arraylist<int>":
            case "arraylist<integer>":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<Integer>");
                break;
            case "int[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(int[][].class);
                entityField.setFieldTypeString("int[][]");
                break;
            case "java.lang.integer[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Integer[][].class);
                entityField.setFieldTypeString("Integer[][]");
                break;
            case "long[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(long[].class);
                entityField.setFieldTypeString("long[]");
                break;
            case "java.lang.long[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Long[].class);
                entityField.setFieldTypeString("Long[]");
                break;
            case "list<long>":
            case "arraylist<long>":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<Long>");
                break;
            case "[[j":
            case "long[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(long[][].class);
                entityField.setFieldTypeString("long[][]");
                break;
            case "java.lang.long[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Long[][].class);
                entityField.setFieldTypeString("Long[][]");
                break;
            case "float[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(float[].class);
                entityField.setFieldTypeString("float[]");
                break;
            case "java.lang.float[]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Float[].class);
                entityField.setFieldTypeString("Float[]");
                break;
            case "list<float>":
            case "arraylist<float>":
            case "list<java.lang.float>":
            case "arraylist<java.lang.float>":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<Float>");
                break;
            case "list<float[]>":
            case "arraylist<float[]>":
            case "list<java.lang.float[]>":
            case "arraylist<java.lang.float[]>":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(ArrayList.class);
                entityField.setFieldTypeString("ArrayList<Float[]>");
                break;
            case "float[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(float[][].class);
                entityField.setFieldTypeString("float[][]");
                break;
            case "java.lang.float[][]":
                entityField.setColumnType(ColumnType.Json);
                entityField.setFieldType(Float[][].class);
                entityField.setFieldTypeString("Float[][]");
                break;
            default:
                if (client) {
                    entityField.setColumnType(ColumnType.Json);
                    entityField.setFieldType(String.class);
                    entityField.setFieldTypeString("String");
                    break;
                } else {
                    entityField.setColumnType(ColumnType.Json);
                    entityField.setFieldType(String.class);
                    try {
                        Class<?> aClass = this.getClass().getClassLoader().loadClass(fieldTypeName);
                        entityField.setFieldType(aClass);
                    } catch (ClassNotFoundException e) {
                        if (fieldTypeName.contains("List<")) {
                            entityField.setFieldType(List.class);
                        } else if (fieldTypeName.contains("Map<")) {
                            entityField.setFieldType(List.class);
                        } else {
                            log.error(entityField.getFieldName() + " - " + fieldTypeName, e);
                        }
                    }
                    entityField.setFieldTypeString(fieldTypeName);
                }
        }
    }

    /** 转化成写入数据库的值 ,是为了避免线程并发问题带来的数据处理异常 */
    public Map<String, Object> toStringDbMap(Object model) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        DM entityTable = asEntityTable(model);
        Map<String, EntityField> columnMap = entityTable.getColumnMap();
        for (EntityField value : columnMap.values()) {
            dataMap.put(value.getColumnName(), toDbValue(value, value.getFieldValue(model)));
        }
        return dataMap;
    }

    /** 转化成写入数据库的值 ,是为了避免线程并发问题带来的数据处理异常 */
    public Map<EntityField, Object> toDbMap(Object model) {
        Map<EntityField, Object> dataMap = new LinkedHashMap<>();
        DM entityTable = asEntityTable(model);
        Map<String, EntityField> columnMap = entityTable.getColumnMap();
        for (EntityField value : columnMap.values()) {
            dataMap.put(value, toDbValue(value, value.getFieldValue(model)));
        }
        return dataMap;
    }

    /**
     * 解析模型的全部数据
     *
     * @param model
     * @return
     */
    public Map<EntityField, Object> toDataMap(Object model) {
        Map<EntityField, Object> dataMap = new LinkedHashMap<>();
        DM entityTable = asEntityTable(model);
        final Map<String, EntityField> columnMap = entityTable.getColumnMap();
        for (EntityField value : columnMap.values()) {
            dataMap.put(value, value.getFieldValue(model));
        }
        return dataMap;
    }

    /**
     * 把对象转换成json格式的map对象
     *
     * @param model
     * @return
     */
    public Map<String, String> toJsonMapString(Object model) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        DM entityTable = asEntityTable(model);
        Map<String, EntityField> columnMap = entityTable.getColumnMap();
        for (EntityField value : columnMap.values()) {
            dataMap.put(value.getColumnName(), FastJsonUtil.toJson(value.getFieldValue(model)));
        }
        return dataMap;
    }

    /**
     * 把对象转换成json格式的map对象
     *
     * @param model
     * @return
     */
    public Map<String, Object> toJsonMap(Object model) {
        Map<String, Object> dataMap = new LinkedHashMap<>();
        DM entityTable = asEntityTable(model);
        Map<String, EntityField> columnMap = entityTable.getColumnMap();
        for (EntityField value : columnMap.values()) {
            dataMap.put(value.getColumnName(), value.getFieldValue(model));
        }
        return dataMap;
    }

    public static void builderString(StreamWriter streamWriter, EntityTable dataMapping) {
        builderString(streamWriter, dataMapping, 50);
    }

    /**
     * 把datastruct结构输出字符结构集
     *
     * @param streamWriter
     * @param dataMapping
     */
    public static void builderString(StreamWriter streamWriter, EntityTable dataMapping, int len) {
        streamWriter.write("解析：").write(dataMapping.getLogTableName()).write("\n");
        streamWriter.write("表名：").write(dataMapping.getTableName()).write("\n");
        Collection<EntityField> columns = dataMapping.getColumns();
        streamWriter.writeRight("-", len * dataMapping.getColumns().size(), '-').write("\n");
        for (EntityField entityField : columns) {
            streamWriter.write("|").writeRight(entityField.getColumnName(), len, ' ');
        }

        streamWriter.write("\n");

        for (EntityField entityField : columns) {
            streamWriter.write("|")
                    .writeRight(entityField.typeName(), len, ' ');
        }

        streamWriter.write("\n");

        for (EntityField entityField : columns) {
            streamWriter.write("|")
                    .writeRight(entityField.checkColumnType().formatString(entityField.getColumnLength()), len, ' ');
        }

        streamWriter.write("\n");

        for (EntityField entityField : columns) {
            streamWriter.write("|").writeRight(entityField.getColumnComment(), len, ' ');
        }

        streamWriter.write("\n");

        streamWriter.writeRight("-", len * columns.size(), '-').write("\n");

        for (LinkedHashMap<EntityField, Object> row : dataMapping.getRows()) {
            streamWriter.write("\n");
            for (Object value : row.values()) {
                if (value == null) {
                    streamWriter.write("|").writeRight("-", len, ' ');
                } else if (ConvertUtil.isBaseType(value.getClass())) {
                    streamWriter.write("|").writeRight(String.valueOf(value), len, ' ');
                } else {
                    streamWriter.write("|").writeRight(FastJsonUtil.toJson(value), len, ' ');
                }
            }
        }

        streamWriter.write("\n");
    }

    /**
     * 强制把0.0替换成0
     *
     * @param obj
     * @return
     */
    public static String stringValueOf(Object obj) {
        String valueOf = String.valueOf(obj);
        if (valueOf.endsWith(".0")) {
            valueOf = valueOf.substring(0, valueOf.length() - 2);
        }
        return valueOf;
    }

}
