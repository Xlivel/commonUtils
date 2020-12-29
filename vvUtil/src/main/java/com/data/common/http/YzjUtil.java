package com.data.common.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.data.common.ticket.TokenBean;
import com.data.common.utils.RedisUtils;
import com.data.common.utils.XTHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wj
 */
@Repository
public class YzjUtil {

    private static final Logger logger = LoggerFactory.getLogger(YzjUtil.class);

    @Value("${yzj.accessTokenKey}")
    private static final String ACCESS_TOKEN_KEY = "YZJ_SYNC_ACCESS_TOKEN_KEY";
    @Value("${yzj.host}")
    private String host;
    @Value("${yzj.eid}")
    private String eid;
    @Value("${yzj.secret}")
    private String secret;
    @Value("${yzj.appId}")
    private String appId;
    @Value("${yzj.defaultOid}")
    private String defaultOid;

    private final RedisUtils redisUtils;

    @Autowired
    public YzjUtil(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public String getAccessToken() {
        // 如果没有token信息或者已经过期, 重新从api获取
        String accessToken = redisUtils.get(ACCESS_TOKEN_KEY);
        if (StringUtils.isNotBlank(accessToken)) {
            return accessToken;
        } else {
            final String[] scopes = {"app", "team", "resGroupSecret"};
            String timestamp = String.valueOf(System.currentTimeMillis());
            Map<String, String> param = new HashMap<>(4);

            param.put("timestamp", timestamp);
            param.put("secret", secret);
            if (StringUtils.isNotBlank(appId)) {
                param.put("appId", appId);
                param.put("scope", scopes[0]);
            } else {
                param.put("scope", scopes[2]);
                param.put("eid", eid);
            }
            String url = host.concat("/gateway/oauth2/token/getAccessToken");
            JSONObject result = null;
            XTHttpClient httpClient = XTHttpClient.getHttpClient();
            try {
                logger.info("getAccessToken req params: " + JSONObject.toJSONString(param));
                String s = httpClient.httpPost(url, JSONObject.parseObject(JSON.toJSONString(param)), 30000);
                result = JSONObject.parseObject(s).getJSONObject("data");
            } catch (Exception e) {
                logger.error("获取access_token信息失败!, 返回null", e);
            }
            logger.info("获取access_token返回数据: " + result);
            TokenBean tokenBean = JSON.toJavaObject(result, TokenBean.class);
            if (tokenBean != null && StringUtils.isNotBlank(tokenBean.getAccessToken())) {
                tokenBean.setUpdateTime(new Date());
                tokenBean.setScope("resGroupSecret");
                tokenBean.setEid("101");
                redisUtils.set(ACCESS_TOKEN_KEY, tokenBean.getAccessToken(), tokenBean.getExpireIn() - 5);
                logger.info("返回新获取的access_token: {}, 缓存key:{}  ttl:{}", tokenBean.getAccessToken(), ACCESS_TOKEN_KEY, tokenBean.getExpireIn() - 5);
                return tokenBean.getAccessToken();
            }
        }
        logger.info("获取access_token信息失败!, 返回null");
        return "";
    }

    public String doReq(String baseUrl, String paramJson) {
        String accessToken = getAccessToken();
        if (StringUtils.isNotBlank(accessToken)) {
            XTHttpClient httpClient = XTHttpClient.getHttpClient();
            String url = host + baseUrl + "?accessToken=" + accessToken;
            Map<String, Object> params = new HashMap<>(3);
            String nonce = String.valueOf(Math.random());
            params.put("nonce", nonce);
            params.put("eid", eid);
            params.put("data", paramJson);
            try {
                logger.info("request {} params: {}", baseUrl, JSON.toJSONString(params));
                String s = httpClient.postUrlEncodeForm(url, params);
                logger.info("request {} result: {}", baseUrl, s);
                return s;
            } catch (Exception e) {
                logger.error("doReq error : ", e);
            }
        }
        return null;
    }

    public String doJsonReq(String baseUrl, String paramJson) {
        String accessToken = getOpenToken(defaultOid);
        if (StringUtils.isNotBlank(accessToken)) {
            XTHttpClient httpClient = XTHttpClient.getHttpClient();
            String url = host + baseUrl;
            Map<String, String> header = new HashMap<>(1);
            header.put("openToken", accessToken);
            try {
                logger.info("request updateInfo params: " + paramJson);
                String s = httpClient.httpPost(url, paramJson, "application/json", header, 10000);
                logger.info("request updateInfo result: " + s);
                return s;
            } catch (Exception e) {
                logger.error("doReq error : ", e);
            }
        }
        return null;
    }

    private String getOpenToken(String oId) {
        String url = host + "/openaccess/user/getXTTokenByoId";
        JSONObject params = new JSONObject(1);
        params.put("oId", oId);
        XTHttpClient httpClient = XTHttpClient.getHttpClient();
        try {
            String post = httpClient.httpPost(url, params, 5000);
            JSONObject jsonObject = JSONObject.parseObject(post);
            return jsonObject.getString("data");
        } catch (Exception e) {
            logger.error("getOpenToken error : ", e);
        }
        return null;
    }
}