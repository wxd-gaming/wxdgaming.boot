package wxdgaming.boot.batis.code;

import wxdgaming.boot.core.str.TemplatePack;
import wxdgaming.boot.batis.EntityTable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-17 23:56
 **/
public interface ICreateCode {

    CodeLan getCodeLan();

    void createCodeFile(TemplatePack templatePack, EntityTable entityTable, String savePath, String packageName);

}
