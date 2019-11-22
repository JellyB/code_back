package com.huatu.tiku.util.file;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具
 * Created by ht on 2017/3/10.
 */
@Slf4j
public class FunFileUtils {
    //服务器存储excel文档，邮件和word文档的路径 + pdf
    public static final String TMP_EXCEL_SOURCE_FILEPATH = "/tmp/excel/";
    public static final String TMP_MAIL_SOURCE_FILEPATH = "/tmp/mail/";
    public static final String TMP_WORD_SOURCE_FILEPATH = "/tmp/word/";
    public static final String TMP_PDF_SOURCE_FILEPATH = "/tmp/pdf/";
    //public static final String TMP_PDF_SOURCE_FILEPATH = "/Users/lizhenjuan/tool/";
    //试卷word文件路径(cdn文件路径)
    public static final String WORD_FILE_SAVE_PATH = "/var/www/cdn/paper/word/";

    public static final String PDF_FILE_SAVE_PATH = "/var/www/cdn/paper/pdf/";
    public static final String EXCEL_FILE_SAVE_PATH = "/var/www/cdn/paper/excel/";
    //cdn外网访问地址（试卷word文件）
    public static final String WORD_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/word/";

    //pdf文件下载路径
    public static final String PDF_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/pdf/";
    public static final String EXCEL_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/excel/";
//    public static final String TMP_WORD_SOURCE_FILEPATH = "d:/tmp/word/";
    //    //试卷word文件路径
//    public static final String WORD_FILE_SAVE_PATH = "/var/www/cdn/paper/word/";
//
//    public static final String WORD_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/word/";
//
//    public static final String TMP_PDF_SOURCE_FILEPATH = "d:/tmp/pdf/";
//    public static final String TMP_EXCEL_SOURCE_FILEPATH = "d:/tmp/excel/";
//
//    public static final String PDF_FILE_SAVE_PATH = "/var/www/cdn/paper/pdf/";
//    //pdf文件下载路径
//    public static final String PDF_FILE_SAVE_URL = "http://tiku.huatu.com/cdn/paper/pdf/";
//    public static final String TMP_MAIL_SOURCE_FILEPATH = "d:/tmp/mail/";

    /**
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
     * sourceFilePath 下的所有文件都是需要打包的
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

    /**
     * 压缩特定文件
     *
     * @param fileName      压缩后的文件名称（不带后缀）
     * @param fileNameLists 压缩对象文件的文件名
     * @param tailName
     * @return
     * @throws Exception
     */
    public static boolean zipFile(String fileName, List<String> fileNameLists, String tailName, String dirPath) throws Exception {
        boolean flag = false;
        File sourceFile = new File(dirPath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            File zipFile = new File(dirPath + fileName + ".zip");
            if (zipFile.exists()) {
                zipFile.delete();
            }
            File[] sourceFiles = sourceFile.listFiles();
            if (null == sourceFiles || sourceFiles.length < 1) {
                throw new Exception("待压缩的文件目录：" + dirPath + "里 面不存在文件，无需压缩.");
            } else {
                fos = new FileOutputStream(zipFile);
                zos = new ZipOutputStream(new BufferedOutputStream(fos));
                byte[] bufs = new byte[1024 * 10];
                for (int i = 0; i < sourceFiles.length; i++) {
                    //创建ZIP实体，并添加进压缩包
                    String sourceName = sourceFiles[i].getName();
                    String name = sourceName.replace(tailName, "");
                    if (fileNameLists.contains(name)) {
                        ZipEntry zipEntry = new ZipEntry(sourceName);
                        zos.putNextEntry(zipEntry);
                    } else {
                        continue;
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

    public static void main(String[] args) throws Exception {
        int screenWidth = ((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().width);
        int screenHeight = ((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().height);
        System.out.println(screenWidth + "" + screenHeight);
    }

    /**
     * 获取内容中单引号或双引号的区间的值
     *
     * @param content
     * @return
     */
    public static String getQuoteContent(String content) {
        int compIndex = content.indexOf("\"");
        int singleIndex = content.indexOf("'");
        int startIndex = -1;
        int endIndex = -1;
        if (compIndex == -1 && singleIndex == -1) {
            return "";
        } else if (compIndex == -1) {
            endIndex = content.substring(singleIndex + 1).indexOf("'");
            startIndex = singleIndex;
        } else if (singleIndex == -1) {
            endIndex = content.substring(compIndex + 1).indexOf("\"");
            startIndex = compIndex;
        } else {
            if (singleIndex < compIndex) {
                endIndex = content.substring(singleIndex + 1).indexOf("'");
                startIndex = singleIndex;
            } else {
                endIndex = content.substring(compIndex + 1).indexOf("\"");
                startIndex = compIndex;
            }
        }
        if (startIndex > -1 && endIndex > -1) {
            return content.substring(startIndex + 1, startIndex + endIndex + 1);
        }
        System.out.println("getQuoteContent error ,content = " + content);
        return "";
    }
}
