package wxdgaming.boot.core.system;

import wxdgaming.boot.core.append.StreamWriter;

import java.util.List;

/**
 * 控制输出
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-10-28 11:01
 **/
public interface PrintConsole {

    default void outPrint(Object obj) {
        System.out.print(obj);
        System.out.flush();
    }

    default void outPrintln(Object obj) {
        System.out.println(obj);
        System.out.flush();
    }

    default void outPrintln() {
        System.out.println();
        System.out.flush();
    }

    default void errPrint(Object obj) {
        System.err.print(obj);
        System.err.flush();
    }

    default void errPrintln(Object obj) {
        System.err.println(obj);
        System.err.flush();
    }

    default void errPrintln() {
        System.err.println();
        System.err.flush();
    }

    static public String print(List<List> data) {
        return print(data, 30);
    }

    static public String print(List<List> data, int len) {
        try (StreamWriter streamWriter = new StreamWriter()) {
            streamWriter.writeLn();
            for (List datum : data) {
                List<Object> row = datum;
                for (Object o : row) {
                    streamWriter.write("|").writeRight(o, len, ' ').write("\t");
                }
                streamWriter.writeLn();
            }
            return streamWriter.toString();
        }
    }

}
