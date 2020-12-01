package com.data.common.ticket;

public class UserContext {

    /*{
        "xtid": "5c8f09ddd5deef7a46c17e15",
            "oid": "5c8f09ddd5deef7a46c185e3",
            "eid": "16216997",
            "ticket": null,
            "appid": "1011297",
            "uid": "55659",
            "jobNo": "zhouzy13",
            "openid": "5c8f09ddd5deef7a46c185e3",
            "networkid": "5c80ba2fd5de4204446f7577",
            "deviceId": "a4911bdf-8c44-3444-94d8-4997cf5498b6",
            "username": "周展源",
            "tid": "16216997",
            "userid": "5c8f09dad5deef7a46c16606"
    }*/

    private String appid;
    private String eid;
    private String oid;
    private String xtid;
    private String jobNo;
    private String networkid;
    private String deviceId;
    private String openid;
    private String username;
    private String uid;
    private String tid;
    private String userid;
    private Integer yearCount;
    private Integer monthCount;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getXtid() {
        return xtid;
    }

    public void setXtid(String xtid) {
        this.xtid = xtid;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public String getNetworkid() {
        return networkid;
    }

    public void setNetworkid(String networkid) {
        this.networkid = networkid;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Integer getYearCount() {
        return yearCount;
    }

    public void setYearCount(Integer yearCount) {
        this.yearCount = yearCount;
    }

    public Integer getMonthCount() {
        return monthCount;
    }

    public void setMonthCount(Integer monthCount) {
        this.monthCount = monthCount;
    }
}