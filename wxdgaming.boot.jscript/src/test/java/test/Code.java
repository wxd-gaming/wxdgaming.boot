package test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.Test;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.jscript.JScript;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-02-09 17:23
 **/
public class Code {

    private static final Base64.Decoder Base64_DECODER = Base64.getDecoder();

    @Test
    public void jsFile() throws Exception {
        try (Context context = Context.create()) {
            Source.Builder builder = Source.newBuilder("js", FileReadUtil.readString("MyRandom.js"), "MyRandom.js");
//            builder.mimeType("application/javascript+module");

            Value eval = context.eval(builder.build());
            Value nextInt = context.getBindings("js").getMember("nextInt");
            System.out.println(nextInt.execute(2, 100));
            System.out.println(nextInt.execute(2, 100));
            System.out.println(nextInt.execute(2, 100, 3));
            System.out.println(nextInt.execute(2, 100, 3));
        }
    }

    @Test
    public void jsFile2() throws Exception {
        final JScript script = JScript.Default;
        script.eval4File("MyRandom2.js");
        final Value randomFloat = script.getMember("randomFloat");
        for (int i = 0; i < 5; i++) {
            System.out.println(randomFloat.execute(2, 100.0));
        }
        System.out.println(randomFloat.execute(2, 100, 3));
        System.out.println(randomFloat.execute(2, 100, 3));
        final Value member = script.getMember("test11").execute();
    }


    @Test
    public void testBase64() throws Exception {
        final JScript script = JScript.Default;
        script.eval4File("D:\\gitee\\org.wxd\\org.wxd.boot\\src\\main\\resources\\html\\js\\base64.js");
        final Value execute = script.getMember("encodeBase64")
                .execute("绿钻");
        final String s = execute.toString();
        System.out.println(s);
        final byte[] decode = Base64_DECODER.decode(s);
        final String s1 = new String(decode);
        System.out.println(s1);
        System.out.println(new String(Base64_DECODER.decode(s.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        final Value encodeBase642 = script.getMember("Base64").getMember("encode");
        final Value value = encodeBase642.execute("我是是是顶顶顶顶顶是防守打法是gewgegeg");
        System.out.println(value);

    }

}
