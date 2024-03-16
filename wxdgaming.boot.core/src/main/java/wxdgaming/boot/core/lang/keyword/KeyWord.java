package wxdgaming.boot.core.lang.keyword;


import wxdgaming.boot.agent.exception.Throw;
import wxdgaming.boot.core.str.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 关键字实体类
 * <p>
 * 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 *
 * @author: Troy.Chen(無心道, 15388152619)
 * @version: 2021-04-07 18:17
 **/
public class KeyWord {

    /**
     * @param args
     */
    public static void main(String[] args) {
        String guanjianzi = "TMD;习近平;fuck";

        KeyWord keyWord1 = new KeyWord(false, false)
                .addKeyWord2HashMap(guanjianzi);
        keyWord1.remove("TMD");

        KeyWord keyWord2 = new KeyWord(true, true).addKeyWord2HashMap(guanjianzi);

        String string = "I'm 日习Xyfu123近平,F~u~c~k you,F~uck me,fuck f u c k dog,Fuc dog,T~m~d you,T~md me,TMD dog,Tm dog";

        keyWord1.test(guanjianzi, string);
        keyWord2.test(guanjianzi, string);

        string = "f u c k";

        keyWord1.test(guanjianzi, string);
        keyWord2.test(guanjianzi, string);
    }

    public void test(String keyWord, String checkTxt) {
        System.out.println();
        System.out.println("-------------------------------------------------------------------------------------------------");
        String check = checkLowerCase ? "忽律大小写精准匹配" : "原始大小写模糊匹配";
        if (checkLowerCase) {
            keyWord = keyWord.toLowerCase();
        }
        System.out.println(check + ", 敏感词：" + keyWord + "；数量：" + this.keyWordMap.size());
        System.out.println(check + ", 待检测语句：" + checkTxt + "；数量：" + checkTxt.length());

        long beginTime = System.currentTimeMillis();
        System.out.println(check + ", 最小匹配：" + hasSensitiveWord(checkTxt, KeyWordMatchType.MIN));
        System.out.println(check + ", 最小匹配：" + hasSensitiveWord(checkTxt, KeyWordMatchType.MAX));
        System.out.println(check + ", 最小匹配：" + replaceKeyWord(checkTxt, '*', KeyWordMatchType.MIN));
        System.out.println(check + ", 完全匹配：" + replaceKeyWord(checkTxt, '*'));
        TreeSet<Integer> set = getKeyWords(checkTxt, KeyWordMatchType.MAX);
        long endTime = System.currentTimeMillis();
        System.out.println(check + ", 语句中包含敏感词的位置：" + set);
        System.out.println(check + ", 总共消耗时间为：" + (endTime - beginTime));
        System.out.println("-------------------------------------------------------------------------------------------------");
    }

    /**
     * 转化小写检测
     */
    private boolean checkLowerCase;
    /**
     * 中英文分组检查，会去掉空格
     */
    private boolean checkENCNCase;

    private Map keyWordMap = new ConcurrentSkipListMap<>();

    /**
     * @param checkLowerCase 转化小写检测
     * @param checkENCNCase  中英文分组检查，会去掉空格
     */
    public KeyWord(boolean checkLowerCase, boolean checkENCNCase) {
        this.checkLowerCase = checkLowerCase;
        this.checkENCNCase = checkENCNCase;
    }

    /**
     * 中英文分组检查
     */
    public boolean isCheckENCNCase() {
        return checkENCNCase;
    }

    /**
     * 中英文分组检查
     */
    public KeyWord setCheckENCNCase(boolean checkENCNCase) {
        this.checkENCNCase = checkENCNCase;
        return this;
    }

    /**
     * 加载关键字文件
     */
    public KeyWord loadWordFile(String filepath) {
        loadWordFile(filepath, StandardCharsets.UTF_8);
        return this;
    }

    /**
     * 加载关键字文件
     */
    public KeyWord loadWordFile(String filepath, Charset charsetName) {
        // 读取敏感词库
        Set<String> keyWordSet = new LinkedHashSet<>();
        File file = new File(filepath);    // 读取文件
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            try (InputStreamReader read = new InputStreamReader(fileInputStream, charsetName)) {
                if (file.isFile() && file.exists()) {      // 文件流是否存在
                    BufferedReader bufferedReader = new BufferedReader(read);
                    String txt = null;
                    while ((txt = bufferedReader.readLine()) != null) {    // 读取文件，将文件内容放入到set中
                        keyWordSet.add(txt);
                    }
                } else {         // 不存在抛出异常信息
                    throw new Exception("敏感词库文件不存在");
                }
            }
        } catch (Exception e) {
            throw Throw.as(e);
        }
        // 将敏感词库加入到HashMap中
        for (String keyWord : keyWordSet) {
            addKeyWord2HashMap(keyWord);
        }
        return this;
    }

    /**
     * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<p>
     * 中 = {
     * isEnd = 0 国 = {<p>
     * isEnd = 1 人 = {isEnd = 0 民 = {isEnd = 1} } 男 = { isEnd = 0 人 = { isEnd = 1 } } } } 五 = { isEnd = 0 星 = { isEnd = 0 红 = { isEnd = 0 旗 = { isEnd = 1 } } } }
     *
     * @param keyWord 敏感词库 ,|，|;|；
     */
    public KeyWord addKeyWord2HashMap(String keyWord) {
        if (checkLowerCase) {
            keyWord = keyWord.toLowerCase();
        }
        String[] split = keyWord.split(",|，|;|；");
        Map newWorMap = null;
        Map nowMap = null;
        String keyChar = null;       // 转换成char型
        Object wordMap = null;       // 获取
        for (String string : split) {
            nowMap = this.keyWordMap;
            // 迭代keyWordSet
            for (int i = 0; i < string.length(); i++) {
                keyChar = String.valueOf(string.charAt(i));
                wordMap = nowMap.get(keyChar);
                if (wordMap != null) {        // 如果存在该key，直接赋值
                    nowMap = (Map) wordMap;
                } else {     // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
                    newWorMap = new ConcurrentSkipListMap<>();
                    newWorMap.put("isEnd", "0");     // 不是最后一个
                    nowMap.put(keyChar, newWorMap);
                    nowMap = newWorMap;
                }
                if (i == string.length() - 1) {
                    nowMap.put("isEnd", "1");    // 最后一个
                }
            }
        }
        return this;
    }

    public KeyWord remove(String keyWord) {
        if (checkLowerCase) {
            keyWord = keyWord.toLowerCase();
        }
        String[] split = keyWord.split(",|，|;|；");
        for (String s : split) {
            remove(this.keyWordMap, s, 0);
        }
        return this;
    }

    private Map remove(Map nowMap, String keyWord, int index) {
        String keyChar = String.valueOf(keyWord.charAt(index));       // 转换成char型
        final Object o = nowMap.get(keyChar);
        if (o != null) {
            final Map next = (Map) o;
            if (index == keyWord.length() - 1) {
                return next;
            } else {
                final Map remove = remove(next, keyWord, index + 1);
                if (remove != null && (remove.size() == 0 || (remove.size() == 1 && remove.containsKey("isEnd")))) {
                    nowMap.remove(keyChar);
                    return nowMap;
                }
            }
        }
        return null;
    }

    /**
     * 查找敏感词
     */
    public TreeSet<Integer> getKeyWords(String txt) {
        return getKeyWords(txt, KeyWordMatchType.MAX);
    }

    /**
     * 获取文字中的敏感词
     *
     * @param txt       文字
     * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
     */
    public TreeSet<Integer> getKeyWords(String txt, KeyWordMatchType matchType) {
        TreeSet<Integer> keyWordSet = new TreeSet<>();
        if (checkLowerCase) {
            txt = txt.toLowerCase();
        }
        getKeyWords0(keyWordSet, txt, txt, matchType);
        if (checkENCNCase) {
            getKeyWords0(keyWordSet, txt, StringUtil.replaceFilter(txt, StringUtil.PATTERN_REPLACE_UUU), matchType);
            getKeyWords0(keyWordSet, txt, StringUtil.replaceFilter(txt, StringUtil.PATTERN_REPLACE_UUU_1), matchType);
            getKeyWords0(keyWordSet, txt, StringUtil.replaceFilter(txt, StringUtil.PATTERN_REPLACE_UUU_2), matchType);
            getKeyWords0(keyWordSet, txt, StringUtil.replaceFilter(txt, StringUtil.PATTERN_REPLACE_UUU_3), matchType);
        }
        return keyWordSet;
    }

    /**
     * @param keyWordSet 里面存的是关键字所在的位置，index
     * @param oldTxt
     * @param checkTxt
     * @param matchType
     */
    private void getKeyWords0(Set<Integer> keyWordSet, String oldTxt, String checkTxt, KeyWordMatchType matchType) {

        int formIndex = -1;
        for (int i = 0; i < checkTxt.length(); i++) {
            // 判断是否包含敏感字符
            String indexString = String.valueOf(checkTxt.charAt(i));
            formIndex = oldTxt.indexOf(indexString, formIndex + 1);
            int length = checkSensitiveWord(checkTxt, matchType, i);
            if (length > 0) {
                // 存在,加入list中
                String substring = checkTxt.substring(i, i + length);
                for (int k = 0; k < substring.length(); k++) {
                    String charAt = String.valueOf(substring.charAt(k));
                    int indexOf = oldTxt.indexOf(charAt, formIndex);
                    formIndex = indexOf + 1;
                    keyWordSet.add(indexOf);
                }
                // 减1的原因，是因为for会自增
                i = i + length - 1;
            }
        }
    }

    public String replaceKeyWord(String content, char replaceChar) {
        return this.replaceKeyWord(content, replaceChar, KeyWordMatchType.MAX);
    }

    /**
     * 替换敏感字字符
     *
     * @param content     原始字符串
     * @param replaceChar 替换字符，默认
     * @param matchType
     * @return
     */
    public String replaceKeyWord(String content, char replaceChar, KeyWordMatchType matchType) {
        if (!this.keyWordMap.isEmpty()) {
            Set<Integer> keyWords = getKeyWords(content, matchType);
            if (keyWords != null && !keyWords.isEmpty()) {
                char[] chars = content.toCharArray();
                for (Integer index : keyWords) {
                    if (index >= 0) {
                        chars[index] = replaceChar;
                    }
                }
                return new String(chars);
            }
        }
        return content;
    }

    /**
     * 包含敏感字符
     *
     * @param txt
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public boolean hasSensitiveWord(String txt) {
        return hasSensitiveWord(txt, KeyWordMatchType.MAX);
    }

    /**
     * 判断文字是否包含敏感字符
     *
     * @param content   文字
     * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
     * @return 若包含返回true，否则返回false
     */
    @SuppressWarnings({"rawtypes"})
    public boolean hasSensitiveWord(String content, KeyWordMatchType matchType) {
        if (!this.keyWordMap.isEmpty()) {
            if (checkLowerCase) {
                content = content.toLowerCase();
            }
            if (hasSensitiveWord0(content, matchType)/*整体过滤*/
                    || hasSensitiveWord0(StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU), matchType) /*过滤纯汉字的关键字*/
                    || hasSensitiveWord0(StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU_1), matchType) /*过滤纯英文的关键字*/
                    || hasSensitiveWord0(StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU_2), matchType) /*过滤纯数字的关键字*/
                    || hasSensitiveWord0(StringUtil.replaceFilter(content, StringUtil.PATTERN_REPLACE_UUU_3), matchType)/*过滤掉汉字英文数字*/) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param content
     * @param matchType
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    private boolean hasSensitiveWord0(String content, KeyWordMatchType matchType) {
        if (!this.keyWordMap.isEmpty()) {
            for (int i = 0; i < content.length(); i++) {
                int matchFlag = this.checkSensitiveWord(content, matchType, i); // 判断是否包含敏感字符
                if (matchFlag > 0) {    // 大于0存在，返回true
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查文字中是否包含敏感字符，检查规则如下：<p>
     *
     * @param txt
     * @param beginIndex 起始字符，从0开始
     * @param matchType
     * @return，如果存在，则返回敏感词字符的长度，不存在返回0
     */
    @SuppressWarnings({"rawtypes"})
    private int checkSensitiveWord(String txt, KeyWordMatchType matchType, int beginIndex) {
        boolean flag = false;    // 敏感词结束标识位：用于敏感词只有1位的情况
        int matchFlag = 0;     // 匹配标识数默认为0
        String word = null;
        Map nowMap = this.keyWordMap;
        for (int i = beginIndex; i < txt.length(); i++) {
            word = String.valueOf(txt.charAt(i));
            nowMap = (Map) nowMap.get(word);     // 获取指定key
            if (nowMap != null) {     // 存在，则判断是否为最后一个
                matchFlag++;     // 找到相应key，匹配标识+1
                if ("1".equals(nowMap.get("isEnd"))) {       // 如果为最后一个匹配规则,结束循环，返回匹配标识数
                    flag = true;       // 结束标志位为true
                    if (KeyWordMatchType.MIN == matchType) {    // 最小规则，直接返回,最大规则还需继续查找
                        break;
                    }
                }
            } else {     // 不存在，直接返回
                break;
            }
        }
        /*长度必须大于等于1，为词*/
        if (/*(WordFilterUtil.MatchType.MIN != matchType && matchFlag < 2) ||*/(!flag && KeyWordMatchType.MAX == matchType)) {
            matchFlag = 0;
        }
        return matchFlag;
    }

}
