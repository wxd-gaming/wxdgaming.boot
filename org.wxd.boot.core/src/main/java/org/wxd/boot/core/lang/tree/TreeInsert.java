package org.wxd.boot.core.lang.tree;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-07-20 20:20
 **/
interface TreeInsert<E> {
    /**
     * 总体插入操作
     * 1.判断是否有树根，没有的话将数据添加到树根里
     * 2.有树根调用insert的重载方法，判断插入到左son还是右son
     */
    default My2Tree<E> add(E e) {
        if (((My2Tree<E>) this).root == null) {
            ((My2Tree<E>) this).root = ((My2Tree<E>) this).createNode(e);
        } else {
            ((My2Tree<E>) this).root = insert(e, ((My2Tree<E>) this).root);
        }
        return ((My2Tree<E>) this);
    }

    // 插入节点
    default My2Tree<E>.TreeNode insert(E e, My2Tree<E>.TreeNode curNode) {
        if (curNode == null) {
            return ((My2Tree<E>) this).createNode(e);
        }
        /* 思想：先让data和root中的值进行比较，大于0插入右边，小于0插入左边，计划使用递归思想*/
        @SuppressWarnings("unchecked")
        Comparable<? super E> car = (Comparable<? super E>) e;

        int compareTo = car.compareTo(curNode.getData());
        // 等于root.data
        if (compareTo == 0) {
            return curNode;
        }

        if (compareTo > 0) {
            curNode.rNode = insert(e, curNode.rNode);
        } else {
            curNode.lNode = insert(e, curNode.lNode);
        }
        return curNode;
    }
}
