package wxdgaming.boot.batis.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.batis.DataWrapper;
import wxdgaming.boot.batis.EntityField;
import wxdgaming.boot.batis.EntityTable;
import wxdgaming.boot.batis.text.json.JsonDataWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 导出数据到excel
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-10-14 11:18
 **/
@Slf4j
public class ExcelOut implements Serializable, AutoCloseable {

    public static ExcelOut builder(String fileName) {
        return new ExcelOut(fileName);
    }

    final String fileName;
    Workbook workbook = null;
    Sheet sheet = null;
    Row sheetRow = null;
    List<String> titleList = null;
    DataWrapper dataWrapper = JsonDataWrapper.Default;

    public ExcelOut(String fileName) {
        this.fileName = fileName;
        createWorkbook();
    }

    @Override
    public void close() {
        if (workbook == null) {
            return;
        }
        try {
            File file = new File(fileName);
            FileUtil.mkdirs(file);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            } finally {
                workbook.close();
            }
        } catch (Exception e) {
            throw Throw.as(fileName, e);
        }
    }

    protected final void createWorkbook() {
        if (fileName.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new HSSFWorkbook();
        }
    }

    public ExcelOut createSheet(String sheetName) {
        sheet = workbook.createSheet(sheetName);
        return this;
    }

    public ExcelOut createRow() {
        sheetRow = sheet.createRow(sheet.getLastRowNum() + 1);
        return this;
    }

    /** 根据实体类导出 */
    public ExcelOut addTitle(EntityTable entityTable) {
        createSheet(entityTable.getTableName());
        createRow();
        addCell(entityTable.getTableComment());
        Collection<EntityField> columns = entityTable.getColumns();
        createRow();
        for (int i = 0; i < columns.size(); i++) {
            /*设置自动归属，默认是服务器*/
            addCell("server");
        }
        createRow();
        titleList = new ArrayList<>();
        for (EntityField column : columns) {
            /*设置列名*/
            titleList.add(column.getColumnName());
            addCell(column.getColumnName());
        }
        createRow();
        for (EntityField column : columns) {
            /*设置类型*/
            addCell(column.typeName());
        }
        createRow();
        for (EntityField column : columns) {
            /*设置列的注释*/
            addCell(column.getColumnComment());
        }
        return this;
    }

    /** 根据实体类导出 */
    public ExcelOut addRows(EntityTable entityTable, List source) {
        for (Object o : source) {
            addRow(entityTable, o);
        }
        return this;
    }

    /** 根据实体类导出 */
    public ExcelOut addRow(EntityTable entityTable, Object source) {
        createRow();
        for (EntityField column : entityTable.getColumns()) {
            /*设置列名*/
            Object fieldValue = column.getFieldValue(source);
            Object dbValue = dataWrapper.toDbValue(column, fieldValue);
            addCell(dbValue);
        }
        return this;
    }

    /** 根据标题处理类型 */
    public ExcelOut addTitle(Map<String, String> row) {
        addTitle(0, row);
        return this;
    }

    public ExcelOut addTitle(int width, Map<String, String> row) {
        if (sheet == null) {
            throw new RuntimeException("尚未初始化");
        }
        createRow();
        titleList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : row.entrySet()) {
            titleList.add(entry.getKey());
            addCell0(width, entry.getValue());
        }
        return this;
    }

    /** 插入行 */
    public ExcelOut addRow(Map<String, ? extends Object> row) {
        if (sheet == null) {
            throw new RuntimeException("尚未初始化");
        }
        if (titleList == null) {
            throw new RuntimeException("请先插入title");
        }
        createRow();
        int size = titleList.size();
        for (int cellIndex = 0; cellIndex < size; cellIndex++) {
            String s = titleList.get(cellIndex);
            addCell0(0, row.get(s));
        }
        return this;
    }

    /** 插入多行 */
    public ExcelOut addRows(List<List> rows) {
        for (List row : rows) {
            addRow0(0, row);
        }
        return this;
    }

    /** 插入一行 */
    public ExcelOut addRow(Object... row) {
        addRow0(0, row);
        return this;
    }

    public ExcelOut addRow0(int width, Object... row) {
        if (sheet == null) {
            throw new RuntimeException("尚未初始化");
        }
        createRow();
        int size = row.length;
        for (int cellIndex = 0; cellIndex < size; cellIndex++) {
            addCell0(width, row[cellIndex]);
        }
        return this;
    }

    /** 插入一行 */
    public ExcelOut addRow(List row) {
        addRow0(0, row);
        return this;
    }

    public ExcelOut addRow0(int width, List row) {
        if (sheet == null) {
            throw new RuntimeException("尚未初始化");
        }
        createRow();
        for (Object o : row) {
            addCell0(width, o);
        }
        return this;
    }

    /** 插入一个格子 */
    public ExcelOut addCell(Object cellValue) {
        addCell0(0, cellValue);
        return this;
    }

    /**
     * 添加一个单元格
     *
     * @param width     字符宽度，也就是字符个数
     * @param cellValue
     * @return
     */
    public ExcelOut addCell0(int width, Object cellValue) {
        if (sheet == null) {
            throw new RuntimeException("尚未初始化");
        }
        if (sheetRow == null) {
            throw new RuntimeException("请先创建行");
        }
        short lastCellNum = sheetRow.getLastCellNum();
        if (lastCellNum < 0) lastCellNum = 0;
        Cell cell = sheetRow.createCell(lastCellNum);
        if (width > 0)
            setColumnWidth0(cell.getColumnIndex(), width);
        cell.setCellValue(String.valueOf(cellValue));
        return this;
    }

    /**
     * 按顺序设置单元格宽度
     *
     * @param widths 字符数
     * @return
     */
    public ExcelOut setColumnWidth(int... widths) {
        for (int i = 0; i < widths.length; i++) {
            setColumnWidth0(i, widths[i]);
        }
        return this;
    }

    /**
     * 设置单元格宽度
     *
     * @param columnIndex 单元格索引，从0开始
     * @param width       字符数
     * @return
     */
    public ExcelOut setColumnWidth0(int columnIndex, int width) {
        sheet.setColumnWidth(columnIndex, width * 256 + 512);
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public List<String> getTitleList() {
        return titleList;
    }
}
