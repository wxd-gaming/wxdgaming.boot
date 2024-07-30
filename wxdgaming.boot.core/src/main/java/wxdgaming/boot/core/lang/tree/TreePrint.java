package wxdgaming.boot.core.lang.tree;

import java.util.*;

/**
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2021-07-20 20:39
 **/
interface TreePrint<E> {

    static String pad(String str, int len, char c) {
        if (str.length() < len) {
            final char[] chars = str.toCharArray();
            final char[] chars1 = Arrays.copyOf(chars, len);
            for (int i = chars.length; i < len; i++) {
                chars1[i] = c;
            }
            return new String(chars1);
        }
        return str;
    }

    /**
     * 遍历树中的集合
     */
    default My2Tree<E> print() {
        System.out.print("所有元素：");
        final List<E> values = ((My2Tree<E>) this).root.values();
        for (E value : values) {
            System.out.print(value + ", ");
        }
        System.out.println();
        return (My2Tree<E>) this;
    }

    /**
     * 按照2叉树结构打印
     */
    default My2Tree<E> printTree() {
        ((My2Tree<E>) this).resetSd();
        TreeMap<Integer, LinkedList<My2Tree<E>.TreeNode>> integerLinkedListTreeMap = ((My2Tree<E>) this).nodeMap();
        for (Map.Entry<Integer, LinkedList<My2Tree<E>.TreeNode>> integerLinkedListEntry : integerLinkedListTreeMap.entrySet()) {
            Integer h = integerLinkedListEntry.getKey();
            LinkedList<My2Tree<E>.TreeNode> nodes = integerLinkedListEntry.getValue();
            StringBuilder builder = new StringBuilder();

            int tmpH = h - 1;
            tmpH = (int) Math.pow(2, tmpH);
            if (tmpH < 1) {
                tmpH = 1;
            }
            int tmpL = 0;
            for (My2Tree.TreeNode node : nodes) {
                int tmpk = node.getKd() * tmpH - tmpL - 1;
                tmpL = node.getKd() * tmpH;

                for (int j = 0; j < tmpk; j++) {
                    builder.append("|")
                            .append(pad(" ", 3, ' '))
                            .append("|");
//                    builder.append(pad(" ", 5, ' '));
                }
                builder.append(node.toString());
            }
            System.out.println();
            System.out.println(builder.toString());
        }
        return (My2Tree<E>) this;
    }


    default My2Tree<E> printMinValue() {
        My2Tree<E>.TreeNode minNode = ((My2Tree<E>) this).minNode(((My2Tree<E>) this).root);
        System.out.println("最小值" + minNode.getData());
        return (My2Tree<E>) this;
    }


    default My2Tree<E> printMaxValue() {
        My2Tree<E>.TreeNode maxValue = ((My2Tree<E>) this).maxNode(((My2Tree<E>) this).root);
        System.out.println("最大值" + maxValue.getData());
        return (My2Tree<E>) this;
    }
}
