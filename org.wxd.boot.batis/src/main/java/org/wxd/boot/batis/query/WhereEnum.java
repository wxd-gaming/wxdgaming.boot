package org.wxd.boot.batis.query;

/**
 * 查询限制
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-11-08 10:29
 **/
public enum WhereEnum {
    /**
     * =
     */
    None,
    /**
     * mysql like '%%'
     * <p>mongo无效
     */
    like,
    /**
     * 大于等于
     * <p>相当于
     * <p> mysql >= ?
     * <p> mongo $gte
     */
    Gte,
    /**
     * 小于等于
     * <p>相当于
     * <p> mysql <=?
     * <p> mongo $lte
     */
    Lte,
    /**
     * 区间值
     * <p>相当于
     * <p> mysql <=? and >= ?
     * <p> mongo $gte $lte
     */
    GteAndLte,
    /**
     * <p> mysql xx in ()
     */
    In,
    /**
     * <p> mysql xx not in ()
     */
    NIn,
    ;

}
