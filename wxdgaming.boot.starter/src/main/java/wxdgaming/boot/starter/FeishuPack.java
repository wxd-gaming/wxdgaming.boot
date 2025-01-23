package wxdgaming.boot.starter;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.collection.SplitCollection;
import wxdgaming.boot.core.lang.LockBase;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.core.system.LocalHostUtil;
import wxdgaming.boot.core.system.ThrowableCache;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.httpclient.apache.HttpBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 飞书通知容器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-12-20 15:38
 **/
@Slf4j
public class FeishuPack extends Event {

    public static final FeishuPack Default = new FeishuPack();
    protected final LockBase lockBase = new LockBase();
    /** 飞书默认通知地址 */
    public String DefaultFeishuUrl = null;

    /** 飞书通知的功能属性 */
    public String FeishuPublicProperty = "程序目录：" + JvmUtil.userHome() + "\n内网ip：" + LocalHostUtil.getAllIp();

    private TreeMap<String, TreeMap<String, SplitCollection<String>>> cache = new TreeMap<>();

    public FeishuPack() {
        super("飞书通知", 4000, 20000);
        Executors.getDefaultExecutor().scheduleAtFixedDelay(this, 10, 10, TimeUnit.SECONDS);
    }

    /** feiShuN通知 "<at user_id=\"xxx\">xx</at>" */
    public void asyncFeiShuNotice(String title, String content) {
        asyncFeiShuNotice(DefaultFeishuUrl, title, content);
    }

    /** feiShuN通知 "<at user_id=\"xxx\">xx</at>" */
    public void asyncFeiShuNotice(String title, String content, Throwable throwable) {
        Integer integer = ThrowableCache.addException(throwable);
        if (integer != null) {
            asyncFeiShuNotice(DefaultFeishuUrl, title, content + "\n异常累计出现次数：" + integer + "\n" + Throw.ofString(throwable));
        }
    }

    /** feiShuN通知 "<at user_id=\"xxx\">xx</at>" */
    public void asyncFeiShuNotice(String url, String title, String content, Throwable throwable) {
        Integer integer = ThrowableCache.addException(throwable);
        if (integer != null) {
            asyncFeiShuNotice(url, title, content + "\n异常累计出现次数：" + integer + "\n" + Throw.ofString(throwable));
        }
    }

    /** feiShuN通知 "<at user_id=\"xxx\">xx</at>" */
    public void asyncFeiShuNotice(String url, String title, String content) {
        if (StringUtil.emptyOrNull(url) || StringUtil.emptyOrNull(title) || StringUtil.emptyOrNull(content)) return;
        lockBase.lock();
        try {
            TreeMap<String, SplitCollection<String>> map = cache.computeIfAbsent(url, l -> new TreeMap<>());
            SplitCollection<String> absent = map.computeIfAbsent(title, l -> new SplitCollection<>(50));
            absent.add(content);
        } finally {
            lockBase.unlock();
        }
    }

    @Override public void onEvent() {
        TreeMap<String, TreeMap<String, SplitCollection<String>>> tmpCache;
        lockBase.lock();
        try {
            tmpCache = cache;
            cache = new TreeMap<>();
        } finally {
            lockBase.unlock();
        }
        for (Map.Entry<String, TreeMap<String, SplitCollection<String>>> entry : tmpCache.entrySet()) {
            String url = entry.getKey();
            TreeMap<String, SplitCollection<String>> titleMap = entry.getValue();
            for (Map.Entry<String, SplitCollection<String>> collectionEntry : titleMap.entrySet()) {
                String title = collectionEntry.getKey();
                SplitCollection<String> entryValue = collectionEntry.getValue();

                LinkedList<List<String>> es = entryValue.getEs();
                for (List<String> list : es) {
                    String content = String.join(
                            "\n\n--------------------------------------------------------------\n\n",
                            list
                    );

                    String text = title + "\n" + FeishuPublicProperty + "\n内容：\n" + content;

                    final Object put = MapOf.newJSONObject("msg_type", "text")
                            .fluentPut("content", MapOf.newJSONObject("text", text));

                    String requestText = FastJsonUtil.toJson(put);

                    try {

                        final String bodyString = HttpBuilder.postJson(url, requestText)
                                .request()
                                .bodyString();

                        if (log.isDebugEnabled()) {
                            log.debug(bodyString);
                        }

                    } catch (Exception e) {
                        log.error("飞书上报异常：{}", requestText, e);
                    }
                }
            }
        }
    }

}
