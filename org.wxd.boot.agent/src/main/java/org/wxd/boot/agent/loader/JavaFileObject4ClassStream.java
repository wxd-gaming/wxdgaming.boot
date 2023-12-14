package org.wxd.boot.agent.loader;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-10 10:55
 **/
public class JavaFileObject4ClassStream extends SimpleJavaFileObject {

    /**
     * 存放编译后的字节码
     */
    private ByteArrayOutputStream outputStream;

    public JavaFileObject4ClassStream(URI uri, Kind kind) {
        super(uri, kind);
    }

    /**
     * StringJavaFileManage 编译之后的字节码输出会调用该方法（把字节码输出到outputStream）
     */
    @Override
    public OutputStream openOutputStream() {
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    /**
     * 在类加载器加载的时候需要用到
     */
    public byte[] getCompiledBytes() {
        return outputStream.toByteArray();
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return super.getCharContent(ignoreEncodingErrors);
    }
}
