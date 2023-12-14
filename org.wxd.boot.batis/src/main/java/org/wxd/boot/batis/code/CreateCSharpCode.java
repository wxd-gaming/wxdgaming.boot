package org.wxd.boot.batis.code;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.io.FileWriteUtil;
import org.wxd.boot.agent.io.TemplatePack;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.EntityTable;
import org.wxd.boot.str.StringUtil;

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
        StreamBuilder streamBuilder = new StreamBuilder();
        streamBuilder
                .append("using System;\n")
                .append("using System.Collections.Generic;\n")
                .append("using System.Linq;\n")
                .append("using System.Text;\n")
                .append("using System.Threading.Tasks;\n")
                .append("\n")
                .append("namespace ").append(packageName).append(".Bean.Mapping").appendLn()
                .append("{").appendLn()
                .appendLn()
                .append("    /// <summary>").appendLn()
                .append("    /// ").append(entityTable.getTableComment()).appendLn()
                .append("    /// </summary>").appendLn()
                .append("    public class ").append(entityTable.getCodeClassName()).append("Mapping").appendLn()
                .append("    {").appendLn()
                .appendLn();

        for (EntityField column : entityTable.getColumnMap().values()) {
            streamBuilder
                    .append("        /// <summary>").appendLn()
                    .append("        /// ").append(column.getColumnComment()).appendLn()
                    .append("        /// </summary>").appendLn()
                    .append("        public ");

            String fieldTypeString = column.getFieldTypeString();

            if (fieldTypeString.toLowerCase().startsWith("boolean")) {
                fieldTypeString.replace("Boolean", "bool");
                fieldTypeString.replace("boolean", "bool");
            }

            streamBuilder.append(fieldTypeString);

            streamBuilder
                    .append(" ").append(StringUtil.upperFirst(column.getFieldName())).append(" { get; set; }").appendLn();
        }

        streamBuilder
                .appendLn()
                .append("    }\n")
                .append("}");
        return streamBuilder.toBytes();
    }

    protected byte[] createCodeBean(EntityTable entityTable, String packageName) {
        StreamBuilder streamBuilder = new StreamBuilder();
        streamBuilder
                .append("using System;\n")
                .append("using System.Collections.Generic;\n")
                .append("using System.Linq;\n")
                .append("using System.Text;\n")
                .append("using System.Threading.Tasks;\n")
                .append("using ").append(packageName).append(".Bean.Mapping;").appendLn()
                .append("\n")
                .append("namespace ").append(packageName).append(".Bean").appendLn()
                .append("{").appendLn()
                .appendLn()
                .append("    /// <summary>").appendLn()
                .append("    /// ").append(entityTable.getTableComment()).appendLn()
                .append("    /// </summary>").appendLn()
                .append("    public class ").append(entityTable.getCodeClassName()).append("Bean : ").append(entityTable.getCodeClassName()).append("Mapping").appendLn()
                .append("    {").appendLn()
                .appendLn()
                .append("    }\n")
                .append("}");
        return streamBuilder.toBytes();
    }

    protected byte[] createCodeFactory(EntityTable entityTable, String packageName) {
        StreamBuilder streamBuilder = new StreamBuilder();
        final EntityField dataColumnKey = entityTable.getDataColumnKey();
        final String keyFieldName = StringUtil.upperFirst(dataColumnKey.getFieldName());
        final String KeyFieldTypeString = dataColumnKey.getFieldTypeString();
        streamBuilder
                .append("using System;\n")
                .append("using System.Collections.Generic;\n")
                .append("using System.IO;\n")
                .append("using System.Linq;\n")
                .append("using System.Text;\n")
                .append("using System.Threading.Tasks;\n")
                .append("using ").append(packageName).append(".Bean;").appendLn()
                .append("\n")
                .append("namespace ").append(packageName).append(".Factory").appendLn()
                .append("{").appendLn()
                .appendLn()
                .append("    /// <summary>").appendLn()
                .append("    /// ").append(entityTable.getTableComment()).appendLn()
                .append("    /// </summary>").appendLn()
                .append("    public class ").append(entityTable.getCodeClassName()).append("Factory").appendLn()
                .append("    {").appendLn()
                .append("        public ").append(entityTable.getCodeClassName()).append("Factory()").appendLn()
                .append("        {").appendLn()
                .append("            string jsonString = File.ReadAllText(\"config_json/").append(entityTable.getTableName()).append(".json\");").appendLn()
                .append("            Beans = Newtonsoft.Json.JsonConvert.DeserializeObject<List<").append(entityTable.getCodeClassName()).append("Bean>>(jsonString);").appendLn()
                .append("            foreach (var bean in Beans)").appendLn()
                .append("            {").appendLn()
                .append("                beanMap[bean.").append(keyFieldName).append("] = bean;").appendLn()
                .append("            }").appendLn()
                .append("        }").appendLn()
                .append("").appendLn()
                .append("        public List<").append(entityTable.getCodeClassName()).append("Bean> Beans { get; set; }").appendLn()
                .append("").appendLn()
                .append("").appendLn()
                .append("        private Dictionary<").append(KeyFieldTypeString).append(", ").append(entityTable.getCodeClassName()).append("Bean> beanMap = new Dictionary<").append(KeyFieldTypeString).append(", ").append(entityTable.getCodeClassName()).append("Bean>();").appendLn()
                .append("").appendLn()
                .append("        public Dictionary<").append(KeyFieldTypeString).append(", ").append(entityTable.getCodeClassName()).append("Bean> BeanMap").appendLn()
                .append("        {").appendLn()
                .append("            get { return beanMap; }").appendLn()
                .append("            set { beanMap = value; }").appendLn()
                .append("        }").appendLn()
                .append("").appendLn()
                .append("    }").appendLn()
                .append("}");
        return streamBuilder.toBytes();
    }
}
