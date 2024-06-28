package code;

import org.junit.Test;
import wxdgaming.boot.agent.io.FileUtil;

import java.io.File;

public class D1Test {

    @Test
    public void t1() {

        String path = "src/main/lua/";
        File script_path = new File(path + "/script");
        FileUtil.walkDirs(script_path.getPath(), 1).forEach(dir -> {
            if (dir.equals(script_path)) return;

            System.out.println(dir+" - "+dir.getName());
        });

        System.out.println("========================================================");

        FileUtil.walkFiles(path + "/util", 99).forEach(file -> {
            System.out.println(file);
        });

    }

}
