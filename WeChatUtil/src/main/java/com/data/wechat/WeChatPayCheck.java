package com.data.wechat;

import com.data.common.commons.WeChatCommonUtils;
import com.data.common.commons.XMLUtil;
import com.data.common.utils.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author wj
 * @Date 2021-01-20 下午18:08
 */
public abstract class WeChatPayCheck {

    private String apiKey;
    private String mchId;
    private String appId;
    private String signType;
    private Integer limitTime;

    /**
     * 支付成功后调用
     *
     * @param orderId
     * @return
     */
    public abstract boolean afterSuccess(String orderId);

    /**
     * 支付失败后调用
     *
     * @param orderId
     * @return
     */
    public abstract boolean afterfail(String orderId);

    public Boolean checkWeChatStatus(String orderId, Date orderTime) throws Exception {
        //如果大于5分钟，关闭订单
        if (System.currentTimeMillis() - orderTime.getTime() > limitTime) {
            //先查询微信订单状态
            String s = requestWeChatOrderStatus(orderId);
            if (StringUtils.isNoneBlank(s)) {
                Map map = XMLUtil.doXMLParse2(s);
                String returnCode = (String) map.get("return_code");
                if ("SUCCESS".equals(returnCode)) {
                    String resultCode = (String) map.get("result_code");
                    if ("SUCCESS".equals(resultCode)) {
                        String tradeState = (String) map.get("trade_state");
                        if ("SUCCESS".equals(tradeState)) {
                            afterSuccess(orderId);
                            return true;
                        }
                    }
                }
            }
            afterfail(orderId);
            return false;
        }
        closeWeChatOrder(orderId);
        return null;
    }

    private String requestWeChatOrderStatus(String orderId) throws Exception {
        Map<String, String> params = new HashMap<>();
        /**
         * 公众账号ID
         */
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("out_trade_no", orderId);
        params.put("nonce_str", "1560262851");
        params.put("sign_type", signType);
        String sign = WeChatCommonUtils.genSign(params, apiKey);
        params.put("sign", sign);
        String reqParams = WeChatCommonUtils.mapToXmlParams(params);
        return HttpClientUtil.doPostJson("https://api.mch.weixin.qq.com/pay/orderquery", reqParams);
    }

    private String closeWeChatOrder(String orderId) {
        Map<String, String> params = new HashMap<>();
        /**
         * 公众账号ID
         */
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("out_trade_no", orderId);
        params.put("nonce_str", "1560262851");
        params.put("sign_type", signType);
        String sign = WeChatCommonUtils.genSign(params, apiKey);
        params.put("sign", sign);
        String reqParams = WeChatCommonUtils.mapToXmlParams(params);
        return HttpClientUtil.doPostJson("https://api.mch.weixin.qq.com/pay/closeorder", reqParams);
    }
}