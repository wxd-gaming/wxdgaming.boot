package wxdgaming.boot.net.message;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.net.pojo.PojoBase;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 消息容器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-07 18:14
 **/
@Slf4j
public class MessagePackage {


    /** 消息的id和名字映射 */
    public static final Map<Integer, String> MsgId2NameMap = new ConcurrentSkipListMap<>();
    /** 消息的名字和id映射 */
    public static final Map<String, Integer> MsgName2IdMap = new ConcurrentSkipListMap<>();
    /** 消息备注 */
    public static final Map<String, String> MsgName2RemarkMap = new ConcurrentSkipListMap<>();
    public static final Map<Integer, Class<? extends PojoBase>> MsgId2ClassMap = new ConcurrentSkipListMap<>();

    static {
        /*注册同步消息算法*/
        // loadMessageId_HashCode(ReqRemote.class, false);
        // loadMessageId_HashCode(ResRemote.class, false);
    }

    public static void main(String[] args) {
        System.out.println(StringUtil.hashcode(new String(new char[]{2, 1})));
        System.out.println(StringUtil.hashcode(new String(new char[]{1, 2})));
        System.out.println(StringUtil.hashcode("re"));
        System.out.println(StringUtil.hashcode("er"));
    }

    /** 获取 获取消息信息 messageName = msgid */
    static public String msgInfo(Class<?> message) {
        return msgInfo(getMessageId(message));
    }

    /** 获取 获取消息信息 messageName = msgid */
    static public String msgInfo(int msgId) {
        String msgName = MsgId2NameMap.get(msgId);
        if (msgName == null) {
            return msgId + " = null";
        }
        return msgId + " = " + msgName;
    }

    /** 获取消息id */
    static public int getMessageId(Class<?> message) {
        String simpleName = message.getName();
        return getMessageId(simpleName);
    }

    /** 获取消息id */
    static public int getMessageId(String simpleName) {
        Integer msgId = MsgName2IdMap.get(simpleName);
        if (msgId == null) {
            log.warn("没有注册的消息：{}", simpleName, new RuntimeException("路由跟踪"));
            return 0;
        }
        return msgId;
    }

    static public <R extends PojoBase> R parseMessage(int messageId, byte[] bytes) {
        try {
            Class<? extends PojoBase> messageBuilder = MsgId2ClassMap.get(messageId);
            if (messageBuilder == null) throw new RuntimeException("找不到消息：" + messageId);
            PojoBase pojoBase = ReflectContext.newInstance(messageBuilder);
            pojoBase.decode(bytes);
            return (R) pojoBase;
        } catch (Exception e) {
            throw Throw.of("messageId=" + messageId, e);
        }
    }

    /**
     * 用名字 获取 hashcode 方案
     *
     * @param packageNames 包名
     */
    static public void loadMessageId_HashCode(String... packageNames) {
        loadMessageId_HashCode(Thread.currentThread().getContextClassLoader(), packageNames);
    }

    static public void loadMessageId_HashCode(ClassLoader classLoader, String... packageNames) {
        ReflectContext.Builder
                .of(classLoader, packageNames)
                .build()
                .classStream()
                .forEach(aClass -> loadMessageId_HashCode(aClass, true));
    }

    /** 用名字 获取 hashcode 方案 */
    static public void loadMessageId_HashCode(Class<?> declaredClass, boolean reqOrRes) {
        _loadMessageId_HashCode(declaredClass, reqOrRes);
        Class<?>[] declaredClasses = declaredClass.getDeclaredClasses();
        if (declaredClasses == null || declaredClasses.length == 0) {
            return;
        }
        for (Class<?> clazz : declaredClasses) {
            _loadMessageId_HashCode(clazz, reqOrRes);
        }
    }

    /** 用名字 获取 hashcode 方案 */
    static void _loadMessageId_HashCode(Class<?> declaredClass, boolean reqOrRes) {
        if (PojoBase.class.isAssignableFrom(declaredClass)) {
            final String messageName = declaredClass.getName();
            if (reqOrRes) {
                if (!declaredClass.getSimpleName().startsWith("Req")
                    && !declaredClass.getSimpleName().startsWith("Res")) {
                    return;
                }
            }
            int number = StringUtil.hashcode(declaredClass.getSimpleName());
            final Integer nameNumber = MsgName2IdMap.get(messageName);
            if (nameNumber != null && number != nameNumber) {
                throw new RuntimeException("存在相同的消息名称：" + messageName + "=" + nameNumber);
            }
            final String numberName = MsgId2NameMap.get(number);
            if (numberName != null && !messageName.equalsIgnoreCase(numberName)) {
                throw new RuntimeException("存在相同的消息协议id：" + messageName + "=" + number + "," + numberName + "=" + number);
            }
            if (log.isDebugEnabled())
                log.debug("读取消息解析文件：{} 类", declaredClass.getName());
            add(number, (Class<? extends PojoBase>) declaredClass);
        }
    }

    public static void add(int messageId, Class<? extends PojoBase> messageClass) {
        try {
            MsgId2ClassMap.put(messageId, messageClass);
            MsgId2NameMap.put(messageId, messageClass.getName());
            MsgName2IdMap.put(messageClass.getName(), messageId);
            log.info("读取消息解析文件：{} = {}", messageId, messageClass.getName());
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

}

