package org.wxd.boot.str;

import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * 字符串检查
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class PatternStringUtil {

    public static boolean ShowLogOut = false;
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00%");// 构造方法的字符格式这里如果小数不足2位,会以0补足.

    public static void main(String[] args) {
        ShowLogOut = true;
        LinkedList<String> subSet2 = new LinkedList<>();
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公1众2号3公4众5号6公7众8号adfasf");// 保存s1的子串
        checkFilter(subSet2, "公q众w号e公r众t号y公u众i号asdfgh");// 保存s2的子串
        checkFilter(subSet2, "公q众w号e公r众t号y公u众i号mjgdsrt");// 保存s2的子串
        checkFilter(subSet2, "公f众w%号^公&众*号(公众)号mjgdsrt");// 保存s2的子串)
        checkFilter(subSet2, "公f众w%号^公8众*号(公众)号mjg5srt");// 保存s2的子串)
        checkFilter(subSet2, "是事实市师湿");// 保存s2的子串)
    }

    public static void checkFilter(LinkedList<String> ses, String content) {
        System.out.print("检查字符串：" + content + ", ");
        System.out.println(decimalFormat.format(checkFilter(ses, 50, content, 0.03f, 0.02f)));
//        float v = checkFilter(ses, 50, StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU), 70, 80);
//        v += checkFilter(ses, 50, StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU_1), 70, 80);
//        v += checkFilter(ses, 50, StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU_2), 70, 80);
//        v += checkFilter(ses, 50, StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU_3), 70, 80);
//        System.out.println(decimalFormat.format(v / 4));
    }

    /**
     * @param ses         类似累计字符
     * @param saveCount
     * @param content     当前对比字符
     * @param perAll      历史累计字符串中重现重复关键字的累计条数概率
     * @param perCountMax 出现了相同字符最大的累计字符数
     */
    public static float checkFilter(LinkedList<String> ses, int saveCount, String content, float perAll, float perCountMax) {
        float countiMax = 0;
        float countiMin = content.length();
        float countAll = 0;
        if (ses.size() > 5) {
            for (String se : ses) {
                int counti = 0;
//                if (se.equalsIgnoreCase(content)) {
//                    /*如果完全匹配*/
//                    counti = content.length();
//                } else
                {
                    for (int i = 0; i < content.length(); i++) {
                        char newcharAt = content.charAt(i);
                        if (se.indexOf(newcharAt, i) >= 0) {
                            counti++;
                            break;
                        }
                    }
                }
                if (counti > 0) {
                    if (countiMin > counti) {
                        countiMin = counti;
                    }

                    if (countiMax < counti) {
                        countiMax = counti;
                    }
                    countAll++;
                }
            }
        }

        ses.add(content);
        if (ses.size() > saveCount) {
            ses.remove(0);
        }
        if (countiMin == -1) {
            countiMin = 0;
        }
        float pall = countAll / ses.size();
        float pmax = countiMax / content.length();
        float pmin = countiMin / content.length();
        if (ShowLogOut) {
            if (pall >= perAll && pmax >= perCountMax) {
                System.out.print("历史累计重复率：" + decimalFormat.format(pall) + " , ");
                System.out.print("历史记录匹配的最高重复率：" + decimalFormat.format(pmax) + ", ");
                System.out.print("历史记录匹配的最低重复率：" + decimalFormat.format(pmin) + "");
            }
        }
        return pall;
    }

    /**
     * 把数组类型还原成代码 字符串
     *
     * @param source
     * @return
     */
    public static String typeString(String source) {
        if (source.startsWith("[[L")) {
            source = source.substring(2, source.length() - 1) + "[][]";
        } else if (source.startsWith("[L")) {
            source = source.substring(2, source.length() - 1) + "[]";
        } else if (source.startsWith("[[Z")) {
            source = "boolean[][]";
        } else if (source.startsWith("[Z")) {
            source = "boolean[]";
        } else if (source.startsWith("[[B")) {
            source = "byte[][]";
        } else if (source.startsWith("[B")) {
            source = "byte[]";
        } else if (source.startsWith("[[C")) {
            source = "char[][]";
        } else if (source.startsWith("[C")) {
            source = "char[]";
        } else if (source.startsWith("[[S")) {
            source = "short[][]";
        } else if (source.startsWith("[S")) {
            source = "short[]";
        } else if (source.startsWith("[[I")) {
            source = "int[][]";
        } else if (source.startsWith("[I")) {
            source = "int[]";
        } else if (source.startsWith("[[J")) {
            source = "long[][]";
        } else if (source.startsWith("[J")) {
            source = "long[]";
        } else if (source.startsWith("[[F")) {
            source = "float[][]";
        } else if (source.startsWith("[F")) {
            source = "float[]";
        } else if (source.startsWith("[[D")) {
            source = "double[][]";
        } else if (source.startsWith("[D")) {
            source = "double[]";
        }
        source = source.replace("$", ".");
        return source;
    }

}
