package org.wxd.boot.lang;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.wxd.boot.timer.MyClock;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 每日数据记录
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-01-10 12:14
 **/
@Getter
@Setter
@Accessors(chain = true)
public class DayRecord extends ObjectBase {

    /** 记录日期的开始时间 */
    private long days;
    /** 记录日期的开始时间 */
    private String dayString;
    /** 新增账号 */
    private ConcurrentSkipListSet<String> registerAccountSet = new ConcurrentSkipListSet<>();
    /** 新增账号创角的账号 */
    private ConcurrentSkipListSet<String> registerRoleAccountSet = new ConcurrentSkipListSet<>();
    /** 滚服创角账号 */
    private ConcurrentSkipListSet<String> oldRegisterRoleAccountSet = new ConcurrentSkipListSet<>();
    /** 今日新增创角，角色ID */
    private ConcurrentSkipListSet<Long> registerRoleSet = new ConcurrentSkipListSet<>();
    /** 今日登录用户，相当于活跃用户 */
    private ConcurrentSkipListSet<String> todayLoginAccountSet = new ConcurrentSkipListSet<>();
    /** 今日登录角色，相当于活跃用户 */
    private ConcurrentSkipListSet<Long> todayLoginRoleSet = new ConcurrentSkipListSet<>();
    /** 玩家每日登录记录，留存统计 */
    private ConcurrentSkipListMap<Integer, ConcurrentSkipListSet<String>> loginDayAccountMap = new ConcurrentSkipListMap<>();
    /** 今日注册的玩家每日累充数据 ltv */
    private ConcurrentSkipListMap<Integer, Long> rechargeDayAccountMap = new ConcurrentSkipListMap<>();
    /** 今天玩家的充值次数统计 */
    private ConcurrentSkipListMap<String, Long> rechargeAccountMap = new ConcurrentSkipListMap<>();
    /** 今日新增注册的充值玩家 */
    private ConcurrentSkipListSet<String> newRechargeAccountMap = new ConcurrentSkipListSet<>();
    /** 今日充值充值金额 */
    private AtomicLong rechargeMoneyCount = new AtomicLong();
    /** 今日新增充值充值金额 */
    private AtomicLong newRechargeMoneyCount = new AtomicLong();
    /** 今日充值代金券 */
    private AtomicLong replaceMoneyCount = new AtomicLong();
    /** 今日最高在线 */
    private int maxRow;

    public DayRecord() {
    }

    public DayRecord(long days) {
        this.days = days;
        this.dayString = MyClock.formatDate("yyyy/MM/dd", days);
    }

    public DayRecord(String dayString) {
        this.dayString = dayString;
        this.days = MyClock.parseDate("yyyy/MM/dd", dayString).getTime();
    }

    public void combine(DayRecord joiner) {
        this.registerAccountSet.addAll(joiner.registerAccountSet);
        this.registerRoleSet.addAll(joiner.registerRoleSet);
        this.todayLoginAccountSet.addAll(joiner.todayLoginAccountSet);

        for (Map.Entry<Integer, ConcurrentSkipListSet<String>> entry : joiner.loginDayAccountMap.entrySet()) {
            this.loginDayAccountMap
                    .computeIfAbsent(entry.getKey(), l -> new ConcurrentSkipListSet<>())
                    .addAll(entry.getValue());
        }

        for (Map.Entry<Integer, Long> entry : joiner.getRechargeDayAccountMap().entrySet()) {
            this.rechargeDayAccountMap.merge(entry.getKey(), entry.getValue(), Math::addExact);
        }
        for (Map.Entry<String, Long> entry : joiner.rechargeAccountMap.entrySet()) {
            this.rechargeAccountMap.merge(entry.getKey(), entry.getValue(), Math::addExact);
        }
        this.newRechargeAccountMap.addAll(joiner.newRechargeAccountMap);
        this.rechargeMoneyCount.addAndGet(joiner.rechargeMoneyCount.get());
        this.replaceMoneyCount.addAndGet(joiner.replaceMoneyCount.get());

    }

    public int dayLoginCount(int d) {
        ConcurrentSkipListSet<String> strings = getLoginDayAccountMap().get(d);
        int size = 0;
        if (strings != null) {
            size = strings.size();
        }
        return size;
    }

    /** ltv 充值情况 */
    public long dayRechargeCount(int d) {
        return getRechargeDayAccountMap().getOrDefault(d, 0L);
    }

}
