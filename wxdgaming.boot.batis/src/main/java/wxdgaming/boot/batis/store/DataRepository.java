package wxdgaming.boot.batis.store;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.batis.DataWrapper;
import wxdgaming.boot.batis.EntityTable;
import wxdgaming.boot.batis.struct.DbBean;
import wxdgaming.boot.core.str.StringUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 数据仓储
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-04-19 15:00
 **/
@Slf4j
public abstract class DataRepository<DM extends EntityTable, DW extends DataWrapper<DM>> implements Serializable {

    protected ConcurrentMap<String, DbBean<?, ?>> beanMap = new ConcurrentHashMap<>();

    /** 获取编译器 */
    public abstract DW dataBuilder();

    /** 数据来源名字 */
    public abstract String dataName();

    public <B, T extends DbBean<B, ?>> B getDbBean(Class<T> beanClazz, Object key) {
        return getDbBean(beanClazz).get(key);
    }

    public <B, T extends DbBean<B, ?>> T getDbBean(Class<T> beanClazz) {
        Class<?> tClass = ReflectContext.getTClass(beanClazz);
        DM entityTable = dataBuilder().asEntityTable(tClass);
        return getDbBean(beanClazz, entityTable);
    }

    public <B, T extends DbBean<B, ?>> T getDbBean(Class<T> beanClazz, DM entityTable) {
        return (T) beanMap.computeIfAbsent(entityTable.getTableName(), l -> load(beanClazz, entityTable));
    }

    /** 实现重新加载配置 */
    public void removeAll() {
        beanMap = new ConcurrentHashMap<>();
    }

    /** 实现重新加载配置--如果只是某个配置表更新了，不需要解析其他的 */
    public void removeDbBean(Class<? extends DbBean> beanClazz) {
        Class<?> tClass = ReflectContext.getTClass(beanClazz);
        DM entityTable = dataBuilder().asEntityTable(tClass);
        removeDbBean(entityTable.getTableName());
    }

    /**
     * 是否包含
     */
    public boolean hasDbBean(Class<? extends DbBean> beanClazz) {
        Class<?> tClass = ReflectContext.getTClass(beanClazz);
        DM entityTable = dataBuilder().asEntityTable(tClass);
        return hasDbBean(entityTable.getTableName());
    }

    /**
     * 是否包含
     */
    public boolean hasDbBean(String dbName) {
        return beanMap.containsKey(dbName);
    }

    public void removeDbBean(String name) {
        beanMap.remove(name);
    }

    public void clearBean() {
        beanMap.clear();
    }

    /**
     * 加载
     *
     * @param forceLoad 强制加载
     * @param packages  需要加载的包名
     */
    public void load(boolean forceLoad, ClassLoader classLoader, String... packages) {
        StringBuilder stringBuilder = new StringBuilder();

        List<Class<?>> list = ReflectContext.Builder
                .of(classLoader, packages).build()
                .classWithSuper(DbBean.class)
                .collect(Collectors.toList());
        load(stringBuilder, forceLoad, list);
        log.info(stringBuilder.toString());
    }

    /**
     * 加载
     *
     * @param forceLoad   强制加载
     * @param beanClasses 需要加载的类
     */
    public void load(boolean forceLoad, Collection<Class<?>> beanClasses) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        load(stringBuilder, forceLoad, beanClasses);
        log.info(stringBuilder.toString());
    }

    /**
     * @param stringAppend
     * @param forceLoad    强制重新加载
     * @param beanClasses
     */
    public void load(StringBuilder stringAppend, boolean forceLoad, Collection<Class<?>> beanClasses) {
        writerTitle(stringAppend, 1);
        for (Class<?> clazz : beanClasses) {
            if (!DbBean.class.isAssignableFrom(clazz)) {
                /*如果不是dbbean*/
                continue;
            }
            Class<? extends DbBean> beanClazz = (Class<? extends DbBean>) clazz;
            Class<?> tClass = ReflectContext.getTClass(beanClazz);
            DM entityTable = this.dataBuilder().asEntityTable(tClass);
            DbBean bean;
            if (forceLoad) {
                bean = load(beanClazz, entityTable);
            } else {
                bean = getDbBean(beanClazz, entityTable);
            }
            addReloadMsg(stringAppend, entityTable, bean);
        }
        writerTitle(stringAppend, 2);
    }

    public <B, T extends DbBean<B, ?>> T load(Class<T> beanClazz, DM entityTable) {
        try {
            T dbBean = beanClazz.getDeclaredConstructor().newInstance();
            dbBean.setDataMapping(entityTable);
            dbBean.setModelList(readDbList(entityTable));
            dbBean.initDb();
            return dbBean;
        } catch (Exception e) {
            throw new Throw("实体类：" + beanClazz.getName() + ", " + e.getMessage(), e);
        }
    }

    public abstract List readDbList(DM entityTable) throws Exception;

    protected void addReloadMsg(StringBuilder out, DM entityTable, DbBean<?, ?> dbBean) {
        if (out != null) {
            out
                    .append("|").append(StringUtil.padRight(String.valueOf(dbBean.dbSize()), 10, ' ')).append("\t")
                    .append("|").append(StringUtil.padRight(entityTable.getTableName(), 40, ' ')).append("\t")
                    .append("|").append(StringUtil.padRight(entityTable.getEntityClass().getSimpleName(), 40, ' ')).append("\t")
                    .append("|").append(StringUtil.padRight(entityTable.getTableComment(), 60, ' ')).append("\t")
                    .append("\n");
        }
    }

    protected void writerTitle(StringBuilder out, int type) {
        out.append("\n");
        if (type == 1) {
            out.append("数据库：").append(dataName()).append("\n");
        }

        out
                .append("|").append(StringUtil.padRight("读取数量", 10, ' ')).append("\t")
                .append("|").append(StringUtil.padRight("映射表名", 40, ' ')).append("\t")
                .append("|").append(StringUtil.padRight("映射类名", 40, ' ')).append("\t")
                .append("|").append(StringUtil.padRight("映射备注", 60, ' ')).append("\t")
                .append("\n");

        if (type == 2) {
            out.append("数据库：").append(dataName()).append("\n");
        } else {
            out.append("\n");
        }
    }

}
