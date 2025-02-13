package wxdgaming.boot.starter.pgsql;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.starter.BootConfig;
import wxdgaming.boot.starter.ServiceModule;

/**
 * pgsql引用
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-22 14:24
 **/
public class PgsqlModule extends ServiceModule {

    public PgsqlModule(ReflectContext reflectContext) {
        super(reflectContext);
    }

    @Override protected void bind() throws Throwable {
        BootConfig bootConfig = BootConfig.getInstance();

        dbAction.accept(PgsqlService.class, bootConfig.getPgsql());
        dbAction.accept(PgsqlService1.class, bootConfig.getPgsql1());
        dbAction.accept(PgsqlService2.class, bootConfig.getPgsql2());
        dbAction.accept(PgsqlService3.class, bootConfig.getPgsql3());

    }
}
