package org.wxd.boot.lang.tree;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-07-20 20:30
 **/
interface TreeRemove<E> {

    default void remove(E e) {
        ((My2Tree<E>) this).root = remove(e, ((My2Tree<E>) this).root);
    }

    default My2Tree<E>.TreeNode remove(E e, My2Tree<E>.TreeNode curNode) {
        if (curNode == null) return curNode;
        /* 思想：先让data和root中的值进行比较，大于0插入右边，小于0插入左边，计划使用递归思想*/
        @SuppressWarnings("unchecked")
        Comparable<? super E> car = (Comparable<? super E>) e;
        int compareTo = car.compareTo(curNode.data);
        if (compareTo == 0) {
            if (curNode.lNode == null) {
                return curNode.rNode;
            } else if (curNode.rNode == null) {
                return curNode.lNode;
            } else {
                My2Tree.TreeNode y = curNode;
                curNode = ((My2Tree<E>) this).minNode(y.rNode);
                curNode.rNode = removeMin(y.rNode);
                curNode.lNode = y.lNode;
            }
        } else if (compareTo < 0) {
            curNode.lNode = remove(e, curNode.lNode);
        } else if (compareTo > 0) {
            curNode.rNode = remove(e, curNode.rNode);
        }
        return curNode;
    }

    /**
     * 移除当前节点的最小节点
     */
    default My2Tree<E>.TreeNode removeMin(My2Tree<E>.TreeNode curNode) {
        if (curNode.lNode == null) return curNode.rNode;
        curNode.lNode = removeMin(curNode.lNode);
        return curNode;
    }

}
