package wxdgaming.boot.batis.sql.mysql;

import wxdgaming.boot.agent.LogbackUtil;
import wxdgaming.boot.batis.DbConfig;

/**
 * dump工具
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-12-23 12:09
 **/
public class MysqlDump {

    public static void main(String[] args) throws Exception {
        LogbackUtil.setLogbackConfig();
        MysqlDump.action(args);
    }

    /**
     * dump 127.0.0.1 3306 root test test3 /data/mysql-bak
     * revert 127.0.0.1 3306 root test test3 xxx.zip
     */
    public static void action(String[] args) throws Exception {
        String oper = args[0];
        DbConfig dbConfig = new DbConfig();
        dbConfig.setDbHost(args[1]);
        dbConfig.setDbPort(Integer.parseInt(args[2]));
        dbConfig.setDbUser(args[3]);
        dbConfig.setDbPwd(args[4]);
        dbConfig.setDbBase(args[5]);
        dbConfig.setShow_sql(true);
        if ("dump".equalsIgnoreCase(oper)) {
            dump(dbConfig, args[6]);
        } else if ("revert".equalsIgnoreCase(oper)) {
            revert(dbConfig, args[6]);
        } else {
            System.out.println("无法识别的操作");
        }
    }

    public static void dump(DbConfig dbConfig, String bakFile) throws Exception {
        MysqlDataHelper mysqlDataHelper = new MysqlDataHelper(dbConfig);
        System.out.println(mysqlDataHelper.outDb2File(bakFile));
    }

    public static void revert(DbConfig dbConfig, String revertFile) throws Exception {
        MysqlDataHelper mysqlDataHelper = new MysqlDataHelper(dbConfig);
        System.out.println(mysqlDataHelper.inDb4File(revertFile, 500));
    }

}
