package wxdgaming.boot.net.handler;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.system.MarkTimer;
import wxdgaming.boot.net.SocketSession;
import wxdgaming.boot.net.controller.MappingFactory;
import wxdgaming.boot.net.controller.TextMappingRecord;
import wxdgaming.boot.net.controller.ann.ProtoController;
import wxdgaming.boot.net.controller.ann.ProtoMapping;
import wxdgaming.boot.net.message.UpFileAccess;
import wxdgaming.boot.net.message.rpc.ReqRemote;

/**
 * 处理 ReqRemote 消息
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-23 17:25
 **/
@Slf4j
@ProtoController
public class ReqRemoteHandler {

    @ProtoMapping
    public void action(SocketSession session, ReqRemote reqRemote) throws Exception {
        long rpcId = reqRemote.getRpcId();
        String params = reqRemote.getParams();
        if (reqRemote.getGzip() == 1) {
            params = GzipUtil.unGzip2String(params);
        }
        /*处理消息--理论上是丢出去了的*/
        final JSONObject putData = FastJsonUtil.parse(params);
        String cmd = reqRemote.getCmd().toLowerCase();
        switch (cmd) {
            case "rpc.heart" -> {
                session.rpcResponse(rpcId, "OK!");
            }
            case "rpc.upload.file.head" -> {
                /*todo 接收文件头*/
                long fileId = putData.getLongValue("fileId");
                int bodyCount = putData.getIntValue("bodyCount");
                String objectString = putData.getString("params");
                UpFileAccess.readHead(fileId, bodyCount, objectString);
                session.rpcResponse(rpcId, "OK!");
            }
            case "rpc.upload.file.body" -> {
                /*todo 接收文件内容*/
                long fileId = putData.getLongValue("fileId");
                int bodyId = putData.getIntValue("bodyId");
                int offset = putData.getIntValue("offset");
                byte[] datas = putData.getObject("datas", byte[].class);
                UpFileAccess fileAccess = UpFileAccess.readBody(fileId, bodyId, offset, datas);
                if (fileAccess.getReadMaxCount() <= bodyId) {
                    /*表示读取完成*/
                    fileAccess.saveFile();
                }
                session.rpcResponse(rpcId, "OK!");
            }
            default -> {
                if (StringUtil.emptyOrNull(cmd)) {
                    log.info("{} 命令参数 cmd , 未找到", session.toString());
                    if (rpcId > 0) {
                        session.rpcResponse(rpcId, RunResult.error("命令参数 cmd , 未找到").toJSONString());
                    }
                    return;
                }
                actionCmdListener(session, rpcId, cmd, putData);
            }
        }
    }

    void actionCmdListener(SocketSession session, long rpcId, String cmd, JSONObject putData) {
        final String methodNameLowerCase = cmd.toLowerCase().trim();
        TextMappingRecord mappingRecord = MappingFactory.textMappingRecord(session.getNioBaseClass(), methodNameLowerCase);
        if (mappingRecord == null) {
            log.info("{} not found url {}", session.toString(), cmd);
            if (rpcId > 0) {
                session.rpcResponse(rpcId, RunResult.error("not found url " + cmd).toJSONString());
            }
            return;
        }
        final MarkTimer markTimer = MarkTimer.build();
        final StreamWriter outAppend = new StreamWriter(1024);
        TextListenerAction listenerAction = new TextListenerAction(mappingRecord, session, cmd, putData, outAppend, (showLog) -> {
            if (showLog) {
                log.info("\n执行：{}\n{}\nrpcId={}\ncmd = {}, {}\n结果 = {}",
                        session.toString(), markTimer.execTime2String(), rpcId, cmd, FastJsonUtil.toJson(putData), outAppend.toString());
            }
            if (rpcId > 0) {
                session.rpcResponse(rpcId, outAppend.toString());
            }
        });

        if (MappingFactory.TextMappingSubmitBefore != null) {
            try {
                Boolean apply = MappingFactory.TextMappingSubmitBefore.apply(session, listenerAction);
                if (Boolean.FALSE.equals(apply)) return;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        listenerAction.submit();
    }

}
