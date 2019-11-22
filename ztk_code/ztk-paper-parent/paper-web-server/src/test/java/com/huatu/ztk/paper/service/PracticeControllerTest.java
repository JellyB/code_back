package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.enums.CustomizeEnum;
import com.huatu.ztk.paper.service.v4.CustomizeService;
import com.huatu.ztk.paper.service.v4.impl.AnswerCardUtil;
import org.eclipse.core.resources.mapping.ModelStatus;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PracticeControllerTest extends BaseTest{


    @Autowired
    PracticeService practiceService;

    @Autowired
    CustomizeService customizeService;

    @Autowired
    PracticeCardService practiceCardService;
    @Test
    public void test(){
        AnswerCard unFinishedCard = practiceService.findUnFinishedCard(3340, 234694130, 2, 10);
        System.out.println("JsonUtil.toJson(unFinishedCard) = " + JsonUtil.toJson(unFinishedCard));
    }

    @Test
    public void test2(){
        try {

            createCustomize(3340, 234694130, 2, 10);
        } catch (BizException e) {
            e.printStackTrace();
        }
    }

    private void createCustomize( int pointId,long userId, int subject,int  size) throws BizException {


        AnswerCard unFinishedCard = practiceService.findUnFinishedCard(pointId, userId, subject, size);
        if (null != unFinishedCard) {
            System.out.println("unFinishedCard(1) = " + unFinishedCard);
        }
        PracticePaper practicePaper = customizeService.createPracticePaper(pointId,size,userId,subject, CustomizeEnum.ModeEnum.Write);
        if (practicePaper == null) {//没有查到
            System.out.println("practicePaper(0) = " + practicePaper);
        }
        final PracticeCard practiceCard = practiceCardService.create(practicePaper, 1, AnswerCardType.CUSTOMIZE_PAPER, userId);
        //添加未完成练习id
        practiceCardService.addCustomizesUnfinishedId(pointId, practiceCard);
        AnswerCardUtil.fillIdStr(practiceCard);
        System.out.println("practiceCard(1) = " + practiceCard);

    }
}
