package org.wxd.boot.core.append;

import java.io.Serializable;

/**
 * 通过追加分隔符方式指定字符串
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-05-13 12:48
 **/
public class JoinBuilder implements Serializable {

    private final StringBuilder stringBuilder = new StringBuilder(512);

    /** 分隔符 */
    private final CharSequence delimiter;
    /** 后缀 */
    private final CharSequence suffix;

    private boolean appendDelimiter = false;

    public JoinBuilder() {
        this(null, ", ", null);
    }

    public JoinBuilder(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        if (prefix != null) {
            this.stringBuilder.append(prefix);
        }
        this.delimiter = delimiter;
        this.suffix = suffix;
    }

    /** 添加元素 , 不会增加换行符 */
    public JoinBuilder append(Object obj) {
        stringBuilder.append(obj);
        return this;
    }

    /** 添加分隔符 */
    public JoinBuilder joiner() {
        append(delimiter);
        return this;
    }

    /** 添加分隔符 增加换行符 */
    public JoinBuilder joinLine() {
        append(delimiter).addLine();
        return this;
    }

    /** 添加元素 并且 添加分隔符 */
    public JoinBuilder add(Object obj) {
        if (appendDelimiter) joiner();
        append(obj);
        appendDelimiter = true;
        return this;
    }

    /** 添加元素 , 并且添加分隔符 并且添加换行 */
    public JoinBuilder addLine(Object obj) {
        add(obj).addLine();
        return this;
    }

    /** 追加换行 */
    public JoinBuilder addLine() {
        if (appendDelimiter) joiner();
        appendDelimiter = false;
        append("\n");
        return this;
    }

    public void clear() {
        stringBuilder.setLength(0);
    }

    @Override
    public String toString() {
        if (suffix != null) {
            stringBuilder.append(suffix);
        }
        return stringBuilder.toString();
    }
}
