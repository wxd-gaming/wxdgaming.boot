package wxdgaming.boot.starter.action;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.net.controller.MappingProtoAction;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.ProtoMapping;
import wxdgaming.boot.starter.IocContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理通信
 *
 * @author: wxd-gaming(無心道, 15388152619)
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
                    MappingProtoAction.register(textController.service(), instance, methodList);
                }
            }
        });
    }

}
