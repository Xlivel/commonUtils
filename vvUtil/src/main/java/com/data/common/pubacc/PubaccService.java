package com.data.common.pubacc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.data.common.utils.XTHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 推送公共号消息
 * @author wj
 */
public class PubaccService {

    private static Logger logger = LoggerFactory.getLogger(PubaccService.class);

    @Value("${vv.host:http://vvtest.vanke.com}")
    private static String host;

    @Value("${vv.pushUrl:/pubacc/pubsend}")
    private static String pushUrl;

    @Value("${vv.pubaccId:1}")
    private static String pubacc;

    @Value("${vv.pubaccKey:1}")
    private static String pubaccKey;


    public static boolean push(PushMsgEntity pushMessage) {
        String random = String.valueOf(Math.random());
        String nowDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        long time = System.currentTimeMillis();
        Map<String, Object> from = new HashMap<>();
        String pub = StringUtils.isNotBlank(pushMessage.getPubacc()) ? pushMessage.getPubacc() : pubacc;
        String pkey = StringUtils.isNotBlank(pushMessage.getPubaccKey()) ? pushMessage.getPubacc() : pubaccKey;
        from.put("no", pushMessage.getFromEid());
        from.put("pub", pub);
        from.put("time", time);
        from.put("nonce", random);
        from.put("pubtoken", shacode.sha(pushMessage.getFromEid(), pub, pkey, random, String.valueOf(time)));

        List<Map<String, Object>> tos = new ArrayList<Map<String, Object>>();
        Map<String, Object> to = new HashMap<>();
        to.put("no", pushMessage.getToEid());
        to.put("user", pushMessage.getToUsersid());
        to.put("code", pushMessage.getCode());
        tos.add(to);

        JSONArray details = new JSONArray();
        JSONObject detail = new JSONObject();

        Map<String, Object> msg = new HashMap<>();

        if (pushMessage.getType() == 2) {
            msg.put("text", pushMessage.getContent());
        }

        if (pushMessage.getType() == 5) {
            msg.put("text", pushMessage.getContent());
            msg.put("url", pushMessage.getUrl());
            msg.put("appid", pushMessage.getAppid());
            msg.put("todo", "0");
        }

        if (pushMessage.getType() == 6) {
            if (pushMessage.getModel() == 1) {
                detail.put("date", nowDate);
                detail.put("title", pushMessage.getMsgTitle());
                detail.put("zip", pushMessage.getMsgzip());
                detail.put("text", pushMessage.getMsgContent());
                detail.put("url", pushMessage.getUrl());
                detail.put("appid", pushMessage.getAppid());
                details.add(detail);
                msg.put("todo", "0");
                msg.put("model", pushMessage.getModel());
                msg.put("list", details);
            }
            if (pushMessage.getModel() == 2) {
                detail.put("date", nowDate);
                detail.put("title", pushMessage.getMsgTitle());
                detail.put("text", pushMessage.getMsgContent());
                detail.put("url", pushMessage.getUrl());
                detail.put("appid", pushMessage.getAppid());
                detail.put("name", pushMessage.getMsgpicname());
                detail.put("pic", pushMessage.getMsgpic());
                details.add(detail);
                msg.put("model", pushMessage.getModel());
                msg.put("list", details);
                msg.put("appid", pushMessage.getAppid());
                msg.put("todo", "0");
            }

            if (pushMessage.getModel() == 3) {
                detail.put("date", nowDate);
                detail.put("title", pushMessage.getMsgTitle());
                detail.put("text", pushMessage.getMsgContent());
                detail.put("zip", pushMessage.getMsgzip());
                detail.put("url", pushMessage.getUrl());
                detail.put("appid", pushMessage.getAppid());
                detail.put("name", pushMessage.getMsgpicname());
                detail.put("pic", pushMessage.getMsgpic());
                details.add(detail);
                msg.put("model", pushMessage.getModel());
                msg.put("list", details);
                msg.put("appid", pushMessage.getAppid());
                msg.put("todo", "0");
            }

        }

        JSONObject content = new JSONObject();
        content.put("from", from);
        content.put("to", tos);
        content.put("type", pushMessage.getType());
        content.put("msg", msg);
        XTHttpClient httpClient = XTHttpClient.getHttpClient();
        try {
            String doPostJson = httpClient.httpPost(host + pushUrl, content, 10000);
            if (StringUtils.isNoneBlank(doPostJson)) {
                logger.info("v9PubaccServiceImpl doPostJson: " + doPostJson);
                try {
                    JSONObject parseObject = JSON.parseObject(doPostJson);
                    logger.info("v9PubaccServiceImpl 推送结果: " + parseObject.toJSONString());
                    String sourceMsgId = parseObject.getString("sourceMsgId");
                    String pubId = parseObject.getString("pubId");
                    if (StringUtils.isNotBlank(sourceMsgId) || StringUtils.isNotBlank(pubId)) {
                        return true;
                    }
                } catch (Exception e) {
                    logger.error("v9PubaccServiceImpl error: ", e);
                }
            }
        } catch (Exception e) {
            logger.error("getPerInfo error : ", e);
        }
        return false;
    }

}
