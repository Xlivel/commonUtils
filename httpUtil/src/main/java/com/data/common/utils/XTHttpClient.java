package com.data.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class XTHttpClient {

    private static Logger logger = LoggerFactory.getLogger(XTHttpClient.class);
    private static XTHttpClient instance = null;
    private static Lock lock = new ReentrantLock();
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;
    private static ThreadLocal<Map<String, String>> threadLocalRequestHeader = new ThreadLocal<Map<String, String>>();

    private static Charset UTF_8 = Charset.forName("UTF-8");

    private XTHttpClient() {
        instance = this;
    }

    public static XTHttpClient getHttpClient() {
        if (instance == null) {
            instance = new XTHttpClient();
        }
        return instance;
    }

    private void init() {
        if (httpClient == null) {
            long begin = System.currentTimeMillis();
            lock.lock();
            try {
                if (httpClient == null) {
                    connectionManager = new PoolingHttpClientConnectionManager();
                    connectionManager.setMaxTotal( 500);
                    connectionManager.setDefaultMaxPerRoute(500);
                    connectionManager.closeExpiredConnections();
                    httpClient = HttpClientBuilder.create()
                            .setConnectionManager(connectionManager)
                            .setDefaultRequestConfig(XTRequestConfig.getInstance().getRequestConfig(5000))
                            .disableCookieManagement()
                            .disableAutomaticRetries()
//                    .setRoutePlanner(new org.apache.http.impl.conn.DefaultProxyRoutePlanner(new HttpHost("127.0.0.1", 8888)))
                            .build();
                }
            } finally {
                lock.unlock();
                begin = System.currentTimeMillis() - begin;
                if (begin > 200 && logger.isInfoEnabled()) {
                    logger.info("init httpclient cost:{}", begin);
                }
            }
        }
    }

    public PoolStats getConnectionStatus(URI uri) {
        if (connectionManager != null) {
            HttpHost target = URIUtils.extractHost(uri);
            if (target.getPort() <= 0) {
                try {
                    target = new HttpHost(
                            target.getHostName(),
                            DefaultSchemePortResolver.INSTANCE.resolve(target),
                            target.getSchemeName());
                } catch (Throwable ignore) {
                    target = null;
                }
            }
            if (target != null)
                return connectionManager.getStats(new HttpRoute(target));
        }
        return null;
    }

    public PoolStats getConnectionStatus() {
        if (connectionManager != null) {
            return connectionManager.getTotalStats();
        }
        return null;
    }

    public static void addThreadLocalRequestHeader(String key, String value) {
        if (threadLocalRequestHeader.get() == null) {
            threadLocalRequestHeader.set(new HashMap<String, String>());
        }
        threadLocalRequestHeader.get().put(key, value);
    }

    public static void initThreadLocalRequestHeader() {
        threadLocalRequestHeader.remove();
    }

    public static void clearThreadLocalRequestHeader() {
        threadLocalRequestHeader.remove();
    }

    public CloseableHttpResponse executeReturnResp(HttpRequestBase request) throws Exception{
        init();
        if (request == null) return null;
        HttpEntity entity = null;
        CloseableHttpResponse resp = null;
        long beginTime = System.currentTimeMillis();
        try {
            if (threadLocalRequestHeader.get() != null) {
                for (Map.Entry<String, String> entry : threadLocalRequestHeader.get().entrySet()) {
                    if (!request.containsHeader(entry.getKey())) {
                        request.addHeader(entry.getKey(), entry.getValue());
                    }
                }
            }
            resp = httpClient.execute(request);
            long t = System.currentTimeMillis() - beginTime;
            if (t > 10) {
                logger.info("--", "--", "XTHttpClient.execute cost:" + t, request.getURI().toString());
            }
            entity = resp.getEntity();
            if (resp.getStatusLine().getStatusCode() == 200) {
                String encoding = ("" + resp.getFirstHeader("Content-Encoding")).toLowerCase();
                if (encoding.indexOf("gzip") > 0) {
                    entity = new GzipDecompressingEntity(entity);
                    resp.setEntity(entity);
                }
                if (!resp.containsHeader("Content-Length")) {
                    logger.info("--", "--", "no Content-Length", request.getURI().toString());
                }
                if (StringUtils.equals("" + resp.getFirstHeader("Connection"), "Close")) {
                    logger.info("--", "--", "no keep-alive", request.getURI().toString());
                }
            } else {
                String postData = getPostData(request);
                logger.warn("{}{}\nreturn error {}", request.getURI().toString(), postData, resp.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            String postData = getPostData(request);
            String msg = e.getMessage();
            if (e instanceof SocketTimeoutException) {
                RequestConfig requestConfig = request.getConfig();
                if (requestConfig != null) {
                    msg = msg + "  timeout:" + requestConfig.getSocketTimeout();
                }
            }
            logger.error(request.getURI().toString() + postData + "\n" + msg, e);
            throw e;
        }
        return resp;
    }

    public byte[] executeAndReturnByte(HttpRequestBase request) throws Exception {
        init();
        HttpEntity entity = null;
        CloseableHttpResponse resp = null;
        byte[] rtn = new byte[0];
        if (request == null) return rtn;
        long beginTime = System.currentTimeMillis();
        try {
            if (threadLocalRequestHeader.get() != null) {
                for (Map.Entry<String, String> entry : threadLocalRequestHeader.get().entrySet()) {
                    if (!request.containsHeader(entry.getKey())) {
                        request.addHeader(entry.getKey(), entry.getValue());
                    }
                }
            }
            resp = httpClient.execute(request);
            long t = System.currentTimeMillis() - beginTime;
            if (t > 10) {
                logger.info("--", "--", "XTHttpClient.execute cost:" + t, request.getURI().toString());
            }
            beginTime = System.currentTimeMillis();
            entity = resp.getEntity();
            if (resp.getStatusLine().getStatusCode() == 200) {
                String encoding = ("" + resp.getFirstHeader("Content-Encoding")).toLowerCase();
                if (encoding.indexOf("gzip") > 0) {
                    entity = new GzipDecompressingEntity(entity);
                }
                if (!resp.containsHeader("Content-Length")) {
                    logger.info("--", "--", "no Content-Length", request.getURI().toString());
                }
                if (StringUtils.equals("" + resp.getFirstHeader("Connection"), "Close")) {
                    logger.info("--", "--", "no keep-alive", request.getURI().toString());
                }
                rtn = EntityUtils.toByteArray(entity);
            } else {
                String encoding = ("" + resp.getFirstHeader("Content-Encoding")).toLowerCase();
                if (encoding.indexOf("gzip") > 0) {
                    entity = new GzipDecompressingEntity(entity);
                }
                if (!resp.containsHeader("Content-Length")) {
                    logger.info("--", "--", "no Content-Length", request.getURI().toString());
                }
                if (StringUtils.equals("" + resp.getFirstHeader("Connection"), "Close")) {
                    logger.info("--", "--", "no keep-alive", request.getURI().toString());
                }
//                rtn = EntityUtils.toByteArray(entity);
                String postData = getPostData(request);
                logger.warn("{}{}\nreturn error {}{}", request.getURI().toString(), postData, resp.getStatusLine().getStatusCode(), new String(EntityUtils.toByteArray(entity), UTF_8));
            }
            t = System.currentTimeMillis() - beginTime;
            if (t > 10) {
                logger.info("--", "--", "XTHttpClient.getResponseContent cost:" + t, request.getURI().toString());
            }
        } catch (Exception e) {
            String postData = getPostData(request);
            String msg = e.getMessage();
            if (e instanceof SocketTimeoutException) {
                RequestConfig requestConfig = request.getConfig();
                if (requestConfig != null) {
                    msg = msg + "  timeout:" + requestConfig.getSocketTimeout();
                }
            }
            logger.error(request.getURI().toString() + postData + "\n" + msg, e);
            throw e;
        } finally {
            EntityUtils.consumeQuietly(entity);
            if (resp != null) {
                try {
                    resp.close();
                } catch (Exception ignore) {
                }
            }
        }
        return rtn;
    }

    private String getPostData(HttpRequestBase request) throws Exception {
        String postData = "";
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntity httpEntity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (httpEntity instanceof StringEntity) {
                postData = "\npost data:" + IOUtils.toString(((StringEntity) httpEntity).getContent(), "UTF-8");
            }
        }
        return postData;
    }

    public String execute(HttpRequestBase request) throws Exception {
        byte[] bytes = executeAndReturnByte(request);
        if (bytes == null || bytes.length == 0) return null;
        return new String(bytes, UTF_8);
    }

    public String httpGet(String url, int timeout) throws Exception {
        HttpGet get = null;
        long begin = System.currentTimeMillis();
        try {
            get = new HttpGet(url);
            get.setConfig(XTRequestConfig.getInstance().getRequestConfig(timeout));
            return execute(get);
        } finally {
            try {
                if (get != null) get.abort();
            } catch (Exception ignore) {
            }
            long t = System.currentTimeMillis() - begin;
            logger.info("--", "httpGet cost:" + t, "url:" + url);
        }
    }

    public String httpPost(String url, List<NameValuePair> params, int timeout) throws Exception {
        String res = null;
        HttpPost post = null;
        long begin = System.currentTimeMillis();
        try {
            post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(params, UTF_8));
            post.setConfig(XTRequestConfig.getInstance().getRequestConfig(timeout));
            res = execute(post);
        } finally {
            if (post != null)
                post.abort();
            begin = System.currentTimeMillis() - begin;
            if (begin > 200) {
                logger.info("cost:{}\n{}\n{}", begin, url, JSON.toJSONString(params));
            }
        }
        return res;
    }
    public String httpPost(String url, List<NameValuePair> params, Map<String,String> header, int timeout) throws Exception {
        String res = null;
        HttpPost post = null;
        long begin = System.currentTimeMillis();
        try {
            post = new HttpPost(url);
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            post.setEntity(new UrlEncodedFormEntity(params, UTF_8));
            post.setConfig(XTRequestConfig.getInstance().getRequestConfig(timeout));
            res = execute(post);
        } finally {
            if (post != null)
                post.abort();
            begin = System.currentTimeMillis() - begin;
            if (begin > 200) {
                logger.info("cost:{}\n{}\n{}", begin, url, JSON.toJSONString(params));
            }
        }
        return res;
    }

    public String httpPost(String url, JSONObject jsonParam, int timeout) throws Exception {
        return httpPost(url, jsonParam.toJSONString(), "application/json", null, timeout);
    }

    public String httpPost(String url, JSONObject jsonParam, Map<String, String> header, int timeout) throws Exception {
        return httpPost(url, jsonParam.toJSONString(), "application/json", header, timeout);
    }


    public String httpPost(String url, String postData, String postContentType, int timeout) throws Exception {
        return httpPost(url, postData, postContentType, null, timeout);
    }

    public String httpPost(String url, String postData, String postContentType, Map<String, String> header, int timeout) throws Exception {
        String res = null;
        HttpPost post = null;
        long begin = System.currentTimeMillis();
        try {
            post = new HttpPost(url);
            if (header != null) {
                for (Map.Entry<String, String> entry : header.entrySet()) {
                    post.setHeader(entry.getKey(), entry.getValue());
                }
            }
            post.setConfig(XTRequestConfig.getInstance().getRequestConfig(timeout));
            StringEntity reqEntity = new StringEntity(postData, UTF_8);
            reqEntity.setContentType(postContentType);
            post.setEntity(reqEntity);
            res = execute(post);
        } finally {
            if (post != null) {
                post.abort();
            }
            begin = System.currentTimeMillis() - begin;
            if (begin > 200) {
                logger.info("cost:{}\n{}\n{}", begin, url, postData);
            }
        }
        return res;
    }

    public String postUrlEncodeForm(String url, Map<String, Object> map) throws Exception {
        String coding = "UTF-8";
        String result = null;
        //处理请求参数
        List<NameValuePair> valuePairs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            NameValuePair valuePair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
            valuePairs.add(valuePair);
        }

        //设置client参数
        HttpClient client = HttpClientBuilder.create().build();

        //发送请求
        HttpPost post = new HttpPost(url);
        HttpEntity entity = new UrlEncodedFormEntity(valuePairs, coding);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);

        //处理响应结果
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new RuntimeException("statusCode = [" + statusCode + "]");
        } else {
            HttpEntity respEntity = response.getEntity();
            result = EntityUtils.toString(respEntity, coding);
        }
        return result;
    }
}
