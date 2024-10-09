package wxdgaming.boot.batis.code;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.EntityTable;
import wxdgaming.boot.batis.struct.DataChecked;
import wxdgaming.boot.batis.struct.DbBean;
import wxdgaming.boot.batis.struct.DbColumn;
import wxdgaming.boot.batis.struct.DbTable;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.core.field.ClassMapping;
import wxdgaming.boot.core.field.ClassWrapper;
import wxdgaming.boot.core.str.TemplatePack;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author: wxd-gaming(無心道, 15388152619)
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

        ObjMap parse = new ObjMap();
        parse.put("packageName", packageName);
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

        String tmpPath = savePath + packageName.replace(".", "/") + "/";
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

    public void createCodeMapping(StreamWriter stringAppend, EntityTable entityTable, String packageName) {
        stringAppend.write("package ").write(packageName).write(".bean.mapping").write(";").write("\n");
        stringAppend.write("\n");
        stringAppend.write("\n");
        stringAppend.write("import ").write(DbColumn.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(DataChecked.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(DbTable.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(Getter.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(Setter.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(Accessors.class.getName()).write(";").write("\n");
        stringAppend.write("\n");
        stringAppend.write("import java.io.Serializable;").write("\n");
        stringAppend.write("\n");
        stringAppend.write("\n");
        stringAppend.write("/**").write("\n");
        stringAppend.write(" * excel 构建").write(entityTable.getTableComment()).write("\n");
        stringAppend.write(" *").write("\n");
        stringAppend.write(" * @author: wxd-gaming(無心道, 15388152619)").write("\n");
        stringAppend.write(" * @version: 2021/01/14 09:50").write("\n");
        stringAppend.write(" **/").write("\n");
        stringAppend.write("@Getter").write("\n");
        stringAppend.write("@Setter").write("\n");
        stringAppend.write("@Accessors(chain = true)").write("\n");
        stringAppend.write("@").write(DbTable.class.getSimpleName()).write("(mappedSuperclass = true, name = \"").write(entityTable.getTableName()).write("\", comment = \"file = ").write(entityTable.getTableComment()).write("\")").write("\n");
        stringAppend.write("public abstract class ").write(entityTable.getCodeClassName()).write("Mapping implements ").write(DataChecked.class.getSimpleName()).write(", Serializable {").write("\n");
        stringAppend.write("\n");

        for (EntityField column : entityTable.getColumnMap().values()) {
            stringAppend.write("    ").write("/**").write(column.getColumnComment()).write(" */").write("\n");
            stringAppend.write("    ").write("@").write(DbColumn.class.getSimpleName()).write("(name = \"").write(column.getColumnName()).write("\"");
            if (column.isColumnKey()) {
                stringAppend.write(", key = true");
            }
            stringAppend.write(")").write("\n");
            stringAppend.write("    ").write("protected ").write(column.getFieldTypeString()).write(" ").write(column.getFieldName()).write(";").write("\n");
        }
        stringAppend.write("\n");
        stringAppend.write("}").write("\n");
    }

    /**
     * 构建扩展文件
     *
     * @param stringAppend
     * @param entityTable
     * @param packageName
     */
    public void createCodeBean(StreamWriter stringAppend, EntityTable entityTable, String packageName) {
        stringAppend.write("package ").write(packageName).write(".bean;").write("\n");
        stringAppend.write("\n");
        stringAppend.write("\n");
        stringAppend.write("import ").write(packageName).write(".bean.mapping.").write(entityTable.getCodeClassName()).write("Mapping;").write("\n");
        stringAppend.write("import ").write(DbColumn.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(DbTable.class.getName()).write(";").write("\n");
        stringAppend.write("import ").write(Getter.class.getName()).write(";").write("\n");
        stringAppend.write("\n");
        stringAppend.write("import java.io.Serializable;").write("\n");
        stringAppend.write("\n");
        stringAppend.write("\n");
        stringAppend.write("/**").write("\n");
        stringAppend.write(" * excel 构建").write(entityTable.getTableComment()).write("\n");
        stringAppend.write(" *").write("\n");
        stringAppend.write(" * @author: wxd-gaming(無心道, 15388152619)").write("\n");
        stringAppend.write(" * @version: 2021/01/14 09:50").write("\n");
        stringAppend.write(" **/").write("\n");
        stringAppend.write("@Getter").write("\n");
        stringAppend.write("@").write(DbTable.class.getSimpleName()).write("(name = \"").write(entityTable.getTableName()).write("\", comment = \"file = ").write(entityTable.getTableComment()).write("\")").write("\n");
        stringAppend.write("public class ").write(entityTable.getCodeClassName()).write("Bean extends ").write(entityTable.getCodeClassName()).write("Mapping implements Serializable {").write("\n");
        stringAppend.write("\n");

        stringAppend.write("}").write("\n");
    }

    /**
     * 构建扩展文件
     *
     * @param stringAppend
     * @param entityTable
     * @param packageName
     */
    public void createCodeFactory(StreamWriter stringAppend, EntityTable entityTable, String packageName) {
        stringAppend.write("package ").write(packageName).write(".factory").write(";").write("\n");
        stringAppend.write("\n");
        stringAppend.write("\n");
        stringAppend.write("import ").write(packageName).write(".bean.").write(entityTable.getCodeClassName()).write("Bean;").write("\n");
        stringAppend.write("import ").write(DbBean.class.getName()).write(";").write("\n");
        stringAppend.write("\n");
        stringAppend.write("import java.io.Serializable;").write("\n");
        stringAppend.write("import ").write(Getter.class.getName()).write(";").write("\n");
        stringAppend.write("\n");
        stringAppend.write("\n");
        stringAppend.write("/**").write("\n");
        stringAppend.write(" * excel 构建").write(entityTable.getTableComment()).write("\n");
        stringAppend.write(" *").write("\n");
        stringAppend.write(" * @author: wxd-gaming(無心道, 15388152619)").write("\n");
        stringAppend.write(" * @version: 2021/01/14 09:50").write("\n");
        stringAppend.write(" **/").write("\n");
        stringAppend.write("@Getter").write("\n");
        stringAppend.write("public class ").write(entityTable.getCodeClassName()).write("Factory extends ").write(DbBean.class.getSimpleName()).write("<" + entityTable.getCodeClassName() + "Bean>").write(" implements Serializable {").write("\n");
        stringAppend.write("\n");

        stringAppend.write("}").write("\n");
    }

}
