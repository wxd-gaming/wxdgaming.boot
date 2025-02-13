package com.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import wxdgaming.boot.agent.AgentService;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Comparator;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-08-06 17:51
 **/
@Slf4j
public class FileUtilTest implements Serializable {

    @Test
    public void write() {
        FileWriteUtil.writeString("target/tmp/sss.log", "ssss", false);
    }

    @Test
    public void throwTest() {
        RuntimeException runtimeException = new RuntimeException("t");
        RuntimeException runtimeException1 = Throw.of("t2", runtimeException);
        runtimeException1.printStackTrace();
    }

    @Test
    public void ct() {
        ct0(SQLException.class);
        ct0(AgentService.class);
    }

    public void ct0(Class clazz) {
        String x = FileUtil.clazzJarPath(clazz);
        System.out.println(x + "/" + clazz.getName());
        File file = new File(x);
        System.out.println(file.getName() + " - " + file.length());
    }

    @Test
    public void walk() throws Exception {
        String s = "e:\\out";
        int maxDepth = 20;
        File file = new File(s);
        Files.walk(file.toPath(), maxDepth)
                .map(Path::toFile)
                .sorted(Comparator.reverseOrder())
                .forEach(System.out::println);
    }

    @Test
    public void findFile() throws Exception {
        String s = "e:\\out1\\1.log";
        int maxDepth = 2;
        File file = new File(s);
        Files.walk(file.toPath(), maxDepth)
                .map(Path::toFile)
                .filter(File::isFile)
                .forEach(System.out::println);

         FileUtil.resourceStreams(s).forEach(v-> System.out.println(v.t1()));

    }

    @Test
    public void findDir() throws Exception {
        String s = "e:/out";
        int maxDepth = 2;
        File file = new File(s);
        Files.walk(file.toPath(), maxDepth)
                .map(Path::toFile)
                .filter(File::isDirectory)
                .forEach(System.out::println);
    }


}
