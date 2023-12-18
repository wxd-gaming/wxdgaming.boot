package org.wxd.boot.starter.action;

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.system.AnnUtil;
import org.wxd.boot.agent.system.MethodUtil;
import org.wxd.boot.agent.system.ReflectContext;
import org.wxd.boot.net.SocketSession;
import org.wxd.boot.net.controller.MappingFactory;
import org.wxd.boot.net.controller.ann.ProtoController;
import org.wxd.boot.net.controller.ann.ProtoMapping;
import org.wxd.boot.net.message.MessagePackage;
import org.wxd.boot.starter.IocContext;
import org.wxd.boot.str.StringUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理通信
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-16 11:00
 **/
@Slf4j
public class ActionProtoController {

    public static void action(IocContext iocInjector, ReflectContext reflectContext) {
        reflectContext.withAnnotated(ProtoController.class).forEach(content -> {
            Class<?> aClass = content.getCls();
            Collection<ProtoController> controllerList = AnnUtil.annStream(aClass, ProtoController.class).toList();
            if (controllerList.isEmpty()) {
                log.debug("类：{} 没有标记 {} 已经忽略", aClass.getName(), ProtoController.class);
            } else {
                for (ProtoController textController : controllerList) {
                    if (textController.alligatorAutoRegister()) {
                        log.debug("自动注册 {} 过滤", aClass);
                        continue;
                    }
                    Object instance = iocInjector.getInstance(aClass);
                    List<Method> methodList = content.methodsWithAnnotated(ProtoMapping.class).collect(Collectors.toList());
                    register(textController.service(), instance, methodList);
                }
            }
        });
    }

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

    static void register(String serviceName, Object instance, Collection<Method> methodList) {
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
            if (!SocketSession.class.isAssignableFrom(socketClass) || !Message.class.isAssignableFrom(messageClass)) {
                throw new Throw("文件：" + instance.getClass().getName() + " 注册 proto service 方法 " + method + " 参数异常 第一个参数：" + SocketSession.class + ", 第二个参数：" + Message.class);
            }
            if (!MessagePackage.MsgName2IdMap.containsKey(messageClass.getName())) {
                MessagePackage.loadMessageId_HashCode(messageClass, true);
            }

            int messageId = MessagePackage.getMessageId(messageClass);

            String remarks = protoMapping.remarks();
            if (StringUtil.emptyOrNull(remarks)) {
                remarks = instance.getClass().getName() + "." + method.getName();
            }
            MappingFactory.putProto(
                    serviceName,
                    remarks,
                    messageId,
                    instance,
                    method,
                    protoMapping.logTime(),
                    protoMapping.warningTime()
            );
            /**消息的中午注解隐射*/
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
