package org.wxd.boot.lang;

import org.wxd.agent.exception.Throw;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 大数字管理器
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-09-12 10:28
 **/
public class Bigint extends BigintFinal implements Serializable, Cloneable, Comparable<BigintFinal> {

    private static final long serialVersionUID = 1L;

    public Bigint() {
        super();
    }

    public Bigint(double val) {
        super(val);
    }

    public Bigint(String val) {
        super(val);
    }

    @Override
    public Bigint clone() {
        try {
            return (Bigint) super.clone();
        } catch (Exception e) {
            throw Throw.as(e);
        }
    }

    /**
     * 如果值小于0 。归零
     */
    public void clearZero() {
        if (this.value.doubleValue() < 0) {
            setValue(BigDecimal.valueOf(0));
        }
    }

    /**
     * 清空属性，归零处理
     */
    public void zero() {
        setValue(BigDecimal.valueOf(0));
    }

    /**
     * 增加
     *
     * @param val
     */
    public Bigint add(BigintFinal val) {
        synchronized (this) {
            this.value = this.value.add(val.value);
            clearZero();
            return this;
        }
    }

    /**
     * 增加
     *
     * @param val
     */
    public Bigint add(double val) {
        synchronized (this) {
            BigDecimal bigVal = BigDecimal.valueOf(val);
            this.value = this.value.add(bigVal);
            clearZero();
            return this;
        }
    }

    /**
     * 增加
     *
     * @param val
     */
    public Bigint add(String val) {
        synchronized (this) {
            BigDecimal bigVal = new BigDecimal(val);
            this.value = this.value.add(bigVal);
            clearZero();
            return this;
        }
    }

    /**
     * 扣除
     *
     * @param val
     */
    public Bigint subtract(BigintFinal val) {
        {
            this.value = this.value.subtract(val.value);
            clearZero();
            return this;
        }
    }

    /**
     * 扣除
     *
     * @param val
     */
    public Bigint subtract(double val) {
        synchronized (this) {
            BigDecimal bigVal = BigDecimal.valueOf(val);
            this.value = this.value.subtract(bigVal);
            clearZero();
            return this;
        }
    }


    /**
     * 扣除
     *
     * @param val
     */
    public Bigint subtract(String val) {
        synchronized (this) {
            BigDecimal bigVal = new BigDecimal(val);
            this.value = this.value.subtract(bigVal);
            clearZero();
            return this;
        }
    }

    /**
     * 乘
     *
     * @param val
     */
    public Bigint multiply(BigintFinal val) {
        synchronized (this) {
            this.value = this.value.multiply(val.value);
            clearZero();
            return this;
        }
    }

    /**
     * 乘
     *
     * @param val
     */
    public Bigint multiply(double val) {
        synchronized (this) {
            BigDecimal bigVal = BigDecimal.valueOf(val);
            this.value = this.value.multiply(bigVal);
            clearZero();
            return this;
        }
    }

    /**
     * 乘
     *
     * @param val
     */
    public Bigint multiply(String val) {
        synchronized (this) {
            BigDecimal bigVal = new BigDecimal(val);
            this.value = this.value.multiply(bigVal);
            clearZero();
            return this;
        }
    }

    /**
     * 乘
     *
     * @param val
     */
    public Bigint divide(BigintFinal val) {
        synchronized (this) {
            this.value = this.value.divide(val.value);
            clearZero();
            return this;
        }
    }

    /**
     * 乘
     *
     * @param val
     */
    public Bigint divide(double val) {
        synchronized (this) {
            BigDecimal bigVal = BigDecimal.valueOf(val);
            this.value = this.value.divide(bigVal);
            clearZero();
            return this;
        }
    }

    /**
     * 乘
     *
     * @param val
     */
    public Bigint divide(String val) {
        synchronized (this) {
            BigDecimal bigVal = new BigDecimal(val);
            this.value = this.value.divide(bigVal);
            clearZero();
            return this;
        }
    }
}
