package code;

import wxdgaming.boot.assist.IAssistMonitor;
import wxdgaming.boot.assist.IAssistOutFile;

public class T2 implements IAssistMonitor, IAssistOutFile {
    public void t2() throws InterruptedException {
        System.out.println(Thread.currentThread());
    }
}
