package org.wxd.boot.core.lang;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-03-19 15:41
 **/
@Root(name = "property")
public class Property implements Serializable {

    @Attribute
    private String Key;
    @Attribute
    private String value;

    public Property() {
    }

    public Property(String key, String value) {
        Key = key;
        this.value = value;
    }

    public String getKey() {
        return Key;
    }

    public Property setKey(String key) {
        Key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Property setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "Key='" + Key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
