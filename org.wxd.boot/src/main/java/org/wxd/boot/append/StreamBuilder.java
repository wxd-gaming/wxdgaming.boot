package org.wxd.boot.append;


import org.wxd.boot.agent.exception.Throw;
import org.wxd.boot.str.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

/**
 * 输出
 * <p>
 * 并发会导致输出混乱
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-01-12 15:02
 **/
public class StreamBuilder implements Closeable, AutoCloseable {

    public static void main(String[] args) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.reset();
        StreamBuilder out = new StreamBuilder(byteArrayOutputStream);
        out.append(1);
        out.appendFmt("%-10s", "int");
        out.appendFmt("%-10s", "ddd");
        System.out.println(out.toString());
        System.out.printf(String.format("%-10s", "int", "rtrtrt"));
    }

    protected final ByteArrayOutputStream outputStream;

    public StreamBuilder() {
        this(1024);
    }

    public StreamBuilder(int minCapacity) {
        this(new ByteArrayOutputStream(minCapacity));
    }

    public StreamBuilder(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void close() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Throwable throwable) {
                throw Throw.as("关闭资源", throwable);
            }
        }
    }

    /**
     * 转化成字符串输出
     *
     * @param append
     * @return
     */
    public StreamBuilder appendLn(Object append) {
        append(append);
        appendLn();
        return this;
    }

    /**
     * 使用utf-8转化字符串
     *
     * @param append
     * @return
     */
    public StreamBuilder append(Object append) {
        append(append, StandardCharsets.UTF_8);
        return this;
    }

    /**
     * 转化成字符串输出
     *
     * @param append
     * @param charsetName
     * @return
     */
    public StreamBuilder appendLn(Object append, Charset charsetName) {
        append(append, charsetName);
        appendLn();
        return this;
    }

    /**
     * @param append
     * @param charsetName
     * @return
     */
    public StreamBuilder append(Object append, Charset charsetName) {
        if (append == null || StringUtil.nullStr.equals(append)) {
            append(StringUtil.nullBytes);
        } else if (append instanceof byte[]) {
            append((byte[]) append);
        } else {
            append(String.valueOf(append).getBytes(charsetName));
        }
        return this;
    }

    public StreamBuilder append(Throwable exception) {
        final String s = Throw.ofString(exception);
        append(s.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public StreamBuilder append(byte[] bytes) {
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
            }
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public StreamBuilder append(byte[] bytes, int off, int len) {
        try {
            if (outputStream != null) {
                outputStream.write(bytes, off, len);
            }
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 格式化输出
     * <p>
     * 性能消耗过大
     */
    public StreamBuilder appendFmt(String format, Object... args) {
        append(new Formatter().format(format, args).toString());
        return this;
    }

    /**
     * 向坐边添加字符
     *
     * @param src 源字符
     * @param len 长度
     * @param ch  补起字符
     * @return
     */
    public StreamBuilder appendLeft(Object src, int len, char ch) {
        append(StringUtil.padLeft(String.valueOf(src), len, ch));
        return this;
    }

    /**
     * 向右边添加字符
     *
     * @param src 源字符
     * @param len 长度
     * @param ch  补起字符
     * @return
     */
    public StreamBuilder appendRight(Object src, int len, char ch) {
        append(StringUtil.padRight(String.valueOf(src), len, ch));
        return this;
    }

    /**
     * 增加换行符
     */
    public StreamBuilder appendLn() {
        append(StringUtil.LineBytes);
        return this;
    }

    public void clear() {
        this.outputStream.reset();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        if (outputStream == null) {
            return 0;
        }
        return outputStream.size();
    }

    public byte[] toBytes() {
        if (outputStream == null) {
            return null;
        }
        return outputStream.toByteArray();
    }

    @Override
    public String toString() {
        final byte[] bytes = toBytes();
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
