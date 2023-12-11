package org.wxd.boot.lang.tree;

/**
 * 自平衡二叉树
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-07-19 10:29
 **/
public class MyAvl2Tree<E> extends My2Tree<E> {
    public MyAvl2Tree() {
    }

    @Override
    public TreeNode insert(E e, TreeNode curNode) {
        curNode = super.insert(e, curNode);
        /*普通二叉树是不做旋转的，平衡二叉树需要做左右旋转，达到最后树平衡状态*/
        return balance(curNode);
    }

    @Override
    public TreeNode remove(E e, TreeNode curNode) {
        curNode = super.remove(e, curNode);
        /*普通二叉树是不做旋转的，平衡二叉树需要做左右旋转，达到最后树平衡状态*/
        return balance(curNode);
    }

    /**
     * 旋转处理;通过判断当前树的左右高度差判定左右树
     */
    protected TreeNode balance(TreeNode x) {
        if (balanceFactor(x) < -1) {
            if (balanceFactor(x.rNode) > 0) {
                x.rNode = balanceRight(x.rNode);
            }
            x = balanceLeft(x);
        } else if (balanceFactor(x) > 1) {
            if (balanceFactor(x.lNode) < 0) {
                x.lNode = balanceLeft(x.lNode);
            }
            x = balanceRight(x);
        }
        return x;
    }

    /**
     * 查找当前树的左右高度差
     */
    protected int balanceFactor(TreeNode curNode) {
        if (curNode == null) return 0;
        int leftH = 0;
        int rightH = 0;
        if (curNode.hasLNode())
            leftH = curNode.getLNode().hd();
        if (curNode.hasRNode())
            rightH = curNode.getRNode().hd();
        return leftH - rightH;
    }

    /**
     * 将传入节点的左子节点提升为根
     *
     * @return 返回旋转完毕后的子树
     */
    protected TreeNode balanceRight(TreeNode curRoot) {
        // 将被破坏节点的左子节点提升为根节点
        TreeNode tmpRoot = curRoot.getLNode();
        // 如果新根节点的右边不为空，把它放进被破坏节点的左边
        curRoot.setLNode(tmpRoot == null ? null : tmpRoot.getRNode());
        // 被破坏节点放进新根节点的右边
        tmpRoot.setRNode(curRoot);
        return tmpRoot;
    }

    /**
     * 将传入节点的右子节点提升为根
     *
     * @return 返回旋转完毕后的子树
     */
    protected TreeNode balanceLeft(TreeNode curRoot) {
        // 将被破坏节点的右子节点提升为根节点
        TreeNode tmpRoot = curRoot.getRNode();
        // 如果新根节点的左边不为空，转移到被破坏节点的右边
        curRoot.setRNode(tmpRoot == null ? null : tmpRoot.getLNode());
        // 被破坏节点放进新根节点的左边
        tmpRoot.setLNode(curRoot);
        return tmpRoot;
    }

}
