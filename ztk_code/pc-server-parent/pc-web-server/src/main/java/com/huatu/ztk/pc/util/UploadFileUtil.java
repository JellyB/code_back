package com.huatu.ztk.pc.util;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.ztk.pc.util.etag.Etag;
import com.huatu.ztk.question.util.ImageUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by renwenlong on 2016/11/16.
 */
@Component
public class UploadFileUtil {
    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtil.class);

    @Autowired
    private FtpClientPool ftpClientPool;

    //图片文件的基本保存路径
    public static final String IMG_FILE_BASE_BATH = "/var/www/cdn/images/vhuatu/tiku/";
    //分享图片基本保存路径
    public static final String PC_IMG_FILE_BASE_BATH = "/var/www/cdn/pc/share/image/";
    //图片url前缀
    public static final String PC_IMG_BASE_URL = "http://tiku.huatu.com/cdn/pc/share/image/";

    //图片url前缀
    public static final String IMG_BASE_URL = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";

    private static final ErrorResult UPLOAD_FILE_FAIL = ErrorResult.create(1315000, "上传文件失败");

    //试卷文件路径
    public static final String PDF_FILE_BASE_BATH="/var/www/cdn/images/vhuatu/tiku/pdf";

    /**
     * ftp上传文件
     * @param file file对象
     * @param fileName 文件名称
     * @param savePath 保存路径
     * @return
     * @throws BizException
     */
    public void ftpUpload(File file, String fileName, String savePath) throws BizException {
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        FileInputStream fis = null;
        try {
            //目录不存在，创建目录
            ftpClient.makeDirectory(savePath);
            //切换工作目录
            ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            fis = new FileInputStream(file);
            ftpClient.storeFile(fileName, fis);
        } catch (IOException e) {
            logger.error("ex", e);
            throw new BizException(UPLOAD_FILE_FAIL);
        } finally {
            //回收到连接池
            try {
                if (fis != null) {
                    fis.close();
                    file.delete();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            ftpClientPool.returnFTPClient(ftpClient);
        }
    }


    /**
     * 上传文件
     * @param file
     * @param fileName
     * @param savePath
     * @throws BizException
     */
    public void ftpUploadFile(File file, String fileName, String savePath) throws BizException{

        logger.info("===上传文件开始===");
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork =  ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.storeFile(fileName, new FileInputStream(file));

            logger.info(""+makeDir + changeWork +storeFile) ;
            logger.info("===上传文件结束===");
        } catch (IOException e) {
            logger.error("ex", e);
            throw new BizException(UPLOAD_FILE_FAIL);
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
    }

    /**
     *
     * @param fileInput
     * @param fileName
     * @param savePath
     * @throws BizException
     */
    public void ftpUploadFileInputStream(InputStream fileInput, String fileName, String savePath) throws BizException{

        logger.info("===上传文件开始===");
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork =  ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.storeFile(fileName,fileInput);

            logger.info(""+makeDir + changeWork +storeFile) ;
            logger.info("===上传文件结束===");
        } catch (IOException e) {
            logger.error("ex", e);
            throw new BizException(UPLOAD_FILE_FAIL);
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
    }

    /**
     * 将所有的非tiku.huatu.com域名下的图片复制到该域名下的cdn中
     * @return
     */
    public String moveImgToTiku(String target){
        if(target.indexOf("tiku.huatu.com")!=-1){
            return target;
        }
        String[] targetPart = target.split("/");
        String fileName = targetPart[targetPart.length-1];
        String path =  fileName.charAt(0) + "/";
        String savePath = PC_IMG_FILE_BASE_BATH + path;
        //new一个URL对象
        try {
            URL url = new URL(target);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //设置请求方式为"GET"
            conn.setRequestMethod("GET");
            //打开链接
            //超时响应时间为5秒
            conn.setConnectTimeout(2 * 1000);
            //通过输入流获取图片数据
            InputStream inStream = conn.getInputStream();
            logger.info("fileName={},savePath={}",fileName,savePath);
            ftpUploadFileInputStream(inStream,fileName,savePath);
        } catch (Exception e) {
            e.printStackTrace();
            return target;
        }
        return  PC_IMG_BASE_URL + path + fileName;
    }
    /**
     * 组装文件路径
     *
     * @param fileName
     * @return
     */
    private String makeImgSavePath(String fileName) {
        return IMG_FILE_BASE_BATH + fileName.charAt(0);
    }
    /**
     * 组装文件路径
     *
     * @param fileName
     * @return
     */
    private String makePdfSavePath(String fileName){
        return PDF_FILE_BASE_BATH + fileName.charAt(0);
    }


    /**
     * ftp 上传图片
     * @param imgFile
     * @return
     * @throws BizException
     */
    public String ftpUploadPic(File imgFile) throws BizException{
        String fileName = makeImgFileName(imgFile);
        String savePath = makeImgSavePath(fileName);
        ftpUpload(imgFile, fileName, savePath);
        String url = makeImgUrl(fileName);
        return url;
    }
    /**
     * ftp 上传图片
     * @param imgFile
     * @return
     * @throws BizException
     */
    public String ftpUploadPicByThread(File imgFile) throws BizException{
        String fileName = makeImgFileName(imgFile);
        String savePath = makeImgSavePath(fileName);
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    ftpUpload(imgFile, fileName, savePath);
                } catch (BizException e) {
                    e.printStackTrace();
                    logger.error("文件+"+fileName+"ftp上传失败");
                }
            }
        });
        thread.start();
        String url = makeImgUrl(fileName);
        return url;
    }

    /**
     * 简单组装图片url
     *
     * @param fileName
     * @return
     */
    private String makeImgUrl(String fileName) {
        return IMG_BASE_URL + fileName.charAt(0) + "/" + fileName;
    }

    public static void  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3*1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
        File saveDir = new File(savePath);
        if(!saveDir.exists()){
            saveDir.mkdir();
        }
        File file = new File(saveDir+File.separator+fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if(fos!=null){
            fos.close();
        }
        if(inputStream!=null){
            inputStream.close();
        }


        System.out.println("info:"+url+" download success");

    }

    /**
     * 组装图片名称
     * @param file
     * @return
     * @throws Exception
     */
    private String makeImgFileName(File file) throws BizException{
        String fileName = "";
        try {
            //保持文件后缀名
            String extName =file.getName().substring(file.getName().lastIndexOf("."));
            final InputStream stream = new FileInputStream(file);
            //生成文件名
            String fileNameTmp = Etag.stream(stream, stream.available()) + extName;
            fileName = fileNameTmp.substring(1);
            stream.close();
        } catch (IOException e) {
            logger.error("ex", e);
            throw new BizException(UPLOAD_FILE_FAIL);
        }
        return fileName;
    }


    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}
