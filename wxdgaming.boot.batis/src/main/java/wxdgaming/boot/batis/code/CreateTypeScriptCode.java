package wxdgaming.boot.batis.code;

import wxdgaming.boot.core.str.TemplatePack;
import wxdgaming.boot.batis.EntityTable;

import java.io.Serializable;

/**
 * TypeScript代码
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-05-18 15:17
 **/
public class CreateTypeScriptCode implements Serializable, ICreateCode {

    @Override
    public CodeLan getCodeLan() {
        return CodeLan.TypeScript;
    }

    @Override
    public void createCodeFile(TemplatePack templatePack, EntityTable entityTable, String savePath, String packageName) {

    }
}
