package org.wxd.boot.http.ssl;


import java.io.Serializable;

/**
 * ssl版本协议
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-11 14:30
 **/
public enum SslProtocolType implements Serializable {
    /** SSL */
    SSL("SSL"),
    /** SSLV1 */
    SSLV1("SSLV1"),
    /** SSLV2 */
    SSLV2("SSLV2"),
    /** SSLV3 */
    SSLV3("SSLV3"),
    /** TLS */
    TLS("TLS"),
    /** TLSv1 */
    TLSV1("TLSv1"),
    /** TLSv1.2 */
    TLSV12("TLSv1.2");

    public static SslProtocolType of(String source) {
        for (SslProtocolType value : SslProtocolType.values()) {
            if (value.getTypeName().equalsIgnoreCase(source)) {
                return value;
            }
        }
        return null;
    }

    private final String typeStr;

    SslProtocolType(String typeStr) {
        this.typeStr = typeStr;
    }

    public String getTypeName() {
        return typeStr;
    }


}
