package com.data.common.context;


public class OpenApiOperator extends Operator {
    private String userId;
    private String personId;
    private String networkId;
    private String openId;
    private String openToken;
    private String eid;
    private String oid;
    private String uid;
    private String personIp;

    public OpenApiOperator() {
    }

    public OpenApiOperator(String userId, String personId, String networkId, String openId, String openToken, String eid, String oid ,String uid ,String personIp) {
        this.userId = userId;
        this.personId = personId;
        this.networkId = networkId;
        this.openId = openId;
        this.openToken = openToken;
        this.eid = eid;
        this.oid = oid;
        this.uid = uid;
        this.personIp = personIp;       
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getOpenToken() {
        return openToken;
    }

    public void setOpenToken(String openToken) {
        this.openToken = openToken;
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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPersonIp() {
		return personIp;
	}

	public void setPersonIp(String personIp) {
		this.personIp = personIp;
	}
}
