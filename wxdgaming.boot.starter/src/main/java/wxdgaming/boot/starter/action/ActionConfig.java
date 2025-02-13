package wxdgaming.boot.starter.action;

import lombok.extern.slf4j.Slf4j;
import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.agent.io.FileReadUtil;
import wxdgaming.boot.agent.io.FileUtil;
import wxdgaming.boot.agent.io.FileWriteUtil;
import wxdgaming.boot.agent.lang.Record2;
import wxdgaming.boot.agent.system.AnnUtil;
import wxdgaming.boot.agent.zip.GzipUtil;
import wxdgaming.boot.core.str.StringUtil;
import wxdgaming.boot.core.str.json.FastJsonUtil;
import wxdgaming.boot.core.str.xml.XmlUtil;
import wxdgaming.boot.starter.config.Config;

import java.io.InputStream;
import java.lang.reflect.Constructor;

/**
 * config处理
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2022-04-15 08:54
 **/
@Slf4j
public class ActionConfig {

    /** 相当于初始化 */
    public static <R> R action(Class<R> aClass) throws Exception {
        Config config = AnnUtil.ann(aClass, Config.class);
        return action(config, aClass);
    }

    public static <R> R action(Config config, Class<R> aClass) throws Exception {
        Object o = null;
        if (config != null) {
            /*配置文件路径*/
            String configPath = config.value();
            if (StringUtil.emptyOrNull(configPath)) {
                configPath = aClass.getSimpleName().toLowerCase() + "." + config.configType().name().toLowerCase();
            }

            Record2<String, InputStream> inputStream = FileUtil.findInputStream(configPath);
            if (inputStream != null) {
                if (log.isDebugEnabled()) {
                    log.debug("配置文件初始化：" + aClass.getSimpleName() + " - " + configPath);
                }
                try {
                    switch (config.configType()) {
                        case Bin: {
                            byte[] bytes = FileReadUtil.readBytes(inputStream.t2());
                            byte[] unGZip = GzipUtil.unGZip(bytes);
                            o = FastJsonUtil.parse(unGZip, aClass);
                        }
                        break;
                        case Xml: {
                            o = XmlUtil.fromXml(FileReadUtil.readString(inputStream.t2()), aClass);
                        }
                        break;
                        case Json: {
                            o = FastJsonUtil.parse(FileReadUtil.readString(inputStream.t2()), aClass);
                        }
                        break;
                    }
                } catch (Exception e) {
                    throw Throw.of("configPath：" + configPath, e);
                }
            } else if (config.notConfigInit()) {
                Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
                o = declaredConstructor.newInstance();
                save(o);
                if (log.isDebugEnabled()) {
                    log.debug("默认初始化：" + aClass.getSimpleName() + " - " + configPath);
                }
            } else if (config.notConfigAlligator()) {
                if (log.isDebugEnabled())
                    log.debug("ioc 注册 " + aClass.getSimpleName() + ", 配置文件：" + configPath + ", 未找到忽律处理!");
            } else {
                throw new RuntimeException("ioc 注册 " + aClass.getSimpleName() + ", 配置文件：" + configPath + ", 查找失败！！！");
            }
        }
        return (R) o;
    }

    /**
     * 存储 config 类
     *
     * @param object 对象
     */
    public static void save(Object object) {
        Config config = AnnUtil.ann(object.getClass(), Config.class);
        /*配置文件路径*/
        String configPath = config.value();
        if (StringUtil.emptyOrNull(configPath)) {
            configPath = "config/" + object.getClass().getSimpleName().toLowerCase() + "." + config.configType().name().toLowerCase();
        }
        switch (config.configType()) {
            case Bin:
                FileWriteUtil.writeBytes(configPath, GzipUtil.gzip(FastJsonUtil.toBytes(object)));
                break;
            case Json:
                FileWriteUtil.writeString(configPath, FastJsonUtil.toJson(object));
                break;
            case Xml:
                FileWriteUtil.writeString(configPath, XmlUtil.toXml(object));
                break;
        }
    }
}
