package com.data.common.pubacc;

import com.alibaba.fastjson.JSONObject;
import com.data.common.utils.XTHttpClient;

/**
 * 公共号人员订阅管理
 */
public class PubaccManageService {

    public static String pubSsbUrl = "/pubacc/api/pubssb";

    /**
     * @param pubId     公共号Id
     * @param pubSecret 公共号密钥
     * @param eid       圈id
     * @param userId    人员ID
     * @param ssb       1是订阅；0是取消；没有参数，表示查询是否订阅
     * @return
     * @throws Exception
     */
    public static String pubSsb(String pubId, String pubSecret, String eid, String userId, String ssb) throws Exception {
        JSONObject params = new JSONObject();
        long time = System.currentTimeMillis();
        params.put("pubid", pubId);
        params.put("mid", eid);
        params.put("userid", userId);
        params.put("ssb", ssb);
        params.put("time", time);
        params.put("pubtoken", shacode.sha(eid, pubId, pubSecret, String.valueOf(time)));
        return XTHttpClient.getHttpClient().httpPost(pubSsbUrl, params.toJSONString(), "application/x-www-form-urlencoded", null, 5000);
    }

}
