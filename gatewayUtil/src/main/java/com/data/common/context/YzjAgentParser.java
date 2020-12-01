package com.data.common.context;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * Created by dhf on 2017/6/2.
 */
public class YzjAgentParser {
    private YzjAgentParser() {
    }

    private static Pattern pattern = Pattern.compile(
            "([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");
    private static Pattern numberPattern = Pattern.compile("\\d+");

    private static boolean isNumber(String number) {
        return numberPattern.matcher(number).matches();
    }
    
    
    public static void main(String[] args) {
		System.out.println(YzjAgentParser.parse("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36; Qing/0.0.4; App/cloudhub 38886/1.1.6; 1.1.6 (1700000000); deviceId:1a514a93185254ae5547de84af94ca39; clientId:38886;"));
	}

    public static YzjAgent parse(String userAgentString) {
        if (StringUtils.isEmpty(userAgentString)) {
            return YzjAgent.DEFAULT_AGENT;
        }
        int desktopPrefixIndex = userAgentString.indexOf(" App/cloudhub ");
        if (desktopPrefixIndex > 0) {
            userAgentString = userAgentString.substring(desktopPrefixIndex + " App/cloudhub ".length());
        }
        /*if (!userAgentString.startsWith("102")) {
            return YzjAgent.DEFAULT_AGENT;
        }*/

        String appClientId = null;
        String appClientVersion = null;
        String appClientOSVersion = null;
        String appClientDeviceId = null;
        String appClientDeviceName = null;
        String[] s1 = StringUtils.split(userAgentString, "/", 2);
        if (s1.length != 2 || s1[0].length() <= 3 || !isNumber(s1[0])) {
            return YzjAgent.DEFAULT_AGENT;
        }
        String browserName = s1[0];
        /*if (!isYzjClient(browserName)) {
            return YzjAgent.DEFAULT_AGENT;
        }*/
        String[] s = StringUtils.split(s1[1], ";");
        appClientId = browserName;
        //旧版本的桌面端user-agent的格式和其他端都不一致 云之家版本号要特殊处理
        if (StringUtils.equals("10204", appClientId)) {
            appClientVersion = getAppClientVersion(s[2]);
        } else {
            appClientVersion = getAppClientVersion(s[0]);
        }
        if (s.length > 1) {
            appClientOSVersion = s[1];
        }
        for (String ss : s) {
            int i = ss.indexOf(":");
            if (i <= 0) {
                continue;
            }
            String key = StringUtils.trimToEmpty(ss.substring(0, i).toLowerCase());
            String value = StringUtils.trimToEmpty(ss.substring(i + 1));
            if (key.equals("deviceid")) {
                appClientDeviceId = value;
            } else if (key.equals("devicename")) {
                try {
                    appClientDeviceName = URLDecoder.decode(value, "utf-8");
                } catch (Throwable ignore) {
                    appClientDeviceName = value;
                }
            }
        }

        return new YzjAgent(appClientId, appClientVersion, appClientOSVersion, appClientDeviceId, appClientDeviceName);
    }

    private static String getAppClientVersion(String s) {
        if (s.contains(" ")) {
            String[] s2 = s.split(" ", 2);
            if (s2.length > 0) {
                return s2[0];
            }
        }
        return s;
    }

    private static boolean isYzjClient(String browserName) {
        if (isNumber(browserName) && browserName.length() == 5
                && StringUtils.startsWith(browserName, "102")) {
            return true;
        }
        return false;
    }
}
