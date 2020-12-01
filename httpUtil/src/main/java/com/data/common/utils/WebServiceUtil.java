package com.data.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;

import java.io.InputStream;
import java.util.List;

public class WebServiceUtil {

    private String username;
    private String password;

    public WebServiceUtil(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public WebServiceUtil() {
    }

    public JSONObject request(String paramXml) throws Exception {
        // HttpClient发送SOAP请求
        int timeout = 10000;
        System.out.println("HttpClient 发送SOAP请求");
        HttpClient client = new HttpClient();
        if (StringUtils.isNoneBlank(username)) {
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(username, password);
            client.getState().setCredentials(AuthScope.ANY, usernamePasswordCredentials);
        }
        PostMethod postMethod = new PostMethod("http://pidev.vanke.com:51000/XISOAPAdapter/MessageServlet?senderParty=&senderService=BC_PUBLIC&receiverParty=&receiverService=&interface=SI_PI1203_O_PUBLIC_ECC_GPNODE_DAT&interfaceNamespace=http://vk.com/pl");
        // 设置连接超时
        client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
        // 设置读取时间超时
        client.getHttpConnectionManager().getParams().setSoTimeout(timeout);
        // 然后把Soap请求数据添加到PostMethod中
        RequestEntity requestEntity = new StringRequestEntity(paramXml, "text/xml", "UTF-8");

        // 设置请求头部，否则可能会报 “no SOAPAction header” 的错误
//            postMethod.setRequestHeader("SOAPAction", "SI_PI1203_O_PUBLIC_ECC_GPNODE_DAT");
        // 设置请求体
        postMethod.setRequestEntity(requestEntity);
        int status = client.executeMethod(postMethod);
        if (status == 200) {// 成功
            InputStream is = postMethod.getResponseBodyAsStream();
            // 获取请求结果字符串
            String result = IOUtils.toString(is);
            System.out.println("返回结果:" + result);
            JSONObject jsonObject = xml2Json(result);
            return jsonObject.getJSONObject("Body");
        } else {
            System.out.println("错误代码：" + status + ":" + postMethod.getResponseBodyAsString());
        }
        return null;
    }

    public JSONObject xml2Json(String xmlStr) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlStr);
        JSONObject json = new JSONObject();
        dom4j2Json(doc.getRootElement(), json);
        return json;
    }

    public void dom4j2Json(Element element, JSONObject json) {
        //如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (isEmpty(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        if (chdEl.isEmpty() && isEmpty(element.getText())) {//如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for (Element e : chdEl) {//有子元素
            if (!e.elements().isEmpty()) {//子元素也有子元素
                JSONObject chdjson = new JSONObject();
                dom4j2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {//如果此元素已存在,则转为jsonArray
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }
            } else {//子元素没有子元素
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (isEmpty(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }

    public boolean isEmpty(String str) {
        return str != null && !str.trim().isEmpty() && !"null".equals(str);
    }

}
