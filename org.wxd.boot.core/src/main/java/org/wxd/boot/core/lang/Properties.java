package org.wxd.boot.core.lang;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.wxd.boot.core.collection.ObjMap;
import org.wxd.boot.core.str.xml.XmlUtil;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-03-17 16:20
 **/
@Root(name = "properties")
public final class Properties implements Serializable {

    public static void main(String[] args) throws Exception {
        Properties iniTest = new Properties();
        iniTest.put("user.tcp.host", "1");
        iniTest.put("user.tcp.port", "1");
        System.out.println(XmlUtil.toXml(iniTest));
    }

    public static Properties readXml(String filePath) throws Exception {
        return XmlUtil.fromXml4File(filePath, Properties.class);
    }

    @ElementList(inline = true)
    private ArrayList<Property> datas = new ArrayList<>();

    public ObjMap toMap() {
        ObjMap jsonObject = ObjMap.build();
        for (Property data : datas) {
            jsonObject.put(data.getKey(), data.getValue());
        }
        return jsonObject;
    }

    public Properties put(String key, String value) {
        datas.add(new Property(key, value));
        return this;
    }

    public ArrayList<Property> getDatas() {
        return datas;
    }

    public Properties setDatas(ArrayList<Property> datas) {
        this.datas = datas;
        return this;
    }

    @Override
    public String toString() {
        return toMap().toString();
    }

}
