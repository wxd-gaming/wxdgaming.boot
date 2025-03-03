package test;

import wxdgaming.boot.jscript.JScript;

/**
 * java object 类转换成 js
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-03-03 19:42
 **/
public class JavaObject2Js {

    public static void main(String[] args) {
        JScript build = JScript.build();
        build.getContext().getBindings("js").putMember("jlog", new JLog());
        build.eval4Code("jlog.info('hello world');");
    }

    public static class JLog {

        public void info(String msg) {
            System.out.println(msg);
        }

    }

}
