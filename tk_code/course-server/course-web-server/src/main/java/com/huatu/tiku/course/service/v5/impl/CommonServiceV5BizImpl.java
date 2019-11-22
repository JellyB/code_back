package com.huatu.tiku.course.service.v5.impl;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.vo.EssayAnswerCardInfo;
import com.huatu.tiku.course.common.SubjectEnum;
import com.huatu.tiku.course.consts.SyllabusInfo;
import com.huatu.tiku.course.netschool.api.v4.CommonServiceV4;
import com.huatu.tiku.course.service.manager.CourseExercisesProcessLogManager;
import com.huatu.tiku.course.service.manager.EssayExercisesAnswerMetaManager;
import com.huatu.tiku.course.service.v5.CommonServiceV5Biz;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.util.ZTKResponseUtil;
import com.huatu.tiku.course.ztk.api.v1.paper.PracticeCardServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-09-27 4:26 PM
 **/

@Slf4j
@Service
public class CommonServiceV5BizImpl implements CommonServiceV5Biz {

    @Autowired
    private CommonServiceV4 commonServiceV4;

    @Autowired
    private CourseExercisesProcessLogManager courseExercisesProcessLogManager;

    @Autowired
    private PracticeCardServiceV1 practiceCardServiceV1;

    @Autowired
    private EssayExercisesAnswerMetaManager essayExercisesAnswerMetaManager;

    /**
     * pc 听课获取课后作业信息
     *
     * @param params
     * @param syllabusId
     * @param subjectType
     * @param buildType
     * @param afterCoreseNum
     * @return
     */
    @Override
    public Object classToken(Map<String, Object> params, int userId, long syllabusId, int subjectType, int buildType, int afterCoreseNum) throws BizException {

        Map<Object, Object> defaultMap = HashMapBuilder.newBuilder()
                .put("status", 0)
                .put("rcount", 0)
                .put("wcount", 0)
                .put("ucount", 0)
                .put("id", "0")
                .build();
        Object response = ResponseUtil.build(commonServiceV4.classToken(params));
        Map<String, Object> result = (Map<String, Object> ) response;
        result.put("answerCard", defaultMap);
        if(afterCoreseNum == 0){
            return result;
        }
        if(SubjectEnum.XC.getCode() == subjectType){
            List<Long> syllabusIds = Lists.newArrayList();
            syllabusIds.add(syllabusId);
            List<Long> cardIds = courseExercisesProcessLogManager.obtainCardIdsBySyllabusIds(userId, syllabusIds);
            if(CollectionUtils.isEmpty(cardIds)){
                return result;
            }
            Object courseExercisesCardInfo = practiceCardServiceV1.getCourseExercisesCardInfoV2(cardIds);
            Object object = ZTKResponseUtil.build(courseExercisesCardInfo);
            Map<String,Object> map = ((List<Map>) object).get(0);
            map.remove("courseId");
            map.remove("courseType");
            result.put("answerCard", map);
            return result;
        }else if(SubjectEnum.SL.getCode() == subjectType){
            params.put(SyllabusInfo.AfterCourseNum, afterCoreseNum);
            params.put(SyllabusInfo.SyllabusId, syllabusId);
            EssayAnswerCardInfo essayAnswerCardInfo = essayExercisesAnswerMetaManager.buildEssayAnswerCardInfo(userId, params);
            result.put("answerCard", essayAnswerCardInfo);
            return result;
        }else{
            throw new BizException(ErrorResult.create(100010, "参数错误"));
        }
    }
}
