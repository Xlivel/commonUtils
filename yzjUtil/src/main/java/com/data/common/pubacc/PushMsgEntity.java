package com.data.common.pubacc;

import java.util.Collection;

public class PushMsgEntity {

    /**
     * 发送方的eid
     **/
    private String fromEid;
    /**
     * 接收方的eid
     **/
    private String toEid;
    /**
     * 接收方的userid
     **/
    private Collection<String> toUsersid;
    /**
     * 消息头
     **/
    private String msgTitle;
    /**
     * 消息内容
     **/
    private String msgContent;
    /**
     * 应用appid
     **/
    private String appid;
    /**
     * 公共号
     **/
    private String pubacc;
    /**
     * 公共号key
     **/
    private String pubaccKey;
    /**
     * 消息模型(1：单条文本编排模板,2：单条图文混排模板,3：多条图文混排模板,4：应用消息模板)
     **/
    private int model;
    /**
     * code=all: “to”:[{“no”:”10001″,”code”:”all”}] 表示企业10001所有订阅的用户。”code”:”all”一定不要漏,否则报错。
     * code不传: “to”:[{“no”:”10002″,”user”:[“3″,”4”]}] 表示企业10001的openId为3, 4的用户
     * code=2: “to”:[{“no”:”10001″,”user”:[“admin.admin.com”,”13750067719″],”code”:”2″}] 表示企业10001的账号为admin.admin.com, 13750067719的用户
     */
    private String code;
    /**
     * 内容url
     **/
    private String url;
    /**
     * (取值 2：单文本,5：文本链接,6：图文链接)
     **/
    private int type;
    /**
     * 文本链接地址，格式为经过URLENCODE编码的字符串
     **/
    private String pushUrl;
    /**
     * 文本消息内容，格式为字符串
     **/
    private String content;
    private String msgzip;
    private String msgpic;
    private String msgpicname;

    public String getFromEid() {
        return fromEid;
    }

    public void setFromEid(String fromEid) {
        this.fromEid = fromEid;
    }

    public String getPubacc() {
        return pubacc;
    }

    public void setPubacc(String pubacc) {
        this.pubacc = pubacc;
    }

    public String getPubaccKey() {
        return pubaccKey;
    }

    public void setPubaccKey(String pubaccKey) {
        this.pubaccKey = pubaccKey;
    }

    public String getMsgpicname() {
        return msgpicname;
    }

    public void setMsgpicname(String msgpicname) {
        this.msgpicname = msgpicname;
    }

    public String getMsgpic() {
        return msgpic;
    }

    public void setMsgpic(String msgpic) {
        this.msgpic = msgpic;
    }

    public String getMsgzip() {
        return msgzip;
    }

    public void setMsgzip(String msgzip) {
        this.msgzip = msgzip;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToEid() {
        return toEid;
    }

    public void setToEid(String toEid) {
        this.toEid = toEid;
    }

    public Collection<String> getToUsersid() {
        return toUsersid;
    }

    public void setToUsersid(Collection<String> toUsersid) {
        this.toUsersid = toUsersid;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPushUrl() {
        return pushUrl;
    }

    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }


    @Override
    public String toString() {
        return "PushMsgEntity [fromEid=" + fromEid + ", toEid=" + toEid + ", toUsersid=" + toUsersid + ", msgTitle="
                + msgTitle + ", msgContent=" + msgContent + ", appid=" + appid + ", pubacc=" + pubacc + ", pubaccKey="
                + pubaccKey + ", model=" + model + ", code=" + code + ", url=" + url + ", type=" + type + ", pushUrl="
                + pushUrl + ", content=" + content + ", msgzip=" + msgzip + ", msgpic=" + msgpic + ", msgpicname="
                + msgpicname + "]";
    }

}
