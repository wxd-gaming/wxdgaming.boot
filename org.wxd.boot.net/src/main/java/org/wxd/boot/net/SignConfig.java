package org.wxd.boot.net;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.wxd.boot.agent.io.FileUtil;
import org.wxd.boot.agent.io.FileWriteUtil;
import org.wxd.boot.collection.OfSet;
import org.wxd.boot.lang.ObjectBase;
import org.wxd.boot.str.Md5Util;
import org.wxd.boot.str.xml.XmlUtil;
import org.wxd.boot.timer.MyClock;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-03-03 15:56
 **/
@Getter
@Setter
@Accessors(chain = true)
@Slf4j
@Root
public class SignConfig extends ObjectBase implements Serializable {

    private static final long serialVersionUID = 1L;

    private static SignConfig instance = null;


    public static SignConfig get() {
        if (instance == null) {
            try {
                instance = signConfig();
            } catch (Exception e) {
                log.error("读取配置数据", e);
            }
            if (instance == null) {
                try {
                    instance = new SignConfig();
                    instance.setToken(Md5Util.md5DigestEncode("root-" + MyClock.millis() + ""));
                    instance.getUserList().add(
                            new SignUser()
                                    .setUserName("root")
                                    .setToken(Md5Util.md5DigestEncode(MyClock.millis() + ""))
                                    .setAuthority(OfSet.asSet(1, 2, 3, 4, 5, 6, 7, 8, 9))
                    );
                    FileWriteUtil.writeString("config/sign-config.xml", instance.toXml());
                } catch (Exception e) {
                    log.error("初始化配置", e);
                }
            }
        }
        return instance;
    }

    public static SignConfig signConfig() throws Exception {
        InputStream stream = FileUtil.findInputStream("sign-config.xml");
        return signConfig(stream);
    }

    public static SignConfig signConfig(InputStream stream) throws Exception {
        if (stream != null) {
            try {
                return XmlUtil.fromXml(stream, SignConfig.class);
            } finally {
                stream.close();
            }
        }
        return null;
    }

    /** 通用秘钥 */
    @Element
    private String token = "";
    /** 验签秘钥账户 */
    @ElementList(required = false, inline = true, entry = "sign")
    private List<IAuth> userList = new ArrayList<>();

    public <R extends IAuth> Optional<R> optional(String userName) {
        Stream<IAuth> iAuthStream = userList.stream().filter(v -> v.getUserName().equalsIgnoreCase(userName));
        return (Optional<R>) iAuthStream.findFirst();
    }

}
