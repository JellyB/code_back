package com.huatu.ztk.user.utils;

import com.huatu.ztk.user.common.AvatarFileType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

/**
 *  通过魔数判断文件类型
 * Created by linkang on 9/12/16.
 */
public class FileTypeUtils {

    public static final int IMAGE_TYPE_BYTE_LEN = 28;

    /**
     * 读取文件头
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static String getFileHeader(BufferedInputStream inputStream) throws IOException {
        byte[] b = new byte[IMAGE_TYPE_BYTE_LEN];
        inputStream.mark(IMAGE_TYPE_BYTE_LEN);
        inputStream.read(b, 0, IMAGE_TYPE_BYTE_LEN);
        inputStream.reset();
        return bytes2Hex(b);
    }

    /**
     * byte数组转hex字符串
     * @param src
     * @return
     */
    private static String bytes2Hex(byte[] src){
        char[] res = new char[src.length*2];
        final char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        for(int i=0,j=0; i<src.length; i++){
            res[j++] = hexDigits[src[i] >>>4 & 0x0f];
            res[j++] = hexDigits[src[i] & 0x0f];
        }

        return new String(res);
    }

    /**
     * 判断文件类型
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static AvatarFileType getType(BufferedInputStream inputStream) throws IOException {
        String fileHead = getFileHeader(inputStream);

        if (StringUtils.isBlank(fileHead)) {
            return null;
        }

        fileHead = fileHead.toUpperCase();
        AvatarFileType[] avatarFileTypes = AvatarFileType.values();
        for (AvatarFileType avatarFileType : avatarFileTypes) {
            if (fileHead.startsWith(avatarFileType.getValue())) {
                return avatarFileType;
            }
        }

        return null;
    }
}
