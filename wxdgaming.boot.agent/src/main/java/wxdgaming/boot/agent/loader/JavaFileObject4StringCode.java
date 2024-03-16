package wxdgaming.boot.agent.loader;

import wxdgaming.boot.agent.exception.Throw;

import javax.tools.SimpleJavaFileObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-08-10 10:56
 **/
public class JavaFileObject4StringCode extends SimpleJavaFileObject {

    private static final long serialVersionUID = 1L;

    /**
     * 获取类源码文件里面的类名
     *
     * @param sourceCode 源码
     * @return 类的全名称
     */
    public static String readFullClassName(String sourceCode) {
        try (StringReader stringReader = new StringReader(sourceCode)) {
            try (BufferedReader br = new BufferedReader(stringReader)) {
                String className = "";
                while (br.ready()) {
                    String readLine = br.readLine();
                    if (readLine == null || readLine.isBlank())
                        continue;

                    if (readLine.contains("import ")) continue;

                    readLine = readLine.trim();
                    
                    if (readLine.startsWith("package") && readLine.endsWith(";")) {
                        className += readLine.substring(readLine.indexOf(" ") + 1, readLine.length() - 1);
                        className += ".";
                    }

                    String tmp = findName("class", readLine);

                    if (tmp == null || tmp.isBlank()) {
                        tmp = findName("interface", readLine);
                    }

                    if (tmp == null || tmp.isBlank()) {
                        tmp = findName("enum", readLine);
                    }

                    if (tmp != null && !tmp.isBlank()) {
                        className += tmp;
                        return className;
                    }

                }
                throw new UnsupportedOperationException("并未找到的 类名");
            }
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    public static String findName(String contain, String line) {
        if (line.contains(contain)) {
            String[] split = line.split(" ");
            for (int i = 0; i < split.length; i++) {
                String str = split[i];
                if (str.equals(contain)) {
                    String className = split[i + 1];
                    if (className.contains("<")) {
                        className = className.substring(0, className.indexOf("<"));
                    }
                    return className;
                }
            }
        }
        return null;
    }

    /**
     * 等待编译的源码字段
     */
    private String sourceCoder;

    /**
     * java源代码  StringJavaFileObject对象 的时候使用
     */
    public JavaFileObject4StringCode(String className, String contents) {
        super(
                URI.create("string:///" + className.replaceAll("\\.", "/") + Kind.SOURCE.extension),
                Kind.SOURCE
        );
        this.sourceCoder = contents;
    }

    /**
     * 字符串源码会调用该方法
     *
     * @param ignoreEncodingErrors
     * @return
     * @throws IOException
     */
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return sourceCoder;
    }

}
