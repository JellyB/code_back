package com.huatu.ztk.knowledge.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.knowledge.common.QuestionIdsParse;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-05-20 10:24
 */
public class QuestionIdsParseTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionIdsParseTest.class);

    @Autowired
    private QuestionIdsParse questionIdsParse;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Test
    public void aa(){
        final Question genericQuestion = questionDubboService.findById(45769);
        final StringBuilder combine = questionIdsParse.combine(genericQuestion);
        logger.info(combine.toString());
        //45765:3@,45766:3@,45767:3@,45768:3@,45769:3@@754
        final List<Question> bath = questionDubboService.findBath(Lists.<Integer>newArrayList(45765, 45766, 45767));
        final StringBuilder combine1 = questionIdsParse.combine(bath);
        logger.info(combine1.toString());
    }

    @Test
    public void parseTest(){
        final ArrayList<String> arrayList = Lists.newArrayList("43095:3@754","43095:3@754","43095:3@754", "43095:3@754", "95044:2@754", "39961:5@392","43095:3@754","43095:3@754", "39961:5,39962:5,39963:5@392");
        QuestionStrategy parse = questionIdsParse.parse(arrayList, 2);
        logger.info(JsonUtil.toJson(parse));
        parse = questionIdsParse.parse(arrayList, 5);
        logger.info(JsonUtil.toJson(parse));
        parse = questionIdsParse.parse(arrayList, 20);
        logger.info(JsonUtil.toJson(parse));
    }

    @Test
    public void test(){
        ArrayList<Integer> ids = Lists.newArrayList(30006057, 45120, 30006154, 268768, 45125, 262753, 45071, 266165, 30006299, 45074, 30006298, 45139, 30005917, 30006323, 30006485, 226495, 232450, 226567, 259782, 264461, 270861, 226568, 45518, 100494, 45137, 45073, 259735, 59860, 268764, 259736, 43806, 268763, 110430, 110433, 30006059, 244256, 236455, 236456, 268776, 78000, 264502, 66229, 266290, 259700, 66230, 45049, 266431, 271288, 46397, 226494, 44031, 45078, 261209, 261912, 261886, 95020, 43389, 113582, 265349, 235817, 264463, 218915, 233474, 43393, 236049, 46468, 236458, 95018, 46095, 43264, 44033, 265350, 233472, 259654, 46471, 266253, 259785, 259784, 259656, 266248, 100492, 45072, 260050, 259729, 268765, 259738, 259737, 263448, 263450, 260067, 262756, 260066, 218914, 61410, 268775, 44003, 259748, 78441, 268781, 95019, 66219, 66218, 99181, 236460, 264501, 45811, 264503, 259703, 45812, 45813, 45048, 268671, 261887, 264504, 228322, 218913, 45123, 268769, 226566, 259653, 113579, 266251, 266164, 236048, 260080, 259728, 268784, 261212, 43391, 228323, 259779, 268774, 226599, 262992, 268782, 268783, 226527, 78444, 263449, 271289, 45038, 45121, 110432, 45122, 45098, 77999, 45199, 45810, 45178, 259672, 45179, 45180, 45116, 45119, 261889, 236047, 270859, 233475, 259778, 45954, 259655, 45381, 268674, 45382, 45383, 44361, 262989, 100491, 44811, 44364, 45517, 44813, 260049, 45076, 45077, 45079, 46168, 259674, 43929, 44894, 45092, 259749, 268771, 260068, 236457, 78445, 45036, 260082, 266167, 259702, 30006399, 115902, 266432, 226598, 235819, 95021, 266249, 95057, 60563, 59859);
        System.out.println("ids = " + ids.size());
        List<Question> bath = questionDubboService.findBath(ids);
        System.out.println("bath = " + bath.size());
    }
}
