package org.wxd.boot.lang;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import org.wxd.boot.i.ILock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 基类
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-06-26 17:12
 **/
@Getter
public class LockBase implements ILock {

    @JSONField(serialize = false, deserialize = false)
    protected transient final ReentrantLock lock = new ReentrantLock();

}
