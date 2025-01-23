package wxdgaming.boot.net.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.system.MarkTimer;
import wxdgaming.boot.core.timer.MyClock;
import wxdgaming.boot.net.SocketSession;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 利用rpc通信传输文件
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-02-01 10:01
 **/
@Slf4j
@Getter
public class UpFileAccess implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final Map<Long, UpFileAccess> FileAccessMap = new ConcurrentHashMap<>();

    public static final String FileDir = "FileDir";
    public static final String FileName = "FileName";
    public static final String FileLen = "FileLen";
    public static final String FileLastModified = "FileLastModified";

    /**
     * 单次传输控制大小，
     */
    public static int SpiltLen = 30 * 1024;

    public static UpFileAccess builder(String upFileDir, String filePath) throws Exception {
        return builder(upFileDir, new File(filePath));
    }

    public static UpFileAccess builder(String upFileDir, File file) throws Exception {
        if (!file.exists()) {
            throw new Throw("文件：" + file.getPath() + ", 找不到");
        }
        if (!file.isFile()) {
            throw new Throw("文件：" + file.getPath() + ", 不是文件，不支持文件夹传输");
        }

        UpFileAccess upFileAccess = new UpFileAccess();
        upFileAccess.fileId = StringUtil.hashcode(file.getPath());
        upFileAccess.filePath = file.getPath();
        upFileAccess.jsonObject = MapOf.newJSONObject();
        upFileAccess.jsonObject.put(FileDir, upFileDir);
        upFileAccess.jsonObject.put(FileName, file.getName());
        upFileAccess.jsonObject.put(FileLen, file.length());
        upFileAccess.jsonObject.put(FileLastModified, file.lastModified());
        upFileAccess.fileDatas = FileReadUtil.readBytes(file);

        return upFileAccess;
    }

    public static UpFileAccess readHead(long fileId, int readMaxCount, String jsonParams) throws Exception {
        UpFileAccess fileAccess = new UpFileAccess();
        fileAccess.fileId = fileId;
        fileAccess.readMaxCount = readMaxCount;
        fileAccess.jsonObject = FastJsonUtil.parse(jsonParams);
        fileAccess.fileName = fileAccess.jsonObject.getString(FileName);

        String fileDir = fileAccess.jsonObject.getString(FileDir);
        String upLoadPath;
        if (StringUtil.emptyOrNull(fileDir)) {
            upLoadPath = FileUtil.getCanonicalPath(new File("upload"));
        } else {
            upLoadPath = FileUtil.getCanonicalPath(new File(fileDir));
        }

        if (!upLoadPath.endsWith("\\|/")) {
            upLoadPath += "/";
        }
        fileAccess.filePath = upLoadPath + fileAccess.fileName;

        int fileLen = fileAccess.jsonObject.getIntValue(FileLen);
        fileAccess.fileDatas = new byte[fileLen];
        FileAccessMap.put(fileAccess.fileId, fileAccess);
        return fileAccess;
    }

    public static UpFileAccess readBody(long fileId, int bodyId, int offSet, byte[] datas) {
        UpFileAccess fileAccess = FileAccessMap.get(fileId);

        System.arraycopy(datas, 0, fileAccess.fileDatas, offSet, datas.length);

        if (bodyId >= fileAccess.readMaxCount) {
            FileAccessMap.remove(fileAccess.fileId);
        }

        return fileAccess;
    }

    /*传输的唯一id*/
    private long fileId;
    /*附加参数*/
    private JSONObject jsonObject;
    private String filePath;
    private String fileName;
    private final MarkTimer markTime = MarkTimer.build();
    private int readMaxCount = 0;
    private byte[] fileDatas;

    public void upload(SocketSession session) {
        JSONObject headMap = MapOf.newJSONObject();
        headMap.fluentPut("fileId", this.fileId);
        headMap.fluentPut("params", jsonObject.toString());
        AtomicInteger forCount = new AtomicInteger(fileDatas.length / SpiltLen);
        int forCount1 = fileDatas.length % SpiltLen;
        if (forCount1 > 0) {
            forCount.incrementAndGet();
        }
        /*设置消息内容发送次数*/
        headMap.fluentPut("bodyCount", forCount);

        session.rpc("rpc.upload.file.head", headMap.toJSONString()).asyncString((result) -> {

            if (!"OK!".equalsIgnoreCase(result)) {
                throw new Throw("文件传输失败-HEAD：" + result);
            }

            if (forCount.get() >= 1) {
                for (int i = 0; i < forCount.get(); i++) {
                    int offSet = i * SpiltLen;
                    int readCount = fileDatas.length - offSet;
                    if (readCount > SpiltLen) {
                        readCount = SpiltLen;
                    }

                    JSONObject bodyMap = MapOf.newJSONObject();
                    headMap.fluentPut("fileId", this.fileId);
                    /*设置消息内容发送次数*/
                    headMap.fluentPut("bodyId", i + 1);
                    headMap.fluentPut("offset", offSet);
                    headMap.fluentPut("datas", ByteString.copyFrom(this.fileDatas, offSet, readCount).toByteArray());

                    result = session.rpc("rpc.upload.file.body", bodyMap.toJSONString()).get();
                    if (!"OK!".equalsIgnoreCase(result)) {
                        throw new Throw("文件传输失败-BODY：" + result);
                    }
                }
            }
            String retStr = "发送文件：" + formatString();
            log.warn(retStr);
        });
    }

    public String saveFile() throws Exception {
        File file = new File(filePath);
        FileWriteUtil.writeBytes(file, fileDatas);
        file.setLastModified(jsonObject.getLongValue(FileLastModified));
        String retStr = "接收文件：" + formatString();
        log.warn(retStr);
        return retStr;
    }

    public String formatString() {

        float execTime = markTime.execTime();
        float s = (fileDatas.length / 1024f) / execTime * 1000;

        return filePath
               + ", 最后修改时间：" + MyClock.formatDate(MyClock.SDF_YYYYMMDDHHMMSS_1, jsonObject.getLongValue(FileLastModified))
               + ", 大小：" + fileDatas.length + " b"
               + ", 耗时：" + execTime + " ms, 速率：" + (s) + " KB/S";
    }

}
