package wxdgaming.boot.net.message;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.format.UniqueID;
import wxdgaming.boot.core.lang.Cache;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.publisher.Mono;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.MarkTimer;
import wxdgaming.boot.core.threading.Event;
import wxdgaming.boot.core.threading.Executors;
import wxdgaming.boot.net.SocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 同步等待结果
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-28 20:51
 **/
@Slf4j
@Getter
@Accessors(chain = true)
public class RpcEvent {

    public static final Cache<Long, RpcEvent> RPC_REQUEST_CACHE_PACK = Cache.<Long, RpcEvent>builder()
            .cacheName("rpc-sync")
            .expireAfterWrite(30000)
            .delay(1000)
            .build();

    public static String RPC_TOKEN = "5cb703a024e54648a70da85890ed7dbb";
    /** 默认的等待时间 单位 毫秒 */
    public static long RPC_WAITE_MILL = 3000;
    /** id生成器 */
    public static UniqueID.HeadUid RPC_ID_FORMAT = new UniqueID.HeadUid(1, 1);

    protected final BlockingQueue<Boolean> queue = new LinkedBlockingQueue<>(1);
    protected MarkTimer markTime;
    protected long waiteMill = RPC_WAITE_MILL;
    protected final SocketSession session;
    protected long rpcId;
    protected final String cmd;
    protected final String reqJson;
    protected boolean sendEnd;
    protected boolean res = false;
    protected String resJson = null;
    protected StackTraceElement[] stackTraceElements = null;

    public RpcEvent(SocketSession session, String cmd, String reqJson) {
        this.session = session;
        this.cmd = cmd;
        this.reqJson = reqJson;
    }

    /** 发送出去，不管了 */
    public void send() {
        if (sendEnd) return;
        Rpc.ReqRemote.Builder builder = Rpc.ReqRemote.newBuilder();
        builder.setRpcId(rpcId);
        builder.setCmd(cmd);
        if (reqJson.length() > 1024) {
            builder.setGzip(1);
            builder.setParams(GzipUtil.gzip2String(reqJson));
        } else {
            builder.setParams(reqJson);
        }
        session.writeFlush(builder.build());
        sendEnd = true;
    }

    public Mono<RpcEvent> async() {
        return sendAsync(3);
    }

    public void async(Consumer<RpcEvent> consumer) {
        sendAsync(3)
                .subscribe(consumer)
                .onError(this::actionThrowable);
    }

    public Mono<String> asyncString() {
        return sendAsync(3).map(RpcEvent::getResJson);
    }

    public void asyncString(Consumer<String> consumer) {
        sendAsync(3)
                .subscribe(rpcEvent -> consumer.accept(rpcEvent.getResJson()))
                .onError(this::actionThrowable);
    }

    public Mono<RunResult> asyncSyncJson() {
        return sendAsync(3).map(rpcEvent -> RunResult.parse(rpcEvent.getResJson()));
    }

    public void asyncSyncJson(Consumer<RunResult> consumer) {
        sendAsync(3)
                .subscribe(rpcEvent -> consumer.accept(RunResult.parse(rpcEvent.getResJson())))
                .onError(this::actionThrowable);
    }

    Mono<RpcEvent> sendAsync(int stackTraceIndex) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        stackTraceElements = new StackTraceElement[stackTrace.length - stackTraceIndex];
        System.arraycopy(stackTrace, stackTraceIndex, stackTraceElements, 0, stackTraceElements.length);
        final Mono<RpcEvent> optFuture = Mono.empty();
        Executors.getVTExecutor().submit(new Event(this.toString(), 150, 1500) {

            @Override public void onEvent() throws Exception {
                try {
                    get();
                    optFuture.completableFuture().complete(RpcEvent.this);
                } catch (Throwable throwable) {
                    RuntimeException runtimeException = Throw.as(RpcEvent.this.toString(), throwable);
                    if (stackTraceElements != null) {
                        runtimeException.setStackTrace(stackTraceElements);
                    }
                    optFuture.completableFuture().completeExceptionally(runtimeException);
                }
            }
        }, stackTraceIndex + 2);
        return optFuture;
    }

    public void actionThrowable(Throwable throwable) {
        log.error("{} url:{}", this.getClass().getSimpleName(), this.toString(), throwable);
    }

    /** 同步请求，等待结果 */
    public String get() {
        return get(waiteMill);
    }

    /** 同步请求，等待结果 */
    public String get(long timeoutMillis) {
        if (!res) {
            this.rpcId = RPC_ID_FORMAT.next();
            RPC_REQUEST_CACHE_PACK.put(this.getRpcId(), this);
            send();
            Boolean poll = null;
            try {
                if (timeoutMillis > 0) {
                    poll = this.queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
                } else poll = this.queue.take();
            } catch (InterruptedException e) {
                throw Throw.as(e);
            }
            if (StringUtil.emptyOrNull(this.resJson)) {
                throw new RuntimeException("get time out");
            }
        }
        return resJson;
    }

    /** 回调回来 */
    public void response(String resJson) {
        this.res = true;
        this.resJson = resJson;
        this.queue.add(true);
    }

    @Override public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "rpcId=" + rpcId + ", "
                + "cmd=" + cmd + ", "
                + "reqJson=" + reqJson
                + '}';
    }
}
