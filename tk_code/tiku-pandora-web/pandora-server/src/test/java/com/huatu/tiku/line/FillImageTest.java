package com.huatu.tiku.line;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by huangqingpeng on 2018/10/30.
 */
@Slf4j
public class FillImageTest extends TikuBaseTest {
//public class FillImageTest {
    @Autowired
    NewQuestionDao questionDao;

    @Autowired
    ImportService importService;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    @Test
    public void test() {
        int index = 0;
        List<Integer> list = Lists.newArrayList();
        while (true) {
            List<Question> questions = questionDao.findByIdGtAndLimit(index, 1000);
            if (CollectionUtils.isEmpty(questions)) {
                break;
            }
            for (Question question : questions) {
//                checkQuestionHttpUrl(question);
                if(checkQuestionChoicesE(question)){
                    list.add(question.getId());
                }
            }
//            List<Integer> collect = questions.parallelStream().filter(i -> checkQuestionSort(i)).map(i -> i.getId()).collect(Collectors.toList());
//            if(CollectionUtils.isNotEmpty(collect)){
//                list.addAll(collect);
//            }
            index = questions.stream().map(Question::getId).max(Integer::compareTo).get();
            System.out.println("*****************" + index + "***************");
        }
        System.out.println("list=" + JsonUtil.toJson(list));
    }

    private boolean checkQuestionSort(Question question) {
        if (question.getType() != QuestionInfoEnum.QuestionTypeEnum.MULTI.getCode()) {
            return false;
        }
        if(question instanceof GenericQuestion){
            return sortInteger(((GenericQuestion) question).getAnswer());
        }
        return false;
    }

    private boolean checkQuestionChoicesE(Question question) {
        System.out.println("===============处理E选项问题================");
        if (question instanceof GenericQuestion) {
            GenericQuestion genericQuestion = (GenericQuestion) question;
            List<String> choices = genericQuestion.getChoices();
            int answer = genericQuestion.getAnswer();
            int max = getMaxInteger(answer);
            if (max > choices.size()) {
                return true;
            }
        }
        return false;
    }
    @Test
    public void test1() {
        System.out.println(sortInteger(113));
        System.out.println(sortInteger(123));
        System.out.println(sortInteger(213));
    }

    /**
     * @param answer
     * @return false排序争取true乱序
     */
    private static boolean sortInteger(int answer) {
        int minner = Integer.MAX_VALUE;
        while (answer > 0) {
            int i = answer % 10;
            if (minner > i) {
                minner = i;
            } else {
                return true;
            }
            answer = answer / 10;
        }
        return false;
    }

    private int getMaxInteger(int answer) {
        int max = 0;
        while (answer > 0) {
            int i = answer % 10;
            if (max < i) {
                max = i;
            }
            answer = answer / 10;
        }
        return max;
    }

    private void checkQuestionHttpUrl(Question question) {
        System.out.println("===============处理http链接问题===============");
        String content = JsonUtil.toJson(question);
        if (checkoutHttpUrl(content)) {
            importService.sendQuestion2Mongo(question.getId());
        }
    }

    private boolean checkoutHttpUrl(String content) {
        String[] https = content.split("http");
        String[] imgs = content.split("<img[^>]+>");
        if (https.length > imgs.length) {
            return true;
        }
        return false;
    }
}
