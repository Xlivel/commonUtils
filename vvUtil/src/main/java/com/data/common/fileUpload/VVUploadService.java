package com.data.common.fileUpload;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.data.common.ticket.TicketUtil;
import com.data.common.utils.XTHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import sun.misc.BASE64Decoder;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;

/**
 * vv文件上传和图片上传
 * @author wj
 */
public class VVUploadService {

    private static Logger logger = LoggerFactory.getLogger(VVUploadService.class);

    private static final String uploadFileUrl = "/docrest/doc/file/uploadfile?accessToken=";
    private static final String uploadBase64Url = "/docrest/base64/upload";
    private static final String downFileUrl = "/docrest/doc/user/downloadfile?fileId=";
    private static final String downFileUrlWithoutAuth = "/microblog/filesvr/";
    private static final String imageBaseUrlWithoutAuth = "/space/c/photo/load?id=";

    @Value("${vv.host:http://vvtest.vanke.com}")
    private String opensysHost = "http://vvtest.vanke.com";

    @Value("${vv.openFileSecret:wWmFZWbuFyUfztdPUc4mwi5MiVPKnXoa}")
    private String openFileSecret = "wWmFZWbuFyUfztdPUc4mwi5MiVPKnXoa";

    @Value("${vv.defaultEid:16216997}")
    private String defaultEid = "16216997";

    //文件上传
    public String uploadFile(String contentType, InputStream inputStream, String fileName) throws Exception {
        String token = TicketUtil.getAccessToken(defaultEid, "resGroupSecret", openFileSecret, opensysHost);
        try {
            String url = opensysHost + uploadFileUrl + token;
            HttpPost httpPost = new HttpPost(url);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", new InputStreamBody(inputStream, ContentType.create(contentType), fileName))
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .setCharset(CharsetUtils.get("UTF-8"))
                    .build();
            httpPost.setEntity(reqEntity);
            httpPost.setHeader("x-accessToken", token);
            CloseableHttpResponse resp = null;
            HttpEntity respEntity = null;
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                resp = httpClient.execute(httpPost);
                respEntity = resp.getEntity();
                String content = EntityUtils.toString(respEntity);
                if (StringUtils.isNotEmpty(content)) {
                    return content;
                }
                throw new Exception();
            } finally {
                if (respEntity != null) {
                    EntityUtils.consumeQuietly(respEntity);
                }
                if (resp != null) {
                    try {
                        resp.close();
                    } catch (Exception ignore) {
                    }
                }
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException var2) {
                }
            }
        }
    }

    public String uploadFile(File file) throws Exception {
        if (file != null) {
            FileInputStream fis = new FileInputStream(file);
            String contentType = new MimetypesFileTypeMap().getContentType(file);
            String s = uploadFile(contentType, fis, file.getName());
            JSONObject jsonObject = JSONObject.parseObject(s);
            if (jsonObject.getBooleanValue("success")) {
                JSONArray data = jsonObject.getJSONArray("data");
                if (data != null && !data.isEmpty()) {
                    JSONObject jsonObject1 = data.getJSONObject(0);
                    return opensysHost + downFileUrlWithoutAuth + jsonObject1.getString("fileId");
                }
            }
        }
        return null;
    }

    //图片上传
    public String uploadPic(String base64Str, String fileName) throws Exception {
        try {
            JSONObject param = new JSONObject();
            param.put("base64Data", base64Str);
            String s = XTHttpClient.getHttpClient().httpPost(opensysHost + uploadBase64Url, param, 5000);
            logger.info("result:" + s);
            if (StringUtils.isNotBlank(s)) {
                JSONObject jsonObject = JSONObject.parseObject(s);
                if (jsonObject.getBoolean("success")) {
                    JSONArray data = jsonObject.getJSONArray("data");
                    return opensysHost + imageBaseUrlWithoutAuth + data.getString(0);
                }
            }
        } catch (Exception e) {
            logger.error("add error", e);
        }
        //调用另一套上传
        return uploadPic(BaseToInputStream(base64Str), fileName);
    }

    public String uploadPic(InputStream inputStream, String fileName) throws Exception {
        String s = uploadFile("image/png", inputStream, fileName);
        logger.info("docrestService.uploadFile result:  " + s);
        JSONObject json = JSONObject.parseObject(s);
        if (json.getBoolean("success")) {
            JSONArray data = json.getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                String fileId = data.getJSONObject(0).getString("fileId");
                return opensysHost + imageBaseUrlWithoutAuth + fileId;
            }
        }
        return null;
    }

    private InputStream BaseToInputStream(String base64string) {
        if (base64string.contains("data:image")) {
            base64string = base64string.substring(base64string.indexOf(",") + 1);
        }
        ByteArrayInputStream stream = null;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64string);
            stream = new ByteArrayInputStream(bytes1);
        } catch (Exception e) {
        }
        return stream;
    }

    public static void main(String[] args) throws Exception {
        String eid = "16216997";
        VVUploadService vvUploadService = new VVUploadService();

        String s = vvUploadService.uploadFile(new File("C:\\Users\\wu_ang\\Desktop\\pc9.2.4下载地址.txt"));
        System.out.println("asdsad");
    }

}
