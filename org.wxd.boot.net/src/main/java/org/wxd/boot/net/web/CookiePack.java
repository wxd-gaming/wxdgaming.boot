package org.wxd.boot.net.web;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.*;
import io.netty.util.AsciiString;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.str.StringUtil;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * cookie管理
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-03-14 11:34
 **/
@Slf4j
public class CookiePack implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Cookie> cookies = new LinkedHashSet<>();

    public Set<Cookie> getCookies() {
        return cookies;
    }

    public String findCookieValue(AsciiString cookieKey) {
        return findCookieValue(cookieKey.toString(), "/");
    }

    public String findCookieValue(AsciiString cookieKey, String cookiePath) {
        return findCookieValue(cookieKey.toString(), cookiePath);
    }

    public String findCookieValue(String cookieKey) {
        return findCookieValue(cookieKey, "/");
    }

    public String findCookieValue(String cookieKey, String cookiePath) {
        final Cookie cookie = findCookie(cookieKey, cookiePath);
        if (cookie != null) return cookie.value();
        return null;
    }

    public Cookie findCookie(AsciiString cookieKey) {
        return findCookie(cookieKey.toString(), "/");
    }

    public Cookie findCookie(AsciiString cookieKey, String cookiePath) {
        return findCookie(cookieKey.toString(), cookiePath);
    }

    public Cookie findCookie(String cookieKey) {
        return findCookie(cookieKey, "/");
    }

    public Cookie findCookie(String cookieKey, String cookiePath) {
        return cookies.stream()
                .filter(v -> Objects.equals(v.name(), cookieKey))
                .filter(v -> {
                    if (StringUtil.emptyOrNull(v.path())) return true;
                    if (StringUtil.emptyOrNull(cookiePath)) return true;
                    return Objects.equals(v.path(), cookiePath);
                })
                .findFirst()
                .orElse(null);
    }

    public CookiePack addCookie(AsciiString cookieKey, Object cookieValue) {
        addCookie(cookieKey.toString(), cookieValue);
        return this;
    }

    public CookiePack addCookie(AsciiString cookieKey, Object cookieValue, String cookiePath) {
        addCookie(cookieKey.toString(), cookieValue, cookiePath);
        return this;
    }

    /**
     * 默认是根目录的cookie
     *
     * @param cookieKey
     * @param cookieValue
     */
    public CookiePack addCookie(String cookieKey, Object cookieValue) {
        return addCookie(cookieKey, cookieValue, "/");
    }

    public CookiePack addCookie(String cookieKey, Object cookieValue, String cookiePath) {
        return addCookie(cookieKey, cookieValue, cookiePath, null, null);
    }

    /**
     * @param cookieKey
     * @param cookieValue
     * @param cookiePath  路径
     * @param domain      站点域名
     * @return
     */
    public CookiePack addCookie(String cookieKey, Object cookieValue, String cookiePath, String domain, Long maxAge) {
        if (cookieKey == null || cookieKey.length() < 1 || cookieValue == null) {
            return this;
        }

        if (log.isDebugEnabled()) {
            log.debug("cookiePath " + cookiePath + ", domain = " + domain);
        }

        final DefaultCookie defaultCookie = new DefaultCookie(cookieKey, String.valueOf(cookieValue));
        if (cookiePath != null) {
            defaultCookie.setPath(cookiePath);
        }

        if (domain != null) {
            defaultCookie.setDomain(domain);
        }
        if (maxAge != null) {
            defaultCookie.setMaxAge(maxAge);
        }
        this.cookies.add(defaultCookie);
        return this;
    }

    /**
     * 解析服务器cookie
     *
     * @param cookieString
     */
    public CookiePack decodeClientCookie(String cookieString) {
        if (cookieString != null && cookieString.length() > 0) {
            Cookie cookie = ClientCookieDecoder.STRICT.decode(cookieString);
            cookies.add(cookie);
        }
        return this;
    }

    /**
     * 解析服务器cookie
     *
     * @param cookieString
     */
    public CookiePack decodeServerCookie(String cookieString) {
        if (cookieString != null && cookieString.length() > 0) {
            final Set<Cookie> cookieSet = ServerCookieDecoder.STRICT.decode(cookieString);
            for (Cookie cookie : cookieSet) {
                cookies.add(cookie);
            }
        }
        return this;
    }

    /**
     * 服务器端的设置
     */
    public CookiePack serverCookie(HttpHeaders httpHeaders) {
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieBuilder = new StringBuilder();
            for (Cookie cookie : cookies) {
                if (cookieBuilder.length() > 0) {
                    cookieBuilder.append("; ");
                }
                cookieBuilder.append(ServerCookieEncoder.STRICT.encode(cookie));
            }
            httpHeaders.set(HttpHeaderNames.SET_COOKIE, cookieBuilder.toString());
        }
        return this;
    }

    /**
     * 把 cookie 转化成客户端标记
     *
     * @return
     */
    public CookiePack clientCookie(HttpHeaders httpHeaders) {
        final String clientCookieString = clientCookieString();
        if (StringUtil.notEmptyOrNull(clientCookieString)) {
            httpHeaders.set(HttpHeaderNames.COOKIE, clientCookieString);
        }
        return this;
    }

    public String clientCookieString() {
        if (cookies != null && !cookies.isEmpty()) {
            StringBuilder cookieBuilder = new StringBuilder();
            for (Cookie cookie : cookies) {
                if (!cookieBuilder.isEmpty()) {
                    cookieBuilder.append("; ");
                }
                cookieBuilder.append(ClientCookieEncoder.STRICT.encode(cookie));
            }
            return cookieBuilder.toString();
        }
        return null;
    }
}
