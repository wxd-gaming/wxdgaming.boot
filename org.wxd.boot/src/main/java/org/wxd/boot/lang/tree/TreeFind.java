package org.wxd.boot.lang.tree;

import java.util.LinkedList;
import java.util.TreeMap;

/**
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-07-20 20:34
 **/
interface TreeFind<E> {
    /**
     * 找出树中最小的值
     */
    default My2Tree<E>.TreeNode minNode(My2Tree<E>.TreeNode tree) {
        if (tree.hasLNode()) {
            return minNode(tree.lNode);
        }
        return tree;
    }

    /*找出树中最大的值*/
    default My2Tree<E>.TreeNode maxNode(My2Tree<E>.TreeNode tree) {
        if (tree.hasRNode()) {
            return maxNode(tree.rNode);
        }
        return tree;
    }

    default TreeMap<Integer, LinkedList<My2Tree<E>.TreeNode>> nodeMap() {
        TreeMap<Integer, LinkedList<My2Tree<E>.TreeNode>> map = new TreeMap<>((o1, o2) -> Integer.compare(o2, o1));
        ((My2Tree<E>) this).root.nodeMap(((My2Tree<E>) this).root.hd(), map);
        return map;
    }
}
