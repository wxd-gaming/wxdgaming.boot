package wxdgaming.boot.net.web.hs.controller.cmd;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.system.JvmUtil;
import wxdgaming.boot.net.controller.ann.TextMapping;
import wxdgaming.boot.net.http.HttpDataAction;
import wxdgaming.boot.net.web.hs.HttpServer;
import wxdgaming.boot.net.web.hs.HttpSession;
import wxdgaming.boot.net.web.hs.util.FtpFileUtil;

import java.io.File;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-07-02 22:28
 **/
public interface HttpFtp {

    Logger log = LoggerFactory.getLogger(HttpFtp.class);

    @TextMapping
    default void ftp(HttpSession httpSession, JSONObject putData) throws Exception {

        String userHome = JvmUtil.userHome();
        userHome = FileUtil.getCanonicalPath(userHome);

        boolean show = putData.getBooleanValue("show");

        int pageSize = putData.getIntValue("pageSize");
        int pageNumber = putData.getIntValue("pageNumber");
        String search = putData.getString("search");

        String path = putData.getString("path");
        if (StringUtil.emptyOrNull(path)) {
            path = userHome;
        }

        path = HttpDataAction.urlDecoder(path);
        path = FileUtil.getCanonicalPath(path);
        if (!path.startsWith(userHome)) {
            path = userHome;
        }

        final File file = new File(path);
        if (file.isFile()) {
            if (show) {
                httpSession.responseText(FileReadUtil.readBytes(file));
            } else {
                HttpServer.downloadFile(httpSession, file);
            }
        } else {
            FtpFileUtil.ftpFile(httpSession, httpSession.getUriPath(), path, search, pageSize, pageNumber);
        }
    }
}
