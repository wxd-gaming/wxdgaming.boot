package straightedge.geom.path;

import straightedge.geom.Vector3;

/**
 * 寻路数据
 * author 失足程序员<br>
 * mail 492794628@qq.com<br>
 * phone 13882122019<br>
 */
public class NavMeshData {
    private int[] blockTriangles;
    private Vector3[] blockVertices;

    private int[] pathTriangles;
    private Vector3[] pathVertices;

    private int[] safeTriangles;
    private Vector3[] safeVertices;

    private volatile float startX;
    private volatile float startZ;
    private volatile float endX;
    private volatile float endZ;
    private int mapID;

    public NavMeshData() {
    }

    /**
     * @return the blockTriangles
     */
    public int[] getBlockTriangles() {
        return blockTriangles;
    }

    public void Sub(float x, float z) {
        Vector3 v = null;
        if (blockVertices != null) {
            for (int i = 0, length = blockVertices.length; i < length; i++) {
                v = blockVertices[i];
                v.setX(v.x - x);
                v.setZ(v.z - z);
            }
        }

        if (pathVertices != null) {
            for (int i = 0, length = pathVertices.length; i < length; i++) {
                v = pathVertices[i];
                v.setX(v.x - x);
                v.setZ(v.z - z);
            }
        }

        if (safeVertices != null) {
            for (int i = 0, length = safeVertices.length; i < length; i++) {
                v = safeVertices[i];
                v.setX(v.x - x);
                v.setZ(v.z - z);
            }
        }

    }

    /**
     * @param blockTriangles the blockTriangles to set
     */
    public void setBlockTriangles(int[] blockTriangles) {
        this.blockTriangles = blockTriangles;
    }

    /**
     * @return the blockVertices
     */
    public Vector3[] getBlockVertices() {
        return blockVertices;
    }

    /**
     * @param blockVertices the blockVertices to set
     */
    public void setBlockVertices(Vector3[] blockVertices) {
        this.blockVertices = blockVertices;
    }

    /**
     * @return the pathTriangles
     */
    public int[] getPathTriangles() {
        return pathTriangles;
    }

    /**
     * @param pathTriangles the pathTriangles to set
     */
    public void setPathTriangles(int[] pathTriangles) {
        this.pathTriangles = pathTriangles;
    }

    /**
     * @return the pathVertices
     */
    public Vector3[] getPathVertices() {
        return pathVertices;
    }

    /**
     * @param pathVertices the pathVertices to set
     */
    public void setPathVertices(Vector3[] pathVertices) {
        this.pathVertices = pathVertices;
    }

    /**
     * @return the mapID
     */
    public int getMapID() {
        return mapID;
    }

    /**
     * @param mapID the mapID to set
     */
    public void setMapID(int mapID) {
        this.mapID = mapID;
    }

    /**
     * @return the startX
     */
    public float getStartX() {
        return startX;
    }

    /**
     * @param startX the startX to set
     */
    public void setStartX(float startX) {
        this.startX = startX;
    }

    /**
     * @return the startZ
     */
    public float getStartZ() {
        return startZ;
    }

    /**
     * @param startZ the startZ to set
     */
    public void setStartZ(float startZ) {
        this.startZ = startZ;
    }

    /**
     * @return the endX
     */
    public float getEndX() {
        return endX;
    }

    /**
     * @param endX the endX to set
     */
    public void setEndX(float endX) {
        this.endX = endX;
    }

    /**
     * @return the endZ
     */
    public float getEndZ() {
        return endZ;
    }

    /**
     * @param endZ the endZ to set
     */
    public void setEndZ(float endZ) {
        this.endZ = endZ;
    }

    /**
     * @return the safeTriangles
     */
    public int[] getSafeTriangles() {
        return safeTriangles;
    }

    /**
     * @param safeTriangles the safeTriangles to set
     */
    public void setSafeTriangles(int[] safeTriangles) {
        this.safeTriangles = safeTriangles;
    }

    /**
     * @return the safeVertices
     */
    public Vector3[] getSafeVertices() {
        return safeVertices;
    }

    /**
     * @param safeVertices the safeVertices to set
     */
    public void setSafeVertices(Vector3[] safeVertices) {
        this.safeVertices = safeVertices;
    }


}
