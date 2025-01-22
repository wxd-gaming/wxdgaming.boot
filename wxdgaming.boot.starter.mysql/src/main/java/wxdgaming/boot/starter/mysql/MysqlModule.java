package wxdgaming.boot.starter.mysql;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.starter.BootConfig;
import wxdgaming.boot.starter.ServiceModule;

/**
 * pgsql引用
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-22 14:24
 **/
public class MysqlModule extends ServiceModule {

    public MysqlModule(ReflectContext reflectContext) {
        super(reflectContext);
    }


    @Override protected ServiceModule bind() throws Throwable {

        BootConfig bootConfig = getProvider(BootConfig.class).get();

        dbAction.accept(MysqlService.class, bootConfig.getMysql());
        dbAction.accept(MysqlService1.class, bootConfig.getMysql1());
        dbAction.accept(MysqlService2.class, bootConfig.getMysql2());
        dbAction.accept(MysqlService3.class, bootConfig.getMysql3());

        return this;
    }
}
