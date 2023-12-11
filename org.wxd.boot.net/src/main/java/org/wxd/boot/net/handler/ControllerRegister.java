package org.wxd.boot.net.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wxd.boot.net.controller.ProtoMappingRecord;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Controller 映射
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-05-18 09:17
 **/
public interface ControllerRegister {

    final Logger log = LoggerFactory.getLogger(ControllerRegister.class);

    /** 消息映射注册 */
    ConcurrentMap<Integer, ProtoMappingRecord> getMessageBeanMap();

    ControllerRegister setMessageBeanMap(ConcurrentMap<Integer, ProtoMappingRecord> messageBeanMap);

    /**
     * 获取消息注册信息
     *
     * @param mid
     * @return
     */
    default ProtoMappingRecord getMessageMapping(int mid) {
        return getMessageBeanMap().get(mid);
    }


    ControllerRegister setMsgIds(List<Integer> msgIds);

    default void resetMsgIds() {
        Integer[] integers = getMessageBeanMap().keySet().toArray(new Integer[getMessageBeanMap().size()]);
        setMsgIds(Arrays.asList(integers));
    }


}
