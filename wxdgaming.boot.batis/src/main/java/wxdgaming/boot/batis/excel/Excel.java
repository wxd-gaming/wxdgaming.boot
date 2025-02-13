package wxdgaming.boot.batis.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.str.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-10-14 11:20
 **/
@Slf4j
public class Excel implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final Workbook builderWorkbook(File file) {
        if (file == null || StringUtil.emptyOrNull(file.getName()) || file.getName().contains("@") || file.getName().contains("$")) {
            log.info("Excel文件不能解析：" + file.getPath());
            return null;
        }
        try {
            String fileName = file.getName().toLowerCase();
            Workbook workbook;
            InputStream is = new FileInputStream(file.getPath());
            if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(is);
            } else {
                log.info("无法识别的文件：" + file.getPath());
                return null;
            }
            if (workbook.getNumberOfSheets() < 1) {
                log.info("文件空的：" + file.getPath());
                return null;
            }
            return workbook;
        } catch (Throwable throwable) {
            throw Throw.of(throwable);
        }
    }
}
