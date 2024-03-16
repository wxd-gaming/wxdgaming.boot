package org.wxd.boot.batis.mongodb.excel;

import com.mongodb.client.model.ReplaceOneModel;
import org.apache.poi.ss.usermodel.Cell;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.core.str.TemplatePack;
import org.wxd.boot.batis.EntityField;
import org.wxd.boot.batis.code.CodeLan;
import org.wxd.boot.batis.excel.ExcelRead;
import org.wxd.boot.batis.mongodb.MongoDataHelper;
import org.wxd.boot.batis.mongodb.MongoDataWrapper;
import org.wxd.boot.batis.mongodb.MongoEntityTable;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 读取excel
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-13 15:06
 **/
public class ExcelRead2Mongo extends ExcelRead<MongoEntityTable, MongoDataWrapper> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ExcelRead2Mongo.class);

    public static void main(String[] args) throws Exception {
        MongoDataHelper mongoDataHelper = new MongoDataHelper("192.168.30.254", 27017, "igg-game-data");
        ExcelRead2Mongo.builder()
                .excelPaths("D:\\work\\qiyou_igg\\igg-策划-配置")
                .loadExcel()
                .showData()
                .createCode(CodeLan.Java, "src\\com\\qy\\mg\\dbmodel\\po\\data", "com.qy.mg.dbmodel.po.data")
                .saveData(mongoDataHelper);
    }

    public static ExcelRead2Mongo builder() throws Exception {
        return new ExcelRead2Mongo();
    }

    /*如果要导入数据库*/
    private MongoDataHelper mongoDataHelper;

    @Override
    protected String getCellString(Cell data, boolean isColumnName) {
        String cellString = super.getCellString(data, isColumnName);
        if ("id".equalsIgnoreCase(cellString)) {
            cellString = "_id";
        }
        return cellString;
    }

    @Override
    public MongoDataWrapper getDataWrapper() {
        return mongoDataHelper.getDataWrapper();
    }

    @Override
    protected MongoEntityTable createDataStruct() {
        return new MongoEntityTable();
    }

    @Override
    public ExcelRead2Mongo loadExcel(String... haveExtends) {
        super.loadExcel(haveExtends);
        return this;
    }

    @Override public ExcelRead2Mongo createCode(CodeLan codeLan, String saveCodePath, String savePackageName) {
        super.createCode(codeLan, saveCodePath, savePackageName);
        return this;
    }

    @Override
    public ExcelRead2Mongo createCode(TemplatePack templatePack, String houZhui, String saveCodePath, String savePackageName) {
        super.createCode(templatePack, houZhui, saveCodePath, savePackageName);
        return this;
    }

    @Override
    public ExcelRead2Mongo showData() {
        super.showData();
        return this;
    }

    public ExcelRead2Mongo saveData(MongoDataHelper mongoDataHelper) {
        this.mongoDataHelper = mongoDataHelper;
        this.saveData();
        return this;
    }

    @Override
    public ExcelRead2Mongo saveData(MongoEntityTable entityTable) {
        this.mongoDataHelper.dropTable(entityTable.getTableName());
        if (!entityTable.getRows().isEmpty()) {
            List<ReplaceOneModel<Document>> replaceOneModels = new LinkedList<>();
            final MongoDataWrapper dataWrapper = getDataWrapper();
            LinkedList<LinkedHashMap<EntityField, Object>> rows = entityTable.getRows();
            for (Map<EntityField, Object> row : rows) {
                Document document = new Document();
                Document whereDocument = new Document();
                for (Map.Entry<EntityField, Object> dataColumnObjectEntry : row.entrySet()) {
                    EntityField entityField = dataColumnObjectEntry.getKey();
                    Object value = dataColumnObjectEntry.getValue();
                    value = dataWrapper.toDbValue(entityField, value);
                    document.append(entityField.getColumnName(), value);

                    if (entityField.equals(entityTable.getDataColumnKey())) {
                        whereDocument.append(entityField.getColumnName(), value);
                    }
                }
                replaceOneModels.add(new ReplaceOneModel<>(whereDocument, document, MongoDataHelper.Replace_Options));
            }
            this.mongoDataHelper.getCollection(entityTable.getTableName()).bulkWrite(replaceOneModels, MongoDataHelper.Bulk_Write_Options);
            log.info("数据库：" + this.mongoDataHelper.getDbBase() + ", " + entityTable.getLogTableName() + ", 影响行数：" + rows.size());
        }
        return this;
    }

    @Override
    public ExcelRead2Mongo excelPaths(String... excelPaths) {
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
    public ExcelRead2Mongo excelPaths(boolean loop, String... excelPaths) {
        super.excelPaths(loop, excelPaths);
        return this;
    }

    /**
     * 行号从0开始
     *
     * @param dataNameRowIndex   映射字段名起始行号
     * @param dataTypeRowIndex   映射字段数据类型
     * @param dataDescRowIndex   映射字段名起始行号
     * @param dataExtendRowIndex 扩展内容 通常 server, client, all, no
     * @param dataRowIndex       数据起始行号
     * @return
     */
    @Override
    public ExcelRead2Mongo dataIndex(int dataNameRowIndex,
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
    public ExcelRead2Mongo dataStartRowIndex(int dataStartRowIndex) {
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
    public ExcelRead2Mongo dataTypeRowIndex(int dataTypeRowIndex) {
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
    public ExcelRead2Mongo dataNameRowIndex(int dataNameRowIndex) {
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
    public ExcelRead2Mongo dataDescRowIndex(int dataDescRowIndex) {
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
    public ExcelRead2Mongo dataExtendRowIndex(int dataExtendRowIndex) {
        super.dataExtendRowIndex(dataExtendRowIndex);
        return this;
    }

}

