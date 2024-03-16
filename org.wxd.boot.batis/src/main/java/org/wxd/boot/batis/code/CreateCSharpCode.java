package org.wxd.boot.batis.code;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.io.FileWriteUtil;
import org.wxd.boot.core.str.TemplatePack;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.EntityTable;
import org.wxd.boot.core.append.StreamWriter;
import org.wxd.boot.core.str.StringUtil;

import java.io.File;
import java.io.Serializable;

/**
 * C#代码
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-18 15:17
 **/
@Slf4j
public class CreateCSharpCode implements Serializable, ICreateCode {

    @Override
    public CodeLan getCodeLan() {
        return CodeLan.CSharp;
    }

    @Override
    public void createCodeFile(TemplatePack templatePack, EntityTable entityTable, String savePath, String packageName) {
        if (!savePath.endsWith("/")) {
            savePath += "/";
        }

        String tmpPath = savePath + packageName.replace(".", "/") + "/";

        File file;
        {
            file = new File(tmpPath + "Bean/Mapping/" + entityTable.getCodeClassName() + "Mapping.cs");
            final byte[] codeBean = createCodeMapping(entityTable, packageName);
            FileWriteUtil.writeBytes(file, codeBean);
            log.info("生成 Bean 文件：" + entityTable.getTableComment() + ", " + entityTable.getTableName() + ", " + FileUtil.getCanonicalPath(file));
        }
        {
            file = new File(tmpPath + "Bean/" + entityTable.getCodeClassName() + "Bean.cs");
            if (!file.exists()) {
                final byte[] codeBean = createCodeBean(entityTable, packageName);
                FileWriteUtil.writeBytes(file, codeBean);
                log.info("生成 映射 文件：" + entityTable.getTableComment() + ", " + entityTable.getTableName() + ", " + FileUtil.getCanonicalPath(file));
            }
        }
        {
            file = new File(tmpPath + "Factory/" + entityTable.getCodeClassName() + "Factory.cs");
            if (!file.exists()) {
                final byte[] codeBean = createCodeFactory(entityTable, packageName);
                FileWriteUtil.writeBytes(file, codeBean);
                log.info("生成 Factory 文件：" + entityTable.getTableComment() + ", " + entityTable.getTableName() + ", " + FileUtil.getCanonicalPath(file));
            }
        }
    }

    protected byte[] createCodeMapping(EntityTable entityTable, String packageName) {
        StreamWriter streamWriter = new StreamWriter();
        streamWriter
                .write("using System;\n")
                .write("using System.Collections.Generic;\n")
                .write("using System.Linq;\n")
                .write("using System.Text;\n")
                .write("using System.Threading.Tasks;\n")
                .write("\n")
                .write("namespace ").write(packageName).write(".Bean.Mapping").writeLn()
                .write("{").writeLn()
                .writeLn()
                .write("    /// <summary>").writeLn()
                .write("    /// ").write(entityTable.getTableComment()).writeLn()
                .write("    /// </summary>").writeLn()
                .write("    public class ").write(entityTable.getCodeClassName()).write("Mapping").writeLn()
                .write("    {").writeLn()
                .writeLn();

        for (EntityField column : entityTable.getColumnMap().values()) {
            streamWriter
                    .write("        /// <summary>").writeLn()
                    .write("        /// ").write(column.getColumnComment()).writeLn()
                    .write("        /// </summary>").writeLn()
                    .write("        public ");

            String fieldTypeString = column.getFieldTypeString();

            if (fieldTypeString.toLowerCase().startsWith("boolean")) {
                fieldTypeString.replace("Boolean", "bool");
                fieldTypeString.replace("boolean", "bool");
            }

            streamWriter.write(fieldTypeString);

            streamWriter
                    .write(" ").write(StringUtil.upperFirst(column.getFieldName())).write(" { get; set; }").writeLn();
        }

        streamWriter
                .writeLn()
                .write("    }\n")
                .write("}");
        return streamWriter.toBytes();
    }

    protected byte[] createCodeBean(EntityTable entityTable, String packageName) {
        StreamWriter streamWriter = new StreamWriter();
        streamWriter
                .write("using System;\n")
                .write("using System.Collections.Generic;\n")
                .write("using System.Linq;\n")
                .write("using System.Text;\n")
                .write("using System.Threading.Tasks;\n")
                .write("using ").write(packageName).write(".Bean.Mapping;").writeLn()
                .write("\n")
                .write("namespace ").write(packageName).write(".Bean").writeLn()
                .write("{").writeLn()
                .writeLn()
                .write("    /// <summary>").writeLn()
                .write("    /// ").write(entityTable.getTableComment()).writeLn()
                .write("    /// </summary>").writeLn()
                .write("    public class ").write(entityTable.getCodeClassName()).write("Bean : ").write(entityTable.getCodeClassName()).write("Mapping").writeLn()
                .write("    {").writeLn()
                .writeLn()
                .write("    }\n")
                .write("}");
        return streamWriter.toBytes();
    }

    protected byte[] createCodeFactory(EntityTable entityTable, String packageName) {
        StreamWriter streamWriter = new StreamWriter();
        final EntityField dataColumnKey = entityTable.getDataColumnKey();
        final String keyFieldName = StringUtil.upperFirst(dataColumnKey.getFieldName());
        final String KeyFieldTypeString = dataColumnKey.getFieldTypeString();
        streamWriter
                .write("using System;\n")
                .write("using System.Collections.Generic;\n")
                .write("using System.IO;\n")
                .write("using System.Linq;\n")
                .write("using System.Text;\n")
                .write("using System.Threading.Tasks;\n")
                .write("using ").write(packageName).write(".Bean;").writeLn()
                .write("\n")
                .write("namespace ").write(packageName).write(".Factory").writeLn()
                .write("{").writeLn()
                .writeLn()
                .write("    /// <summary>").writeLn()
                .write("    /// ").write(entityTable.getTableComment()).writeLn()
                .write("    /// </summary>").writeLn()
                .write("    public class ").write(entityTable.getCodeClassName()).write("Factory").writeLn()
                .write("    {").writeLn()
                .write("        public ").write(entityTable.getCodeClassName()).write("Factory()").writeLn()
                .write("        {").writeLn()
                .write("            string jsonString = File.ReadAllText(\"config_json/").write(entityTable.getTableName()).write(".json\");").writeLn()
                .write("            Beans = Newtonsoft.Json.JsonConvert.DeserializeObject<List<").write(entityTable.getCodeClassName()).write("Bean>>(jsonString);").writeLn()
                .write("            foreach (var bean in Beans)").writeLn()
                .write("            {").writeLn()
                .write("                beanMap[bean.").write(keyFieldName).write("] = bean;").writeLn()
                .write("            }").writeLn()
                .write("        }").writeLn()
                .write("").writeLn()
                .write("        public List<").write(entityTable.getCodeClassName()).write("Bean> Beans { get; set; }").writeLn()
                .write("").writeLn()
                .write("").writeLn()
                .write("        private Dictionary<").write(KeyFieldTypeString).write(", ").write(entityTable.getCodeClassName()).write("Bean> beanMap = new Dictionary<").write(KeyFieldTypeString).write(", ").write(entityTable.getCodeClassName()).write("Bean>();").writeLn()
                .write("").writeLn()
                .write("        public Dictionary<").write(KeyFieldTypeString).write(", ").write(entityTable.getCodeClassName()).write("Bean> BeanMap").writeLn()
                .write("        {").writeLn()
                .write("            get { return beanMap; }").writeLn()
                .write("            set { beanMap = value; }").writeLn()
                .write("        }").writeLn()
                .write("").writeLn()
                .write("    }").writeLn()
                .write("}");
        return streamWriter.toBytes();
    }
}
