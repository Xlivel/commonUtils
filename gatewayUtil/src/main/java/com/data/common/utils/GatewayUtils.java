package com.data.common.utils;

import com.data.common.context.OpenApiOperator;
import com.data.common.context.RequestContext;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class GatewayUtils {

    public static RequestContext getGatewayInfo(HttpServletRequest request) {
        String openToken = request.getHeader("X-Requested-openToken");
        String personId = request.getHeader("X-Requested-personId");
        String userId = request.getHeader("X-Requested-userId");
        String networkId = request.getHeader("X-Requested-networkId");
        String eid = request.getHeader("X-Requested-eid");
        String oid = request.getHeader("X-Requested-oid");
        String openId = request.getHeader("X-Requested-openId");
        String agent = request.getHeader("User-Agent");
        String wbUserId = request.getHeader("X-Requested-wbUserId");
        String personIp = request.getHeader("X-Requested-personIp");

        RequestContext context = new RequestContext();
        context.setUserAgent(agent);
        context.setUserId(userId);
        if (StringUtils.isNotEmpty(userId)
                || StringUtils.isNotEmpty(personId)
                || StringUtils.isNotEmpty(networkId)
                || StringUtils.isNotEmpty(eid)
        ) {
            OpenApiOperator operator = new OpenApiOperator(wbUserId, personId, networkId, openId, openToken, eid, oid, userId, personIp);
            context.setOperator(operator);
        }
        return context;
    }

}
