package com.huatu.tiku.teacher.util.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 组合图片生成水印图片的工具
 */
public class NewImageUtils {
    /**
     * 
     * @Title: 构造图片
     * @Description: 生成水印并返回java.awt.image.BufferedImage
     * @param file
     *            源文件(图片)
     * @param waterFile
     *            水印文件(图片)
     * @param x
     *            距离右下角的X偏移量
     * @param y
     *            距离右下角的Y偏移量
     * @param alpha
     *            透明度, 选择值从0.0~1.0: 完全透明~完全不透明
     * @return BufferedImage
     * @throws IOException
     */
    public static BufferedImage watermark(File file, File waterFile, int x, int y, float alpha) throws IOException {
        // 获取底图
        BufferedImage buffImg = ImageIO.read(file);
        // 获取层图
        BufferedImage waterImg = ImageIO.read(waterFile);
        return watermark(buffImg,waterImg,x,y,alpha);
    }

    public static BufferedImage watermark(BufferedImage buffImg,BufferedImage waterImg,int x,int y ,float alpha) throws IOException{
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        int waterImgWidth = waterImg.getWidth();// 获取层图的宽度
        int waterImgHeight = waterImg.getHeight();// 获取层图的高度
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        g2d.drawImage(waterImg, x, y, waterImgWidth, waterImgHeight, null);
        g2d.dispose();// 释放图形上下文使用的系统资源
        return buffImg;
    }

    /**
     *
     * @param buffImg
     * @param waterImg
     * @param x
     * @param y
     * @param alpha
     * @param widthSize   设定水印图的宽度
     * @param heightSize  设定水印图的高度
     * @return
     * @throws IOException
     */
    public static BufferedImage watermark(BufferedImage buffImg,BufferedImage waterImg,int x,int y ,float alpha,int widthSize,int heightSize) throws IOException{
        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = buffImg.createGraphics();
        // 在图形和图像中实现混合和透明效果
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        // 绘制
        g2d.drawImage(waterImg, x, y, widthSize, heightSize, null);
        g2d.dispose();// 释放图形上下文使用的系统资源
        return buffImg;
    }
    /**
     * 输出水印图片
     * 
     * @param buffImg
     *            图像加水印之后的BufferedImage对象
     * @param savePath
     *            图像加水印之后的保存路径
     */
    public static void generateWaterFile(BufferedImage buffImg, String savePath) {
        int temp = savePath.lastIndexOf(".") + 1;
        try {
            ImageIO.write(buffImg, savePath.substring(temp), new File(savePath));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * 
     * @param args
     * @throws IOException
     *             IO异常直接抛出了
     * @author bls
     */
    public static void main(String[] args) throws IOException {
//        String sourceFilePath = "D://img//QcodeMode.png";
        String sourceFilePath = "D://img//answer-qcode-back-ground.png";
        String waterFilePath = "D://img//ceng.png";
        File file = new File(sourceFilePath);
        System.out.println("file.exists() = " + file.exists());
        BufferedImage bufferedImage = ImageIO.read(file);
        System.out.println("bufferedImage.getHeight() = " + bufferedImage.getHeight());
        System.out.println("bufferedImage.getWidth() = " + bufferedImage.getWidth());
        BufferedImage waterImage = ImageIO.read(new URL("http://tiku.huatu.com/cdn/pandora/img/question/68ee195c-7c68-4172-8790-a47e2a1be510.png"));
        System.out.println("waterImage.getHeight() = " + waterImage.getHeight());
        System.out.println("waterImage.getWidth() = " + waterImage.getWidth());
        String saveFilePath = "D://img//new.png";
        NewImageUtils newImageUtils = new NewImageUtils();
        // 构建叠加层
        BufferedImage buffImg = NewImageUtils.watermark(bufferedImage, waterImage, 90, 60, 1.0f,250,250);

        // 输出水印图片
        newImageUtils.generateWaterFile(buffImg, saveFilePath);

    }
}