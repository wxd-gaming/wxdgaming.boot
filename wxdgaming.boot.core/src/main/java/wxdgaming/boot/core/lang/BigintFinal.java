package wxdgaming.boot.core.lang;

import wxdgaming.boot.agent.exception.Throw;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 大数字管理器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-09-12 10:28
 **/
public class BigintFinal extends LockBase implements Serializable, Cloneable, Comparable<BigintFinal> {

    /** 常量 0 */
    public static final BigintFinal ZERO = new BigintFinal(1);
    /** 常量 1 */
    public static final BigintFinal ONE = new BigintFinal(1);

    protected BigDecimal value;

    public BigintFinal() {
        value = BigDecimal.valueOf(0);
    }

    public BigintFinal(double val) {
        value = BigDecimal.valueOf(val);
    }

    public BigintFinal(String val) {
        value = new BigDecimal(val);
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        lock();
        try {
            this.value = value;
        } finally {
            unlock();
        }
    }

    public void resetValue(double value) {
        lock();
        try {
            this.value = new BigDecimal(value);
        } finally {
            unlock();
        }
    }

    public void resetValue(String value) {
        lock();
        try {
            this.value = new BigDecimal(value);
        } finally {
            unlock();
        }
    }

    @Override
    public BigintFinal clone() {
        try {
            return (BigintFinal) super.clone();
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    @Override
    public int compareTo(BigintFinal o) {
        return this.value.compareTo(o.getValue());
    }

    /**
     * true 表示 =0
     *
     * @return
     */
    public boolean checkZero() {
        return this.value.longValue() == 0;
    }

    /** 大于等于 */
    public boolean bigThan(long o) {
        return this.value.longValue() >= o;
    }

    /** 大于等于 */
    public boolean bigThan(BigintFinal o) {
        return this.value.compareTo(o.getValue()) >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BigintFinal bigint = (BigintFinal) o;
        return Objects.equals(value, bigint.value);
    }


    /** 重写后，根据biginteger.tostring().hashcode() */
    @Override
    public int hashCode() {
        return value != null ? value.toString().hashCode() : 0;
    }

    public int intValue() {
        return this.value.intValue();
    }

    public float floatValue() {
        return this.value.floatValue();
    }

    public long longValue() {
        return this.value.longValue();
    }

    public double doubleValue() {
        return this.value.doubleValue();
    }

    /**
     * 去掉小数的字符串表示结果
     *
     * @return
     */
    public String stringValue() {
        return value.toBigInteger().toString();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }


}
