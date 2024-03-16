package wxdgaming.boot.batis.excel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.core.str.TemplatePack;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.code.CodeLan;
import wxdgaming.boot.batis.text.json.JsonDataWrapper;
import wxdgaming.boot.batis.text.json.JsonEntityTable;
import wxdgaming.boot.core.collection.ObjMap;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 读取excel
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-13 15:06
 **/
public class ExcelRead2Json extends ExcelRead<JsonEntityTable, JsonDataWrapper> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ExcelRead2Json.class);

    public static ExcelRead2Json builder() {
        return new ExcelRead2Json();
    }

    /** 保存 数据文件 文件 */
    private String saveJsonPath;

    @Override
    public JsonDataWrapper getDataWrapper() {
        return JsonDataWrapper.Default;
    }

    @Override
    protected JsonEntityTable createDataStruct() {
        return new JsonEntityTable();
    }

    /**
     * @param haveExtends
     * @return
     * @throws Exception
     */
    @Override
    public ExcelRead2Json loadExcel(String... haveExtends) {
        super.loadExcel(haveExtends);
        return this;
    }

    @Override
    public ExcelRead2Json showData() {
        super.showData();
        return this;
    }

    @Override public ExcelRead2Json createCode(CodeLan codeLan, String saveCodePath, String savePackageName) {
        super.createCode(codeLan, saveCodePath, savePackageName);
        return this;
    }

    @Override
    public ExcelRead2Json createCode(TemplatePack templatePack, String houZhui, String saveCodePath, String savePackageName) {
        super.createCode(templatePack, houZhui, saveCodePath, savePackageName);
        return this;
    }

    public ExcelRead2Json saveData(String saveJsonPath) {
        this.saveJsonPath = saveJsonPath;
        this.saveData();
        return this;
    }

    @Override
    public ExcelRead2Json saveData(JsonEntityTable entityTable) {
        if (saveJsonPath != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[").append("\n");
            LinkedList<LinkedHashMap<EntityField, Object>> rows = entityTable.getRows();
            for (LinkedHashMap<EntityField, Object> row : rows) {
                ObjMap dataRow = new ObjMap();
                for (Map.Entry<EntityField, Object> entry : row.entrySet()) {
                    dataRow.put(entry.getKey().getColumnName(), entry.getValue());
                }
                if (stringBuilder.length() > 2) {
                    stringBuilder.append(",\n");
                }
                stringBuilder.append("    ").append(dataRow.toString());
            }
            stringBuilder.append("\n]");
            FileWriteUtil.writeString(
                    saveJsonPath + "/" + entityTable.getTableName() + ".json",
                    stringBuilder.toString()
            );
        }
        return this;
    }

    @Override
    public ExcelRead2Json excelPaths(String... excelPaths) {
        super.excelPaths(excelPaths);
        return this;
    }

    /**
     * 设置读取目录
     *
     * @param loop
     * @param excelPaths
     */
    @Override
    public ExcelRead2Json excelPaths(boolean loop, String... excelPaths) {
        super.excelPaths(loop, excelPaths);
        return this;
    }

    /**
     * 行号从0开始
     *
     * @param dataNameRowIndex   映射字段名行号
     * @param dataTypeRowIndex   映射字段数据类型行号
     * @param dataDescRowIndex   映射字段描述行号
     * @param dataExtendRowIndex 扩展内容行号 通常 server, client, all, no
     * @param dataRowIndex       数据行号
     * @return
     */
    @Override
    public ExcelRead2Json dataIndex(int dataNameRowIndex,
                                    int dataTypeRowIndex,
                                    int dataDescRowIndex,
                                    int dataExtendRowIndex,
                                    int dataRowIndex) {
        super.dataIndex(
                dataNameRowIndex,
                dataTypeRowIndex,
                dataDescRowIndex,
                dataExtendRowIndex,
                dataRowIndex
        );
        return this;
    }

    /**
     * 数据起始行号
     * <p>行号从0开始
     *
     * @param dataStartRowIndex
     */
    @Override
    public ExcelRead2Json dataStartRowIndex(int dataStartRowIndex) {
        super.dataStartRowIndex(dataStartRowIndex);
        return this;
    }

    /**
     * 映射字段名
     * <p>行号从0开始
     *
     * @param dataTypeRowIndex
     */
    @Override
    public ExcelRead2Json dataTypeRowIndex(int dataTypeRowIndex) {
        super.dataTypeRowIndex(dataTypeRowIndex);
        return this;
    }

    /**
     * 映射字段名
     * <p>行号从0开始
     *
     * @param dataNameRowIndex
     */
    @Override
    public ExcelRead2Json dataNameRowIndex(int dataNameRowIndex) {
        super.dataNameRowIndex(dataNameRowIndex);
        return this;
    }

    /**
     * 描述
     * <p>行号从0开始
     *
     * @param dataDescRowIndex
     */
    @Override
    public ExcelRead2Json dataDescRowIndex(int dataDescRowIndex) {
        super.dataDescRowIndex(dataDescRowIndex);
        return this;
    }

    /**
     * 扩展内容
     * <p>通常 server，client， all
     * <p>行号从0开始
     *
     * @param dataExtendRowIndex
     */
    @Override
    public ExcelRead2Json dataExtendRowIndex(int dataExtendRowIndex) {
        super.dataExtendRowIndex(dataExtendRowIndex);
        return this;
    }

    public ExcelRead2Json setSaveJsonPath(String saveJsonPath) {
        this.saveJsonPath = saveJsonPath;
        return this;
    }

    public String getSaveJsonPath() {
        return saveJsonPath;
    }

}

