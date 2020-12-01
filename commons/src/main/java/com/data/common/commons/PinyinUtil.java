package com.data.common.commons;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.Collator;
import java.util.*;

public class PinyinUtil {

    public static String getFullSpell(String chinese) {
        try {
            StringBuilder pypy = new StringBuilder();
            char[] arr = chinese.toCharArray();
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            for (char c : arr) {
                if (c > 128) {
                    try {
                        pypy.append(PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat)[0]);
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else {
                    pypy.append(c);
                }
            }
            return pypy.toString();
        } catch (Exception e) {

        }
        return "";
    }

    public static Map<String, List<String>> sortByFirstPinyin(List<String> list) {

        Collator instance = Collator.getInstance(Locale.CHINA);
        list.sort(instance);
        Map<String, List<String>> result = new TreeMap<>();
        for (int i = 1; i <= 26; i++) {
            String s = String.valueOf((char) (96 + i)).toUpperCase();
            List<String> objects = new ArrayList<>();
            result.put(s, objects);
        }
        for (String string : list) {
            List<String> list1 = result.get(getFullSpell(string).substring(0, 1));
            if (list1 != null) {
                list1.add(string);
            }
        }
        return result;
    }

}
