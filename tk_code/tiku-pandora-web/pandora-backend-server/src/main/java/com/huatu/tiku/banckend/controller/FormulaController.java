package com.huatu.tiku.banckend.controller;

import com.google.common.base.Stopwatch;
import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.teacher.service.common.FileService;
import com.huatu.tiku.util.file.*;
import com.huatu.tiku.util.log.LogPrint;
import javax.imageio.ImageIO;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;


/**
 * @author zhaoxi
 * @Description: 中文公式转换
 * @date 2019/1/2410:44 AM
 */
@Slf4j
@RestController
@RequestMapping(value = "/backend/formula")
public class FormulaController {

    @Autowired
    FileService fileService;
    @Autowired
    HtmlFileUtil htmlFileUtil;


//    /**
//     * 将公式转换成文件VO
//     *
//     * @param content
//     * @return
//     */
//    @RequestMapping(value = "/vo", method = RequestMethod.GET)
//    public ImgInfoVO getFormulaPicVO(@RequestParam String content, HttpServletRequest request, HttpServletResponse response) {
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        ImgInfoVO imgInfoVO = FormulaUtil.latex2Png(content, 20);
//        log.info("=======将公式转换成文件VO，接口用时=====" + String.valueOf(stopwatch.stop()));
//
//        return imgInfoVO;
//    }

    /**
     * 富文本编辑器内容保存
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "")
    public Object upload(@RequestBody FormulaImgInfoVO vo) {
        String imgLabel = fileService.latex2ImgLabel(vo.getLatex(), vo.getHeight(), vo.getWidth());
        HashMap<String, String> map = new HashMap<>();
        map.put("imgLabel", imgLabel);
        return map;
    }


    /**
     * 将公式转换成文件流
     *
     * @return
     */
    @RequestMapping(value = "/file", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public void getFormulaPic(@RequestParam String content,
                              @RequestParam(defaultValue = "200") int flag,
                              HttpServletResponse response) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        if(flag != 200){
            flag = 17;
        }

        content = content.replace("\\left", "")
                .replace("\\right", "")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">");
        log.info("formula content:{}", content);
        try {
            TeXFormula formula = new TeXFormula(content);
            // render the formula to an icon of the same size as the formula.
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, flag);
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();

            // insert a border
            icon.setInsets(new Insets(0, 0, 0, 0));
            // now create an actual image of the rendered equation
            BufferedImage image = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            image = g2.getDeviceConfiguration().createCompatibleImage(iconWidth, iconHeight, Transparency.TRANSLUCENT);
            g2.fillRect(0, 0, iconWidth, iconHeight);
            g2 = image.createGraphics();
            JLabel jl = new JLabel();
            jl.setForeground(new Color(0, 0, 0));
            icon.paintIcon(jl, g2, 0, 0);


            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            ImageIO.write(image, "png", outputStream);
            //强制将缓存区的数据进行输出
            outputStream.flush();
            //关流
            outputStream.close();
        } catch (Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
        }
        log.info("=======将公式转换成文件VO，接口用时=====" + String.valueOf(stopwatch.stop()));

    }

}