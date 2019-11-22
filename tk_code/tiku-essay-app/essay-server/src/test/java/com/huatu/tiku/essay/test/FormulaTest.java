package com.huatu.tiku.essay.test;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.huatu.tiku.essay.util.file.UploadFileUtil;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.scilab.forge.jlatexmath.TeXConstants;//主要引用的几个类
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.beans.factory.annotation.Autowired;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;




/**
 * @author zhaoxi
 * @Description: 公式转换策四
 * @date 2018/12/275:40 PM
 */
@Slf4j
public class FormulaTest {

    @Autowired
    private UploadFileUtil uploadFileUtil;


    private String imgUrl = "<img src=\"%s\" width=\"%s\" height=\"%s\" style=\"width:%s;height:%s\">";

    public static void main(String[] args) throws IOException {

        LinkedList<String> latexList = new LinkedList<>();
        latexList.add("\\frac{\\sqrt{3}}{2}");
        latexList.add("\\frac{1}{2}");
        latexList.add("\\tan {{231}^{{}^\\circ }}tan {{237}^{{}^\\circ }}");

       for(String latex:latexList){
           String imgPath = UUID.randomUUID().toString().replace("-","") + ".png";
           System.out.println("imgPath:"+imgPath);
           String imgBase = latex2Png(latex);
           generateImage(imgBase, imgPath);
       }


    }


    /**
     * @Description: 将base64编码字符串转换为图片
     * @Author:
     * @CreateTime:
     * @param imgStr
     *            base64编码字符串
     * @param path
     *            图片路径-具体到文件
     * @return
     */
    public static boolean generateImage(String imgStr, String path) {
        if (imgStr == null) {
            return false;
        }
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; i++) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(path);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     *  latex 转 imgbase64
     */
    public static String latex2Png(String latex) {
        try {
            TeXFormula formula = new TeXFormula(latex);
            // render the formula to an icon of the same size as the formula.
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 100);
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();

            System.out.println("iconWidth："+iconWidth+"，iconHeight："+iconHeight);
            // insert a border
            icon.setInsets(new Insets(5, 5, 5, 5));
            // now create an actual image of the rendered equation
            BufferedImage image = new BufferedImage(iconWidth, iconHeight,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            image = g2.getDeviceConfiguration().createCompatibleImage(iconWidth, iconHeight, Transparency.TRANSLUCENT);
            g2.fillRect(0, 0, iconWidth, iconHeight);
            g2 = image.createGraphics();


            JLabel jl = new JLabel();
            jl.setForeground(new Color(0, 0, 0));
            icon.paintIcon(jl, g2, 0, 0);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", outputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            byte[] buffer = outputStream.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            return (encoder.encode(buffer)+"\"");
        } catch (Exception e) {
            System.err.println("公式解析有误：\n" + latex);
            return null;
        }

    }








}
