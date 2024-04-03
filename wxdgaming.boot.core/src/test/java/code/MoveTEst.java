package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import straightedge.geom.Vector3;
import wxdgaming.boot.core.struct.CheckPolygon;
import wxdgaming.boot.core.struct.CheckRound;
import wxdgaming.boot.core.struct.MoveUtil;
import wxdgaming.boot.core.struct.Vector;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2023-06-13 12:05
 **/
@Slf4j
public class MoveTEst {

    @Test
    public void t12() {
        System.out.println(MoveUtil.distance(1, 10, 5, 15));
        System.out.println(distance(1, 10, 5, 15));
    }

    public int distance(int x1, int y1, int x2, int y2) {
        return Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    @Test
    public void t13() {
        /*坐标是是4，4 半径是4的圆形*/
        Vector3 curPos = new Vector3(4, 0, 4);
        CheckRound checkRound = new CheckRound(4, curPos);
        /*检查坐标3.5，3.5 是否在圆中，运行误差范围0.2 相当于前后的端坐标误差*/
        System.out.println("圆：" + checkRound.toString() + " " + checkRound.contains(3.5, 3.5, 0.2f));

        /*坐标是是4，4 矩形*/
        CheckPolygon checkPolygon = new CheckPolygon(curPos);
        checkPolygon.initRectangle(180, 0, 4, 0.8f);
        /*检查坐标3.5，3.5 是否在矩形，运行误差范围0.2 相当于前后的端坐标误差*/
        System.out.println("矩形：" + checkPolygon.toString() + " " + checkPolygon.contains(3.5, 3.5, 0.2f));

    }

    @Test
    public void t14() {
        Vector3 curPos = new Vector3(4, 0, 4);
        CheckPolygon checkPolygon = new CheckPolygon(curPos);
        checkPolygon.init(5);/*构建一个5个顶点的多边形 自己那纸和笔画一下就知道了 加入的点位一定要顺序，就算相互是挨着的*/
        checkPolygon.add(4, 8);
        checkPolygon.add(8, 8);
        checkPolygon.add(8, 12);
        checkPolygon.add(7, 15);
        checkPolygon.add(4, 12);
        /*检查坐标7,10 是否在矩形，运行误差范围0.2 相当于前后的端坐标误差*/
        System.out.println("矩形：" + checkPolygon.toString() + " " + checkPolygon.contains(7, 10, 0.2f));
    }

    @Test
    public void t20() {
        Vector3 pos = Vector3.clone(2.5, 0, 4);
        Vector3 tar = Vector3.clone(2.0, 0, 4);
        /*计算当前朝向*/
        double dir = MoveUtil.getATan360(pos.getX(), pos.getZ(), tar.getX(), tar.getZ());
        int TIMEPERIOD = 80;
        /*计算移动次数*/
        int moveCount = (1000 / TIMEPERIOD) + (1000 % TIMEPERIOD > 0 ? 1 : 0);
        /*计算每一次移动的距离*/
        float height = 6 * 1f / moveCount;// 每帧飞行的距离

        Vector vectorBy360Atan = MoveUtil.getVectorBy360Atan(dir);
        System.out.println(vectorBy360Atan.toString());
        /*计算朝向位移量*/
        Vector3 translation = Vector3.clone0();
        translation.x += vectorBy360Atan.getDir_x() * MoveUtil.getV12XD(height, vectorBy360Atan.getAtan());
        translation.z += vectorBy360Atan.getDir_z() * MoveUtil.getV12ZD(height, vectorBy360Atan.getAtan());
        /*构建一个矩形框*/
        CheckPolygon checkPolygon = new CheckPolygon(pos);
        checkPolygon.initRectangle(dir, 0, 3, height);
        checkPolygon.setId(1);/*相当于场景特效id*/
        checkPolygon.setTranslationCount(moveCount);/*需要模拟的次数*/
        checkPolygon.setTranslation(translation);/*根据方向，移动速度等计算出每一次模拟移动的距离，*/
        do {
            System.out.println(checkPolygon.toString());
            System.out.print(checkPolygon.isInPolygon(tar) + "-----");
            System.out.println(checkPolygon.contains(tar, 5));
        } while (!checkPolygon.move());
    }

}

