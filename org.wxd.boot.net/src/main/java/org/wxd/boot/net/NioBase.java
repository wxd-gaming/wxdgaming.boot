package org.wxd.boot.net;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.cache.CachePack;
import org.wxd.boot.net.controller.CmdMappingRecord;
import org.wxd.boot.net.handler.CmdService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-01 11:01
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public abstract class NioBase implements CmdService {

    protected String name;
    protected String host = null;
    protected int port;
    /*                   方法名         方法      实例对象     */
    private ConcurrentMap<String, CmdMappingRecord> cmdMethodMap;
    /** 消息执行前，可以添加过滤器 */
    protected Predicate<Runnable> cmdExecutorBefore;
    /** 秘钥管理器 */
    protected final CachePack<String, IAuth> tokenCache = new CachePack<String, IAuth>()
            .setCacheName("权限秘钥管理器")
            .setCacheIntervalTime(60 * 1000)
            .setCacheSurvivalTime(60 * 60 * 1000);

    public abstract NioBase initBootstrap();

    protected abstract NioBase initChannel(ChannelPipeline pipeline);

    @Override
    public Predicate<Runnable> getCmdExecutorBefore() {
        return cmdExecutorBefore;
    }

    @Override
    public NioBase setCmdExecutorBefore(Predicate<Runnable> cmdExecutorBefore) {
        this.cmdExecutorBefore = cmdExecutorBefore;
        return this;
    }

    @Override
    public CachePack<String, IAuth> getTokenCache() {
        return tokenCache;
    }

    @Override
    public ConcurrentMap<String, CmdMappingRecord> getCmdMethodMap() {
        if (cmdMethodMap == null) {
            cmdMethodMap = new ConcurrentHashMap<>();
        }
        return cmdMethodMap;
    }

}
