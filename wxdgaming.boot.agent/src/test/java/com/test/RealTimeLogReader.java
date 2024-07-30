package com.test;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 读取文件
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-02-06 20:42
 **/
@Slf4j
public class RealTimeLogReader {

    public static void main(String[] args) {

        log.info("1");
        String logFile = "target/logs/app.log";
        try {
            File file = new File(logFile);

            FileReader fileReader = new FileReader(logFile);
            RandomAccessFile randomAccessFile = new RandomAccessFile(logFile, "r");
            randomAccessFile.seek(randomAccessFile.length());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            final long oldLength = file.length();
            while (true) {
                if (oldLength != file.length()) {
                    if ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line); // 将日志输出到控制台
                    } else {
                        log.info("{}", System.currentTimeMillis());
                    }
                }
                // 其他处理逻辑
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
