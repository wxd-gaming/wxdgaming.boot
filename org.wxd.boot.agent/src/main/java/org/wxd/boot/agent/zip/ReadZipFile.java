package org.wxd.agent.zip;

import lombok.extern.slf4j.Slf4j;
import org.wxd.agent.exception.Throw;
import org.wxd.agent.function.ConsumerE2;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 读取 zip 文件
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-09 20:17
 **/
@Slf4j
public class ReadZipFile implements Serializable, Closeable {

    ZipFile zip;

    public ReadZipFile(String zipPath) {
        try {
            zip = new ZipFile(new File(zipPath));
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    @Override
    public void close() {
        try {
            if (zip != null) {
                zip.close();
            }
        } catch (Exception e) {
            log.error("关闭流：" + zip.getName(), e);
        }
    }

    public byte[] find(String entryName) {
        ZipEntry zipEntry = zip.getEntry(entryName);
        return unzipFile(zip, zipEntry);
    }

    /** 读取一个 .zip 所有内容 */
    public Map<String, byte[]> readAll() {
        Map<String, byte[]> map = new LinkedHashMap<>();
        forEach((name, bytes) -> map.put(name, bytes));
        return map;
    }

    /**
     * 循环读取所有
     *
     * @param eConsumer 循环
     * @return
     */
    public void forEach(ConsumerE2<String, byte[]> eConsumer) {
        zip.stream().forEach(zipEntry -> {
            final String vName = zipEntry.getName();
            final byte[] bytes = unzipFile(zip, zipEntry);
            try {
                eConsumer.accept(vName, bytes);
            } catch (Exception e) {
                throw Throw.as(e);
            }
        });
    }

    byte[] unzipFile(ZipFile zip, ZipEntry zipEntry) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (InputStream inputStream = zip.getInputStream(zipEntry)) {
                int count = 0;
                byte[] readBuffer = new byte[512];
                while ((count = inputStream.read(readBuffer, 0, readBuffer.length)) != -1) {
                    out.write(readBuffer, 0, count);
                }
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

}
