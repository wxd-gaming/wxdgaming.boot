package org.wxd.boot.batis.save;


import org.wxd.boot.core.str.StringUtil;
import org.wxd.boot.core.str.json.FastJsonUtil;

import java.util.Map;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-17 10:41
 **/
public interface CheckSaveCode {

    /*保存数据的 临时hashcode值*/
    String DefaultKey = "DefaultKey";

    Map<String, Integer> getSaveCodeMap();

    /**
     * 检查code 与上一次 code对比 如果不一样返回 true 并存储code
     *
     * @return
     */
    default boolean checkSaveCode() {
        return checkSaveCode(FastJsonUtil.toJson(this));
    }

    /**
     * 检查code 与上一次 code对比 如果不一样返回 true 并存储code
     *
     * @param saveStr
     * @return
     */
    default boolean checkSaveCode(String saveStr) {
        return checkSaveCode(DefaultKey, saveStr);
    }

    /**
     * 指定保存不同的key
     *
     * @param key
     * @param saveStr
     * @return
     */
    default boolean checkSaveCode(String key, String saveStr) {
        return checkSaveCode(key, StringUtil.hashcode(saveStr));
    }


    /**
     * 检查code 与上一次 code对比 如果不一样返回 true 并存储code
     *
     * @param saveCode
     * @return
     */
    default boolean checkSaveCode(Integer saveCode) {
        return checkSaveCode(DefaultKey, saveCode);
    }

    /**
     * 指定保存不同的key
     *
     * @param key
     * @param saveCode
     * @return
     */
    default boolean checkSaveCode(String key, Integer saveCode) {
        Integer lastSaveCode = getSaveCodeMap().get(key);
        if (saveCode.equals(lastSaveCode)) {
            return false;
        }
        getSaveCodeMap().put(key, saveCode);
        return true;
    }

}
