package org.wxd.boot.httpclient.uclient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.httpclient.HttpContentType;
import org.wxd.boot.httpclient.HttpDataAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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
public class UrlPostFile extends UrlPostMulti {

    protected final List<File> uploadFiles = new ArrayList<>();

    protected UrlPostFile(String uriPath) {
        super(uriPath);
        this.reqHttpMethod = "POST";
        this.httpContentType = HttpContentType.Multipart;
    }

    protected void writeTextParams(OutputStream outputStream, OutputStreamWriter outWriter) throws Exception {
        writeFileParams(outputStream, outWriter);
        super.writeTextParams(outputStream, outWriter);
    }

    /**
     * 文件数据
     */
    protected void writeFileParams(OutputStream outputStream, OutputStreamWriter outWriter) throws Exception {
        if (this.uploadFiles != null && !this.uploadFiles.isEmpty()) {
            if (httpContentType != HttpContentType.Multipart) {
                throw new RuntimeException("需要上传文件 请使用 HttpContentType.Multipart");
            }

            for (File file : uploadFiles) {
                putParams(file.getName() + "_lastModified", file.lastModified() + "");
                outWriter.append("--").append(boundary).append("\r\n");

                outWriter.append("Content-Disposition: form-data; name=\"").append(file.getName())
                        .append("\"; filename=\"").append(HttpDataAction.urlEncoder(file.getName())).append("\"\r\n");

                outWriter.append("Content-Type: ").append(getContentType(file).getValue()).append("\r\n");
                outWriter.append("Content-Transfer-Encoding: binary \r\n");
                outWriter.append("\r\n");

                try (FileInputStream inStream = new FileInputStream(file)) {
                    int bytes = 0;
                    byte[] bufferByte = new byte[inStream.available()];
                    while ((bytes = inStream.read(bufferByte)) != -1) {
                        outputStream.write(bufferByte, 0, bytes);
                    }
                }
                outWriter.append("\r\n");
            }
        }
    }
}
