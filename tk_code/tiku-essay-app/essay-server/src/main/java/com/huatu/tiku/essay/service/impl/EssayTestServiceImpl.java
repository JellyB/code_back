package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.UserCorrectGoodsConstant;
import com.huatu.tiku.essay.constant.youtu.EssayYoutuConstant;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssayUserCorrectGoodsRepository;
import com.huatu.tiku.essay.service.EssayTestService;
import com.huatu.tiku.essay.util.file.HtmlFileUtil;
import com.huatu.tiku.essay.util.sign.OCRSign;
import com.itextpdf.text.BadElementException;
import javax.imageio.ImageIO;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Encoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by x6 on 2017/12/16.
 */
@Service
@Slf4j
public class EssayTestServiceImpl implements EssayTestService {
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Override
    public void batchInsert() {

        List<EssayQuestionAnswer> essayQuestionAnswerList = new LinkedList<>();
        List<EssayQuestionBase> questionList = essayQuestionBaseRepository.findByPaperIdAndStatus(746, new Sort(Sort.Direction.ASC, "sort"), EssayStatusEnum.NORMAL.getCode());
        for (EssayQuestionBase questionVO : questionList) {
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder()
                    .userId(233906461)
                    .terminal(1)
                    .areaId(1)
                    .areaName("北京")
                    .questionBaseId(questionVO.getId())
                    .questionYear(questionVO.getQuestionYear())
                    .questionDetailId(questionVO.getDetailId())
                    .score(10)
                    .paperAnswerId(1111)//答题卡对应的paperId是试卷答题卡
                    .paperId(746)
                    .build();
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
            essayQuestionAnswer.setCreator(233906461 + "");
            //  essayQuestionAnswer = essayQuestionAnswerRepository.save(essayQuestionAnswer);
            essayQuestionAnswerList.add(essayQuestionAnswer);
        }
        // 换成批量插入，待测试  id是否会自动存到list中？
        essayQuestionAnswerRepository.save(essayQuestionAnswerList);

        log.info("测试一下下" + essayQuestionAnswerList.get(0).getId());
    }

    @Override
    public void formula() {
//        String imgPath = UUID.randomUUID().toString().replace("-","") + ".png";
//        System.out.println(imgPath);
//        String latex = "\\tan {{231}^{{}^\\circ }}\\_\\_\\_\\_\\_\\_\\_\\_\\tan {{237}^{{}^\\circ }}";
//        String imgBase = latex2Png(latex);
//        System.out.println(imgBase);
//
//        try {
//            String content = htmlFileUtil.imgManage(imgBase, UUID.randomUUID().toString() + "", 0);
//            System.out.println("图片地址："+content);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (BadElementException e) {
//            e.printStackTrace();
//        }

    }


    /**
     *  latex 转 imgbase64
     */
    public static String latex2Png(String latex) {
        try {
            TeXFormula formula = new TeXFormula(latex);
            // render the formla to an icon of the same size as the formula.
            TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
            // insert a border
            icon.setInsets(new Insets(5, 5, 5, 5));
            // now create an actual image of the rendered equation
            BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
            JLabel jl = new JLabel();
            jl.setForeground(new Color(0, 0, 0));
            icon.paintIcon(jl, g2, 0, 0);
            // at this point the image is created, you could also save it with ImageIO
            // saveImage(image, "png", "F:\\b.png");
            // ImageIO.write(image, "png", new File("F:\\c.png"));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", outputStream);
            } catch (IOException e) {
                //Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            byte[] buffer = outputStream.toByteArray();
            BASE64Encoder encoder = new BASE64Encoder();
            return ("data:image/png;base64," + encoder.encode(buffer));
        } catch (Exception e) {
            // e.printStackTrace();
            // ExceptionUtil.log(log, e);
            System.err.println("公式解析有误：\n" + latex);
            // e.printStackTrace();
            return null;
        }

    }

}
