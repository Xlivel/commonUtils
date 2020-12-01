package com.data.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FtpUtil {

    /**
     * 本地字符编码
     */
    private static String LOCAL_CHARSET = "GBK";

    // FTP协议里面，规定文件名编码为iso-8859-1
    private static String SERVER_CHARSET = "ISO-8859-1";

    private static Logger log = LoggerFactory.getLogger(FtpUtil.class);

    /**
     * Description: 向FTP服务器上传文件
     *
     * @param host     FTP服务器hostname
     * @param port     FTP服务器端口
     * @param username FTP登录账号
     * @param password FTP登录密码
     * @param basePath FTP服务器基础目录
     * @param filePath FTP服务器文件存放路径。例如分日期存放：/root/skai/2018/02/02。文件的路径为basePath+filePath
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @return 成功返回true，否则返回false
     */
    public static boolean uploadFile(String host, int port, String username, String password, String basePath,
                                     String filePath, String filename, InputStream input) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                log.error("=======ftp服务器登录失败=======");
                ftp.disconnect();
                return result;
            }
            //切换到上传目录
            if (!ftp.changeWorkingDirectory(basePath + filePath)) {
                //如果目录不存在创建目录
                String[] dirs = filePath.split("/");
                String tempPath = basePath;
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) continue;
                    tempPath += "/" + dir;
                    if (!ftp.changeWorkingDirectory(tempPath)) {//转移到FTP服务器目录
                        if (!ftp.makeDirectory(tempPath)) {//创建目录
                            return result;
                        } else {
                            ftp.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
            //设置上传文件的类型为二进制类型
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.setControlEncoding(LOCAL_CHARSET);
            //上传文件
            ftp.enterLocalPassiveMode();
            if (!ftp.storeFile(new String(filename.getBytes(LOCAL_CHARSET), SERVER_CHARSET), input)) {
                return result;
            }
            input.close();
            ftp.logout();
            result = true;
        } catch (IOException e) {
            log.error("=======ftp文件上传IO异常=======" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }


    /**
     * 下载文件
     *
     * @param path
     * @param fileName
     * @param response
     */
    public static void downloadFile(String host, int port, String username, String password,
                                    String path, String fileName, HttpServletResponse response) {

        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                log.error("=======ftp服务器登录失败=======");
                ftp.disconnect();
            }
            ftp.changeWorkingDirectory(path);// 转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                String name = new String(ff.getName().getBytes(SERVER_CHARSET), LOCAL_CHARSET);
                if (name.equals(fileName)) {
                    // 获得文件大小
                    int size = (int) fs[0].getSize();
                    byte[] bytes = new byte[size];
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    //写入输出流
                    ftp.retrieveFile(new String(name.getBytes(LOCAL_CHARSET), SERVER_CHARSET), os);
                    bytes = os.toByteArray();
                    os.flush();
                    os.close();

                    // 清空response
                    response.reset();
                    name = new String(name.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
                    // 设置response的Header
                    response.addHeader("Content-Disposition", "attachment;filename=" + name);
                    response.addHeader("Content-Length", "" + ff.getSize());
                    OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
                    response.setContentType("application/force-download");
                    toClient.write(bytes);
                    toClient.flush();
                    toClient.close();
                }
            }
            ftp.logout();
        } catch (IOException ex) {
            log.error("=======ftp文下载IO异常=======" + ex.getMessage());
            ex.printStackTrace();
        }
    }


    /**
     * 预览服务器文件数据
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param remotePath
     * @param fileName
     * @param separatorCol
     * @param separatorRow
     * @param readRows
     * @return
     */
    public static List readFileOnServer(String host, int port, String username, String password, String remotePath,
                                        String fileName, String separatorCol, String separatorRow, int readRows) {

        FTPClient ftp = new FTPClient();
        List<String> list = new ArrayList<>();
        try {
            int reply;
            ftp.connect(host, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                log.error("=======ftp服务器登录失败=======");
                ftp.disconnect();
            }
            ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                String name = new String(ff.getName().getBytes(SERVER_CHARSET), LOCAL_CHARSET);
                if (name.equals(fileName)) {
                    //从服务器上读取指定的文件
                    InputStream ins = ftp.retrieveFileStream(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                    Reader reader = new InputStreamReader(ins, LOCAL_CHARSET);

                    String arr = "";
                    List<String> listStr = new ArrayList<>();
                    int tempchar;
                    int i = 0;
                    while ((tempchar = reader.read()) != -1) {
                        char separatorRowChar = '\r';
                        char separatorColChar = separatorCol.charAt(0);
                        if (StringUtils.equals("\\n", separatorRow)) {
                            separatorRowChar = '\n';
                        }
                        if (StringUtils.equals("\\r\\n", separatorRow) && ((char) tempchar) == '\n') {//换行/r/n 去掉/n
                            arr = "";
                        } else {
                            arr += String.valueOf((char) tempchar);
                            if (((char) tempchar) == separatorColChar) {//列数据
                                listStr.add(arr.substring(0, arr.length() - 1));
                                arr = "";
                            }
                            if (((char) tempchar) == separatorRowChar) {//行数据
                                listStr.add(arr.substring(0, arr.length() - 1));
                                arr = "";
                                String hang = "";
                                for (int k = 0; k < listStr.size(); k++) {
                                    hang += listStr.get(k).toString() + ",";
                                }
                                list.add(hang);
//                                System.out.println("=========行数据:  "+list.toString());
                                listStr.clear();
                                i++;
                            }
                            if (i == readRows) {
                                break;
                            }
                        }
                    }
                    reader.close();
                }
            }
            ftp.logout();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public static JSONObject readCSVFile(String ip, int port, String username, String password, String fileName,
                                         String filePath, String previewRows, String isContent) {


        JSONObject JSONObject = new JSONObject();
        Boolean success = true;
        String msg = "";

        FTPClient ftp = new FTPClient();
        List<String> datalst = new ArrayList<>();
        int previewRowsInt = Integer.parseInt(previewRows);

        try {
            int reply;
            ftp.connect(ip, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                success = false;
                msg = "ftp服务器登录失败！";
                ftp.disconnect();
            }
            if (ftp.changeWorkingDirectory(filePath)) {// 转移到FTP服务器目录
                FTPFile[] fs = ftp.listFiles(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                if (fs.length > 0) {
                    for (FTPFile ff : fs) {

                        String name = new String(ff.getName().getBytes(SERVER_CHARSET), LOCAL_CHARSET);
                        if (name.equals(fileName)) {
                            //开通一个端口来传输数据
                            ftp.enterLocalPassiveMode();
                            InputStream ins = ftp.retrieveFileStream(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                            CSVReader csvReader = new CSVReader(new InputStreamReader(ins, LOCAL_CHARSET));
                            StringBuffer defaultTitle = new StringBuffer();
                            StringBuffer titleRow = new StringBuffer();
                            String[] titles = new String[0];
                            try {
                                titles = csvReader.readNext();
                            } catch (CsvValidationException e) {
                                e.printStackTrace();
                            }
                            if (titles != null && titles.length > 0) {
                                int i = 0;
                                for (String title : titles) {
                                    i++;
                                    if (title != null && !title.equals("")) {
                                        titleRow.append(title + ",");
                                        defaultTitle.append("filed_" + i + ",");
                                    }
                                }
                                if (StringUtils.equals("false", isContent)) {
                                    datalst.add(defaultTitle.substring(0, defaultTitle.lastIndexOf(",")));
                                    datalst.add(titleRow.substring(0, titleRow.lastIndexOf(",")));
                                    --previewRowsInt;
                                } else {
                                    datalst.add(titleRow.substring(0, titleRow.lastIndexOf(",")));
                                }
                            }

                            List<String[]> list = null;
                            try {
                                list = csvReader.readAll();
                            } catch (CsvException e) {
                                e.printStackTrace();
                            }

                            for (String[] ss : list) {
                                if (--previewRowsInt < 0) {
                                    break;
                                }
                                StringBuffer dataRow = new StringBuffer();
                                for (String s : ss) {
                                    if (null != s && !s.equals("")) {
                                        dataRow.append(s + " , ");
                                    }
                                }
                                datalst.add(dataRow.substring(0, dataRow.lastIndexOf(",")));
                            }
                            if (ins != null) {
                                ins.close();
                            }
                            // 主动调用一次getReply()把接下来的226消费掉. 这样做是可以解决这个返回null问题
                            ftp.getReply();
                        }
                    }
                } else {
                    success = false;
                    msg = "服务器无该文件存在请核对文件名称！";
                }
            } else {
                success = false;
                msg = "ftp服务器无该路径存在请检查文件路径 ！";
            }
            ftp.logout();
        } catch (IOException e) {
            success = false;
            msg = e.getMessage();
            e.printStackTrace();
        }
        JSONObject.put("success", success);
        JSONObject.put("msg", msg);
        JSONObject.put("data", datalst);
        return JSONObject;
    }


    /**
     * 预览图片
     *
     * @param path
     * @param fileName
     * @param response
     */
    public static JSONObject previewPicture(String host, int port, String username, String password,
                                            String path, String fileName, HttpServletResponse response) {

        JSONObject JSONObject = new JSONObject();
        Boolean flag = true;
        String msg = "图片预览成功！";
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                flag = false;
                msg = "ftp服务器登录失败！";
                ftp.disconnect();
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            if (ftp.changeWorkingDirectory(path)) {// 转移到FTP服务器目录
                FTPFile[] fs = ftp.listFiles(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                if (fs.length > 0) {
                    for (FTPFile ff : fs) {
                        String name = new String(ff.getName().getBytes(SERVER_CHARSET), LOCAL_CHARSET);
                        if (name.equals(fileName)) {
                            // 获得文件大小
                            int size = (int) fs[0].getSize();
                            log.info("========图片预览===文件size=====" + size);

                            byte[] bytes = new byte[size];
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            // 写入输出流
                            InputStream ins = ftp.retrieveFileStream(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));

                            // 缓冲大小设置为2kb
                            int bufferSize = 2048;
                            int readCount;
                            byte[] buffer = new byte[bufferSize];
                            //每次读文件流的2kb
                            readCount = ins.read(buffer, 0, bufferSize);
                            while (readCount > 0) {
                                //把内容从文件流写入
                                os.write(buffer, 0, readCount);
                                readCount = ins.read(buffer, 0, bufferSize);
                            }
                            //关闭两个流
                            os.flush();
                            os.close();
                            ins.close();

//                            ftp.retrieveFile(new String(fileName.getBytes(LOCAL_CHARSET),SERVER_CHARSET),os);
//                            os.flush();
                            bytes = os.toByteArray();
                            log.info("========图片预览=====流size=====" + os.size());
                            // 清空response
                            response.reset();
                            // 设置response的Header
                            response.addHeader("Content-Disposition", "inline;filename=" + new String(fileName.getBytes()));
                            response.addHeader("Content-Length", "" + ff.getSize());
                            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
                            response.setContentType("image/jpeg");
                            toClient.write(bytes);
                            toClient.flush();
                            toClient.close();
                            os.close();
                        }
                    }
                } else {
                    flag = false;
                    msg = "服务器无该文件存在请核对文件名称！";
                }

            } else {
                flag = false;
                msg = "ftp服务器无该路径存在请检查文件路径 ！";
            }
            ftp.logout();
        } catch (IOException ex) {
            flag = false;
            msg = ex.getMessage();
            ex.printStackTrace();
        }
        JSONObject.put("success", flag);
        JSONObject.put("msg", msg);
        return JSONObject;
    }


    public static Workbook readExcel(String ip, int port, String username, String password, String fileName,
                                     String filePath) {

        /*JSONObject JSONObject = new JSONObject();
        Boolean success = true;
        String msg = "";*/


        FTPClient ftp = new FTPClient();

        Workbook wb = null;
        try {
            int reply;
            ftp.connect(ip, port);
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                log.error("=======ftp服务器登录失败=======");
                ftp.disconnect();
            }
            if (ftp.changeWorkingDirectory(filePath)) {// 转移到FTP服务器目录
                FTPFile[] fs = ftp.listFiles(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                if (fs.length > 0) {
                    for (FTPFile ff : fs) {
                        String name = new String(ff.getName().getBytes(SERVER_CHARSET), LOCAL_CHARSET);
                        if (name.equals(fileName)) {

                            String extString = fileName.substring(fileName.lastIndexOf("."));
                            InputStream ins = ftp.retrieveFileStream(new String(fileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
                            if (".xls".equals(extString)) {
                                return wb = new HSSFWorkbook(ins);
                            } else if (".xlsx".equals(extString)) {
                                return wb = new XSSFWorkbook(ins);
                            } else {
                                return wb = null;
                               /* success = false;
                                msg = "EXCELL文件扩展名只能以 .xls;.xlsx结尾！请核查！";*/
                            }
                        }
                    }
                } else {
                    /*success = false;
                    msg = "服务器无该文件存在请核对文件名称！";*/
                }
            } else {
                /*success = false;
                msg = "ftp服务器无该路径存在请检查文件路径 ！";*/
            }
            ftp.logout();
        } catch (IOException e) {
            /*success = false;
            msg = e.getMessage();*/
            e.printStackTrace();
        }
        return wb;
    }


    public static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            //判断cell类型
            int i = cell.getCellType().hashCode();
            if (CellType.NUMERIC.hashCode() == i) {
                cellValue = String.valueOf(cell.getNumericCellValue());
            } else if (CellType.FORMULA.hashCode() == i) {
                //判断cell是否为日期格式
                if (DateUtil.isCellDateFormatted(cell)) {
                    //转换为日期格式YYYY-mm-dd
                    cellValue = cell.getDateCellValue();
                } else {
                    //数字
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }
            } else if (CellType.STRING.hashCode() == i) {
                cellValue = cell.getRichStringCellValue().getString();
            } else {
                cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }

    /**
     * 删除FTP上的文件
     *
     * @param ftpDirAndFileName 路径从根目录开始/，/test/filename1 需要添加根目录！Sunk 2018-04-26 16:12)
     * @return
     */
    public static boolean deleteFile(String host, int port, String username, String password, String ftpDirAndFileName) {
        boolean result = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(host, port);// 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(host)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return result;
            }
            System.out.println("要删除的目录文件：" + ftpDirAndFileName);
            if (ftpDirAndFileName.indexOf(".") <= 0) {
                return result;
            }
            result = ftp.deleteFile(new String(ftpDirAndFileName.getBytes(LOCAL_CHARSET), SERVER_CHARSET));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }

}
