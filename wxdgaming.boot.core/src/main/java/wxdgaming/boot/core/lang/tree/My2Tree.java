package wxdgaming.boot.core.lang.tree;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * 二叉树
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-07-19 10:29
 **/
public class My2Tree<E>
        implements
        TreeInsert<E>,
        TreeRemove<E>,
        TreeFind<E>,
        TreePrint<E> {

    protected TreeNode root;

    protected TreeNode createNode(E e) {
        return new TreeNode().setData(e);
    }

    /**
     * 重新计算节点深度和宽度
     */
    protected void resetSd() {
        root.setSd(1).setKd(1);
        root.resetSd();
    }

    /**
     * 所有节点
     */
    public List<E> values() {
        return root.values();
    }

    /**
     * <pre>
     * 高度从叶子开始算数；
     * 深度是从根开始算
     * </pre>
     */
    public int hd() {
        return root.hd();
    }

    /**
     * <pre>
     * 高度从叶子开始算数；
     * 深度是从根开始算
     * </pre>
     */
    public int sd() {
        return root.sd();
    }

    public class TreeNode {

        protected E data;
        /**
         * 当前节点的深度
         */
        protected int sd;
        /**
         * 当前节点的宽度
         */
        protected int kd;
        protected TreeNode lNode;
        protected TreeNode rNode;

        // 左树是否为空
        public boolean hasLNode() {
            return lNode != null;
        }

        // 右树是否为空
        public boolean hasRNode() {
            return rNode != null;
        }

        /*中序遍历所有的节点*/
        public List<E> values() {
            List<E> values = new LinkedList<>();
            values(values);
            return values;
        }

        public void values(List<E> values) {
            // 左节点的遍历结果
            if (hasLNode()) {
                lNode.values(values);
            }
            // 当前节点
            values.add(data);
            // 右节点的遍历结果
            if (hasRNode()) {
                rNode.values(values);
            }
        }

        /**
         * 所有度的所有节点
         *
         * @param h   数的高度
         * @param map
         * @return
         */
        protected void nodeMap(int h, TreeMap<Integer, LinkedList<TreeNode>> map) {
            LinkedList<TreeNode> ts = map.computeIfAbsent(h, l -> new LinkedList<>());
            ts.add(this);
            if (!hasLNode() && !hasRNode()) {
                return;
            }
            int h1 = h - 1;
            // 左节点的遍历结果
            if (hasLNode()) {
                lNode.nodeMap(h1, map);
            }

            // 右节点的遍历结果
            if (hasRNode()) {
                rNode.nodeMap(h1, map);
            }
        }

        /**
         * 高度从叶子开始算数；
         */
        public int hd() {
            int tmpLH = 0;
            if (hasLNode()) {
                tmpLH = lNode.hd();
            }
            int tmpRH = 0;
            if (hasRNode()) {
                tmpRH = rNode.hd();
            }
            int h = Math.max(tmpLH, tmpRH) + 1;

            return h;
        }

        /**
         * 深度是从根开始算
         */
        public int sd() {
            int tmpLS = 0;
            if (hasLNode()) {
                tmpLS = lNode.hd();
            }
            int tmpRH = 0;
            if (hasRNode()) {
                tmpRH = rNode.hd();
            }
            int h = Math.max(tmpLS, tmpRH) + 1;

            return h;
        }

        /**
         * 重新计算节点深度和宽度
         */
        public void resetSd() {
            if (this.hasLNode()) {
                this.getLNode().setSd(this.getSd() + 1).setKd(this.getKd() * 2 - 1);
                this.getLNode().resetSd();
            }
            if (this.hasRNode()) {
                this.getRNode().setSd(this.getSd() + 1).setKd(this.getKd() * 2 + 1);
                this.getRNode().resetSd();
            }
        }

        public E getData() {
            return data;
        }

        public TreeNode setData(E data) {
            this.data = data;
            return this;
        }

        public int getSd() {
            return sd;
        }

        public TreeNode setSd(int sd) {
            this.sd = sd;
            return this;
        }

        /**
         * 当前节点宽度
         */
        public int getKd() {
            return kd;
        }

        /**
         * 当前节点宽度
         */
        public TreeNode setKd(int kd) {
            this.kd = kd;
            return this;
        }

        /**
         * 左节点
         */
        public TreeNode getLNode() {
            return lNode;
        }

        /**
         * 左节点
         */
        public TreeNode setLNode(TreeNode lNode) {
            this.lNode = lNode;
            return this;
        }

        /**
         * 右节点
         */
        public TreeNode getRNode() {
            return rNode;
        }

        /**
         * 右节点
         */
        public TreeNode setRNode(TreeNode rNode) {
            this.rNode = rNode;
            return this;
        }

        @Override
        public String toString() {
            return "[" + TreePrint.pad(String.valueOf(data), 3, ' ') + "]";
        }
    }

}
