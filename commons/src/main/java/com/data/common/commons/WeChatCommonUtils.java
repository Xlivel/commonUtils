package com.data.common.commons;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wj
 */
public class WeChatCommonUtils {

    private static final String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    public static String mapToXmlParams(Map<String, String> params) {
        StringBuilder stringBuffer = new StringBuilder("<xml>");
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                if ("detail".equalsIgnoreCase(key)) {
                    stringBuffer.append("<").append(key).append(">")
                            .append("<![CDATA[").append(params.get(key))
                            .append("]]></").append(key).append(">");
                } else {
                    stringBuffer.append("<").append(key).append(">").append(params.get(key)).append("</").append(key).append(">");
                }
            }
        }
        stringBuffer.append("</xml>");
        return stringBuffer.toString();
    }

    public static String genSign(Map<String, String> params, String api_key) {
        StringBuilder stringBuffer = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());
        List<String> sorted = keys.stream().sorted(Comparator.comparing(key -> key)).collect(Collectors.toList());
        for (String key : sorted) {
            if (!"sign".equalsIgnoreCase(key) && params.get(key) != null && !"".equals(params.get(key))) {
                stringBuffer.append(key).append("=").append(params.get(key)).append("&");
            }
        }
        stringBuffer.append("key=").append(api_key);
        return MD5Encode(stringBuffer.toString(), "UTF-8").toLowerCase();
    }

    public static boolean isSameSign(Map<String, String> params, String apiKey) {
        String genSign = genSign(params, apiKey);
        String sign = params.get("sign");
        return sign.equalsIgnoreCase(genSign);
    }

    public static String MD5Encode(String origin, String charsetName) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetName == null || "".equals(charsetName)) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetName)));
            }
        } catch (Exception exception) {
        }
        return resultString;
    }

    private static String byteArrayToHexString(byte b[]) {
        StringBuilder resultSb = new StringBuilder();
        for (byte value : b) {
            resultSb.append(byteToHexString(value));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 是否签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     *
     * @param characterEncoding
     * @param packageParams
     * @param API_KEY
     * @return boolean
     */
    public static boolean isTenpaySign(String characterEncoding, SortedMap<Object, Object> packageParams, String API_KEY) {
        StringBuilder sb = new StringBuilder();
        Set es = packageParams.entrySet();
        for (Object e : es) {
            Map.Entry entry = (Map.Entry) e;
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (!"sign".equals(k) && null != v && !"".equals(v)) {
                sb.append(k).append("=").append(v).append("&");
            }
        }
        sb.append("key=").append(API_KEY);
        //算出摘要
        String mysign = MD5Encode(sb.toString(), characterEncoding).toLowerCase();
        String tenpaySign = ((String) packageParams.get("sign")).toLowerCase();
        return tenpaySign.equals(mysign);
    }

    /**
     * sign签名
     *
     * @param characterEncoding
     * @param packageParams
     * @param apiKey
     * @return String
     */
    @SuppressWarnings({"rawtypes"})
    public static String createSign(String characterEncoding, SortedMap<Object, Object> packageParams, String apiKey) {
        StringBuilder sb = new StringBuilder();
        Set es = packageParams.entrySet();
        for (Object e : es) {
            Map.Entry entry = (Map.Entry) e;
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k).append("=").append(v).append("&");
            }
        }
        sb.append("key=").append(apiKey);
        return MD5Encode(sb.toString(), characterEncoding).toUpperCase();
    }

    /**
     * 将请求参数转换为xml格式的string
     *
     * @param parameters
     * @return String
     */
    @SuppressWarnings({"rawtypes"})
    public static String getRequestXml(SortedMap<Object, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        for (Object e : es) {
            Map.Entry entry = (Map.Entry) e;
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k) || "sign".equalsIgnoreCase(k)) {
                sb.append("<").append(k).append(">").append("<![CDATA[").append(v).append("]]></").append(k).append(">");
            } else {
                sb.append("<").append(k).append(">").append(v).append("</").append(k).append(">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 取出一个指定长度大小的随机正整数.
     *
     * @param length
     * @return int
     */
    public static int buildRandom(int length) {
        int num = 1;
        double random = Math.random();
        if (random < 0.1) {
            random = random + 0.1;
        }
        for (int i = 0; i < length; i++) {
            num = num * 10;
        }
        return (int) ((random * num));
    }

    /**
     * 获取当前时间 yyyyMMddHHmmss
     *
     * @return String
     */
    public static String getCurrTime() {
        Date now = new Date();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String s = outFormat.format(now);
        return s;
    }

}
