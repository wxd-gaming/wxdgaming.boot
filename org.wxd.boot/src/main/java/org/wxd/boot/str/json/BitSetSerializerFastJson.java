package org.wxd.boot.str.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.BitSet;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2022-04-21 10:11
 **/
public class BitSetSerializerFastJson implements ObjectSerializer, ObjectDeserializer {

    public static final BitSetSerializerFastJson default_instance = new BitSetSerializerFastJson();

    @Override
    public void write(JSONSerializer serializer,
                      Object object,
                      Object fieldName,
                      Type fieldType,
                      int features) throws IOException {
        BitSet bitSet = (BitSet) object;
        serializer.write(bitSet.toLongArray());
    }

    /** 由于集合序列化后为子类,再进行反序列化时,无法还原原对象,需要修改反序列化方法,手动修改反序列化逻辑 */
    @Override
    public Object deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        long[] js = JSON.parseObject(parser.parse(fieldName).toString(), long[].class);
        return BitSet.valueOf(js);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }

    @Override public long getFeatures() {
        return ObjectSerializer.super.getFeatures();
    }
}
