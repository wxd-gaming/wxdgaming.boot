package code;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Splitter;
import com.google.common.cache.*;
import com.google.common.collect.HashMultimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.core.str.json.FastJsonUtil;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * google的cache
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-05-10 14:24
 **/
@Slf4j
public class GuavaTest {

    @Test
    public void t1() throws Exception {
        LoadingCache<Long, String> build = CacheBuilder.newBuilder()
                .initialCapacity(100)
                .concurrencyLevel(8)
                // .expireAfterAccess(10, TimeUnit.SECONDS)/*访问后过期时间 和 写入过期互斥*/
                .expireAfterWrite(10, TimeUnit.SECONDS)/*写入过期时间 和 访问过期互斥 */
                .refreshAfterWrite(5, TimeUnit.SECONDS)/*设置缓存，这个位置可以用来重新加载数据库*/
                .removalListener(new RemovalListener<Long, String>() {
                    @Override public void onRemoval(RemovalNotification<Long, String> notification) {
                        log.error("过期删除 {}", notification);
                    }
                })
                .build(new CacheLoader<Long, String>() {

                    @Override public String load(Long key) throws Exception {
                        return null;
                    }

                    @Override public ListenableFuture<String> reload(Long key, String oldValue) throws Exception {
                        return Futures.immediateFuture(oldValue + "-reload");
                    }
                });

        build.put(1L, "1");
        for (int i = 0; i < 10; i++) {
            System.out.println(i + " - " + build.getIfPresent(1L));
            Thread.sleep(1000);
        }
        Thread.sleep(TimeUnit.MINUTES.toMillis(5));
    }

    @Test
    public void t2() {
        HashMultimap<String, String> multimap = HashMultimap.create();
        multimap.put("狗", "大黄");
        multimap.put("狗", "旺财");
        multimap.put("猫", "加菲");
        multimap.put("猫", "汤姆");

        System.out.println(multimap);

        Set<String> 猫 = multimap.get("猫");
        System.out.println(猫); // [加菲, 汤姆]
        String json = FastJsonUtil.toJson(multimap);
        System.out.println(json);
        /*不支持json序列*/
        HashMultimap<String, String> parse = FastJsonUtil.parse(json, new TypeReference<HashMultimap<String, String>>() {});
        System.out.println(parse);
    }

    @Test
    public void t3() {
        String str = ",a ,,b ,";
        Iterable<String> split = Splitter.on(",")
                .omitEmptyStrings() // 忽略空值
                .trimResults() // 过滤结果中的空白
                .split(str);
        split.forEach(System.out::println);

    }

}
