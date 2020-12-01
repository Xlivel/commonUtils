package com.data.common.context;

import lombok.Data;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.UUID;


/**
 * Created by dhf on 2017/3/17.
 */
@Data
public class RequestContext {
    private String requestId = UUID.randomUUID().toString();
    private String uri;
    private Operator operator;
    private long requestTimestamp = System.currentTimeMillis();
    private String userAgent;
    private transient YzjAgent yzjAgent;
    private String userId;

    public YzjAgent getYzjAgent() {
        if (yzjAgent == null) {
            yzjAgent = YzjAgentParser.parse(userAgent);
        }
        return yzjAgent;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
    
    
    
}
