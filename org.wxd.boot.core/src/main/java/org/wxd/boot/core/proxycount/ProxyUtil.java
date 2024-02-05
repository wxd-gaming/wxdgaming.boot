package org.wxd.boot.core.proxycount;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;

/**
 * 代理统计
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-08 14:48
 **/
@Slf4j
public class ProxyUtil {

    public static boolean OpenCount = false;
    public static boolean OpenLogMessage = false;

    /** 某些时候可能只针对个别人开启 */
    public static ConcurrentSkipListSet OpenUserIdSet = new ConcurrentSkipListSet();

    public static ConcurrentSkipListMap<Long, ProxyUser> User_Proxy_Count_Map = new ConcurrentSkipListMap<>();

    public static boolean checkOpen(long userId) {
        if (OpenCount) {
            if (OpenUserIdSet.isEmpty() || OpenUserIdSet.contains(userId)) {
                return true;
            }
        }
        return false;
    }

    public static void proxyUser(long userId, Consumer<ProxyUser> proxyUser) {
        ProxyUser computeIfAbsent = User_Proxy_Count_Map.computeIfAbsent(userId, l -> new ProxyUser().setUserId(userId));
        try {
            proxyUser.accept(computeIfAbsent);
        } catch (Exception e) {
            log.error("统计代理异常", e);
        }
    }

}
