package com.huatu.tiku.util.file;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Cleanup;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.UUID;

/**
 * Created by lijun on 2018/11/5
 */
public class CourseQCode {

    private final Cache<Long, String> PAPER_ID_CODE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build();

    public static CourseQCode getInstance() {
        return InnerInstance.INSTANCE;
    }

    private static class InnerInstance {
        public static final CourseQCode INSTANCE = new CourseQCode();
    }

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    //默认宽度
    private static final int DEFAULT_WIDTH = 300;
    //默认高度
    private static final int DEFAULT_HEIGHT = 300;
    //默认二维码格式
    private static final String FORMAT = "png";

    //生成可以扫码回答的二维码
    public static final String ENABLE_ANSWER_PAPER_URL = " exercise:{\"paperId\":\"%s\"}";
    public static final String ENABLE_ANSWER_CARD_URL = " exercise:{\"answerId\":\"%s\"}";

    /**
     * 生成二维码并上传至 FTP 服务器
     *
     * @param paperId 课程ID
     * @return 图片路径
     */
    public String uploadQCodeImgAndReturnPath(Long paperId) throws IOException, WriterException {
        return uploadQCodeImgAndReturnPath(paperId,ENABLE_ANSWER_PAPER_URL);
    }
    public String uploadQCodeImgAndReturnPath(Long paperId,String formatStr) throws IOException, WriterException {
        String qCodePath;
        if (StringUtils.isNotBlank(qCodePath = PAPER_ID_CODE.getIfPresent(paperId))) {
            return qCodePath;
        }
        qCodePath = uploadQCodeImgAndReturnPath(DEFAULT_WIDTH, DEFAULT_HEIGHT, FORMAT, String.format(formatStr, paperId));
        PAPER_ID_CODE.put(paperId, qCodePath);
        return PAPER_ID_CODE.getIfPresent(paperId);
    }
    /**
     * 生成二维码并上传至 FTP 服务器
     */
    public String uploadQCodeImgAndReturnPath(Integer width, Integer height, String format, String text) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, getDefaultHit());
        //生成二维码
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        for (int widthIndex = 0; widthIndex < width; widthIndex++) {
            for (int heightIndex = 0; heightIndex < height; heightIndex++) {
                image.setRGB(widthIndex, heightIndex, bitMatrix.get(widthIndex, heightIndex) ? BLACK : WHITE);
            }
        }
        @Cleanup
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        try (InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
            String fileName = UUID.randomUUID().toString() + "." + format;
            try {
                UploadFileUtil.getInstance().ftpUploadFileInputStream(inputStream, fileName, UploadFileUtil.IMG_PATH_QUESTION);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
            return UploadFileUtil.IMG_URL_QUESTION + fileName;
        }
    }

    /**
     * 获取默认属性
     */
    private static Hashtable<EncodeHintType, Object> getDefaultHit() {
        Hashtable<EncodeHintType, Object> hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");//编码格式
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);//设置容错等级
        hints.put(EncodeHintType.MARGIN, 2);//设置边距 默认2
        return hints;
    }

}
