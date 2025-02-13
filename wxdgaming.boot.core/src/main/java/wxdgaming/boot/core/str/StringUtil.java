package wxdgaming.boot.core.str;


import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.lang.RandomUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * utf-8字符集操作
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2020-12-30 20:33
 */
public class StringUtil {

    public static final Charset GB2313 = Charset.forName("GB2312");

    public static final String EMPTY_STRING = "";
    public static final int ZERO = 0;
    /** null 字符串 */
    public static final String nullStr = "null";
    /** null 字符串的字节数组 */
    public static final byte[] nullBytes = "null".getBytes();
    /** 换行符{@code \n} */
    public static final String Line = "\n";
    /** 换行符{@code \n} */
    public static final byte[] LineBytes = "\n".getBytes();
    /** windows 系统换行符 换行符 {@code \r\n} */
    public static final String WinLine = "\r\n";
    /** windows 系统换行符 换行符 {@code \r\n} */
    public static final byte[] WinLineBytes = "\r\n".getBytes();
    /** 空字符串 */
    public static final String FENHAO_REG = ";|；";// 分号
    public static final String MAOHAO_REG = ":|：";// 冒号
    public static final String DOUHAO_REG = ",|，";
    public static final String XIEGANG_REG = "/";
    public static final String SHUXIAN_REG = "\\|";
    public static final String XIAHUAXIAN_REG = "_";
    public static final String JINGHAO_REG = "\\#";
    public static final String DENGHAO = "=";
    public static final String AT_REG = "@";

    /** 随机算法 */
    public static final char[] HEXDIGITS =
            {
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
                    'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                    '2', '3', '4', '5', '6', '7', '8', '9'
            };

    /** 16 进制算法随机数 */
    public static final char[] HEXDIGITS_2 =
            {
                    'A', 'B', 'C', 'D', 'E', 'F',
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
            };

    /** 纯数字切割字符串 */
    public static Pattern PATTERN_0_9_Split = Pattern.compile("[\\d]+");

    /** 验证必须是 0 - 9 的数字 */
    public static final Pattern PATTERN_0_9 = Pattern.compile("[0-9]*");
    public static final Pattern PATTERN_Numeric = Pattern.compile("^[+-]?\\d*[.]?\\d*$");
    /** 验证必须是 数字 or 字母 or 下划线 */
    public static final Pattern PATTERN_ABC_0 = Pattern.compile("^[_\\s@a-zA-Z0-9]*$");
    /** 过滤 非 数字 or 字母 or 下划线 */
    public static final Pattern PATTERN_REPLACE_ABC_0 = Pattern.compile("[^_\\s@a-zA-Z0-9\\u4e00-\\u9fa5]");
    /** 验证必须是 数字 or 字母 or 下划线 */
    public static final Pattern PATTERN_ABC_PWD = Pattern.compile("^[~\"\"?<>\\[\\]={}_\\-*&()!:@$%\\^.\\s@a-zA-Z0-9]*$");
    /** 验证只能是,汉字，数字，字母，下划线 */
    public static final Pattern PATTERN_ABC_1 = Pattern.compile("^[_\\s@a-zA-Z0-9\\u4e00-\\u9fa5]*$");
    /** 不允许出现的特殊字符 */
    public static final Pattern PATTERN_ABC_1_1_1 = Pattern.compile("[\\[\\].,'\":;\\|`~(){}【】\\\\\\-=*&@\\^%]");
    /** 验证只能是,汉字，数字，字母 */
    public static final Pattern PATTERN_ABC_2 = Pattern.compile("^[\\s@a-zA-Z0-9\\u4e00-\\u9fa5]*$");
    /** 过滤 非 汉字，数字，字母，下划线 */
    public static final Pattern PATTERN_REPLACE_ABC_1 = Pattern.compile("[^_\\s@a-zA-Z0-9\\u4e00-\\u9fa5]");
    /** 验证必须是 存字母 A - Z */
    public static final Pattern PATTERN_A_Z = Pattern.compile("[a-zA-Z]*");
    /** 验证必须是字母开头 */
    public static final Pattern PATTERN_ABC = Pattern.compile("^[a-zA-Z]\\S+$");
    /** 纯汉字 */
    public static final Pattern PATTERN_UUU = Pattern.compile("^[\\u4e00-\\u9fa5]*$");
    /** 包含汉字 */
    public static final Pattern PATTERN_Have_UUU = Pattern.compile(".*[\\u4e00-\\u9fa5]+.*");
    /** 过滤 只保留汉字 */
    public static final Pattern PATTERN_REPLACE_UUU = Pattern.compile("[^\\u4e00-\\u9fa5]");
    /** 只保留英文 */
    public static final Pattern PATTERN_REPLACE_UUU_1 = Pattern.compile("[^a-zA-Z]");
    /** 只保留数字 */
    public static final Pattern PATTERN_REPLACE_UUU_2 = Pattern.compile("[^0-9]");
    /** 过滤掉，汉字，英文，数字之后的屏蔽词 */
    public static final Pattern PATTERN_REPLACE_UUU_3 = Pattern.compile("[a-zA-Z0-9\\u4e00-\\u9fa5]");

    /** 汉字，字母，数字，以及一些常规字符 */
    public static final Pattern PATTERN_ABC_00_UUU = Pattern.compile("^[~`“”\"\"?<>\\[\\]【】{}_\\-——=《》*&（）()!！:：#@$￥%……\\^.。,，\\s@a-zA-Z0-9\\u4e00-\\u9fa5]*$");
    /** 过滤 非 汉字，字母，数字，以及一些常规字符 */
    public static final Pattern PATTERN_REPLACE_ABC_00_UUU = Pattern.compile("[^~`“”\"\"?<>\\[\\]【】{}_=\\-——《》*&（）()!！:：#@$￥%……\\^.。,，\\s@a-zA-Z0-9\\u4e00-\\u9fa5]");
    /** 空格，换行符，制表符 */
    public static final Pattern FilterLine = Pattern.compile("\\s*|\t|\r|\n");
    /** 空格，换行符，制表符 */
    public static final Pattern FilterLine1 = Pattern.compile("\n|\r|\t");

    public static void main(String[] args) throws Exception {
        //        System.out.println(padLeft("dd", 4, ' '));
        //        System.out.println(checkMatches("@ageagteyt", PATTERN_A_Z));
        //        String asStr = "伙伴资质";
        //        String newString = new String(asStr.getBytes("gb2312"), StandardCharsets.ISO_8859_1);
        //        System.out.println(newString + ", " + newString.length());
        //        System.out.println(checkMatches("id", PATTERN_Have_UUU));
        //        System.out.println(checkMatches("int(4)", PATTERN_Have_UUU));
        //        System.out.println(checkMatches("伙伴资质id", PATTERN_Have_UUU));
        //        System.out.println(checkMatches("伙伴资质属性类别", PATTERN_Have_UUU));
        //        System.out.println(StringUtil.convert_InBase64_ASE("token=1a45ab3830b340de80da75e124d246f2&cmd=reloadscript大发了书法家拉设计费拉束带结发离开", 3, 2));
        //        System.out.println(checkMatches("aofjoaifj", StringUtil.PATTERN_ABC_1));
        //        System.out.println(checkMatches("aofjoaifjp[喊嫒时代峰峻破案时点击发送", StringUtil.PATTERN_ABC_1));
        //        System.out.println(checkFind("aofjoaifjp.[[喊嫒时代峰峻破案时点击发送", StringUtil.PATTERN_ABC_1_1_1));
        //        System.out.println(checkFind("aofjoaifjp[[喊嫒时代峰峻破案时点击发送", StringUtil.PATTERN_ABC_1_1_1));
        //        System.out.println(checkFind("aofjoaifjp[[喊嫒时代峰峻破案时点击发送", StringUtil.PATTERN_ABC_1_1_1));
        //        System.out.println("数字 or 字母 or 下划线 " + StringUtil.replaceFilter("d阿德发发发委托__-——ASDGFsdfsdg方群刚#%#￥%&￥*￥@3453456548#￥%&%……（*）@￥【】[][", StringUtil.PATTERN_ABC_1));
        //        System.out.println("数字 or 字母 or 下划线 " + StringUtil.replaceFilter("d阿德发发发委托__-—'''—ASDGFsdfsdg方群刚#%#￥%&￥*￥@3453456548#￥%&%……（*）@￥【】[][", StringUtil.PATTERN_REPLACE_UUU));
        //        System.out.println("数字 or 字母 or 下划线 " + StringUtil.checkFind("撸猪哥）（", StringUtil.PATTERN_ABC_00_UUU));
        String str = "  ddd\t嗯嗯   服\ndfd\r\nddd  ";
        System.out.println(str);
        System.out.println(replaceLine(str));
        System.out.println(replaceFilter(str, FilterLine));
        System.out.println(replaceFilter(str, FilterLine1));
        System.exit(0);
    }

    public static String getRandomString(int length) {
        // 产生随机数
        StringBuilder sb = new StringBuilder();
        // 循环length次
        for (int i = 0; i < length; i++) {
            // 产生0-2个随机数，既与a-z，A-Z，0-9三种可能
            int number = ThreadLocalRandom.current().nextInt(3);
            long result = 0;
            switch (number) {
                // 如果number产生的是数字0；
                case 0:
                    // 产生A-Z的ASCII码
                    result = Math.round(Math.random() * 25 + 65);
                    // 将ASCII码转换成字符
                    sb.append((char) result);
                    break;
                case 1:
                    // 产生a-z的ASCII码
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append((char) result);
                    break;
                case 2:
                    // 产生0-9的数字
                    sb.append(new Random().nextInt(10));
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 创建一个随机字符串
     *
     * @param count
     * @return
     */
    public static String uuid32(int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int index = RandomUtils.random(HEXDIGITS.length);
            stringBuilder.append(HEXDIGITS[index]);
        }
        return stringBuilder.toString();
    }

    /** uuid字符格式 8-4-4-4-12 */
    public static String uuid16() {
        return uuid16("");
    }

    /** uuid字符格式 8-4-4-4-12 */
    public static String uuid16(String start) {
        String x1 = randomUuid16(start, 32);
        int index = 0;
        char[] chars = new char[36];
        for (int i = 0; i < x1.length(); i++) {
            chars[index] = x1.charAt(i);
            if (index == 7
                    || index == 12
                    || index == 17
                    || index == 22) {
                index++;
                chars[index] = '-';
            }
            index++;
        }
        return new String(chars);
    }

    /** uuid字符格式 8-4-4-4-12 */
    public static String randomUuid16(String start, int len) {
        StringBuilder stringBuilder = new StringBuilder(len);
        if (notEmptyOrNull(start)) {
            stringBuilder.append(start);
        }
        if (stringBuilder.length() < len) {
            int fi = len - stringBuilder.length();
            for (int i = 0; i < fi; i++) {
                int index = RandomUtils.random(HEXDIGITS_2.length);
                stringBuilder.append(HEXDIGITS_2[index]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 整数，纯数字
     *
     * @param str
     * @return
     */
    public static boolean is0_9(String str) {
        return checkMatches(str, PATTERN_0_9);
    }

    /**
     * 整数或者小数
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return checkMatches(str, PATTERN_Numeric);
    }

    /**
     * 检查是否能通过,正则表达式全匹配规则
     * <p>
     * {@link Pattern#matches}
     *
     * @param str
     * @param regx
     * @return
     */
    public static boolean checkMatches(String str, Pattern regx) {
        return regx.matcher(str).matches();
    }

    /**
     * 检查是否能通过，搜索一组
     * <p>{@link Pattern#matcher(CharSequence)#find()}
     *
     * @param str
     * @param regx
     * @return
     */
    public static boolean checkFind(String str, Pattern regx) {
        return regx.matcher(str).find();
    }

    /** 过滤特殊字符 */
    public static String replaceFilter(String str, Pattern regx) {
        return regx.matcher(str).replaceAll("");
    }

    /** 去掉换行符, 制表符和空格 */
    public static String replaceLine(String str) {
        return FilterLine.matcher(str).replaceAll("");
    }

    /** 清理utf8-bom信息 */
    public static String clearUtf8Bom(String source) {
        char headChar = source.charAt(0);
        // 去除utf8-bom头
        if (headChar == 65279) {
            return source.substring(1);
        }
        return source;
    }

    /**
     * 字符串的组合
     *
     * @param chs
     * @return
     */
    public static List<List<String>> combiantion(String... chs) {
        if (chs == null || chs.length == 0) {
            return null;
        }
        List<String> listIn = new ArrayList<>();
        List<List<String>> listOut = new ArrayList<>();
        for (int i = 1; i <= chs.length; i++) {
            combine(chs, 0, i, listIn, listOut);
        }
        return listOut;
    }

    /**
     * 从字符数组中第begin个字符开始挑选number个字符加入list中
     *
     * @param cs
     * @param begin
     * @param number
     * @param listIn
     * @param listOut
     */
    private static void combine(String[] cs, int begin, int number, List<String> listIn, List<List<String>> listOut) {
        if (number == 0) {
            listOut.add(new ArrayList<>(listIn));
            return;
        }
        if (begin == cs.length) {
            return;
        }
        listIn.add(cs[begin]);
        combine(cs, begin + 1, number - 1, listIn, listOut);
        listIn.remove(cs[begin]);
        combine(cs, begin + 1, number, listIn, listOut);
    }

    /**
     * @param source
     * @param len
     * @param ch
     * @return
     * @author 尧
     * @author String左对齐
     */
    public static String padLeft(Object source, int len, char ch) {
        String valueOf = String.valueOf(source);
        int oldLength = valueOf.length();
        if (checkMatches(valueOf, PATTERN_Have_UUU)) {
            /*有汉字，转化汉字*/
            oldLength = new String(valueOf.getBytes(GB2313), StandardCharsets.ISO_8859_1).length() - 1;
        }
        int diff = len - oldLength;
        if (diff <= 0) {
            return valueOf;
        }
        char[] oldChars = valueOf.toCharArray();
        char[] newChars = new char[oldChars.length + diff];
        for (int i = 0; i < diff; i++) {
            newChars[i] = ch;
        }
        System.arraycopy(oldChars, 0, newChars, diff, oldChars.length);
        return new String(newChars);
    }

    /**
     * @param source
     * @param len
     * @param ch
     * @return
     * @author 尧
     * @author String右对齐
     */
    public static String padRight(Object source, int len, char ch) {
        String valueOf = String.valueOf(source);
        int oldLength = valueOf.length();
        if (checkMatches(valueOf, PATTERN_Have_UUU)) {
            /*有汉字，转化汉字*/
            oldLength = new String(valueOf.getBytes(GB2313), StandardCharsets.ISO_8859_1).length() - 1;
        }
        int diff = len - oldLength;
        if (diff <= 0) {
            return valueOf;
        }
        char[] oldChars = valueOf.toCharArray();
        char[] newChars = new char[oldChars.length + diff];
        System.arraycopy(oldChars, 0, newChars, 0, oldChars.length);
        for (int i = oldChars.length; i < newChars.length; i++) {
            newChars[i] = ch;
        }
        return new String(newChars);
    }

    /** 如果字符是null或者空白字符返回true */
    public static boolean emptyOrNull(String str) {
        return null == str || str.isEmpty() || str.isBlank();
    }

    /** 如果字符是null或者空白字符返回 false */
    public static boolean notEmptyOrNull(String str) {
        return !emptyOrNull(str);
    }

    public static List<String> lines(String str) {
        try (StringReader stringReader = new StringReader(str);
             BufferedReader bufferedReader = new BufferedReader(stringReader)) {
            return bufferedReader.lines().collect(Collectors.toList());
        } catch (Exception e) {
            throw Throw.of(e);
        }
    }

    /** 去掉空格，换行符，制表符 */
    public static String filterLine(String str) {
        Matcher m = FilterLine.matcher(str);
        return m.replaceAll("");
    }

    /**
     * 默认检查字符串左右两位，小于 70%相识度才存储到历史字符中
     *
     * @param oldStrings 历史字符串
     * @param str        当前字符串
     * @return
     */
    public static boolean selectStr(LinkedList<String> oldStrings, String str) {
        return selectStr(oldStrings, str, 2, 20, 85);
    }

    /**
     * 查重率
     *
     * @param oldStrings 历史字符串
     * @param str        当前字符串
     * @param indexofc   检查字符数对应的左右几位数相同
     * @param oldSize    oldStrings 历史字符串存留个数
     * @param dd         需要匹配的概率，
     * @return
     */
    public static boolean selectStr(LinkedList<String> oldStrings, String str, int indexofc, int oldSize, int dd) {
        int maxPro = 0;
        int minPro = 0;
        for (String oldStr : oldStrings) {
            int tis0 = selectStr0(oldStr, str, indexofc);
            int tis1 = selectStr1(oldStr, str, indexofc);
            /*寻找最大概率*/
            int tis = Math.max(tis0, tis1);
            if (maxPro < tis) {
                maxPro = tis;
            }
            /*寻找最小概率*/
            if (tis > 30) {
                minPro++;
            }
            if (maxPro >= dd) {
                break;
            }
        }
        if (maxPro < dd) {
            oldStrings.addFirst(str);
            if (oldStrings.size() > oldSize) {
                oldStrings.removeLast();
            }
        }

        float min = oldSize * 0.55f;
        boolean ret = maxPro >= dd;
        if (!ret) {
            ret = minPro > min;
        }
        System.out.println("字符串：“" + str + "” 与历史字符串最大匹配概率：" + maxPro + ", 包含重复文字的历史：" + minPro + ", 匹配结果：" + ret);
        return ret;
    }

    /**
     * 从前往后匹配
     *
     * @param oldStr
     * @param str
     * @param indexofc
     * @return
     */
    static int selectStr0(String oldStr, String str, int indexofc) {
        float tis = 0;
        for (int i = 0; i < oldStr.length(); i++) {
            char oldChar = oldStr.charAt(i);
            /*查找偏移位置*/
            for (int j = (i - indexofc); j < (i + indexofc); j++) {
                if (j >= 0 && j < str.length()) {
                    char schar = str.charAt(j);
                    if (schar == oldChar) {
                        tis++;
                        break;
                    }
                }
            }
        }
        if (str.length() > oldStr.length()) {
            tis = ((tis / str.length()) * 100);
        } else {
            tis = ((tis / oldStr.length()) * 100);
        }
        return (int) tis;
    }

    /**
     * 从后往前匹配
     *
     * @param oldStr
     * @param str
     * @param indexofc
     * @return
     */
    static int selectStr1(String oldStr, String str, int indexofc) {
        int is = 0;
        float tis = 0;
        for (int i = (oldStr.length() - 1); i >= 0; i--) {
            char oldChar = oldStr.charAt(i);
            /*查找偏移位置*/
            for (int j = (i + indexofc); j >= (i - indexofc); j--) {
                if (j >= 0 && j < str.length()) {
                    char schar = str.charAt(j);
                    if (schar == oldChar) {
                        tis++;
                        break;
                    }
                }
            }
        }
        if (str.length() > oldStr.length()) {
            tis = ((tis / str.length()) * 100);
        } else {
            tis = ((tis / oldStr.length()) * 100);
        }
        if (is < tis) {
            is = (int) tis;
        }
        return is;
    }

    /**
     * 将字符串的首字母转大写
     *
     * @param str 需要转换的字符串
     * @return
     */
    public static String upperFirst(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = str.toCharArray();
        cs[0] = Character.toUpperCase(cs[0]);
        return String.valueOf(cs);
    }

    /**
     * 将字符串的首字母转大写
     *
     * @param str 需要转换的字符串
     * @return
     */
    public static String lowerFirst(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs = str.toCharArray();
        cs[0] = Character.toLowerCase(cs[0]);
        return String.valueOf(cs);
    }

    /**
     * 统计字符串的hash计算方法
     */
    public static int hashcode(Object source) {
        return hashcode(source, false);
    }

    /**
     * 计算字符串的hash值
     *
     * @param source 需要计算hash值的字符串
     * @param abs    是否返回绝对值
     * @return
     */
    public static int hashcode(Object source, boolean abs) {
        double h = 0.0f;
        final String valueOf = String.valueOf(source);
        char[] chars = valueOf.toCharArray();
        for (char aChar : chars) {
            double tmp;
            if (h < 1000) {
                tmp = (h * 0.238f);
            } else {
                tmp = (h * 0.00238f);
            }
            h = h + tmp + (int) aChar;
        }
        h *= 10000;
        int code;
        if (h < Integer.MAX_VALUE) {
            code = (int) h;
        } else {
            code = Double.hashCode(h);
        }
        if (abs) {
            return Math.abs(code);
        }
        return code;
    }

    /**
     * @param source
     * @param abs
     * @param hashFactor hash 算法因子
     * @return
     */
    public static int hashIndex(Object source, boolean abs, int hashFactor) {
        final int hashcode = hashcode(source, abs);
        return hashIndex(hashcode, hashFactor);
    }

    public static int hashIndex(long hashcode, int hashFactor) {
        return (int) (hashcode % (int) (hashFactor * 3.8f) % hashFactor);
    }


    /**
     * @param string
     * @return
     * @Title: unicodeEncode
     * @Description: unicode编码 将中文字符转换成Unicode字符
     */
    public static String unicodeEncode(String string) {
        char[] utfBytes = string.toCharArray();
        String unicodeBytes = "";
        for (int i = 0; i < utfBytes.length; i++) {
            String hexB = Integer.toHexString(utfBytes[i]);
            if (hexB.length() <= 2) {
                hexB = "00" + hexB;
            }
            unicodeBytes = unicodeBytes + "\\u" + hexB;
        }
        return unicodeBytes;
    }

    /**
     * @param string
     * @return 转换之后的内容
     * @Title: unicodeDecode
     * @Description: unicode解码 将Unicode的编码转换为中文
     */
    public static String unicodeDecode(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }
        return string;
    }

}
