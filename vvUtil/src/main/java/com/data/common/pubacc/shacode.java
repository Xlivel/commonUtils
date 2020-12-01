package com.data.common.pubacc;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class shacode {
	@SuppressWarnings("deprecation")
	public static String sha(String... data) {
		Arrays.sort(data);// 在其他语言中按照ASCII顺序排序
		return DigestUtils.shaHex(StringUtils.join(data));// 把数组连接成字符串（无分隔符），并sha1哈希
	}

}
