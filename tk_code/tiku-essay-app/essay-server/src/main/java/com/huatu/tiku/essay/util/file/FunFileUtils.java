package com.huatu.tiku.essay.util.file;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 上传文件相关路径
 */
public class FunFileUtils {
    public static final String TMP_WORD_SOURCE_FILEPATH = "/tmp/word/";
    //试卷word文件路径
    public static final String WORD_FILE_SAVE_PATH = "/var/www/cdn/paper/word/";

    public static final String WORD_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/word/";

    //pdf临时存储路径
    public static final String TMP_PDF_SOURCE_FILEPATH = "/tmp/pdf/";
    //申论pdf临时存储地址
    public static final String TMP_ESSAY_PDF = "/app/logs/essay-server/pdf/essay.pdf";
    //申论pdf临时存储地址
    public static final String TMP_ESSAY_PDF_SOURCE_FILEPATH = "/app/logs/essay-server/pdf/";
    //申论图片
    public static final String ESSAY_PDF_PICTURE_FILEPATH = "/app/logs/essay-server/pdf/logo.png";
    //申论资料图片
    public static final String ESSAY_PDF_PICTURE_DATA = "/app/logs/essay-server/pdf/";


    // 试卷的PDF存储路径
    public static final String PDF_FILE_SAVE_PATH = "/var/www/cdn/paper/pdf/";
    // 申论的PDF存储路径
    public static final String ESSAY_FILE_SAVE_PATH = "/var/www/cdn/essay/pdf/";

    // 申论的照片存储路径
    public static final String PICTURE_SAVE_PATH = "/var/www/cdn/essay/answer/";
    //答题照片下载路径
    public static final String PICTURE_SAVE_URL = "http://tiku.huatu.com/cdn/essay/answer/";

    // 拍照识别照片存储路径
    public static final String PHOTO_ANSWER_PICTURE_SAVE_PATH = "/var/www/cdn/essay/photo/answer/";
    // 答题照片下载路径
    public static final String PHOTO_ANSWER__SAVE_URL = "http://tiku.huatu.com/cdn/essay/photo/answer/";


    //pdf文件下载路径
    public static final String PDF_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/pdf/";

    //申论下载路径
    public static final String PDF_ESSAY_URL = "http://tiku.huatu.com/cdn/essay/pdf/";

    /*public static final String TMP_WORD_SOURCE_FILEPATH="d:/tmp/word/";
    //试卷word文件路径
    public static final String WORD_FILE_SAVE_PATH="/var/www/cdn/paper/word/";

    public static final String WORD_FILE_SAVE_URL="http://tiku.huatu.com/cdn/paper/word/";

    public static final String TMP_PDF_SOURCE_FILEPATH="d:/tmp/pdf/";

    public static final String PDF_FILE_SAVE_PATH="/var/www/cdn/paper/pdf/";
    //pdf文件下载路径
    public static final String PDF_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/pdf/";*/


    // 申论的人工批改批注图片存储路径
    public static final String MANUAL_CORRECT_SAVE_PATH = "/var/www/cdn/essay/correct/";
    //申论的人工批改批注图片下载路径
    public static final String MANUAL_CORRECT_SAVE_URL = "http://tiku.huatu.com/cdn/essay/correct/";

    // 申论的名师之声存储路径
    public static final String AUDIO_CORRECT_SAVE_PATH = "/var/www/cdn/essay/audio/";
    //申论的名师之声下载路径
    public static final String AUDIO_CORRECT_SAVE_URL = "http://tiku.huatu.com/cdn/essay/audio/";
    
    //申论pdf封面下面app图片地址
    public static final String ESSAY_PDF_COVER_DOWNLOAD_URL = "http://tiku.huatu.com/cdn/essay/common/downloadAPP.png";
    //封底广告图片地址
    public static final String ESSAY_PDF_COVER_ADVERT_URL = "http://tiku.huatu.com/cdn/essay/common/advert.png";


    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到
     * zipFilePath路径下
     *
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
            File zipFile = new File(TMP_WORD_SOURCE_FILEPATH + fileName + ".zip");
            if (zipFile.exists()) {
                zipFile.delete();
            }
            File[] sourceFiles = sourceFile.listFiles();
            if (null == sourceFiles || sourceFiles.length < 1) {
                throw new Exception("待压缩的文件目录：" + TMP_WORD_SOURCE_FILEPATH + "里 面不存在文件，无需压缩.");
            } else {
                fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                byte[] bufs = new byte[1024 * 10];
                for (int i = 0; i < sourceFiles.length; i++) {
                    //创建ZIP实体，并添加进压缩包
                    ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                    zos.putNextEntry(zipEntry);
                    //读取待压缩的文件并写进压缩包里
                    fis = new FileInputStream(sourceFiles[i]);
                    bis = new BufferedInputStream(fis, 1024 * 10);
                    int read = 0;
                    while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                        zos.write(bufs, 0, read);
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
        } finally {
            //关闭流
            try {
                if (null != bis) bis.close();
                if (null != zos) zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        ;
        return flag;
    }

    public static boolean unzipFile(String fileName, int type, List<String> fileNameLists) throws Exception {
        boolean flag = false;
        File sourceFile = new File(TMP_WORD_SOURCE_FILEPATH);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            File zipFile = new File(TMP_WORD_SOURCE_FILEPATH + fileName + ".zip");
            if (zipFile.exists()) {
                zipFile.delete();
            }
            File[] sourceFiles = sourceFile.listFiles();
            if (null == sourceFiles || sourceFiles.length < 1) {
                throw new Exception("待压缩的文件目录：" + TMP_WORD_SOURCE_FILEPATH + "里 面不存在文件，无需压缩.");
            } else {
                fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                byte[] bufs = new byte[1024 * 10];
                for (int i = 0; i < sourceFiles.length; i++) {
                    //创建ZIP实体，并添加进压缩包
                    if (type == 1) {
                        String name = sourceFiles[i].getName().substring(0, sourceFiles[i].getName().length() - 4);
                        if (fileNameLists.contains(name)) {
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                            zos.putNextEntry(zipEntry);
                        } else {
                            continue;
                        }
                    } else {
                        if (sourceFiles[i].getName().length() > 6) {
                            String name = sourceFiles[i].getName().substring(0, sourceFiles[i].getName().length() - 6);
                            if (fileNameLists.contains(name)) {
                                ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                                zos.putNextEntry(zipEntry);
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                    //读取待压缩的文件并写进压缩包里
                    fis = new FileInputStream(sourceFiles[i]);
                    bis = new BufferedInputStream(fis, 1024 * 10);
                    int read = 0;
                    while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                        zos.write(bufs, 0, read);
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
        } finally {
            //关闭流
            try {
                if (null != bis) bis.close();
                if (null != zos) zos.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        ;
        return flag;
    }

    /**
     * 本地文件进行下载
     *
     * @param name
     * @param fileUrl
     * @param response
     * @throws Exception
     */
    public static void downLoadlocalFile(String name, String fileUrl, HttpServletResponse response) throws Exception {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            File file = new File(fileUrl);
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/x-msdownload");
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(name, "UTF-8"));
            bis = new BufferedInputStream(new FileInputStream(fileUrl));
            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[1024 * 5];
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
     *
     * @param fileName 文件名称
     * @param fileUrl  url地址
     * @param response
     * @throws Exception
     */
    public static void downLoadNetFile(String fileName, String fileUrl, HttpServletResponse response) throws Exception {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            //获得文件的输出流
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] getData = readInputStream(inputStream);

            response.setCharacterEncoding("utf-8");
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));

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
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024 * 5];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
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
            for (int i = 0; i < sourceFiles.length; i++) {
                sourceFiles[i].delete();
            }
        } else {
            file.delete();
        }
        return true;
    }

    public static boolean fileExists(File file) {
        if (file.exists()) {
            return true;
        }
        return false;
    }

//    public static void main(String[] args)throws Exception{
//        List<String> fileNameLists=Lists.newArrayList();
//        fileNameLists.add("2016年深圳市公务员《行测》真题试题");
//        FunFileUtils.unzipFile("123",1,fileNameLists);
//    }

}
