package com.test;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 读取文件
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2024-02-06 20:42
 **/
@Slf4j
public class RealTimeLogReader {

    public static void main(String[] args) {

        log.info("1");
        String logFile = "target/logs/app.log";
        try {
            FileReader fileReader = new FileReader(logFile);
            RandomAccessFile randomAccessFile = new RandomAccessFile(logFile, "r");
            randomAccessFile.seek(randomAccessFile.length());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while (true) {
                if ((line = bufferedReader.readLine()) != null) {
                    System.out.println(line); // 将日志输出到控制台
                } else {
                    log.info("{}", System.currentTimeMillis());
                }
                // 其他处理逻辑
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
