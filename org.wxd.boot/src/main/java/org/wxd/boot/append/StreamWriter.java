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
public class StreamWriter implements Closeable, AutoCloseable {

    public static void main(String[] args) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.reset();
        StreamWriter out = new StreamWriter(byteArrayOutputStream);
        out.write(1);
        out.writeFmt("%-10s", "int");
        out.writeFmt("%-10s", "ddd");
        System.out.println(out.toString());
        System.out.printf(String.format("%-10s", "int", "rtrtrt"));
    }

    protected final ByteArrayOutputStream outputStream;

    public StreamWriter() {
        this(1024);
    }

    public StreamWriter(int minCapacity) {
        this(new ByteArrayOutputStream(minCapacity));
    }

    public StreamWriter(ByteArrayOutputStream outputStream) {
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
    public StreamWriter writeLn(Object append) {
        write(append);
        writeLn();
        return this;
    }

    /**
     * 使用utf-8转化字符串
     *
     * @param append
     * @return
     */
    public StreamWriter write(Object append) {
        write(append, StandardCharsets.UTF_8);
        return this;
    }

    /**
     * 转化成字符串输出
     *
     * @param append
     * @param charsetName
     * @return
     */
    public StreamWriter writeLn(Object append, Charset charsetName) {
        write(append, charsetName);
        writeLn();
        return this;
    }

    /**
     * @param append
     * @param charsetName
     * @return
     */
    public StreamWriter write(Object append, Charset charsetName) {
        if (append == null || StringUtil.nullStr.equals(append)) {
            write(StringUtil.nullBytes);
        } else if (append instanceof byte[]) {
            write((byte[]) append);
        } else {
            write(String.valueOf(append).getBytes(charsetName));
        }
        return this;
    }

    public StreamWriter write(Throwable exception) {
        final String s = Throw.ofString(exception);
        write(s.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public StreamWriter write(byte[] bytes) {
        try {
            if (outputStream != null) {
                outputStream.write(bytes);
            }
            return this;
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public StreamWriter write(byte[] bytes, int off, int len) {
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
    public StreamWriter writeFmt(String format, Object... args) {
        write(new Formatter().format(format, args).toString());
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
    public StreamWriter writeLeft(Object src, int len, char ch) {
        write(StringUtil.padLeft(String.valueOf(src), len, ch));
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
    public StreamWriter writeRight(Object src, int len, char ch) {
        write(StringUtil.padRight(String.valueOf(src), len, ch));
        return this;
    }

    /**
     * 增加换行符
     */
    public StreamWriter writeLn() {
        write(StringUtil.LineBytes);
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
