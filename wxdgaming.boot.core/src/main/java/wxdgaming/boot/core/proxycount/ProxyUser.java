package wxdgaming.boot.core.proxycount;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wxdgaming.boot.core.format.ByteFormat;
import wxdgaming.boot.core.format.TimeFormat;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 代理统计用户
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-08 16:12
 **/
@Getter
@Setter
@Accessors(chain = true)
public class ProxyUser {

    private long userId;
    private String UserName;
    /** 本次登录时间 */
    private long loginTime;
    /** 发送消息次数 */
    private int sendAllCount;
    private ByteFormat sendAllBytes = new ByteFormat();
    private ConcurrentHashMap<String, MsgCount> sendMap = new ConcurrentHashMap<>();

    private int receiveAllCount;
    private ByteFormat receiveAllBytes = new ByteFormat();
    /**
     * 统计消息执行耗时
     */
    private TimeFormat receiveCostTime = new TimeFormat();
    private ConcurrentHashMap<String, MsgCount> receiveMap = new ConcurrentHashMap<>();

    public ProxyUser clear() {
        this.sendAllCount = 0;
        this.sendAllBytes.setAllBytes(0);
        this.sendMap.clear();

        this.receiveAllCount = 0;
        this.receiveAllBytes.setAllBytes(0);
        this.receiveCostTime.setAllTime(0);
        this.receiveMap.clear();

        return this;
    }

    public ProxyUser onLogin() {
        this.loginTime = System.currentTimeMillis();
        return this;
    }

    public MsgCount sendCount(String name) {
        return sendMap.computeIfAbsent(name, l -> new MsgCount().setName(l));
    }

    public ProxyUser addSendCount(String name, long len) {
        sendCount(name).addByteLen(len, 0);
        sendAllCount++;
        sendAllBytes.addFlow(len);
        return this;
    }

    public MsgCount receiveCount(String name) {
        return receiveMap.computeIfAbsent(name, l -> new MsgCount().setName(l));
    }

    /**
     * 接收消息
     *
     * @param name
     * @param len
     * @param costTime
     * @return
     */
    public ProxyUser addReceiveCount(String name, long len, long costTime) {
        receiveCount(name).addByteLen(len, costTime);
        receiveAllCount++;
        receiveAllBytes.addFlow(len);
        receiveCostTime.addTime(costTime);
        return this;
    }

    @Override
    public String toString() {
        List<MsgCount> collect;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        stringBuilder.append("===================================================================================");
        stringBuilder.append("\n");
        stringBuilder.append("用户：").append(userId).append("(").append(UserName).append(")");
        stringBuilder.append("\n");
        stringBuilder.append("本次在线时长：").append(new TimeFormat().addTime((System.currentTimeMillis() - loginTime) * 100).toString(TimeFormat.FormatInfo.All));
        stringBuilder.append("\n");
        stringBuilder.append("接受消息次数：").append(receiveAllCount).append(", ")
                .append("总耗时：").append(receiveCostTime.toString(TimeFormat.FormatInfo.Second)).append(", ")
                .append("接受总流量：").append(receiveAllBytes.toString(ByteFormat.FormatInfo.All));
        stringBuilder.append("\n");
        stringBuilder
                .append("发送消息次数：").append(sendAllCount).append(", ")
                .append("发送总流量：").append(sendAllBytes.toString(ByteFormat.FormatInfo.All)).append(", ")
        ;
        stringBuilder.append("\n");
        stringBuilder.append("===================================================================================");
        stringBuilder.append("\n");

        collect = receiveMap.values().stream().sorted((o1, o2) -> o1.compareTo2(o2)).collect(Collectors.toList());
        for (MsgCount proxyCount : collect) {
            stringBuilder.append(proxyCount.toString()).append("\n");
        }

        stringBuilder.append("\n");
        stringBuilder.append("===================================================================================");
        stringBuilder.append("\n");

        collect = sendMap.values().stream().sorted((o1, o2) -> o1.compareTo1(o2)).collect(Collectors.toList());
        for (MsgCount proxyCount : collect) {
            stringBuilder.append(proxyCount.toString()).append("\n");
        }

        stringBuilder.append("\n");
        stringBuilder.append("===================================================================================");
        stringBuilder.append("\n");
        stringBuilder.append("用户：").append(userId).append("(").append(UserName).append(")");
        stringBuilder.append("\n");
        stringBuilder.append("本次在线时长：").append(new TimeFormat().addTime((System.currentTimeMillis() - loginTime) * 100).toString(TimeFormat.FormatInfo.All));
        stringBuilder.append("\n");
        stringBuilder.append("接受消息次数：").append(receiveAllCount).append(", ")
                .append("总耗时：").append(receiveCostTime.toString(TimeFormat.FormatInfo.Second)).append(", ")
                .append("接受总流量：").append(receiveAllBytes.toString(ByteFormat.FormatInfo.All));
        stringBuilder.append("\n");
        stringBuilder
                .append("发送消息次数：").append(sendAllCount).append(", ")
                .append("发送总流量：").append(sendAllBytes.toString(ByteFormat.FormatInfo.All)).append(", ")
        ;
        stringBuilder.append("\n");
        stringBuilder.append("===================================================================================");
        return stringBuilder.toString();
    }
}
