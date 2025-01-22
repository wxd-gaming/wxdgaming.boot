package wxdgaming.boot.starter.mongodb;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.starter.BootConfig;
import wxdgaming.boot.starter.ServiceModule;

/**
 * pgsql引用
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-22 14:24
 **/
public class MongodbModule extends ServiceModule {

    public MongodbModule(ReflectContext reflectContext) {
        super(reflectContext);
    }


    @Override protected ServiceModule bind() throws Throwable {

        BootConfig bootConfig = getProvider(BootConfig.class).get();

        dbAction.accept(MongoService.class, bootConfig.getMongodb());
        dbAction.accept(MongoService1.class, bootConfig.getMongodb1());
        dbAction.accept(MongoService2.class, bootConfig.getMongodb2());
        dbAction.accept(MongoService3.class, bootConfig.getMongodb3());

        return this;
    }
}
