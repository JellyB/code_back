package com.huatu.tiku.teacher.service;

import com.google.common.collect.Lists;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.cop.controller.SchoolController;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.teacher.controller.admin.download.WordController;
import com.huatu.tiku.teacher.controller.admin.util.ImportController;
import com.huatu.tiku.teacher.dao.question.DuplicatePartProviderMapper;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeComponent;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.question.DuplicateQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.TextScore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/12/10
 * @描述
 */
@Slf4j
public class ImportPaperTest extends TikuBaseTest {

    @Autowired
    WordController wordController;
    @Autowired
    DuplicatePartProviderMapper duplicatePartProviderMapper;

    @Autowired
    DuplicateQuestionService duplicateQuestionService;
    @Autowired
    private KnowledgeComponent knowledgeComponent;
    @Autowired
    private PaperEntityService paperEntityService;
    @Autowired
    private SchoolController schoolController;

    @Autowired
    private ImportController importController;

    @Autowired
    private CommonQuestionServiceV1 commonQuestionService;


    @Test
    public void importPaperTest() {
        List paperIds = Lists.newArrayList(961, 2184);
        // wordController.downloadPaper(paperIds, 1, 1, 1);
    }

    @Test
    public void testU() {
        Map<Integer, BaseQuestion> questionMap = new HashMap<>();
        BaseQuestion baseQuestion = new BaseQuestion();
        baseQuestion.setId(123L);
        questionMap.put(123, baseQuestion);
        questionMap.put(456, baseQuestion);

        BaseQuestion b = questionMap.get(123);
        System.out.println("试题信息是：{}" + b);
    }

    @Test
    public void duplicateQuestionTest() {
        List<Long> questionIds = Lists.newArrayList(30005999L, 30006022L);
        duplicateQuestionService.assembleQuestionInfo(questionIds, 99);
    }


    @Test
    public void testKnowledge() {
        List<Knowledge> parentUtilRoot = knowledgeComponent.getParentUtilRoot(394L);
        log.info("结果是：{}", parentUtilRoot);
    }


    @Test
    public void testPaperEntity() {
        paperEntityService.deletePaper(4000032L);
    }

    /**
     * 导出模考数据
     */
    @Test
    public void importData() {
        schoolController.importData(3527915);
    }


    @Test
    public void sendQuestion2MongoByPaper() {

        //4000333,4000334, 4000335,4000331,4000336
        importController.sendQuestion2MongoByPaper(false, 4000336L, 1);
    }


    @Test
    public void testParse() {
        String content = "（单选题）测试只支持三级知识点\n" +
                "A、测试只支持三级知识点\n" +
                "B、测试只支持三级知识点\n" +
                "C、测试只支持三级知识点\n" +
                "D、测试只支持三级知识点\n" +
                "【答案】A\n" +
                "【解析】测试只支持三级知识点\n" +
                "【拓展】测试只支持三级知识点\n" +
                "【标签】 \n" +
                "【知识点】测试只能够三级知识点*测试只支持三级知识点 \n" +
                "【难度】中等";
        Object questionInfo = commonQuestionService.parseQuestionInfo(content, 1L, 30006530L);
        log.info("查询结果是:{}", JsonUtil.toJson(questionInfo));
    }


    @Test
    public void testSelectiveProvider() {
        String questionIds = "30006165,30006156,30006045";
        List<DuplicatePartResp> duplicatePartResps = duplicatePartProviderMapper.buildObjectiveInfo(questionIds, 1);
        log.info("结果是:{}", duplicatePartResps);

    }

}
