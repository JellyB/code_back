package com.huatu.tiku.util.file;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author zhaoxi
 * @Description: 工具处理类
 * @date 2019/1/35:41 PM
 */
public class FormulaUtilTest {


    private String imgUrl = "<img src=\"%s\" width=\"%s\" height=\"%s\" style=\"width:%s;height:%s\">";

    public static void main(String[] args) throws IOException {
//
//        String latex = ("\\frac{\\sqrt{3}}{2}");
//
//        String imgPath = UUID.randomUUID().toString().replace("-","") + ".png";
//        System.out.println("imgPath:"+imgPath);
//        FormulaImgBaseVO formulaImgBaseVO = latex2Png(latex);
//        if (GenerateImage(formulaImgBaseVO.getImgBase(), imgPath)) {
//            File file = new File(imgPath);
//            log.info("file的绝对地址：", file.getCanonicalPath());
//            int width = result.get(i).getWidth();
//            int height = result.get(i).getHeight();
//            if (width == -1 && height == -1) {
//                BufferedImage bufferedImage = ImageIO.read(new File(imgPath));
//                width = bufferedImage.getWidth();
//                height = bufferedImage.getHeight();
//            }
////                final String imageUrl = "";
//            final String imageUrl = UploadFileUtil.getInstance().ftpUploadPic(file).replaceAll("\\\\\"", "");
////                ;//上传ftp服务器
//            //文件上传成功后，删除本地文件
//            file.delete();
//        }


    }


    /**
     * latex 转 imgbase64
     */
    public static ImgInfoVO latex2Png(String latex) {
        try {
            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 200);
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();


            System.out.println("iconWidth：" + iconWidth + "，iconHeight：" + iconHeight);
            // insert a border
            icon.setInsets(new Insets(0, 0, 0, 0));
            // now create an actual image of the rendered equation
            BufferedImage image = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font[] allFonts = localGraphicsEnvironment.getAllFonts();
            System.out.println("allFonts = " + Arrays.stream(allFonts).map(Font::getName).collect(Collectors.joining(",")));
            image = g2.getDeviceConfiguration().createCompatibleImage(iconWidth, iconHeight, Transparency.TRANSLUCENT);
            g2.fillRect(0, 0, iconWidth, iconHeight);
            g2 = image.createGraphics();
            g2.setFont(createFont());
            System.out.println("font name = " + g2.getFont().getFontName());

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
            ImgInfoVO imgBaseVO = ImgInfoVO.builder()
                    .base(encoder.encode(buffer) + "\"")
                    .height(iconHeight)
                    .width(iconWidth)
                    .build();

            return imgBaseVO;
        } catch (Exception e) {
            System.err.println("公式解析有误：\n" + latex);
            return null;
        }

    }

    public static Font createFont() throws IOException, FontFormatException {
        File file = new File("C:/WINDOWS/Fonts/simsun.ttf");
        Font font = null;
        if (file.exists()) {
            font = Font.createFont(Font.TRUETYPE_FONT, file);
        } else {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("/usr/share/fonts/simsun.ttf"));
        }
        System.out.println("font.getName() = " + font.getName());
        System.out.println("font.getFontName() = " + font.getFontName());
        return font;
    }
}
