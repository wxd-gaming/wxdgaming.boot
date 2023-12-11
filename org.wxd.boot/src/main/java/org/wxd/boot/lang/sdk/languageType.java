package org.wxd.boot.lang.sdk;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public final class languageType implements Serializable, Cloneable, Comparable {

    static private final ConcurrentHashMap<Serializable, languageType> KeyMap = new ConcurrentHashMap<>();
    /**
     * 英文
     */
    public static languageType EN = languageType.valueOf("en", "英文");
    /**
     * 中文
     */
    public static languageType ZH_CN = languageType.valueOf("zh_cn", "中文");
    /**
     * 繁体中文
     */
    public static languageType CHT = languageType.valueOf("cht", "繁体中文");
    /**
     * 简体中文
     */
    public static languageType CHS = languageType.valueOf("chs", "简体中文");

    /**
     * 如果键相同，获取的是同一个值
     *
     * @param key
     * @return
     */
    public static languageType valueOf(Serializable key, String desc) {
        return KeyMap.computeIfAbsent(key, k -> new languageType(key, desc));
    }

    /**
     * 如果键相同，获取的是同一个值
     *
     * @param key
     * @return
     */
    public static languageType forOf(Serializable key) {
        languageType executorKey = KeyMap.get(key);
        if (executorKey == null) {
            throw new RuntimeException("未初始化语言包：" + key);
        }
        return executorKey;
    }

    private Serializable key;
    private String desc;

    languageType(Serializable key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    /**
     * 指定字符串的唯一实例
     *
     * @return
     */
    public Serializable getKey() {
        return key;
    }

    /**
     * 描述
     *
     * @return
     */
    public String getDesc() {
        return desc;
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.hashCode(), o.hashCode());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }
        final languageType other = (languageType) obj;
        return this.key.equals(other.key);
    }

    @Override
    public String toString() {
        return key.toString();
    }

}
