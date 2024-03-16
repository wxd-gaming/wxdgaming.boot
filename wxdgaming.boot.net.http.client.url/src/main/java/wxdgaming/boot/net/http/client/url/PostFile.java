package wxdgaming.boot.net.http.client.url;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.core.append.StreamWriter;
import wxdgaming.boot.core.collection.ObjMap;
import wxdgaming.boot.net.http.HttpDataAction;
import wxdgaming.boot.net.http.HttpHeadValueType;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于 HttpURLConnection 信息请求
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-05 12:55
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class PostFile extends PostMulti {

    protected final List<File> uploadFiles = new ArrayList<>();

    public PostFile(String uriPath) {
        super(uriPath);
        this.reqHttpMethod = "POST";
        this.contentType = HttpHeadValueType.Multipart;
    }

    @Override protected void writeTextParams(StreamWriter outWriter) throws Exception {
        writeFileParams(outWriter);
        super.writeTextParams(outWriter);
    }

    /**
     * 文件数据
     */
    protected void writeFileParams(StreamWriter outWriter) throws Exception {
        if (this.uploadFiles != null && !this.uploadFiles.isEmpty()) {
            if (contentType != HttpHeadValueType.Multipart) {
                throw new RuntimeException("需要上传文件 请使用 HttpContentType.Multipart");
            }

            for (File file : uploadFiles) {
                putParams(file.getName() + "_lastModified", file.lastModified() + "");
                outWriter.write("--").write(boundary).write("\r\n");

                outWriter.write("Content-Disposition: form-data; name=\"").write(file.getName())
                        .write("\"; filename=\"").write(HttpDataAction.urlEncoder(file.getName())).write("\"\r\n");

                outWriter.write("Content-Type: ").write(HttpHeadValueType.findContentType(file).getValue()).write("\r\n");
                outWriter.write("Content-Transfer-Encoding: binary \r\n");
                outWriter.write("\r\n");

                try (FileInputStream inStream = new FileInputStream(file)) {
                    int bytes = 0;
                    byte[] bufferByte = new byte[inStream.available()];
                    while ((bytes = inStream.read(bufferByte)) != -1) {
                        outWriter.write(bufferByte, 0, bytes);
                    }
                }
                outWriter.write("\r\n");
            }
        }
    }

    public PostFile addFile(File file) {
        this.uploadFiles.add(file);
        return this;
    }

    @Override public PostFile setUrlEncoder(boolean urlEncoder) {
        super.setUrlEncoder(urlEncoder);
        return this;
    }

    @Override public PostFile setReqMap(ObjMap reqMap) {
        super.setReqMap(reqMap);
        return this;
    }

    @Override public PostFile putParams(Object key, Object value) {
        super.putParams(key, value);
        return this;
    }

    @Override public PostFile putParams(Map map) {
        super.putParams(map);
        return this;
    }
}
