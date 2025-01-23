package wxdgaming.boot.starter.redis;

import wxdgaming.boot.agent.system.ReflectContext;
import wxdgaming.boot.starter.BootConfig;
import wxdgaming.boot.starter.ServiceModule;

/**
 * pgsql引用
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-01-22 14:24
 **/
public class RedisModule extends ServiceModule {

    public RedisModule(ReflectContext reflectContext) {
        super(reflectContext);
    }


    @Override protected RedisModule bind() throws Throwable {

        BootConfig bootConfig = BootConfig.getInstance();

        dbAction.accept(RedisService.class, bootConfig.getRedis());
        dbAction.accept(RedisService1.class, bootConfig.getRedis1());
        dbAction.accept(RedisService2.class, bootConfig.getRedis2());
        dbAction.accept(RedisService3.class, bootConfig.getRedis3());

        return this;
    }
}
