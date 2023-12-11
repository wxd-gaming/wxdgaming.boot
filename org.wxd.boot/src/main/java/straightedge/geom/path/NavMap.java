package straightedge.geom.path;

import com.vividsolutions.jts.shape.random.RandomPointsBuilder;
import org.wxd.boot.str.json.FastJsonUtil;
import straightedge.geom.KMultiPolygon;
import straightedge.geom.KPolygon;
import straightedge.geom.PolygonConverter;
import straightedge.geom.Vector3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * author 失足程序员<br>
 * mail 492794628@qq.com<br>
 * phone 13882122019<br>
 */
public class NavMap {

    public static String readTxtFile(String path, String fileName) {
        return readTxtFile(path + File.separatorChar + fileName);
    }

    public static String readTxtFile(String filePath) {
        try {
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = bufferedReader.readLine();
                read.close();
                return lineTxt;
            } else {
                System.out.println("文件{}配置有误,找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错" + e);
        }
        return null;
    }

    private final NodeConnector blockNodeConnector = new NodeConnector();
    private final ArrayList<PathBlockingObstacleImpl> blockStationaryObstacles = new ArrayList<>();

    private final NodeConnector pathNodeConnector = new NodeConnector();
    private final ArrayList<PathBlockingObstacleImpl> pathStationaryObstacles = new ArrayList<>();

    private final NodeConnector safeNodeConnector = new NodeConnector();
    private final ArrayList<PathBlockingObstacleImpl> safeStationaryObstacles = new ArrayList<>();

    // x,z,list
//    private final Map<Integer, Map<Integer, ArrayList<Vector3>>> allRandomPointsInPath = new HashMap<>();
    private final float maxDistanceBetweenObstacles;
    private final PolygonConverter polygonConverter = new PolygonConverter();
    private final PathFinder pathFinder = new PathFinder();

    private final float scale;
    private final float width;
    private final float height;
    private final String navMesh;
    private final int mapID;
    private final float startX;
    private final float startZ;
    private final float endX;
    private final float endZ;
    private final float radius;
    private final Vector3 center;

    public NavMap(String navMesh, boolean editor) throws Exception {
        this(navMesh, editor, true);
    }

    public NavMap(String navMesh, boolean editor, boolean loadSafe) throws Exception {
        this.navMesh = navMesh;
        NavMeshData data = FastJsonUtil.parse(navMesh, NavMeshData.class);
        if (data == null) {
            throw new Exception("地图数据加载错误");
        }
        this.width = Math.abs(data.getEndX() - data.getStartX());
        this.height = Math.abs(data.getEndZ() - data.getStartZ());
        this.startX = data.getStartX();
        this.startZ = data.getStartZ();
        this.endX = data.getEndX();
        this.endZ = data.getEndZ();
        this.radius = Math.max(this.width, this.height) / 2;
        if (editor) {
            data.Sub(this.startX, this.startZ);
            scale = 1024 / width;
        } else {
            scale = 1;
        }
        this.mapID = data.getMapID();
        if (mapID < 1) {
            throw new Exception("地图ID错误");
        }
        this.center = new Vector3(this.width / 2, 0, this.height / 2);
        maxDistanceBetweenObstacles = Math.max(width, height) * scale;
        try {
            createPolygons(blockNodeConnector, blockStationaryObstacles, data.getBlockTriangles(), data.getBlockVertices(), true, editor);
            createPolygons(pathNodeConnector, pathStationaryObstacles, data.getPathTriangles(), data.getPathVertices(), false, editor);
            if (loadSafe) {
                createPolygons(safeNodeConnector, safeStationaryObstacles, data.getSafeTriangles(), data.getSafeVertices(), false, editor);
            }
//            for (PathBlockingObstacleImpl pboi : pathStationaryObstacles) {
//                for (Vector3 randomPoint : pboi.getRandomPoints()) {
//                    Integer x = (int) randomPoint.getX();
//                    Integer z = (int) randomPoint.getZ();
//                    Map<Integer, ArrayList<Vector3>> map = allRandomPointsInPath.get(x);
//                    if (map == null) {
//                        map = new HashMap<>();
//                        allRandomPointsInPath.put(x, map);
//                    }
//                    ArrayList<Vector3> list = map.get(z);
//                    if (list == null) {
//                        list = new ArrayList<>();
//                        map.put(z, list);
//                    }
//                    list.add(randomPoint);
//                }
//            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public NavMap clone() {
        try {
            return new NavMap(navMesh, false);
        } catch (Exception ex) {
            return null;
        }
    }

    public final PathBlockingObstacleImpl addBlock(ArrayList<Vector3> vertices, boolean bufferOuter, boolean editor) {
        if (editor) {
            for (Vector3 li : vertices) {
                li.multiply(scale);
            }
        }
        com.vividsolutions.jts.geom.Polygon jtsPolygon = getPolygon(vertices);
        if (jtsPolygon == null) {
            return null;
        }
        KPolygon poly = polygonConverter.makeKPolygonFromExterior(jtsPolygon);
        if (poly == null) {
            return null;
        }
        KPolygon copy = poly.copy();
        PathBlockingObstacleImpl obst = null;
        if (bufferOuter) {
            obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(copy);
        } else {
            obst = PathBlockingObstacleImpl.createObstacleFromOuterPolygon(copy);
        }
        if (obst == null) {
            return null;
        }
        this.blockStationaryObstacles.add(obst);
        this.blockNodeConnector.addObstacle(obst, this.blockStationaryObstacles, maxDistanceBetweenObstacles);
        return obst;
    }

    /**
     * 删除一个阻挡
     *
     * @param obst
     */
    public final void removeBlock(PathBlockingObstacleImpl obst) {
        this.blockStationaryObstacles.remove(obst);
        this.blockNodeConnector.removeObstacle(obst, maxDistanceBetweenObstacles, blockStationaryObstacles);
    }

    public final void createPolygons2(NodeConnector nodeConnector, ArrayList<PathBlockingObstacleImpl> stationaryObstacles, int[] triangles, Vector3[] vertices, boolean bufferOuter, boolean editor) {
        if (triangles == null || vertices == null) {
            return;
        }
        ArrayList<Vector3> list = new ArrayList<>(3);
        if (editor) {
            for (Vector3 li : vertices) {
                li.multiply(scale);
            }
        }
        ArrayList<Vector3> maskList = new ArrayList<>(4);
        maskList.add(new Vector3(0, 0));
        maskList.add(new Vector3(width, 0));
        maskList.add(new Vector3(width, height));
        maskList.add(new Vector3(0, height));
        if (editor) {
            for (Vector3 li : maskList) {
                li.multiply(scale);
            }
        }
        com.vividsolutions.jts.geom.Geometry mask = getPolygon(maskList);
        RandomPointsBuilder rpb = new RandomPointsBuilder();
        for (int i = 0; i < triangles.length; i += 3) {
            if (triangles.length <= i + 2) {
                break;
            }
            list.clear();
            list.add(vertices[triangles[i]]);
            list.add(vertices[triangles[i + 1]]);
            list.add(vertices[triangles[i + 2]]);
            KPolygon poly = new KPolygon(list);
            com.vividsolutions.jts.geom.Polygon jtsPolygon = getPolygon(list);
            if (jtsPolygon == null) {
                continue;
            }
            mask = mask.difference(jtsPolygon);
        }
        double maxAera = this.width * this.height * this.scale * this.scale;
        for (int i = 0; i < mask.getNumGeometries(); i++) {
            com.vividsolutions.jts.geom.Polygon jtsPolygon = (com.vividsolutions.jts.geom.Polygon) mask.getGeometryN(i);
            if (jtsPolygon == null) {
                continue;
            }
            KMultiPolygon poly = polygonConverter.makeKMultiPolygonFrom(jtsPolygon);
            if (poly == null) {
                continue;
            }
            KPolygon polygon = poly.polygons.get(0);
            if (poly.polygons.size() > 1) {
                KMultiPolygon boundryPolygon = getBoundryPolygons(poly.polygons.get(1));
                for (KPolygon p : boundryPolygon.polygons) {
                    makePathBlockingPolygon(nodeConnector, stationaryObstacles, rpb, p, bufferOuter, editor);
                }
            } else {
                makePathBlockingPolygon(nodeConnector, stationaryObstacles, rpb, polygon, bufferOuter, editor);
            }
        }
    }

    private KMultiPolygon getBoundryPolygons(KPolygon polygon) {
        Vector3 leftBotton = new Vector3(0, 0);
        Vector3 leftTop = new Vector3(0, height * scale);
        Vector3 rightTop = new Vector3(width * scale, height * scale);
        Vector3 rightBotton = new Vector3(width * scale, 0);
        Vector3 leftBottonNear = getNearPoint(polygon.getPoints(), leftBotton);
        Vector3 leftTopNear = getNearPoint(polygon.getPoints(), leftTop);
        Vector3 rightTopNear = getNearPoint(polygon.getPoints(), rightTop);
        Vector3 rightBottonNear = getNearPoint(polygon.getPoints(), rightBotton);
        ArrayList<KPolygon> polygons = new ArrayList<>();
        ArrayList<Vector3> boundry = new ArrayList<>();// 边缘按leftBottonNear起始排序
        int startIndex = -1;
        for (int i = 0; i < polygon.getPoints().size(); i++) {
            if (polygon.getPoints().get(i).equals(leftBottonNear)) {
                startIndex = i;
            }
            if (startIndex >= 0) {
                boundry.add(polygon.getPoints().get(i));
            }
        }
        for (int i = 0; i < startIndex; i++) {
            boundry.add(polygon.getPoints().get(i));
        }
        polygons.add(getBoundryPolygon(boundry, leftBottonNear, leftTopNear, leftBotton, leftTop));
        polygons.add(getBoundryPolygon(boundry, leftTopNear, rightTopNear, leftTop, rightTop));
        polygons.add(getBoundryPolygon(boundry, rightTopNear, rightBottonNear, rightTop, rightBotton));
        polygons.add(getBoundryPolygon(boundry, rightBottonNear, leftBottonNear, rightBotton, leftBotton));
        KMultiPolygon kmp = new KMultiPolygon(polygons);
        return kmp;
    }

    private Vector3 getNearPoint(List<Vector3> list, Vector3 point) {
        Vector3 near = null;
        double dis = Integer.MAX_VALUE;
        for (Vector3 v : list) {
            double d = v.distanceSq(point);
            if (d < dis) {
                near = v;
                dis = d;
            }
        }
        return near;
    }

    private KPolygon getBoundryPolygon(List<Vector3> list, Vector3 start, Vector3 end, Vector3 border1, Vector3 border2) {
        List<Vector3> out = new ArrayList<>();
        List<Vector3> in = new ArrayList<>();
        boolean isIn = false;
        for (Vector3 v : list) {
            if (v.equals(start) || v.equals(end)) {
                isIn = !isIn;
                in.add(v);
            } else if (isIn) {
                in.add(v);
            } else {
                out.add(v);
            }
        }
        List<Vector3> points = new ArrayList<>();
        if (in.size() > out.size()) {
            out.remove(start);
            out.remove(end);
            points.add(end);
            points.addAll(out);
            points.add(start);
        } else {
            points.addAll(in);
        }
        points.add(border1);
        points.add(border2);
        return getKPolygon(points);
    }

    private void makePathBlockingPolygon(NodeConnector nodeConnector, ArrayList<PathBlockingObstacleImpl> stationaryObstacles, RandomPointsBuilder rpb, KPolygon polygon, boolean bufferOuter, boolean editor) {
        PathBlockingObstacleImpl obst = null;
        if (bufferOuter) {
            obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(polygon);
        } else {
            obst = PathBlockingObstacleImpl.createObstacleFromOuterPolygon(polygon);
        }
        com.vividsolutions.jts.geom.Geometry jtsPolygon = polygonConverter.makeJTSPolygonFrom(polygon);
        if (editor) {
            rpb.setNumPoints((int) ((Math.sqrt(jtsPolygon.getArea() / scale / scale) + 1) * 5));
        } else {
            rpb.setNumPoints((int) ((Math.sqrt(jtsPolygon.getArea()) + 1) * 5));
        }
        rpb.setExtent(jtsPolygon);
        if (polygon.getY() > 0) {
            obst.addRandomPoints(rpb.getGeometry().getCoordinates());
        }
        stationaryObstacles.add(obst);
        nodeConnector.addObstacle(obst, stationaryObstacles, maxDistanceBetweenObstacles);
    }

    public final void createPolygons(NodeConnector nodeConnector, ArrayList<PathBlockingObstacleImpl> stationaryObstacles, int[] triangles, Vector3[] vertices, boolean bufferOuter, boolean editor) {
        if (triangles == null || vertices == null) {
            return;
        }
        ArrayList<Vector3> list = new ArrayList<>(3);
        if (editor) {
            for (Vector3 li : vertices) {
                li.multiply(scale);
            }
        }
        RandomPointsBuilder rpb = new RandomPointsBuilder();
        for (int i = 0; i < triangles.length; i += 3) {
            if (triangles.length <= i + 2) {
                break;
            }
            list.clear();
            list.add(vertices[triangles[i]]);
            list.add(vertices[triangles[i + 1]]);
            list.add(vertices[triangles[i + 2]]);
            com.vividsolutions.jts.geom.Polygon jtsPolygon = getPolygon(list);
            if (jtsPolygon == null) {
                continue;
            }
            KPolygon poly = polygonConverter.makeKPolygonFromExterior(jtsPolygon);
            if (poly == null) {
                continue;
            }
            KPolygon copy = poly.copy();
            PathBlockingObstacleImpl obst = null;
            if (bufferOuter) {
                obst = PathBlockingObstacleImpl.createObstacleFromInnerPolygon(copy);
            } else {
                obst = PathBlockingObstacleImpl.createObstacleFromOuterPolygon(copy);
            }
            if (obst == null) {
                continue;
            }
            if (editor) {
                rpb.setNumPoints((int) ((Math.sqrt(jtsPolygon.getArea() / scale / scale) + 1)));
            } else {
                rpb.setNumPoints((int) ((Math.sqrt(jtsPolygon.getArea()) + 1)));
            }
            rpb.setExtent(jtsPolygon);
            if (copy.getY() > 0) {
                obst.addRandomPoints(rpb.getGeometry().getCoordinates());
            }
            stationaryObstacles.add(obst);
            nodeConnector.addObstacle(obst, stationaryObstacles, maxDistanceBetweenObstacles);
        }
    }

    /**
     * 获取矩形,根据当前坐标点的正前方
     *
     * @param position        当前位置
     * @param distance        距离
     * @param sourceDirection 当前方向，注意是unity的方向
     * @param width
     * @param height
     * @return
     */
    public final KPolygon getRectKPolygon(Vector3 position, double distance, double sourceDirection, float width, float height) {
        Vector3 source = position.unityTranslate(sourceDirection, 0, distance);
        Vector3 corner_1 = source.unityTranslate(sourceDirection, -90, width / 2);
        Vector3 corner_2 = source.unityTranslate(sourceDirection, 90, width / 2);
        Vector3 corner_3 = corner_2.unityTranslate(sourceDirection, 0, height);
        Vector3 corner_4 = corner_1.unityTranslate(sourceDirection, 0, height);
        List<Vector3> sectors = new ArrayList<>(4);
        sectors.add(corner_1);
        sectors.add(corner_4);
        sectors.add(corner_3);
        sectors.add(corner_2);
        return getKPolygon(sectors);
    }

    /**
     * 根据当前位置获取扇形
     *
     * @param position
     * @param sourceDirection
     * @param distance
     * @param radius
     * @param degrees
     * @return
     */
    public final KPolygon getKPolygon(Vector3 position, int sourceDirection, float distance, float radius, float degrees) {
        Vector3 source = position.unityTranslate(sourceDirection, 0, distance);
        Vector3 forward_l = position.unityTranslate(sourceDirection, -degrees / 2, radius);
        Vector3 forward_r = position.unityTranslate(sourceDirection, degrees / 2, radius);
        List<Vector3> sectors = new ArrayList<>(4);
        sectors.add(source);
        sectors.add(forward_l);
        int size = (int) (degrees / 10) / 2 - 1;
        for (int i = -size; i <= size; i++) {
            Vector3 forward = position.unityTranslate(sourceDirection, i * 10, radius);
            sectors.add(forward);
        }
        sectors.add(forward_r);
        return getKPolygon(sectors);
    }

    /**
     * 根据半径获取一个多边形
     *
     * @param center
     * @param radius
     * @param vertexCount
     * @return
     */
    public final KPolygon getKPolygon(Vector3 center, float radius, int vertexCount) {
        if (vertexCount < 3) {
            vertexCount = 3;
        }
        List<Vector3> sectors = new ArrayList<>(vertexCount);
        double degrees = 360d / vertexCount;
        Random random = new Random(System.currentTimeMillis());
        double randomDegrees = random.nextFloat() * 360;
        for (int i = 0; i < vertexCount; i++) {
            Vector3 source = center.translateCopy(i * degrees + randomDegrees, radius);
            sectors.add(source);
        }
        return getKPolygon(sectors);
    }

    public final KPolygon getKPolygon(List<Vector3> list) {
        com.vividsolutions.jts.geom.Polygon jtsPolygon = getPolygon(list);
        if (jtsPolygon == null) {
            return null;
        }
        KPolygon poly = polygonConverter.makeKPolygonFromExterior(jtsPolygon);
        return poly;
    }

    public final com.vividsolutions.jts.geom.Polygon getPolygon(List<Vector3> pos) {
        KPolygon poly = new KPolygon(pos);
        com.vividsolutions.jts.geom.Polygon jtsPolygon = polygonConverter.makeJTSPolygonFrom(poly);
        return jtsPolygon;
    }

    public PathData path(Vector3 start, Vector3 end) {
        PathData data;
        synchronized (pathFinder) {
            data = pathFinder.calc(start, end, this.maxDistanceBetweenObstacles, getBlockNodeConnector(), getBlockStationaryObstacles());
        }
        return data;
    }

    /**
     * @param start
     * @param end
     * @param stopDistance 停止距离
     * @return
     */
    public PathData path(Vector3 start, Vector3 end, double stopDistance) {
        PathData data;
        synchronized (pathFinder) {
            data = pathFinder.calc(start, end, this.maxDistanceBetweenObstacles, getBlockNodeConnector(), getBlockStationaryObstacles());
            if (data != null && stopDistance > 0) {
                Vector3 stopEnd = null;
                while (stopDistance > 0 && data.points.size() > 1) {
                    stopEnd = Vector3.getStopPoint(data.points.get(data.points.size() - 2), data.points.get(data.points.size() - 1), stopDistance);
                    stopDistance -= data.points.get(data.points.size() - 2).distance(data.points.get(data.points.size() - 1));
                    data.points.remove(data.points.size() - 1);
                }
                if (stopEnd != null) {
                    data.points.add(stopEnd);
                }
            }
        }
        return data;
    }

    public PathData pathNear(Vector3 start, Vector3 end) {
        PathData data;
        synchronized (pathFinder) {
            data = pathFinder.calcNear(start, end, this.maxDistanceBetweenObstacles, this.maxDistanceBetweenObstacles, getBlockNodeConnector(), getBlockStationaryObstacles());
        }
        return data;
    }

    /**
     * 从集合中随机一个元素
     *
     * @param <T>
     * @param collection
     * @return
     */
    protected <T> T random(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int t = (int) (collection.size() * Math.random());
        int i = 0;
        for (Iterator<T> item = collection.iterator(); i <= t && item.hasNext(); ) {
            T next = item.next();
            if (i == t) {
                return next;
            }
            i++;
        }
        return null;
    }

    public Vector3 getPointInPaths(double x, double z) {
        Vector3 movedPoint = new Vector3(x, z);
        return getPointInPaths(movedPoint);
    }

    public Vector3 getPointInPaths(Vector3 point) {
        for (PathBlockingObstacleImpl obst : getPathStationaryObstacles()) {
            if (obst.getInnerPolygon().contains(point)) {
                KPolygon poly = obst.getInnerPolygon();
                if (poly != null) {
                    point.y = poly.getY();
                    break;
                }
            }
        }
        if (!isPointInBlocks(point) && isPointInPaths(point)) {
            return point;
        }
        return null;
    }

    /**
     * 验证点位是否寻路层上
     *
     * @param movedPoint
     * @return
     */
    public boolean isPointInPaths(Vector3 movedPoint) {
        return isPointInPaths(movedPoint.x, movedPoint.z);
    }

    /**
     * 验证点位是否在寻路层上
     *
     * @param x
     * @param z
     * @return
     */
    public boolean isPointInPaths(double x, double z) {
        for (PathBlockingObstacleImpl obst : getPathStationaryObstacles()) {
            if (obst != null && obst.getInnerPolygon() != null && obst.getInnerPolygon().contains(x, z)) {
                KPolygon poly = obst.getInnerPolygon();
                if (poly != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 验证点位是否是阻挡点位
     *
     * @param movedPoint
     * @return
     */
    public boolean isPointInBlocks(Vector3 movedPoint) {
        return isPointInBlocks(movedPoint.x, movedPoint.z);
    }

    /**
     * 阻挡点位
     *
     * @param x
     * @param z
     * @return
     */
    public boolean isPointInBlocks(double x, double z) {
        for (PathBlockingObstacleImpl obst : getBlockStationaryObstacles()) {
            if (obst.getInnerPolygon().contains(x, z)) {
                KPolygon poly = obst.getInnerPolygon();
                if (poly != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPointInSafes(Vector3 movedPoint) {
        return isPointInSafes(movedPoint.x, movedPoint.z);
    }

    public boolean isPointInSafes(double x, double z) {
        if (!safeStationaryObstacles.isEmpty()) {
            for (PathBlockingObstacleImpl obst : safeStationaryObstacles) {
                if (obst.getInnerPolygon().contains(x, z)) {
                    KPolygon poly = obst.getInnerPolygon();
                    if (poly != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void amendPoint(Vector3 movedPoint) {
        for (PathBlockingObstacleImpl obst : getPathStationaryObstacles()) {
            if (obst.getInnerPolygon().contains(movedPoint)) {
                KPolygon poly = obst.getInnerPolygon();
                if (poly != null) {
                    movedPoint.y = poly.getY();
                    break;
                }
            }
        }
    }

    public KPolygon getPolygonInPaths(Vector3 movedPoint) {
        for (PathBlockingObstacleImpl obst : getPathStationaryObstacles()) {
            if (obst.getInnerPolygon().contains(movedPoint)) {
                KPolygon poly = obst.getInnerPolygon();
                if (poly != null) {
                    return poly;
                }
            }
        }
        return null;
    }

    public Vector3 getNearestPointInPaths(Vector3 point) {
        Vector3 movedPoint = point.copy();
        boolean targetIsInsideObstacle = false;
        int count = 0;
        while (true) {
            for (PathBlockingObstacleImpl obst : getBlockStationaryObstacles()) {
                if (obst.getOuterPolygon().contains(movedPoint)) {
                    targetIsInsideObstacle = true;
                    KPolygon poly = obst.getOuterPolygon();
                    Vector3 p = poly.getBoundaryPointClosestTo(movedPoint);
                    if (p != null) {
                        movedPoint.x = p.x;
                        movedPoint.z = p.z;
                        break;
                    }
                }
            }
            count++;
            if (targetIsInsideObstacle == false || count >= 3) {
                break;
            }
        }
        if (movedPoint != null) {
            movedPoint.y = point.y;
        }
        return movedPoint;
    }

    /**
     * @return the blockNodeConnector
     */
    public NodeConnector getBlockNodeConnector() {
        return blockNodeConnector;
    }

    /**
     * @return the blockStationaryObstacles
     */
    public ArrayList<PathBlockingObstacleImpl> getBlockStationaryObstacles() {
        return blockStationaryObstacles;
    }

    /**
     * @return the pathNodeConnector
     */
    public NodeConnector getPathNodeConnector() {
        return pathNodeConnector;
    }

    /**
     * @return the pathStationaryObstacles
     */
    public ArrayList<PathBlockingObstacleImpl> getPathStationaryObstacles() {
        return pathStationaryObstacles;
    }

    /**
     * @return the maxConnectionDistanceBetweenObstacles
     */
    public float getMaxConnectionDistanceBetweenObstacles() {
        return maxDistanceBetweenObstacles;
    }

    /**
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public float getHeight() {
        return height;
    }

    /**
     * @return the multiply
     */
    public float getScale() {
        return scale;
    }

    /**
     * @return the mapID
     */
    public int getMapID() {
        return mapID;
    }

    /**
     * @return the startX
     */
    public float getStartX() {
        return startX;
    }

    /**
     * @return the startZ
     */
    public float getStartZ() {
        return startZ;
    }

    /**
     * @return the endX
     */
    public float getEndX() {
        return endX;
    }

    /**
     * @return the endZ
     */
    public float getEndZ() {
        return endZ;
    }

    /**
     * @return the center
     */
    public Vector3 getCenter() {
        return center;
    }

    /**
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }
}
