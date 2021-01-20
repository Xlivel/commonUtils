package com.data.wechat;

import com.data.common.commons.WeChatCommonUtils;
import com.data.common.commons.XMLUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Description
 * @Author wj
 * @Date 2021-01-20 下午18:02
 */
public abstract class WeChatCbUtil {

    private String apiKey;

    public abstract boolean businessExe(String orderNo);

    public void weChatCallBack(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 读取参数
        InputStream inputStream = request.getInputStream();
        StringBuilder sb = new StringBuilder();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();

        // 解析xml成map
        Map<String, String> m = XMLUtil.doXMLParse(sb.toString());
        Map<String, String> packageParams = new TreeMap<>();
        assert m != null;
        for (String parameter : m.keySet()) {
            String parameterValue = m.get(parameter);
            String v = "";
            if (null != parameterValue) {
                v = parameterValue.trim();
            }
            packageParams.put(parameter, v);
        }

        // 判断签名是否正确
        if (WeChatCommonUtils.isSameSign(packageParams, apiKey)) {
//            logger.info("微信支付成功回调");
            String resXml = "";
            if ("SUCCESS".equals((String) packageParams.get("result_code"))) {
                //支付成功
                String orderNo = (String) packageParams.get("out_trade_no");
//                logger.info("微信订单号{}付款成功", orderNo);
                //更新充值流水单状态
                boolean result = false;
                try {
                    result = businessExe(orderNo);
                } catch (Exception e) {
//                    logger.error("weChat支付成功，修改金额失败", e);
                }
                // 通知微信.异步确认成功.必写.不然会一直通知后台.八次之后就认为交易失败了.
                if (result) {
                    resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" +
                            "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                }
            } else {
//                logger.info("支付失败,错误信息：{}", packageParams.get("err_code"));
                resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" +
                        "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
            }
            BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            out.write(resXml.getBytes());
            out.flush();
            out.close();
        } else {
//            logger.info("通知签名验证失败");
        }
    }

}
