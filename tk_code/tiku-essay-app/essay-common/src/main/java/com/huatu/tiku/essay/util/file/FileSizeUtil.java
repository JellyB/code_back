package com.huatu.tiku.essay.util.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Created by x6 on 2017/12/22.
 * 文件大小
 */
@Slf4j
public class FileSizeUtil {
    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    public static String getFileSize(String filePath) {
        File file = new File(filePath);
        long length = file.length();
        double fileSize = length / (double) 1024;
        if (fileSize < 1024) {
            String fileSizeStr = String.format("%.2f", fileSize);
            log.info("文件大小："+fileSizeStr + "KB");
            return fileSizeStr + "KB";
        } else {
            String fileSizeStr = String.format("%.2f", fileSize / 1024);
            log.info("文件大小："+ fileSizeStr + "MB");
            return fileSizeStr + "MB";
        }
    }
}
