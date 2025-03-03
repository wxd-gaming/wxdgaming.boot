package wxdgaming.boot.jscript;

import lombok.Getter;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;

import java.io.File;

/**
 * js 容器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-02-09 17:23
 **/
@Getter
public class JScript {

    public static final JScript Default = JScript.build();

    private final Context context;

    public static JScript build() {
        return new JScript();
    }

    public static JScript build(String file) {
        return build(new File(file));
    }

    public static JScript build(File file) {
        return new JScript().eval4File(file);
    }

    private JScript() {
        context = Context.newBuilder("js")
                .allowAllAccess(true)
                .allowHostClassLookup(className -> true)
                .build();
    }

    public JScript eval4File(String fileName) {
        final File file = FileUtil.findFile(fileName);
        eval4File(file);
        return this;
    }

    public JScript eval4File(File file) {
        final String code = FileReadUtil.readString(file);
        eval4Code(code);
        return this;
    }

    /** 附加源码 */
    public JScript eval4Code(String code) {
        context.eval("js", code);
        return this;
    }

    public Value getMember(String find) {
        return context.getBindings("js").getMember(find);
    }

    public void putMember(String id, Object value) {
        context.getBindings("js").putMember(id, value);
    }

}
