package org.wxd.boot.core.lang.sdk;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-19 14:46
 **/
public class SdkParamPool extends LinkedHashMap<String, SdkParam> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static SdkParamPool builder() {
        return new SdkParamPool();
    }

    public SdkParamPool add(SdkParam sdkParam) {
        this.put(sdkParam.getChannel(), sdkParam);
        return this;
    }

    /**
     * key值的数组
     *
     * @return
     */
    public Serializable[] keyArray() {
        return this.keySet().toArray(new Serializable[this.size()]);
    }

}
