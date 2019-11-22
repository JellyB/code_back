package com.huatu.ztk.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by ht on 2017/3/10.
 */
public class FunFileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FunFileUtils.class);
    public static final String TMP_EXCEL_SOURCE_FILEPATH="/tmp/excel/";
    public static final String TMP_MAIL_SOURCE_FILEPATH="/tmp/mail/";
    public static final String TMP_WORD_SOURCE_FILEPATH="/tmp/word/";
    //试卷word文件路径
    public static final String WORD_FILE_SAVE_PATH="/var/www/cdn/paper/word/";

    public static final String WORD_FILE_SAVE_URL="http://tiku.huatu.com/cdn/paper/word/";

    public static final String TMP_PDF_SOURCE_FILEPATH="/tmp/pdf/";

    public static final String PDF_FILE_SAVE_PATH="/var/www/cdn/paper/pdf/";
    //pdf文件下载路径
    public static final String PDF_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/pdf/";
//    public static final String TMP_WORD_SOURCE_FILEPATH="d:/tmp/word/";
//    //试卷word文件路径
//    public static final String WORD_FILE_SAVE_PATH="/var/www/cdn/paper/word/";
//
//    public static final String WORD_FILE_SAVE_URL="http://tiku.huatu.com/cdn/paper/word/";
//
//    public static final String TMP_PDF_SOURCE_FILEPATH="d:/tmp/pdf/";
//    public static final String TMP_EXCEL_SOURCE_FILEPATH="d:/tmp/excel/";
//
//    public static final String PDF_FILE_SAVE_PATH="/var/www/cdn/paper/pdf/";
//    //pdf文件下载路径
//    public static final String PDF_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/pdf/";
//    public static final String TMP_MAIL_SOURCE_FILEPATH = "d:/tmp/mail/";

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到
     zipFilePath路径下
     * @param fileName :压缩后文件的名称
     * @return
     */
    public static boolean fileToZip(String fileName) throws Exception {
        boolean flag = false;
        File sourceFile = new File(TMP_WORD_SOURCE_FILEPATH);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            File zipFile = new File(TMP_WORD_SOURCE_FILEPATH+ fileName+".zip");
            if(zipFile.exists()){
                zipFile.delete();
            }
            File[] sourceFiles = sourceFile.listFiles();
            if(null == sourceFiles || sourceFiles.length<1){
                throw new Exception("待压缩的文件目录：" + TMP_WORD_SOURCE_FILEPATH + "里 面不存在文件，无需压缩.");
            }else{
                fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                byte[] bufs = new byte[1024*10];
                for(int i=0;i<sourceFiles.length;i++){
                    //创建ZIP实体，并添加进压缩包
                    ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                    zos.putNextEntry(zipEntry);
                    //读取待压缩的文件并写进压缩包里
                    fis = new FileInputStream(sourceFiles[i]);
                    bis = new BufferedInputStream(fis, 1024*10);
                    int read = 0;
                    while((read=bis.read(bufs, 0, 1024*10)) != -1){
                        zos.write(bufs,0,read);
                    }
                }
                flag = true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally{
            //关闭流
            try {
                if(null != bis) bis.close();
                if(null != zos) zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
        return flag;
    }
    public static boolean unzipFile(String fileName,int type,List<String> fileNameLists) throws Exception {
        boolean flag = false;
        File sourceFile = new File(TMP_WORD_SOURCE_FILEPATH);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            File zipFile = new File(TMP_WORD_SOURCE_FILEPATH+ fileName+".zip");
            if(zipFile.exists()){
                zipFile.delete();
            }
            File[] sourceFiles = sourceFile.listFiles();
            if(null == sourceFiles || sourceFiles.length<1){
                throw new Exception("待压缩的文件目录：" + TMP_WORD_SOURCE_FILEPATH + "里 面不存在文件，无需压缩.");
            }else{
                fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                byte[] bufs = new byte[1024*10];
                for(int i=0;i<sourceFiles.length;i++){
                    //创建ZIP实体，并添加进压缩包
                     if(type==1){
                         String name=sourceFiles[i].getName().substring(0,sourceFiles[i].getName().length()-4);
//                         logger.info("文件夹下的文件名：{}",name);
                         if(fileNameLists.contains(name)){
                             ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                             zos.putNextEntry(zipEntry);
                         }else{
                             continue;
                         }
                     }else{
                         if(sourceFiles[i].getName().length()>6){
                             String name= sourceFiles[i].getName().substring(0,sourceFiles[i].getName().length()-6);
                             if(fileNameLists.contains(name)){
                                 ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                                 zos.putNextEntry(zipEntry);
                             }else {continue;}
                         }
                         else {continue;}
                     }
                    //读取待压缩的文件并写进压缩包里
                    fis = new FileInputStream(sourceFiles[i]);
                    bis = new BufferedInputStream(fis, 1024*10);
                    int read = 0;
                    while((read=bis.read(bufs, 0, 1024*10)) != -1){
                        zos.write(bufs,0,read);
                    }
                }
                flag = true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally{
            //关闭流
            try {
                if(null != bis) bis.close();
                if(null != zos) zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
        return flag;
    }

    /**
     * 本地文件进行下载
     * @param name
     * @param fileUrl
     * @param response
     * @throws Exception
     */
    public static void downLoadlocalFile(String name, String fileUrl, HttpServletResponse response) throws Exception{
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File file=new File(fileUrl);
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/x-msdownload");
            response.setContentLength((int)file.length());
            response.setHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(name,"UTF-8"));
            bis = new BufferedInputStream(new FileInputStream(fileUrl));
            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[1024*5];
            int bytesRead;

            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    /**
     * 从网络服务器下载文件
     * @param fileName 文件名称
     * @param fileUrl url地址
     * @param response
     * @throws Exception
     */
    public static void downLoadNetFile(String fileName, String fileUrl,HttpServletResponse response) throws Exception{
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            //获得文件的输出流
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3*1000);
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] getData = readInputStream(inputStream);

            response.setCharacterEncoding("utf-8");
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "inline;filename=" +URLEncoder.encode(fileName,"UTF-8"));

            bis = new BufferedInputStream(conn.getInputStream());
            bos = new BufferedOutputStream(response.getOutputStream());
            bos.write(getData);
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    /**
     * 从输入流中获取字节数组
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024*5];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] sourceFiles = file.listFiles();
            //递归删除目录中的子目录下
            for (int i=0; i<sourceFiles.length; i++) {
                 sourceFiles[i].delete();
            }
        }else{
            file.delete();
        }
        return true;
    }

    public static boolean fileExists(File file){
        if(file.exists()){
           return true;
        }
        return false;
    }

    public static void main(String[] args)throws Exception{
//        List<String> fileNameLists=Lists.newArrayList();
//        fileNameLists.add("2016年深圳市公务员《行测》真题试题");
//        FunFileUtils.unzipFile("123",1,fileNameLists);
        int screenWidth=((int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().width);
        int screenHeight = ((int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().height);
        System.out.println(screenWidth+""+screenHeight);
    }

    /**
     * 获取内容中单引号或双引号的区间的值
     * @param content
     * @return
     */
    public static String getQuoteContent(String content) {
        int compIndex = content.indexOf("\"");
        int singleIndex = content.indexOf("'");
        int startIndex = -1;
        int endIndex =-1;
        if(compIndex==-1&&singleIndex==-1){
            return "";
        }else if(compIndex==-1){
            endIndex = content.substring(singleIndex+1).indexOf("'");
            startIndex = singleIndex;
        }else if(singleIndex==-1) {
            endIndex = content.substring(compIndex+1).indexOf("\"");
            startIndex = compIndex;
        }else {
            if (singleIndex < compIndex) {
                endIndex = content.substring(singleIndex+1).indexOf("'");
                startIndex = singleIndex;
            } else {
                endIndex = content.substring(compIndex+1).indexOf("\"");
                startIndex = compIndex;
            }
        }
        return content.substring(startIndex+1,startIndex+endIndex+1);
    }
}
