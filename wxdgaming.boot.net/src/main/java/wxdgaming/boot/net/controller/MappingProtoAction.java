package wxdgaming.boot.net.controller;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.MethodUtil;
import wxdgaming.boot.core.str.StringUtils;
import wxdgaming.boot.net.NioBase;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.ProtoMapping;
import wxdgaming.boot.net.message.MessagePackage;
import wxdgaming.boot.net.pojo.PojoBase;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 解析处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-07-24 19:19
 **/
@Slf4j
public class MappingProtoAction {

    public static void register(Object instance) {
        Collection<Method> methodList = MethodUtil.readAllMethod(instance.getClass()).values();
        Class<?> aClass = instance.getClass();
        Collection<ProtoController> controllerList = AnnUtil.annStream(aClass, ProtoController.class).toList();
        if (controllerList.isEmpty()) {
            log.debug("类：{} 没有标记 {} 已经忽略", aClass.getName(), ProtoController.class);
        } else {
            for (ProtoController textController : controllerList) {
                if (textController.alligatorAutoRegister()) {
                    log.debug("自动注册 {} 过滤", aClass);
                    continue;
                }
                register(textController.service(), instance, methodList);
            }
        }
    }

    public static void register(Class<? extends NioBase> serviceName, Object instance, Collection<Method> methodList) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nprotobuf controller").append(" ").append(" \n")
                .append(instance.getClass().getSimpleName()).append("\n");

        int msize = 0;
        for (Method method : methodList) {
            ProtoMapping protoMapping = AnnUtil.ann(method, ProtoMapping.class);
            if (protoMapping == null) {
                continue;
            }
            Class<?> socketClass = method.getParameterTypes()[0];

            Class<?> messageClass = method.getParameterTypes()[1];
            if (!SocketSession.class.isAssignableFrom(socketClass) || !PojoBase.class.isAssignableFrom(messageClass)) {
                throw new Throw("文件：" + instance.getClass().getName() + " 注册 proto service 方法 " + method + " 参数异常 第一个参数：" + SocketSession.class + ", 第二个参数：" + PojoBase.class);
            }
            if (!MessagePackage.MsgName2IdMap.containsKey(messageClass.getName())) {
                MessagePackage.loadMessageId_HashCode(messageClass, true);
            }

            int messageId = MessagePackage.getMessageId(messageClass);

            String remarks = protoMapping.remarks();
            if (StringUtils.isBlank(remarks)) {
                remarks = instance.getClass().getName() + "." + method.getName();
            }
            MappingFactory.putProto(
                    serviceName,
                    remarks,
                    messageId,
                    instance,
                    method
            );
            /*消息的中午注解映射*/
            MessagePackage.MsgName2RemarkMap.put(messageClass.getName(), remarks);
            if (log.isDebugEnabled()) {
                if (msize > 0) {
                    stringBuilder.append('\n');
                }

                stringBuilder
                        .append("消息 = ").append(messageId).append(":").append(messageClass.getName()).append("\n")
                        .append("备注 = ").append(remarks).append("\n")
                        .append("方法 = ").append(method.getName())
                ;
            }
            msize++;

        }
        if (log.isDebugEnabled()) {
            log.debug(stringBuilder.toString());
        }
    }
}
