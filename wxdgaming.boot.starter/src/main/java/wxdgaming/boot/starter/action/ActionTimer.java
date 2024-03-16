package wxdgaming.boot.starter.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.timer.ScheduledInfo;
import wxdgaming.boot.core.timer.ann.Scheduled;
import wxdgaming.boot.starter.IocContext;
import wxdgaming.boot.starter.service.ScheduledService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 处理定时器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-16 11:12
 **/
public class ActionTimer {

    public static void action(IocContext context, ReflectContext reflectContext) {
        final ScheduledService scheduledService = context.getInstance(ScheduledService.class);

        List<ScheduledInfo> jobList = new ArrayList<>();
        jobList.addAll(scheduledService.getJobList());

        reflectContext.stream().forEach(content -> {
            Stream<Method> methodStream = content.methodsWithAnnotated(Scheduled.class);
            action(context, jobList, methodStream);
        });

        jobList.sort(null);
        scheduledService.setJobList(jobList);
    }

    public static void action(IocContext context, List<ScheduledInfo> jobList, Stream<Method> stream) {
        Logger log = LoggerFactory.getLogger(ActionTimer.class);
        List<Method> list = stream.toList();
        try (StreamWriter streamWriter = new StreamWriter()) {
            for (Method method : list) {
                streamWriter.writeLn();
                streamWriter.write("===============================================timer job=========================================================").writeLn()
                        .write("class = ").write(method.getDeclaringClass().getName()).writeLn();
                Object instance = context.getInstance(method.getDeclaringClass());
                Scheduled scheduled = AnnUtil.ann(method, Scheduled.class);
                if (scheduled != null) {
                    if (method.getParameterCount() > 0) {
                        throw new RuntimeException(method.getDeclaringClass().getName() + " timer job " + method.getName() + " 不允许有参数");
                    }
                    ScheduledInfo scheduledInfo = new ScheduledInfo(instance, method, scheduled);
                    jobList.removeIf(v -> v.toString().equalsIgnoreCase(scheduledInfo.toString()));
                    jobList.add(scheduledInfo);
                    streamWriter.write(scheduledInfo.toString()).writeLn();
                }
                streamWriter.write("===============================================timer job=========================================================").writeLn();
            }
            if (!streamWriter.isEmpty()) {
                log.debug(streamWriter.toString());
            }
        }
    }

}
