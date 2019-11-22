package com.huatu.tiku.teacher.service.impl.paper;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.paper.ChangePaperQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/4/16
 * @描述
 */
@Service
public class ChangePaperQuestionServiceImpl extends BaseServiceImpl<PaperActivity> implements ChangePaperQuestionService {


    public ChangePaperQuestionServiceImpl() {
        super(PaperActivity.class);
    }

    @Autowired
    PaperQuestionService paperQuestionService;
    @Autowired
    CommonQuestionServiceV1 questionService;

    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Autowired
    ImportService importService;

    /**
     * @param oldQuestionId 旧试题ID
     * @param newQuestionId 新试题ID
     *                      此方法主要用户去重题时,（1）将旧试题ID从试卷中解绑（2）然后用新的试题ID替换旧试题ID,绑定此试卷
     *                      * （3）删除旧试题 （4）ztk_reflect表中建立新旧试题的绑定关系
     */
    @Transactional
    public void changePaperQueBindRelation(Long oldQuestionId, Long newQuestionId) {

        // 试题必须存在 && 试题绑定相关试卷
        ArrayList<Long> questionIds = Lists.newArrayList(oldQuestionId, newQuestionId);
        List<BaseQuestion> baseQuestionList = questionService.findByIds(questionIds);
        if (CollectionUtils.isEmpty(baseQuestionList) || baseQuestionList.size() < 2) {
            throwBizException("试题ID不能为空");
        }

        List<PaperQuestion> paperQuestionList = paperQuestionService.findByQuestionId(oldQuestionId);
        if (CollectionUtils.isEmpty(paperQuestionList)) {
            throwBizException(oldQuestionId + "试题未绑定试卷");
        }

        //（1）将旧试题ID从试卷中解绑,建立新试卷的映射关系
        paperQuestionList.stream().forEach(paperQuestion -> {
            Long paperId = paperQuestion.getPaperId();
            if (paperQuestion.getPaperType() != PaperInfoEnum.TypeInfo.ENTITY.getKey()) {
                return;
            }
            //删除旧试题绑定关系
            paperQuestionService.deletePaperQuestionInfo(paperId, PaperInfoEnum.TypeInfo.ENTITY, oldQuestionId);
            //建立新试题的绑定关系
            paperQuestionService.savePaperQuestionWithSort(newQuestionId, paperQuestion.getPaperId(),
                    paperQuestion.getModuleId(), paperQuestion.getSort(), paperQuestion.getScore(),
                    paperEntityService.createPaperQuestionValidate(), PaperInfoEnum.TypeInfo.ENTITY);
        });
        //（2）删除旧试题
        questionService.deleteQuestion(oldQuestionId,1L,true);
        //(3) 建立映射关系
        reflectQuestionDao.insertRelation(oldQuestionId.intValue(), newQuestionId);
        //同步数据到mongo,es,rocksDB
        importService.importQuestion(oldQuestionId);
        importService.importQuestion(newQuestionId);
    }
}
