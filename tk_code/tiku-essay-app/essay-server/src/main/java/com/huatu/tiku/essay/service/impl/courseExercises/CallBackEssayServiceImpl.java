package com.huatu.tiku.essay.service.impl.courseExercises;

import com.huatu.tiku.essay.constant.course.CallBack;
import com.huatu.tiku.essay.entity.courseExercises.EssayCourseExercisesQuestion;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.CourseWareTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.courseExercises.EssayCourseExercisesQuestionRepository;
import com.huatu.tiku.essay.repository.courseExercises.EssayExercisesAnswerMetaRepository;
import com.huatu.tiku.essay.service.courseExercises.CallBackEssayService;
import com.huatu.tiku.essay.service.courseExercises.EssayCourseExercisesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-05 6:53 PM
 **/
@Service
@Slf4j
public class CallBackEssayServiceImpl implements CallBackEssayService {


    @Autowired
    private EssayCourseExercisesQuestionRepository essayCourseExercisesQuestionRepository;

    @Autowired
    private EssayExercisesAnswerMetaRepository essayExercisesAnswerMetaRepository;

    @Autowired
    private EssayCourseExercisesService essayCourseExercisesService;

    @Override
    public void updateBindingAndMetaInfo(CallBack callBack) {
        if(CollectionUtils.isEmpty(callBack.getMetaList())){
            return;
        }
        List<CallBack.Meta> metas = callBack.getMetaList();
        for (CallBack.Meta meta : metas){
            long liveCourseId = meta.getLiveCourseId();
            long recordCourseId = meta.getRecordCourseId();
            List<EssayCourseExercisesQuestion> questions = essayCourseExercisesQuestionRepository.findByCourseWareIdAndCourseTypeAndStatusOrderBySort(liveCourseId, CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType(), EssayStatusEnum.NORMAL.getCode());
            if(CollectionUtils.isEmpty(questions)){
                continue;
            }
            for(EssayCourseExercisesQuestion questionRecord : questions){
                questionRecord.setCourseWareId(recordCourseId);
                questionRecord.setCourseType(CourseWareTypeEnum.TableCourseTypeEnum.RECORD.getType());
                questionRecord.setGmtModify(new Timestamp(System.currentTimeMillis()));
                questionRecord.setModifier("lucifer:" + CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType() + ":" + liveCourseId);
                essayCourseExercisesQuestionRepository.save(questionRecord);
            }

            if(questions.size() > 0){
                log.info("通知 php修改结果:size:{},liveId:{}, roomId:{}", questions.size(), liveCourseId, callBack.getRoomId());
                essayCourseExercisesService.noticePHPUpdateCourseNum(questions.size(), recordCourseId, CourseWareTypeEnum.TableCourseTypeEnum.RECORD.getType(), questions.get(0).getType());
            }

            List<EssayExercisesAnswerMeta> metaList = essayExercisesAnswerMetaRepository.findByCourseWareIdAndCourseTypeAndStatus(liveCourseId, CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType(), EssayStatusEnum.NORMAL.getCode());
            if(CollectionUtils.isEmpty(metaList)){
                continue;
            }
            for(EssayExercisesAnswerMeta metaExample : metaList){
                metaExample.setCourseWareId(recordCourseId);
                metaExample.setGmtModify(new Timestamp(System.currentTimeMillis()));
                metaExample.setCourseType(CourseWareTypeEnum.TableCourseTypeEnum.RECORD.getType());
                metaExample.setModifier("lucifer:" + CourseWareTypeEnum.TableCourseTypeEnum.LIVE.getType() + ":" + liveCourseId);
                essayExercisesAnswerMetaRepository.save(metaExample);
            }
        }
    }
}
