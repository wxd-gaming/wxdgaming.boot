package org.wxd.boot.agent.zip;

import lombok.extern.slf4j.Slf4j;
import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.agent.function.ConsumerE2;
import org.wxd.boot.agent.io.FileReadUtil;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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

    final ZipFile zip;

    public ReadZipFile(String zipPath) {
        try {
            zip = new ZipFile(new File(zipPath));
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public ReadZipFile(File zipPath) {
        try {
            zip = new ZipFile(zipPath);
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
        return unzipFile(zipEntry);
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
            byte[] bytes = unzipFile(zipEntry);
            try {
                eConsumer.accept(vName, bytes);
            } catch (Exception e) {
                throw Throw.as(e);
            }
        });
    }

    byte[] unzipFile(ZipEntry zipEntry) {
        AtomicReference<byte[]> bytes = new AtomicReference<>();
        unzipFileStream(zipEntry, (ConsumerE2<String, InputStream>) (name, inputStream) -> bytes.set(FileReadUtil.readBytes(inputStream)));
        return bytes.get();
    }

    public void forEachStream(ConsumerE2<String, InputStream> call) {
        zip.stream().forEach(zipEntry -> {
            if (!zipEntry.isDirectory())
                unzipFileStream(zipEntry, call);
        });
    }

    void unzipFileStream(ZipEntry zipEntry, ConsumerE2<String, InputStream> call) {
        try (InputStream inputStream = zip.getInputStream(zipEntry)) {
            call.accept(zipEntry.getName(), inputStream);
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

}
