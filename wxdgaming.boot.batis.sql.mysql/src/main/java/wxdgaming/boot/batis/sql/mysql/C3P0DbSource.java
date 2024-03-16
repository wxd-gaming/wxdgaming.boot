package wxdgaming.boot.batis.sql.mysql;// package org.wxd.boot.batis.sql.mysql;
//
// import com.mchange.v2.c3p0.ComboPooledDataSource;
// import com.mchange.v2.log.FallbackMLog;
// import com.mchange.v2.log.MLevel;
// import com.mchange.v2.log.jdk14logging.Jdk14MLog;
// import lombok.extern.slf4j.Slf4j;
// import wxdgaming.boot.agent.exception.Throw;
//
// import java.io.Serializable;
// import java.sql.Connection;
// import java.util.Properties;
//
// /**
//  * @author: Troy.Chen(無心道, 15388152619)
//  * @version: 2021-12-06 17:31
//  **/
// @Slf4j
// public class C3P0DbSource extends DbSource implements Serializable {
//
//     private ComboPooledDataSource bds;
//
//     public C3P0DbSource(String DRIVER_CLASS, String DB_URL, String DB_USER, String DB_PASSWORD) {
//         try {
//             Properties p = new Properties(System.getProperties());
//             p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
//             p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // Off or any other level
//             System.setProperties(p);
//             FallbackMLog.instance().getMLogger().setLevel(MLevel.OFF);
//             Jdk14MLog.getLogger().setLevel(MLevel.OFF);
//             this.bds = new ComboPooledDataSource(false);
//             this.bds.setDriverClass(DRIVER_CLASS);
//             this.bds.setJdbcUrl(DB_URL);
//             this.bds.setUser(DB_USER);
//             this.bds.setPassword(DB_PASSWORD);
//             /*初始化链接池中的链接数*/
//             this.bds.setInitialPoolSize(6);
//             /*链接池中保留的最小链接数*/
//             this.bds.setMinPoolSize(6);
//             /*链接池中保留的最大链接数*/
//             this.bds.setMaxPoolSize(20);
//             /*当链接池中的链接耗尽的时候c3p0一次同时获取的链接数。默认值: 3*/
//             this.bds.setAcquireIncrement(2);
//             /*最大空闲时间，60秒内未使用则链接被丢弃。若为0则永不丢弃。*/
//             this.bds.setMaxIdleTime(60);
//             /*配置链接的生存时间*/
//             this.bds.setMaxConnectionAge(20);
//             /*丢弃缓存statements*/
//             this.bds.setMaxStatements(0);
//             this.bds.setMaxStatementsPerConnection(0);
//             /*这个配置主要是为了减轻链接池的负载*/
//             this.bds.setMaxIdleTimeExcessConnections(50);
//             /*获取链接失败后该数据源将申明已断开并永久关闭*/
//             this.bds.setBreakAfterAcquireFailure(false);
//             /*当链接池用完时客户端调用getConnection()后等待获取新链接的时间，超时后将抛出SQLException,如设为0则无限期等待。单位毫秒。*/
//             this.bds.setCheckoutTimeout(10 * 1000);
//             /*每60秒检查所有链接池中的空闲链接*/
//             this.bds.setIdleConnectionTestPeriod(60);
//             /*缓慢的JDBC操作通过帮助进程完成。扩展这些操作可以有效的提升性能通过多线程实现多个操作同时被执行*/
//             this.bds.setNumHelperThreads(3);
//             /*链接关闭时默认将所有未提交的操作回滚*/
//             this.bds.setAutoCommitOnClose(false);
//             /*防止链接丢失*/
//             this.bds.setTestConnectionOnCheckin(true);
//             this.bds.setTestConnectionOnCheckout(true);
//         } catch (Throwable e) {
//             throw Throw.as(e);
//         }
//     }
//
//     @Override
//     public Connection getConnection() {
//         if (this.bds != null) {
//             try {
//                 return this.bds.getConnection();
//             } catch (Exception e) {
//                 if (e instanceof InterruptedException) {
//                     try {
//                         this.bds.close();
//                     } catch (Exception c3p0e) {
//                         log.error("连接池关闭异常", e);
//                     } finally {
//                         this.bds = null;
//                     }
//                 } else
//                     log.error("获取连接池链接异常", e);
//             }
//         }
//         return null;
//     }
//
//     public void close() {
//         if (this.bds != null) {
//             this.bds.close();
//         }
//     }
//
// }
