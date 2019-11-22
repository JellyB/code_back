package com.huatu.tiku.dataClean;

import com.google.common.base.Stopwatch;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/4/1
 * @描述 将复合题的子题同步到mongo字段中
 */

public class SyncMultiQuestionToMongo extends TikuBaseTest {


    private static final Logger logger = LoggerFactory.getLogger(SyncMultiQuestionToMongo.class);

    @Autowired
    CommonQuestionServiceV1 commonQuestionService;

    @Autowired
    ImportService importService;

    @Autowired
    QuestionKnowledgeService questionKnowledgeService;

    /**
     * 批量同步复合题,修复复合题问题
     */
    @Test
    public void syncMultiQuestionToMongo() {
        int type = 105;
        Stopwatch stopwatch = Stopwatch.createStarted();
        //查询所有的复合题
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("questionType", type);
        List<BaseQuestion> baseQuestions = commonQuestionService.selectByExample(example);
        logger.info("需要同步复合题数量是:{}", baseQuestions.size());
        //批量同步复合题
        if (CollectionUtils.isNotEmpty(baseQuestions)) {
            List<Integer> questionIds = baseQuestions.stream().map(BaseQuestion::getId)
                    .map(id -> id.intValue())
                    .collect(Collectors.toList());
            //将符合题ID写入到文本中
            writeFileWithPath(JsonUtil.toJson(questionIds), "/Users/lizhenjuan/tool/复合题.txt");
            importService.sendQuestion2Mongo(questionIds);
        }
        logger.info("同步完毕耗时:{}", String.valueOf(stopwatch.stop()));
    }


    /**
     * 写入本地文件
     *
     * @param jsonResult
     * @param path
     */
    public void writeFileWithPath(String jsonResult, String path) {
        File file = new File(path);
        Writer out = null;
       /* try {
            //测试可以吗
            out = new FileWriter(file);
            out.write(jsonResult);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

}
