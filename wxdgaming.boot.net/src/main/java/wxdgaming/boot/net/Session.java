package wxdgaming.boot.net;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.lang.LockBase;
import wxdgaming.boot.core.str.StringUtils;
import wxdgaming.boot.core.timer.MyClock;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * session对象
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-25 09:55
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public abstract class Session extends LockBase implements Serializable {

    static public AtomicLong sessionId0 = new AtomicLong();

    private boolean gmSession;
    private long createTime;
    private long id;
    private String name;
    /**
     * 登录用户，自己实现
     */
    protected Object authUser;
    private ChannelHandlerContext channelContext;
    private String ip = null;
    private String localAddress = null;
    private String remoteAddress = null;
    private boolean isDisConnect = false;
    private Class<? extends NioBase> nioBaseClass;

    public Session(NioBase nioBase, ChannelHandlerContext channelContext) {
        this.createTime = MyClock.millis();
        this.id = sessionId0.incrementAndGet();
        this.nioBaseClass = nioBase.getClass();
        this.name = nioBase.getName();
        this.channelContext = channelContext;
        NioFactory.attr(channelContext, NioFactory.Session, this);
        if (log.isDebugEnabled()) {
            log.debug("链接完成 , {}", this.toString());
        }
    }

    /**
     * 获取参数
     */
    public Session attr(String key, Object value) {
        NioFactory.attr(channelContext, key, value);
        return this;
    }

    /**
     * 获取参数
     *
     * @param key
     * @param <R>
     * @return
     */
    public <R> R attr(String key) {
        return NioFactory.attr(channelContext, key);
    }

    public boolean isSsl() {
        final Object ssl = attr("ssl");
        if (ssl == null || Boolean.FALSE.equals(ssl)) {
            return false;
        }
        return true;
    }

    /**
     * 删除参数
     *
     * @param key
     * @param <R>
     * @return
     */
    public <R> R attrDel(String key) {
        return NioFactory.attrDel(channelContext, key);
    }

    public boolean isActive() {
        if (this.channelContext == null || channelContext.channel() == null || isDisConnect) {
            return false;
        }
        return channelContext.channel().isActive();
    }

    public boolean isRegistered() {
        if (this.channelContext == null || channelContext.channel() == null || isDisConnect) {
            return false;
        }
        return channelContext.channel().isRegistered();
    }

    public boolean isWritable() {
        if (this.channelContext == null || channelContext.channel() == null || isDisConnect) {
            return false;
        }
        return channelContext.channel().isWritable();
    }

    /**
     * 释放连接
     */
    public void disConnect(String msg) {
        lock();
        try {
            if (!isDisConnect) {
                if (log.isDebugEnabled()) {
                    log.debug(msg + ", " + this.toString());
                }
                try {
                    this.channelContext.disconnect();
                } catch (Exception e) {
                }
                try {
                    this.channelContext.close();
                } catch (Exception e) {
                }
                isDisConnect = true;
            }
        } finally {
            unlock();
        }
    }


    public <R> R authUser() {
        return (R) authUser;
    }

    public String getIp() {
        if (StringUtils.isBlank(this.ip))
            this.ip = NioFactory.getIP(this.channelContext);
        return ip;
    }

    public String getLocalAddress() {
        if (StringUtils.isBlank(this.localAddress))
            this.localAddress = NioFactory.getLocalAddress(channelContext);
        return localAddress;
    }

    public String getRemoteAddress() {
        if (StringUtils.isBlank(this.remoteAddress))
            this.remoteAddress = NioFactory.getRemoteAddress(channelContext);
        return remoteAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;
        return id == session.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return this.name + "：" + NioFactory.getCtxName(channelContext) + ", "
               + this.getId() + ", "
               + this.getRemoteAddress() + ", 状态：registered = " + isRegistered() + ", active = " + isActive();
    }

}
