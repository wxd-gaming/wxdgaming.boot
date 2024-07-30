package wxdgaming.boot.agent.zip;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 输出zip文件
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-05-07 18:55
 **/
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class OutZipFile implements Serializable, Closeable {

    FileOutputStream outputStream;
    ZipOutputStream zos;
    BufferedOutputStream bufferedOutputStream;

    public OutZipFile(String zipPath) {
        this(zipPath, false);
    }

    public OutZipFile(String zipPath, boolean append) {
        try {
            File file = FileUtil.createFile(zipPath);
            outputStream = new FileOutputStream(file, append);
            zos = new ZipOutputStream(outputStream);
            bufferedOutputStream = new BufferedOutputStream(zos);
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 新建一个压缩内容文件
     *
     * @param fileName
     * @return
     */
    public OutZipFile newZipEntry(String fileName) {
        try {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public OutZipFile write(String source) {
        write(source.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public OutZipFile write(byte[] bytes) {
        try {
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public OutZipFile putZipEntry(String fileName, String source) {
        putZipEntry(fileName, source.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    /**
     * 添加一个压缩文件内容
     *
     * @param fileName
     * @param bytes
     * @return
     */
    public OutZipFile putZipEntry(String fileName, byte[] bytes) {
        newZipEntry(fileName);
        write(bytes);
        return this;
    }

    @Override
    public void close() {
        try {
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
        } catch (Exception e) {
            log.error("关闭 zip 流", e);
        }
        try {
            if (zos != null) {
                zos.close();
            }
        } catch (Exception e) {
            log.error("关闭 zip 流", e);
        }
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            log.error("关闭 zip 流", e);
        }
    }

}
