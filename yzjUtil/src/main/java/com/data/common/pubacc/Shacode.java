package com.data.common.pubacc;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author wj
 */
public class Shacode {
    @SuppressWarnings("deprecation")
    public static String sha(String... data) {
        /** 在其他语言中按照ASCII顺序排序 **/
        Arrays.sort(data);
        /** 把数组连接成字符串（无分隔符），并sha1哈希 **/
        return DigestUtils.shaHex(StringUtils.join(data));
    }

}
