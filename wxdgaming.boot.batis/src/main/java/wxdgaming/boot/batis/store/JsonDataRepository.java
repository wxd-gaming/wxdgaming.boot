package wxdgaming.boot.batis.store;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.batis.struct.DataChecked;
import wxdgaming.boot.batis.struct.DbBean;
import wxdgaming.boot.batis.text.json.JsonDataWrapper;
import wxdgaming.boot.batis.text.json.JsonEntityTable;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * json 配置仓储
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-19 17:51
 **/
@Slf4j
public class JsonDataRepository extends DataRepository<JsonEntityTable, JsonDataWrapper> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;

    public JsonDataRepository() {
    }

    /** json文件目录 */
    public JsonDataRepository setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public JsonDataWrapper dataBuilder() {
        return JsonDataWrapper.Default;
    }

    @Override
    public String dataName() {
        return "Json Data：" + path;
    }

    @Override public <B, T extends DbBean<B, ?>> T load(Class<T> beanClazz, JsonEntityTable entityTable) {
        return super.load(beanClazz, entityTable);
    }

    @Override
    public List readDbList(JsonEntityTable entityTable) throws Exception {
        Class<?> entityClass = entityTable.getEntityClass();
        String readFile = readFile(entityTable.getTableName());
        List objects;
        try {
            objects = FastJsonUtil.parseArray(readFile, entityClass);
        } catch (Exception e) {
            objects = new ArrayList<>();
            List<String> collect = readFile.lines().toList();
            for (int l = 1; l < collect.size() - 1; l++) {
                String trim = collect.get(l).trim();
                if (trim.endsWith(",")) {
                    trim = trim.substring(0, trim.length() - 1);
                }
                try {
                    Object parse = FastJsonUtil.parse(readFile, entityClass);
                    objects.add(parse);
                } catch (Exception eline) {
                    throw new RuntimeException(trim, eline);
                }
            }
        }
        for (Object object : objects) {
            if (object instanceof DataChecked) {
                ((DataChecked) object).initAndCheck();
            }
        }
        return objects;
    }

    public String readFile(String jsonFile) throws Exception {
        String fileName = path + "/" + jsonFile + ".json";
        if (!FileUtil.exists(fileName)) {
            throw new RuntimeException("缺少配置文件：" + fileName);
        }
        return FileReadUtil.readString(fileName);
    }

}
