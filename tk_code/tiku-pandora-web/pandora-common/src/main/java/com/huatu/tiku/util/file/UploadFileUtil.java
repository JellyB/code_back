package com.huatu.tiku.util.file;

import com.huatu.tiku.util.etag.Etag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by renwenlong on 2016/11/16.
 */
@Component
@Slf4j
public class UploadFileUtil {

    public static final UploadFileUtil getInstance() {
        return InnerInstance.INSTANCE;
    }

    private static class InnerInstance {
        public static final UploadFileUtil INSTANCE = new UploadFileUtil();
    }

    private static FtpClientPool ftpClientPool = new FtpClientPool();

    //图片文件的基本保存路径
    public static final String IMG_FILE_BASE_BATH = "var/www/cdn/images/vhuatu/tiku/";

    //图片url前缀
    public static final String IMG_BASE_URL = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";

//    private static final ErrorResult UPLOAD_FILE_FAIL = ErrorResult.create(1315000, "上传文件失败");

    //试卷文件路径
    public static final String PDF_FILE_BASE_BATH = "/var/www/cdn/images/vhuatu/tiku/pdf";

    //{link teacher}
    public final static String IMG_PATH = "/var/www/cdn/pandora/img/";//图片文件夹地址
    public final static String IMG_URL = "http://tiku.huatu.com/cdn/pandora/img/";//图片路径
    public final static String IMG_PATH_QUESTION = "/var/www/cdn/pandora/img/question/";//图片文件夹地址
    public final static String IMG_URL_QUESTION = "http://tiku.huatu.com/cdn/pandora/img/question/";//图片路径

    /**
     * ftp上传文件
     *
     * @param file     file对象
     * @param fileName 文件名称
     * @param savePath 保存路径
     * @return //     * @throws BizException
     */
    public void ftpUpload(File file, String fileName, String savePath) {
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
            log.error("ex", e);
            throw new RuntimeException("上传失败");
        } finally {
            //回收到连接池
            try {
                if (fis != null) {
                    fis.close();
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ftpClientPool.returnFTPClient(ftpClient);
        }
    }

//    public String upload(MultipartFile file) {
//        try {
//            if (null == file) {
//                throw new BizException(ErrorResult.create(100001, "文件上传失败"));
//            }
//            InputStream inputStream = file.getInputStream();
//            //获取文件后缀名
//            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
//            //生成新文件名称
//            String fileName = UUID.randomUUID().toString() + "" + suffix;
//            ftpUploadFileInputStream(inputStream, fileName, IMG_PATH);
//            return IMG_URL + fileName;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    /**
     * 上传文件
     *
     * @param file
     * @param fileName
     * @param savePath
     */
    public void ftpUploadFile(File file, String fileName, String savePath) {

        log.info("===上传文件开始===");
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork = ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.storeFile(fileName, new FileInputStream(file));
            log.info("" + makeDir + changeWork + storeFile);
            log.info("===上传文件结束===");
        } catch (IOException e) {
            log.error("ex", e);
            throw new RuntimeException("上传失败");
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
    }

    /**
     * @param fileInput
     * @param fileName
     * @param savePath
     */
    public void ftpUploadFileInputStream(InputStream fileInput, String fileName, String savePath) {

        log.info("===上传文件开始===");
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork = ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.storeFile(fileName, fileInput);

            log.info("" + makeDir + changeWork + storeFile);
            log.info("===上传文件结束===");
        } catch (IOException e) {
            log.error("ex", e);
            throw new RuntimeException("上传失败");
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
    }

    /**
     * 删除远程文件
     *
     * @param urlPath
     */
    public void ftpDeleteFile(String urlPath) {
        String fileName = urlPath.substring(urlPath.lastIndexOf("/")+1);
        String savePath = urlPath.substring(0,urlPath.lastIndexOf("/")+1);
        log.info("===删除文件开始===，{}",urlPath);
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork = ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.deleteFile(fileName);
            log.info("===删除文件结束===，{}：{}",urlPath,storeFile);
        } catch (IOException e) {
            log.error("ex", e);
            throw new RuntimeException("上传失败");
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
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
    private String makePdfSavePath(String fileName) {
        return PDF_FILE_BASE_BATH + fileName.charAt(0);
    }


    /**
     * ftp 上传图片
     *
     * @param imgFile
     * @return
     */
    public String ftpUploadPic(File imgFile) {
        String fileName = makeImgFileName(imgFile);
        String savePath = makeImgSavePath(fileName);
        ftpUpload(imgFile, fileName, savePath);
        String url = makeImgUrl(fileName);
        return url;
    }

    /**
     * ftp 上传图片
     *
     * @param imgFile
     * @return
     */
    public String ftpUploadPicByThread(File imgFile) {
        String fileName = makeImgFileName(imgFile);
        String savePath = makeImgSavePath(fileName);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ftpUpload(imgFile, fileName, savePath);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    log.error("文件+" + fileName + "ftp上传失败");
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

    public static void downLoadFromUrl(String urlStr, String fileName, String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置超时间为3秒
        conn.setConnectTimeout(3 * 1000);
        //防止屏蔽程序抓取而返回403错误
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        //得到输入流
        InputStream inputStream = conn.getInputStream();
        //获取自己数组
        byte[] getData = readInputStream(inputStream);

        //文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        File file = new File(saveDir + File.separator + fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if (fos != null) {
            fos.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }


        System.out.println("info:" + url + " download success");

    }

    /**
     * 组装图片名称
     *
     * @param file
     * @return
     * @throws Exception
     */
    private String makeImgFileName(File file) {
        String fileName = "";
        try {
            //保持文件后缀名
            String extName = file.getName().substring(file.getName().lastIndexOf("."));
            final InputStream stream = new FileInputStream(file);
            //生成文件名
            String fileNameTmp = Etag.stream(stream, stream.available()) + extName;
            fileName = fileNameTmp.substring(1);
            stream.close();
        } catch (IOException e) {
            log.error("ex", e);
            throw new RuntimeException("上传失败");
        }
        return fileName;
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}
