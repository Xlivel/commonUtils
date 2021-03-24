package com.data.wechat;

import com.data.common.commons.QrCodeUtil;
import com.data.common.commons.WeChatCommonUtils;
import com.data.common.commons.XMLUtil;
import com.data.common.utils.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description
 * @Author wj
 * @Date 2021-01-20 下午16:48
 */
public class WeChatPayUtil {

    private String apiKey;
    private String appId;
    private String mchId;

    public WeChatPayUtil(String apiKey, String appId, String mchId) {
        this.apiKey = apiKey;
        this.appId = appId;
        this.mchId = mchId;
    }

    public File weChatUnifiedOrder(Map<String, String> params, String orderId) throws Exception {
        /*Map<String, String> params = new HashMap<>();
        //公众账号ID
        params.put("appid", "wxe1f66b15bbd93f57");
        //商户号
        params.put("mch_id", "1560262851");
        //随机字符串
        params.put("nonce_str", RandomStringUtils.randomAlphanumeric(32));
        //商品描述
        params.put("body", "物联云商城-微信支付");
        //商户订单号
        params.put("out_trade_no", order.getId());
        Double v = order.getAmount().doubleValue() * 100;
        //标价金额
        params.put("total_fee", v.intValue() + "");
//        params.put("spbill_create_ip", order.getBillCreateIp()); //终端IP
        //通知地址
        params.put("notify_url", baseHost + "/iotShop/order/pay");
        //自定义参数
        params.put("attach", "物联云平台微信支付充值");
        //交易类型
        params.put("trade_type", "NATIVE");
        //商品ID
        params.put("product_id", order.getId());
        String sign = WeChatCommonUtils.genSign(params, API_KEY);
        //签名
        params.put("sign", sign);*/
        String reqParams = WeChatCommonUtils.mapToXmlParams(params);
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        String s = HttpClientUtil.doPostJson(url, reqParams);
        if (StringUtils.isNoneBlank(s)) {
            Map map = XMLUtil.doXMLParse2(s);
            String returnCode = (String) map.get("return_code");
            if ("SUCCESS".equals(returnCode)) {
                String resultCode = (String) map.get("result_code");
                if ("SUCCESS".equals(resultCode)) {
                    //logger.info("订单号：{}生成微信支付码成功", orderId);
                    String urlCode = (String) map.get("code_url");
                    //转换为短链接
                    change2ShortUrl(urlCode);
                    return QrCodeUtil.createQrCode(urlCode, "./qrCode/", System.currentTimeMillis() + ".png");
                } else {
                    String errCodeDes = (String) map.get("err_code_des");
                    //logger.info("订单号：{}生成微信支付码(系统)失败:{}", orderId, errCodeDes);
                }
            } else {
                String returnMsg = (String) map.get("return_msg");
                //logger.info("(订单号：{}生成微信支付码(通信)失败:{}", orderId, returnMsg);
            }
        }
        return null;
    }


    private String change2ShortUrl(String urlCode) {
        try {
            SortedMap<Object, Object> packageParams = new TreeMap<>();
            commonParams(packageParams);
            // URL链接
            packageParams.put("long_url", urlCode);
            String sign = WeChatCommonUtils.createSign("UTF-8", packageParams, apiKey);
            // 签名
            packageParams.put("sign", sign);
            String requestXML = WeChatCommonUtils.getRequestXml(packageParams);
            String resXml = HttpClientUtil.doPostJson("https://api.mch.weixin.qq.com/tools/shorturl", requestXML);

            Map map = XMLUtil.doXMLParse(resXml);
            String returnCode = (String) map.get("return_code");
            if ("SUCCESS".equals(returnCode)) {
                String resultCode = (String) map.get("return_code");
                if ("SUCCESS".equals(resultCode)) {
                    return (String) map.get("short_url");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void commonParams(SortedMap<Object, Object> packageParams) {
        // 生成随机字符串
        String currTime = getCurrTime();
        String strTime = currTime.substring(8, currTime.length());
        String strRandom = buildRandom(4) + "";
        String nonce_str = strTime + strRandom;
        // 公众账号ID
        packageParams.put("appid", appId);
        // 商户号
        packageParams.put("mch_id", mchId);
        // 随机字符串
        packageParams.put("nonce_str", nonce_str);
    }

    private String getCurrTime() {
        Date now = new Date();
        SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return outFormat.format(now);
    }

    private int buildRandom(int length) {
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

}
