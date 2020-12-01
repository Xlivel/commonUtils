package com.data.common.context;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class YzjAgent {
    private static final String DEFAULT_CLIENT_ID = "UNKNOWN"; // 用于兼容旧版本未传 clientId 的情况
    private static final String DEFAULT_CLIENT_VERSION = "UNKNOWN"; // 用于兼容旧版本未传 deviceId 的情况
    private static final String DEFAULT_OS_VERSION = "UNKNOWN"; // 用于兼容旧版本未传 clientId 的情况
    private static final String DEFAULT_DEVICE_ID = "UNKNOWN"; // 用于兼容旧版本未传 deviceId 的情况
    private static final String DEFAULT_DEVICE_NAME = "UNKNOWN"; // 用于兼容旧版本未传 clientId 的情况

    public static final YzjAgent DEFAULT_AGENT = new YzjAgent(null, null, null, null, null);

    public final String appClientId;
    public final String appClientVersion;
    public final String appClientOSVersion;
    public final String appClientDeviceId;
    public final String appClientDeviceName;

    public YzjAgent(String appClientId, String appClientVersion, String appClientOSVersion, String appClientDeviceId, String appClientDeviceName) {
        if (StringUtils.isBlank(appClientId)) {
            appClientId = DEFAULT_CLIENT_ID;
        }
        if (StringUtils.isBlank(appClientVersion)) {
            appClientVersion = DEFAULT_CLIENT_VERSION;
        }
        if (StringUtils.isBlank(appClientOSVersion)) {
            appClientOSVersion = DEFAULT_OS_VERSION;
        }
        if (StringUtils.isBlank(appClientDeviceId)) {
            appClientDeviceId = DEFAULT_DEVICE_ID;
        }
        if (StringUtils.isBlank(appClientDeviceName)) {
            appClientDeviceName = DEFAULT_DEVICE_NAME;
        }

        this.appClientId = appClientId;
        this.appClientVersion = appClientVersion;
        this.appClientOSVersion = appClientOSVersion;
        this.appClientDeviceId = appClientDeviceId;
        this.appClientDeviceName = appClientDeviceName;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
