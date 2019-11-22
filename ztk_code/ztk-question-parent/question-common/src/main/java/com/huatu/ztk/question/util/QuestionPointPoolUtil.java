package com.huatu.ztk.question.util;

import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 抽题池逻辑维护(抽题逻辑出现变动，则需要重新打包pandora和report项目，重新部署)
 *
 * */
public class QuestionPointPoolUtil {
    /**
     * 事业单位所有科目
     */
    public static final List<Integer> SYDW = new ArrayList();
    static {
        List<Integer> ids = Arrays.asList(2, 3, 4, 24, 200100054, 200100055, 200100056, 200100057);
        SYDW.addAll(ids);
    }
    /**
     * 试题是否符合抽题池条件
     *
     * @param question
     * @return
     */
    public static boolean isPoolFlag(Question question) {
        //不处理主观题
        if (question instanceof GenericSubjectiveQuestion ||
                question instanceof CompositeSubjectiveQuestion ||
                question instanceof CompositeQuestion) {
            return false;
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;

        //2008之前的题不参与推送，不在区域内的也不参与推荐
        if (question.getSubject() == SubjectType.GWY_XINGCE) {
            if (question.getYear() < 2008) {
                return false;
            }
            //只处理真题
            if (question.getMode() != QuestionMode.QUESTION_TRUE) {
                return false;
            }
            //只处理单选，复合，对错题
            if (question.getType() != QuestionType.WRONG_RIGHT &&
                    question.getType() != QuestionType.SINGLE_CHOICE &&
                    question.getType() != QuestionType.MULTIPLE_CHOICE &&
                    question.getType() != QuestionType.SINGLE_OR_MULTIPLE_CHOICE) {
                return false;
            }

        }else if(SYDW.contains(question.getSubject())){ //事业单位2014年之前的试题不入抽题池
            if (question.getYear() <= 2014) {
                return false;
            }
        }

        if (question.getStatus() != QuestionStatus.AUDIT_SUCCESS) {//删除则从推荐里面去掉
            return false;
        }
        return true;
    }

}
