package com.data.common.ticket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.data.common.utils.XTHttpClient;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取accessToken和上下文
 */
public class TicketUtil {

    private static final Logger logger = LoggerFactory.getLogger(TicketUtil.class);

    public static String getAccessToken(String eid, String scope, String secret, String host) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        Map parm = new HashMap();
        parm.put("eid", eid);
        parm.put("scope", scope);
        parm.put("timestamp", timestamp);
        parm.put("secret", secret);
        String url = host.concat("/gateway/oauth2/token/getAccessToken");
        JSONObject result = null;
        try {
            String s = gatewayRequestJson(url, JSONObject.toJSONString(parm));
            result = JSONObject.parseObject(s).getJSONObject("data");
        } catch (Exception e) {
            logger.error("获取access_token信息失败!, 返回null", e);
        }
        logger.info("获取access_token返回数据: " + result);
        TokenBean tokenBean = JSON.toJavaObject(result, TokenBean.class);
        if (tokenBean != null && tokenBean.getAccessToken() != null) {
            tokenBean.setUpdateTime(new Date());
            tokenBean.setScope("resGroupSecret");
            tokenBean.setEid(eid);
            logger.info("返回新获取的access_token: {}" + tokenBean.getAccessToken());
            return tokenBean.getAccessToken();
        }
        logger.error("获取access_token信息失败!, 返回null");
        return "";
    }

    private static String gatewayRequestJson(String url, String params) throws Exception {
        /*Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        return HttpHelper.post(headers, parm, url, 5000);*/
        return XTHttpClient.getHttpClient().httpPost(url, params, "application/json", null, 5000);
    }

    public static UserContext getUserContext(String ticket,String host,String appId,String appSecret,String eid) throws Exception {
        String url = host.concat("gateway/ticket/user/acquirecontext?accessToken=").concat(getAccessToken(appId, appSecret, eid, "app"));
        Map<String, String> params = new HashMap<>(2);
        params.put("appid", appId);
        params.put("ticket", ticket);
        String s = gatewayRequestJson(url, JSONObject.toJSONString(params));
        JSONObject jsonObject = JSONObject.parseObject(s);
        if (jsonObject.getBoolean("success")) {
            JSONObject data = jsonObject.getJSONObject("data");
            return JSONObject.toJavaObject(data, UserContext.class);
        } else {
            return null;
        }
    }
}
