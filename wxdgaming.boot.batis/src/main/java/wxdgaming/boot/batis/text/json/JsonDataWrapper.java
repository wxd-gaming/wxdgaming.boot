package wxdgaming.boot.batis.text.json;


import wxdgaming.boot.batis.text.TextDataWrapper;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-19 15:45
 **/
public class JsonDataWrapper extends TextDataWrapper<JsonEntityTable> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static JsonDataWrapper Default = new JsonDataWrapper();

    @Override
    public JsonEntityTable createEntityTable() {
        return new JsonEntityTable();
    }

}
