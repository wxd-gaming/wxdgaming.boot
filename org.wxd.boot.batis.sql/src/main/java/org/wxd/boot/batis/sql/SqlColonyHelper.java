// package org.wxd.boot.batis.sql;
//
// import org.wxd.boot.batis.DbConfig;
// import org.wxd.boot.batis.EntityTable;
// import org.wxd.boot.batis.sql.mysql.MysqlDataHelper;
// import org.wxd.boot.batis.sql.sqlite.SqliteDataHelper;
// import org.wxd.boot.str.StringUtil;
//
// import java.io.Serializable;
// import java.util.ArrayList;
//
// /**
//  * 集群模式
//  *
//  * @author: Troy.Chen(無心道, 15388152619)
//  * @version: 2021-09-22 18:11
//  **/
// public class SqlColonyHelper implements Serializable {
//
//     private static final long serialVersionUID = 1L;
//
//     private ArrayList<SqlDataHelper> dataHelperMap = new ArrayList<>();
//
//     public void addMysqlDataHelper(DbConfig dbConfig) {
//         addDataHelper(new MysqlDataHelper(dbConfig));
//     }
//
//     public void addSqliteDataHelper(DbConfig dbConfig) {
//         addDataHelper(new SqliteDataHelper(dbConfig));
//     }
//
//     public void addDataHelper(SqlDataHelper sqlDataHelper) {
//         synchronized (dataHelperMap) {
//             dataHelperMap.add(sqlDataHelper);
//             dataHelperMap.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getDbConfig().getDbHost() + "_" + o1.getDbBase(), o2.getDbConfig().getDbHost() + "_" + o2.getDbBase()));
//         }
//     }
//
//     /**
//      * 批处理提交
//      */
//     public void addBatch(Object dbBase) {
//         EntityTable entityTable = dataHelperMap.get(0).asEntityTable(dbBase);
//         Object fieldValue = entityTable.getDataColumnKey().getFieldValue(dbBase);
//         int hashcode = StringUtil.hashcode(String.valueOf(fieldValue), true);
//         int index = hashcode % dataHelperMap.size();
//         dataHelperMap.get(index).getBatchPool().replace(dbBase);
//     }
//
//     /**
//      * 立即处理
//      *
//      * @param dbBase
//      * @throws Exception
//      */
//     public void replace(Object dbBase) throws Exception {
//         EntityTable entityTable = dataHelperMap.get(0).asEntityTable(dbBase);
//         Object fieldValue = entityTable.getDataColumnKey().getFieldValue(dbBase);
//         int hashcode = StringUtil.hashcode(String.valueOf(fieldValue), true);
//         int index = hashcode % dataHelperMap.size();
//         dataHelperMap.get(index).replace(dbBase);
//     }
//
// }
