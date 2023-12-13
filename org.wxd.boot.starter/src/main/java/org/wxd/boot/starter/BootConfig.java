package org.wxd.boot.starter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.wxd.boot.batis.DbConfig;
import org.wxd.boot.lang.ObjectBase;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-12-12 20:53
 **/
@Getter
@Setter
@Accessors(chain = true)
@Root
public class BootConfig extends ObjectBase {

    @Element(required = false)
    private ThreadPool defaultExecutor = new ThreadPool(2, 4);

    @Element(required = false)
    private ThreadPool logicExecutor = new ThreadPool(4, 8);

    @Element(required = false)
    private ServerConfig server = new ServerConfig();
    @Element(required = false)
    private WebConfig wsserver = new WebConfig();
    @Element(required = false)
    private WebConfig http = new WebConfig();
    @Element(required = false)
    private DbConfig mysql = new DbConfig();
    @Element(required = false)
    private DbConfig mongodb = new DbConfig();

    @Getter
    @Setter
    public static class ThreadPool extends ObjectBase {

        @Attribute
        private int coreSize;
        @Attribute
        private int maxSize;

        public ThreadPool() {
        }

        public ThreadPool(int coreSize, int maxSize) {
            this.coreSize = coreSize;
            this.maxSize = maxSize;
        }
    }

}
