package org.wxd.boot.struct;

import lombok.extern.slf4j.Slf4j;
import straightedge.geom.Vector3;

import java.io.Serializable;

/**
 * <br>
 * author 失足程序员<br>
 * blog http://www.cnblogs.com/shizuchengxuyuan/<br>
 * mail 492794628@qq.com<br>
 * phone 13882122019<br>
 */
@Slf4j
public abstract class Check implements Serializable {

    private volatile long id = 0;
    private Vector3 center;
    private Vector3 translation;
    private int translationCount;

    public Check() {
    }

    public Check(Vector3 center) {
        this.center = center.clone();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Vector3 getTranslation() {
        return translation;
    }

    public void setTranslation(Vector3 translation) {
        this.translation = translation;
    }

    public int getTranslationCount() {
        return translationCount;
    }

    public void setTranslationCount(int translationCount) {
        this.translationCount = translationCount;
    }

    public Vector3 getCenter() {
        return center;
    }

    protected void setCenter(Vector3 center) {
        this.center = center.clone();
    }

    /**
     * @param targetPosition
     * @param tmpradius
     * @return
     */
    public boolean contains(Vector3 targetPosition, float tmpradius) {
        return contains(targetPosition.getX(), targetPosition.getZ(), tmpradius);
    }

    /**
     * @param x
     * @param z
     * @param tmpradius
     * @return
     */
    public boolean contains(double x, double z, float tmpradius) {
        return false;
    }

    /**
     * 返回true，表示不能再次移动，需要删除了
     *
     * @return
     */
    public boolean move() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Check other = (Check) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

}
