package org.wxd.boot.lang.sdk;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-19 14:46
 */
public final class SdkParam implements Serializable, Comparable<SdkParam> {

    private static final long serialVersionUID = 1L;

    static private final ConcurrentHashMap<String, SdkParam> KeyMap = new ConcurrentHashMap<>();

    /**
     * 如果不存在，会抛异常
     *
     * @param channel
     * @return
     */
    public static SdkParam forChannel(String channel) {
        SdkParam sdkParam = KeyMap.get(channel);
        if (sdkParam == null) {
            throw new RuntimeException("找不到channel = " + channel);
        }
        return sdkParam;
    }

    /**
     * 如果不存在，将会 new 新的
     *
     * @param channel
     * @return
     */
    public static SdkParam valueOf(String channel) {
        return KeyMap.computeIfAbsent(channel, k -> new SdkParam(channel));
    }

    public static SdkParam[] values() {
        return KeyMap.values().toArray(new SdkParam[KeyMap.size()]);
    }

    /**
     * 内部使用 100
     */
    public static final SdkParam OneOf100 = SdkParam.valueOf("100").setLanguage(languageType.CHS);
    /**
     * 内部使用 100
     */
    public static final SdkParam OneOf101 = SdkParam.valueOf("101").setLanguage(languageType.CHS);
    /**
     * 内部使用 100
     */
    public static final SdkParam OneOf102 = SdkParam.valueOf("102").setLanguage(languageType.CHS);
    /**
     * 内部使用 100
     */
    public static final SdkParam OneOf103 = SdkParam.valueOf("103").setLanguage(languageType.CHS);
    /**
     * 内部使用 100
     */
    public static final SdkParam OneOf104 = SdkParam.valueOf("104").setLanguage(languageType.CHS);
    public static final SdkParam OneOf105 = SdkParam.valueOf("105").setLanguage(languageType.CHS);
    public static final SdkParam OneOf106 = SdkParam.valueOf("106").setLanguage(languageType.CHS);
    public static final SdkParam OneOf107 = SdkParam.valueOf("107").setLanguage(languageType.CHS);
    public static final SdkParam OneOf108 = SdkParam.valueOf("108").setLanguage(languageType.CHS);
    public static final SdkParam OneOf109 = SdkParam.valueOf("109").setLanguage(languageType.CHS);
    public static final SdkParam OneOf110 = SdkParam.valueOf("110").setLanguage(languageType.CHS);

    /**
     * facebook
     */
    public static final SdkParam Facebook = SdkParam.valueOf("Facebook").setLanguage(languageType.EN);
    /**
     * google
     */
    public static final SdkParam Google = SdkParam.valueOf("google").setLanguage(languageType.EN);

    /**
     * 账号互通设定相同的渠道
     */
    private String channel;
    /**
     * 语言
     */
    private languageType language;
    /**
     *
     */
    private String appId;
    private String appMD5Key;
    private String postUrl;
    private String bindUrl;

    private SdkParam(String channel) {
        this.channel = channel;
        this.appId = channel;
        this.language = languageType.CHS;
    }

    public String getChannel() {
        return channel;
    }

    public languageType getLanguage() {
        return language;
    }

    public SdkParam setLanguage(languageType language) {
        this.language = language;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public SdkParam setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getAppMD5Key() {
        return appMD5Key;
    }

    public SdkParam setAppMD5Key(String appMD5Key) {
        this.appMD5Key = appMD5Key;
        return this;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public SdkParam setPostUrl(String postUrl) {
        this.postUrl = postUrl;
        return this;
    }

    public String getBindUrl() {
        return bindUrl;
    }

    public SdkParam setBindUrl(String bindUrl) {
        this.bindUrl = bindUrl;
        return this;
    }

    @Override
    public int compareTo(SdkParam o) {
        return Integer.compare(this.hashCode(), o.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SdkParam sdkParam = (SdkParam) o;

        return Objects.equals(channel, sdkParam.channel);
    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{" +
                "language=" + language +
                ", channel='" + channel + '\'' +
                ", appId='" + appId + '\'' +
                ", appMD5Key='" + appMD5Key + '\'' +
                ", postUrl='" + postUrl + '\'' +
                ", bindUrl='" + bindUrl + '\'' +
                '}';
    }
}
