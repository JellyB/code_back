package com.huatu.tiku.interview.util.file;


import com.huatu.tiku.interview.constant.BasicParameters;
import com.huatu.tiku.interview.constant.CourseConstant;
import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.exception.ReqException;
import com.huatu.tiku.interview.util.etag.Etag;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by renwenlong on 2016/11/16.
 */
@Component
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    @Autowired
    private FtpClientPool ftpClientPool;


    //图片文件的基本保存路径
    public static final String IMG_FILE_BASE_BATH = "/var/www/cdn/images/vhuatu/interview/arrangement/";

    //图片url前缀
    public static final String IMG_BASE_URL = "http://tiku.huatu.com/cdn/images/vhuatu/interview/arrangement/";

//    private static final ErrorResult UPLOAD_FILE_FAIL = ErrorResult.create(1315000, "上传文件失败");


    /**
     * ftp上传文件
     *
     * @param file     file对象
     * @param fileName 文件名称
     * @param savePath 保存路径
     * @return
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
            logger.error("ex", e);
            throw new ReqException(ResultEnum.UPLOAD_FILE_FAILED);
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


    /**
     * 上传文件
     *
     * @param file
     * @param fileName
     * @param savePath
     */
    public void ftpUploadFile(File file, String fileName, String savePath) {

        logger.info("===上传文件开始===");
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork = ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.storeFile(fileName, new FileInputStream(file));

            logger.info("" + makeDir + changeWork + storeFile);
            logger.info("===上传文件结束===");
        } catch (IOException e) {
            logger.error("ex", e);
            throw new ReqException(ResultEnum.UPLOAD_FILE_FAILED);
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
    public Boolean ftpUploadFileInputStream(InputStream fileInput, String fileName, String savePath) {

        logger.info("===上传文件开始===");
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //目录不存在，创建目录
            boolean makeDir = ftpClient.makeDirectory(savePath);
            //切换工作目录
            boolean changeWork = ftpClient.changeWorkingDirectory(savePath);
            //ftp上传服务器
            boolean storeFile = ftpClient.storeFile(fileName, fileInput);

            logger.info("" + makeDir + changeWork + storeFile);
            logger.info("===上传文件结束===");
        } catch (IOException e) {
            logger.error("ex", e);
//            throw new ReqException(UPLOAD_FILE_FAIL);
            return false;
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
        return true;
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
     * 也是上传图片，但是是课程安排
     *
     * @param
     * @return
     */
    public String ftpUploadArrangement(MultipartFile multipartFile) throws IOException {
        String[] split = multipartFile.getContentType().split("/");
        String fileName = UUID.randomUUID().toString() + "."+split[1];
        ftpUploadFileInputStream(multipartFile.getInputStream(), fileName, CourseConstant.IMG_FILE_BASE_BATH.getMessage());
        String url = CourseConstant.IMG_BASE_URL.getMessage() + fileName;
        return url;
    }


    /**
     * 【功能描述：删除文件夹】
     * 【功能详细描述：功能详细描述】
     *
     * @param ftpClient
     * @param ftpPath   文件夹的地址
     * @return true 表似成功，false 失败
     * @throws IOException
     * @see 【类、类#方法、类#成员】
     */
    public static boolean iterateDelete(FTPClient ftpClient, String ftpPath) throws IOException {
        FTPFile[] files = ftpClient.listFiles(ftpPath);
        boolean flag = false;
        for (FTPFile f : files) {
            String path = ftpPath + File.separator + f.getName();
            if (f.isFile()) {
                // 是文件就删除文件
                ftpClient.deleteFile(path);
            } else if (f.isDirectory()) {
                iterateDelete(ftpClient, path);
            }
        }
        // 每次删除文件夹以后就去查看该文件夹下面是否还有文件，没有就删除该空文件夹
        FTPFile[] files2 = ftpClient.listFiles(ftpPath);
        if (files2.length == 0) {
            flag = ftpClient.removeDirectory(ftpPath);
        } else {
            flag = false;
        }
        return flag;
    }

    /**
     * 【功能描述：删除文件】
     * 【功能详细描述：功能详细描述】
     *
     * @param filePath
     * @return
     * @see 【类、类#方法、类#成员】
     */
    public boolean deleteFtpFile(String filePath) {
        boolean flag = false;
        try {
            final FTPClient ftpClient = ftpClientPool.getFTPClient();
            flag = ftpClient.deleteFile(filePath);
        } catch (IOException e) {
            // TODO 异常处理块
            e.printStackTrace();
        }
        return flag;

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
                } catch (ReqException e) {
                    throw new ReqException(ResultEnum.UPLOAD_FILE_FAILED);
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
            logger.error("ex", e);
            throw new ReqException(ResultEnum.UPLOAD_FILE_FAILED);
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

    /**
     * 上传文件
     *
     * @param file     文件
     * @param filePath 路径
     * @param fileName 文件名
     * @throws Exception
     */
    public static void uploadFile(byte[] file, String filePath, String fileName) throws Exception {
        File targetFile = new File(filePath);
        System.out.println(targetFile.getPath());
        System.out.println("到这里没有");
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        FileOutputStream out = new FileOutputStream(filePath + fileName);
        out.write(file);
        out.flush();
        out.close();
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return 删除成功返回true，否则返回false
     */
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件:" + fileName + "不存在！");
            return true;
        } else {
            if (file.isFile())
                return deleteFile(fileName);
            else
                return deleteDirectory(fileName);
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除文件：" + fileName + "不存在！");
            return true;
        }
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir 要删除的目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = FileUtil.deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
            // 删除子目录
            else if (files[i].isDirectory()) {
                flag = FileUtil.deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除目录失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除目录" + dir + "成功！");
            return true;
        } else {
            return false;
        }
    }

}
