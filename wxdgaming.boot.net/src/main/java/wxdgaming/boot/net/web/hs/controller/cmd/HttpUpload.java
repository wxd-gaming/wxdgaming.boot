package wxdgaming.boot.net.web.hs.controller.cmd;

import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.util.ReferenceCounted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.core.collection.MapOf;
import wxdgaming.boot.core.lang.RunResult;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.web.hs.HttpSession;

import java.io.File;
import java.util.Map;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-05-21 19:13
 **/
public interface HttpUpload {

    Logger log = LoggerFactory.getLogger(HttpUpload.class);

    /**
     * 上传文件，上传普通文件
     */
    @TextMapping
    default RunResult upload(HttpSession httpSession) throws Exception {
        String userHome = JvmUtil.userHome();
        String fileDir = httpSession.reqParam("filedir");

        String dirPath = null;
        if (StringUtil.emptyOrNull(fileDir)) {
            dirPath = FileUtil.getCanonicalPath(new File("upload"));
        } else {
            dirPath = FileUtil.getCanonicalPath(new File(fileDir));
        }

        if (!dirPath.endsWith("/")) {
            dirPath += "/";
        }
        if (!dirPath.startsWith(userHome)) {
            dirPath = FileUtil.getCanonicalPath(new File("upload"));
        }
        return saveUploadFile(dirPath, httpSession);
    }

    /**
     * 保存上传的文件
     */
    default RunResult saveUploadFile(String outDir, HttpSession request) throws Exception {
        Map<String, FileUpload> uploadFilesMap = request.getUploadFilesMap();
        if (uploadFilesMap.isEmpty()) {
            return RunResult.error(9, "没有收到上传的文件内容！");
        } else {
            if (!outDir.endsWith("/")) {
                outDir += "/";
            }
            new File(outDir).mkdirs();
            RunResult result = RunResult.ok();
            JSONObject data = MapOf.newJSONObject();
            try {
                for (Map.Entry<String, FileUpload> entry : uploadFilesMap.entrySet()) {
                    FileUpload fileUpload = entry.getValue();
                    File file = new File(outDir + fileUpload.getFilename());
                    fileUpload.renameTo(file);
                    String lastModified = request.getReqParams().getString(file.getName() + "_lastModified");
                    try {
                        if (lastModified != null) {
                            file.setLastModified(Long.parseLong(lastModified));
                        }
                    } catch (Exception e) {
                        log.warn("设置文件最后修改时间异常：{}", lastModified);
                    }
                    String msg = "上传成功, 文件：" + file.getCanonicalPath();
                    log.info(msg);
                    data.put(fileUpload.getFilename(), "ok");
                }
                result.data(data);
                return result;
            } finally {
                /*资源释放*/
                uploadFilesMap.values().forEach(ReferenceCounted::release);
            }
        }
    }

}
