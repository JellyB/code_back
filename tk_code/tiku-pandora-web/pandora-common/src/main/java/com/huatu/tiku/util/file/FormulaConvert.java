package com.huatu.tiku.util.file;


import com.huatu.tiku.util.html.UrlConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.huatu.tiku.constant.BaseConstant.IMG_PATH;
import static com.huatu.tiku.constant.BaseConstant.IMG_URL;

/**
 * @创建人
 * @创建时间
 * @描述
 */
@Slf4j
public class FormulaConvert {


    private static String imgLabel = "<img src=\"%s\" width=\"%s\" height=\"%s\" style=\"width:%s;height:%s\">";
    private static Map<String, String> formulaMap = new HashMap<String, String>();

    static {

        //等于号
        String EQUAL_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/f00afb4b86e14a9ab947c7850ecf0ccf.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";
        //小于 小于等于号
        String LITTLE_THAN_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/22906b1de5ea4f32816f30ed8de2a4e3.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";
        String LITTLE_EQUAL_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/46b8aa3451a244408384961f5416974d.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";

        //+ — x /
        String PLUS_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/975c7ced4b6549d395e4f18da31650b6.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";
        String MINUS_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/8b6bdcf4f81545efa779458e9c34e8da.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";
        String MULTI_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/d4eff1b8a4094005b9ece3c4444ec9af.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";
        String SPLIT_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/629e932af5fc43019811d356fd897efe.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";

        //土
        String PEM_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/0591b1df68944bbbb0d18ff45583c818.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";

        //不等于
        String NOT_EQUAL_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/47cf476cae43469e98a60d824504605c.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";

        //大于 大于等于
        String GREATER_THAN_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/1e6a0a3786ed4482a25e694c1100cc8b.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";
        String GREATER_EQUAL_IMG = "<img src=\"http://tiku.huatu.com/cdn/pandora/img/925dee91119a4d1d8ec123c62bcda5d7.png\" width=\"15\" height=\"15\" style=\"width:15;height:15\">";

        formulaMap.put("=", EQUAL_IMG);
        formulaMap.put("<", LITTLE_THAN_IMG);
        formulaMap.put("\\le", LITTLE_EQUAL_IMG);
        formulaMap.put("+", PLUS_IMG);
        formulaMap.put("-", MINUS_IMG);
        formulaMap.put("\\pm", PEM_IMG);
        formulaMap.put("\\times", MULTI_IMG);
        formulaMap.put("\\div", SPLIT_IMG);
        formulaMap.put("\\ne", NOT_EQUAL_IMG);
        formulaMap.put(">", GREATER_THAN_IMG);
        formulaMap.put("\\ge", GREATER_EQUAL_IMG);

    }


    private static final Integer style_height_rule = 32;

    public static String dealTheImage(String urlPath) throws Exception {
        /**
         * url之后需要通过java远程访问，转化img格式
         */
        urlPath = UrlConvertUtil.convert(urlPath);
        URL url = new URL(urlPath);
//        OutputStream out = null;
        BufferedImage image = ImageIO.read(url); //读取文件
//        // 高度和宽度
//        int height = image.getHeight();
//        int width = image.getWidth();
//
//        // 生产背景透明和内容透明的图片
//        ImageIcon imageIcon = new ImageIcon(image);
//        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
//        Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics(); // 获取画笔
//        g2D.drawImage(imageIcon.getImage(), 0, 0, null); // 绘制Image的图片
//        // 绘制设置了RGB的新图片
//        g2D.drawImage(bufferedImage, 0, 0, null);
        // 生成图片为PNG
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());

        String fileName = UUID.randomUUID().toString() + ".png";
        try {
            UploadFileUtil.getInstance().ftpUploadFileInputStream(inputStream, fileName, UploadFileUtil.IMG_PATH_QUESTION);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(null != inputStream){
                inputStream.close();
            }
        }
        //上传CDN，返回Url
        String newUrl = UploadFileUtil.IMG_URL_QUESTION + fileName;
        log.info("picture url is {}:", newUrl);
        return newUrl;

    }

    // 判断是背景还是内容
    public static boolean colorInRange(int color) {
        int red = (color & 0xff0000) >> 16;// 获取color(RGB)中R位
        int green = (color & 0x00ff00) >> 8;// 获取color(RGB)中G位
        int blue = (color & 0x0000ff);// 获取color(RGB)中B位
        // 通过RGB三分量来判断当前颜色是否在指定的颜色区间内
        if (red >= color_range && green >= color_range && blue >= color_range) {
            return true;
        }
        ;
        return false;
    }

    //色差范围0~255
    public static int color_range = 210;


    public static void main(String[] args) {
//        String span2Img = "http://latex.codecogs.com/gif.latex?\\inline&space;\\bf{\\frac{\\sqrt{a^2+b^2}}{\\frac{a2^2-4ac}{2}}+\\int_0^{\\infty}}";
//        try {
//            span2Img = dealTheImage("");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("-------span2Img------"+span2Img);
        String url = "http://latex.codecogs.com/gif.latex?%3C";
        try {
            System.out.println(dealTheImage(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 表达式图片内容（span标签内容）转base64,再实现存储cdn，返回链接
     *
     * @param spanContent
     */
    public static String dealTheImage64(String spanContent) {

        //特殊符号直接返回固定地址
        String img = formulaMap.get(spanContent);
        if (StringUtils.isNotEmpty(img)) {
            return img;
        }
        return latex2ImgLabel(spanContent);
    }

    public static String latex2ImgLabel(String latex) {
        String imgPath = UUID.randomUUID().toString().replace("-", "") + ".png";
        String imageUrl = IMG_URL + imgPath;
        log.info("latex:{}", latex);
        log.info("imgPath:{}", imageUrl);

        float height;
        float width;
        ImgInfoVO formulaImgInfoVO = FormulaUtil.latex2Png(latex,200);
        if (null != formulaImgInfoVO) {
            String base = formulaImgInfoVO.getBase();

            height = formulaImgInfoVO.getHeight();
            width = formulaImgInfoVO.getWidth();

            if (HtmlFileUtil.GenerateImage(base, imgPath)) {
                File file = new File(imgPath);
                FileInputStream fileInputStream = null;
                //图片上传
                try {
                     fileInputStream = new FileInputStream(file);
                    UploadFileUtil.getInstance().ftpUploadFileInputStream(fileInputStream, imgPath, IMG_PATH);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    if(null!=fileInputStream){
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(null != file && file.exists()){
                        //删除本地文件
                        file.delete();
                    }
                }

            }

            //图片尺寸压缩
            width = width * 0.075f;
            height = height * 0.075f;

            String format = String.format(imgLabel, imageUrl, width, height, width, height);
            return format;
        }
        return "";
    }
}
