package com.data.common.message;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageService {

    /*public static boolean sendMsg(){
        JSONObject sendCardParams = new JSONObject();
        sendCardParams.put("content", "送你一张周年纪念贺卡");
        sendCardParams.put("msgLen", "送你一张周年纪念贺卡".length());
        sendCardParams.put("msgType", 7);
        sendCardParams.put("toUserId", toUserPersonId);

        JSONObject cardParams = new JSONObject();
        cardParams.put("unreadMonitor", 1);
        cardParams.put("appName", "来自周年祝福");
        cardParams.put("content", "今天是你的入司纪念日，感谢你的辛苦付出，让我们一起砥砺前行");
        cardParams.put("title", "送你一张周年纪念贺卡");
        cardParams.put("lightAppId", appId);
        cardParams.put("thumbUrl", host + "/tribe/bw/images/joincom.png");
        cardParams.put("pubAccId", pubacc);
        String url = host + "/tribe/bw/rs/detail/" + detailId;
        cardParams.put("webpageUrl", url);
        sendCardParams.put("param", cardParams);
    }

    private boolean sendMsgToYZJ(String fromUserOId, JSONObject params) {
        JSONObject openTokenParams = new JSONObject();
        openTokenParams.put("oId", fromUserOId);
        Map<String, Object> openTokenResult = getOpenTokenByOId(openTokenParams);
        String opentoken = JSONObject.parseObject(openTokenResult.get("openTokenResult").toString()).getString("data");
        String sendMsgResult = "";
        boolean success = true;
        String url = host + "/xuntong/ecLite/convers/send.action";

        logger.info("调用云之家分享消息 params : " + params);
        logger.info("调用云之家分享消息 url : " + url);

        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
        headers.add(new BasicHeader("opentoken", opentoken));
        try {
            sendMsgResult = HttpUtil.sendPostByForm(url, params.toString(), headers, 3000);
            logger.info("sendMsgResult : " + sendMsgResult);
        } catch (Exception e) {
            logger.error("JoinComServiceImpl sendMsgToYZJ error : " + e.getMessage(), e);
            success = false;
        }
        if (StringUtils.isNotBlank(sendMsgResult)) {
            JSONObject jsonObject = JSONObject.parseObject(sendMsgResult);
            return jsonObject.getBoolean("success");
        }
        return false;
    }*/

}
