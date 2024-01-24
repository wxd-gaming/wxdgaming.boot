package org.wxd.boot.net;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.lang.LockBase;

/**
 * 基于 netty 实现 通信基类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-06-01 11:01
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public abstract class NioBase extends LockBase {


    protected String name;
    /** 仅仅只是展示用的 */
    protected String wanIp = null;
    protected String host = null;
    protected int port;

    public abstract NioBase initBootstrap();

    protected abstract NioBase initChannel(ChannelPipeline pipeline);

}
