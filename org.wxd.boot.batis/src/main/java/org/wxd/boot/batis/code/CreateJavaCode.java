package org.wxd.boot.batis.code;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.io.TemplatePack;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.EntityTable;
import org.wxd.boot.batis.struct.DataChecked;
import org.wxd.boot.batis.struct.DbBean;
import org.wxd.boot.batis.struct.DbColumn;
import org.wxd.boot.batis.struct.DbTable;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.field.ClassMapping;
import org.wxd.boot.field.ClassWrapper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-18 00:01
 **/
@Slf4j
public class CreateJavaCode implements Serializable, ICreateCode {

    @Override
    public CodeLan getCodeLan() {
        return CodeLan.Java;
    }

    @Override
    public void createCodeFile(TemplatePack templatePack, EntityTable entityTable, String savePath, String packageName) {
        if (!savePath.endsWith("/")) {
            savePath += "/";
        }
        String tmpPath = savePath + packageName.replace(".", "/") + "/";

        ObjMap parse = new ObjMap();
        parse.put("packageName", "po");
        parse.put("tableName", entityTable.getTableName());
        parse.put("tableComment", entityTable.getTableComment());
        parse.put("codeClassName", entityTable.getCodeClassName());
        ArrayList<Map<String, Object>> columns = new ArrayList<>();
        for (EntityField field : entityTable.getColumnMap().values()) {
            ClassMapping wrapper = ClassWrapper.wrapper(field.getClass());
            Map<String, Object> column = wrapper.toMap(field);
            column.put("fieldName", field.getFieldName());
            columns.add(column);
        }
        parse.put("columns", columns);

//        StreamBuilder stringAppend = new StreamBuilder(1024);

        File file;
        {
            file = new File(tmpPath + "bean/mapping/" + entityTable.getCodeClassName() + "Mapping.java");
            templatePack.ftl2File("bean-mapping.ftl", parse, file.getPath());
//            createCodeMapping(stringAppend, entityTable, packageName);
//            FileWriteUtil.writeBytes(file, stringAppend.toBytes());
            log.info("生成 映射 文件：" + entityTable.getTableComment() + ", " + entityTable.getTableName() + ", " + FileUtil.getCanonicalPath(file));
        }
        file = new File(tmpPath + "bean/" + entityTable.getCodeClassName() + "Bean.java");
        if (!file.exists()) {
            templatePack.ftl2File("bean.ftl", parse, file.getPath());
//            stringAppend.clear();
//            createCodeBean(stringAppend, entityTable, packageName);
//            FileWriteUtil.writeBytes(file, stringAppend.toBytes());
            log.info("生成 扩展 文件：" + entityTable.getTableComment() + ", " + entityTable.getTableName() + ", " + FileUtil.getCanonicalPath(file));
        }

        file = new File(tmpPath + "factory/" + entityTable.getCodeClassName() + "Factory.java");
        if (!file.exists()) {
            templatePack.ftl2File("factory.ftl", parse, file.getPath());
//            stringAppend.clear();
//            createCodeFactory(stringAppend, entityTable, packageName);
//            FileWriteUtil.writeBytes(file, stringAppend.toBytes());
            log.info("生成 工厂 文件：" + entityTable.getTableComment() + ", " + entityTable.getLogTableName() + "Factory, " + FileUtil.getCanonicalPath(file));
        }
    }

    public void createCodeMapping(StreamBuilder stringAppend, EntityTable entityTable, String packageName) {
        stringAppend.append("package ").append(packageName).append(".bean.mapping").append(";").append("\n");
        stringAppend.append("\n");
        stringAppend.append("\n");
        stringAppend.append("import ").append(DbColumn.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(DataChecked.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(DbTable.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(Getter.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(Setter.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(Accessors.class.getName()).append(";").append("\n");
        stringAppend.append("\n");
        stringAppend.append("import java.io.Serializable;").append("\n");
        stringAppend.append("\n");
        stringAppend.append("\n");
        stringAppend.append("/**").append("\n");
        stringAppend.append(" * excel 构建").append(entityTable.getTableComment()).append("\n");
        stringAppend.append(" *").append("\n");
        stringAppend.append(" * @author: Troy.Chen(無心道, 15388152619)").append("\n");
        stringAppend.append(" * @version: 2021/01/14 09:50").append("\n");
        stringAppend.append(" **/").append("\n");
        stringAppend.append("@Getter").append("\n");
        stringAppend.append("@Setter").append("\n");
        stringAppend.append("@Accessors(chain = true)").append("\n");
        stringAppend.append("@").append(DbTable.class.getSimpleName()).append("(mappedSuperclass = true, name = \"").append(entityTable.getTableName()).append("\", comment = \"file = ").append(entityTable.getTableComment()).append("\")").append("\n");
        stringAppend.append("public abstract class ").append(entityTable.getCodeClassName()).append("Mapping implements ").append(DataChecked.class.getSimpleName()).append(", Serializable {").append("\n");
        stringAppend.append("\n");

        for (EntityField column : entityTable.getColumnMap().values()) {
            stringAppend.append("    ").append("/**").append(column.getColumnComment()).append(" */").append("\n");
            stringAppend.append("    ").append("@").append(DbColumn.class.getSimpleName()).append("(name = \"").append(column.getColumnName()).append("\"");
            if (column.isColumnKey()) {
                stringAppend.append(", key = true");
            }
            stringAppend.append(")").append("\n");
            stringAppend.append("    ").append("protected ").append(column.getFieldTypeString()).append(" ").append(column.getFieldName()).append(";").append("\n");
        }
        stringAppend.append("\n");
        stringAppend.append("}").append("\n");
    }

    /**
     * 构建扩展文件
     *
     * @param stringAppend
     * @param entityTable
     * @param packageName
     */
    public void createCodeBean(StreamBuilder stringAppend, EntityTable entityTable, String packageName) {
        stringAppend.append("package ").append(packageName).append(".bean;").append("\n");
        stringAppend.append("\n");
        stringAppend.append("\n");
        stringAppend.append("import ").append(packageName).append(".bean.mapping.").append(entityTable.getCodeClassName()).append("Mapping;").append("\n");
        stringAppend.append("import ").append(DbColumn.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(DbTable.class.getName()).append(";").append("\n");
        stringAppend.append("import ").append(Getter.class.getName()).append(";").append("\n");
        stringAppend.append("\n");
        stringAppend.append("import java.io.Serializable;").append("\n");
        stringAppend.append("\n");
        stringAppend.append("\n");
        stringAppend.append("/**").append("\n");
        stringAppend.append(" * excel 构建").append(entityTable.getTableComment()).append("\n");
        stringAppend.append(" *").append("\n");
        stringAppend.append(" * @author: Troy.Chen(無心道, 15388152619)").append("\n");
        stringAppend.append(" * @version: 2021/01/14 09:50").append("\n");
        stringAppend.append(" **/").append("\n");
        stringAppend.append("@Getter").append("\n");
        stringAppend.append("@").append(DbTable.class.getSimpleName()).append("(name = \"").append(entityTable.getTableName()).append("\", comment = \"file = ").append(entityTable.getTableComment()).append("\")").append("\n");
        stringAppend.append("public class ").append(entityTable.getCodeClassName()).append("Bean extends ").append(entityTable.getCodeClassName()).append("Mapping implements Serializable {").append("\n");
        stringAppend.append("\n");

        stringAppend.append("}").append("\n");
    }

    /**
     * 构建扩展文件
     *
     * @param stringAppend
     * @param entityTable
     * @param packageName
     */
    public void createCodeFactory(StreamBuilder stringAppend, EntityTable entityTable, String packageName) {
        stringAppend.append("package ").append(packageName).append(".factory").append(";").append("\n");
        stringAppend.append("\n");
        stringAppend.append("\n");
        stringAppend.append("import ").append(packageName).append(".bean.").append(entityTable.getCodeClassName()).append("Bean;").append("\n");
        stringAppend.append("import ").append(DbBean.class.getName()).append(";").append("\n");
        stringAppend.append("\n");
        stringAppend.append("import java.io.Serializable;").append("\n");
        stringAppend.append("import ").append(Getter.class.getName()).append(";").append("\n");
        stringAppend.append("\n");
        stringAppend.append("\n");
        stringAppend.append("/**").append("\n");
        stringAppend.append(" * excel 构建").append(entityTable.getTableComment()).append("\n");
        stringAppend.append(" *").append("\n");
        stringAppend.append(" * @author: Troy.Chen(無心道, 15388152619)").append("\n");
        stringAppend.append(" * @version: 2021/01/14 09:50").append("\n");
        stringAppend.append(" **/").append("\n");
        stringAppend.append("@Getter").append("\n");
        stringAppend.append("public class ").append(entityTable.getCodeClassName()).append("Factory extends ").append(DbBean.class.getSimpleName()).append("<" + entityTable.getCodeClassName() + "Bean>").append(" implements Serializable {").append("\n");
        stringAppend.append("\n");

        stringAppend.append("}").append("\n");
    }

}
