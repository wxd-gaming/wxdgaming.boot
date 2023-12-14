package org.wxd.boot.starter.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.agent.system.AnnUtil;
import org.wxd.agent.system.ReflectContext;
import org.wxd.boot.append.StreamBuilder;
import org.wxd.boot.starter.IocContext;
import org.wxd.boot.starter.service.ScheduledService;
import org.wxd.boot.timer.ScheduledInfo;
import org.wxd.boot.timer.ann.Scheduled;

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
        Stream<Method> methodStream = reflectContext.methodsWithAnnotated(Scheduled.class);
        ScheduledService scheduledService = context.getInstance(ScheduledService.class);
        if (scheduledService == null) {
            if (methodStream.count() > 0) {
                throw new RuntimeException("无法找到定时器任务 TimerJobRepository ");
            }
            return;
        }
        List<ScheduledInfo> jobList = new ArrayList<>();
        jobList.addAll(scheduledService.getJobList());
        action(context, jobList, methodStream);
        jobList.sort(null);
        scheduledService.setJobList(jobList);
    }

    public static void action(IocContext context, List<ScheduledInfo> jobList, Stream<Method> stream) {
        Logger log = LoggerFactory.getLogger(ActionTimer.class);

        List<Method> list = stream.toList();
        try (StreamBuilder streamBuilder = new StreamBuilder()) {
            for (Method method : list) {
                streamBuilder.appendLn();
                streamBuilder.append("===============================================timer job=========================================================").appendLn()
                        .append("class = ").append(method.getDeclaringClass().getName()).appendLn();
                Object instance = context.getInstance(method.getDeclaringClass());
                Scheduled scheduled = AnnUtil.ann(method, Scheduled.class);
                if (scheduled != null) {
                    if (method.getParameterCount() > 0) {
                        throw new RuntimeException(method.getDeclaringClass().getName() + " timer job " + method.getName() + " 不允许有参数");
                    }
                    ScheduledInfo scheduledInfo = new ScheduledInfo(instance, method, scheduled);
                    jobList.removeIf(v -> v.toString().equals(scheduledInfo.toString()));
                    jobList.add(scheduledInfo);
                    streamBuilder.append(scheduledInfo.toString()).appendLn();
                }
                streamBuilder.append("===============================================timer job=========================================================").appendLn();
            }
            log.debug(streamBuilder.toString());
        }
    }

}
