package org.wxd.boot.starter;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.collection.ObjMap;
import org.wxd.boot.collection.SplitCollection;
import org.wxd.boot.httpclient.url.HttpBuilder;
import org.wxd.boot.str.StringUtil;
import org.wxd.boot.str.json.FastJsonUtil;
import org.wxd.boot.system.JvmUtil;
import org.wxd.boot.system.LocalHostUtil;
import org.wxd.boot.system.ThrowableCache;
import org.wxd.boot.threading.Executors;
import org.wxd.boot.threading.ICheckTimerRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 飞书通知容器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-12-20 15:38
 **/
@Slf4j
public class FeishuPack implements ICheckTimerRunnable {

    public static final FeishuPack Default = new FeishuPack();

    /** 飞书默认通知地址 */
    public String DefaultFeishuUrl = null;

    /** 飞书通知的功能属性 */
    public String FeishuPublicProperty = "程序目录：" + JvmUtil.userHome() + "\n内网ip：" + LocalHostUtil.getAllIp();

    private TreeMap<String, TreeMap<String, SplitCollection<String>>> cache = new TreeMap<>();

    public FeishuPack() {
        Executors.getDefaultExecutor().scheduleAtFixedDelay(this, 10, 10, TimeUnit.SECONDS);
    }

    /** feiShuN通知 "<at user_id=\"458374gc\">子衣</at><at user_id=\"58bafc6e\">陆仕洋</at>" */
    public void asyncFeiShuNotice(String title, String content) {
        asyncFeiShuNotice(DefaultFeishuUrl, title, content);
    }

    /** feiShuN通知 "<at user_id=\"458374gc\">子衣</at><at user_id=\"58bafc6e\">陆仕洋</at>" */
    public void asyncFeiShuNotice(String title, String content, Throwable throwable) {
        Integer integer = ThrowableCache.addException(throwable);
        if (integer != null) {
            asyncFeiShuNotice(DefaultFeishuUrl, title, content + "\n异常累计出现次数：" + integer + "\n" + Throw.ofString(throwable));
        }
    }

    /** feiShuN通知 "<at user_id=\"458374gc\">子衣</at><at user_id=\"58bafc6e\">陆仕洋</at>" */
    public void asyncFeiShuNotice(String url, String title, String content, Throwable throwable) {
        Integer integer = ThrowableCache.addException(throwable);
        if (integer != null) {
            asyncFeiShuNotice(url, title, content + "\n异常累计出现次数：" + integer + "\n" + Throw.ofString(throwable));
        }
    }

    /** feiShuN通知 "<at user_id=\"458374gc\">子衣</at><at user_id=\"58bafc6e\">陆仕洋</at>" */
    public void asyncFeiShuNotice(String url, String title, String content) {
        if (StringUtil.emptyOrNull(url) || StringUtil.emptyOrNull(title) || StringUtil.emptyOrNull(content)) return;
        synchronized (this) {
            TreeMap<String, SplitCollection<String>> map = cache.computeIfAbsent(url, l -> new TreeMap<>());
            SplitCollection<String> absent = map.computeIfAbsent(title, l -> new SplitCollection<>(50));
            absent.add(content);
        }
    }

    @Override public long logTime() {
        return 4000;
    }

    @Override public long warningTime() {
        return 20000;
    }

    @Override public String taskInfoString() {
        return "飞书通知";
    }

    @Override public void run() {
        TreeMap<String, TreeMap<String, SplitCollection<String>>> tmpCache;
        synchronized (this) {
            tmpCache = cache;
            cache = new TreeMap<>();
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

                    final Object put = ObjMap.build("msg_type", "text")
                            .append("content", ObjMap.build("text", text));

                    String requestText = FastJsonUtil.toJson(put);

                    try {

                        final String bodyString = HttpBuilder.postText(url)
                                .paramJson(requestText)
                                .request()
                                .bodyString();

                        if (log.isDebugEnabled()) {
                            log.debug(bodyString);
                        }

                    } catch (Exception e) {
                        log.error("飞书上报异常：" + requestText, e);
                    }
                }
            }
        }
    }

}
