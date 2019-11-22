package com.huatu.tiku.teacher.service.impl.question;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.question.v1.InsertCommonQuestionReqV1;
import com.huatu.tiku.request.question.v1.InsertQuestionReqV1;
import com.huatu.tiku.response.question.v1.SelectCompositeQuestionRespV1;
import com.huatu.tiku.response.question.v1.SelectQuestionRespV1;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.OldQuestionService;
import com.huatu.tiku.teacher.service.question.SyncQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.question.v1.QuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 数据迁移逻辑专用
 * Created by huangqp on 2018\6\25 0025.
 */
@Slf4j
@Service
public class SyncQuestionServiceImpl implements SyncQuestionService {
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Autowired
    OldQuestionService oldQuestionService;
    @Autowired
    ReflectQuestionDao reflectQuestionService;
    @Autowired
    TeacherSubjectService teacherSubjectService;
    @Autowired
    PaperEntityService paperEntityService;
    @Autowired
    PaperQuestionService paperQuestionService;

    /**
     * 同步试题信息到mysql
     *
     * @param id
     */
    @Override
    @Transactional
    public Long syncQuestion(Integer id) {
        ReflectQuestion reflection = reflectQuestionService.findById(id);
        if(null != reflection){
            log.error("试题{}已被替换为{}",id,reflection.getNewId());
            throw new BizException(ErrorResult.create(10211131,"试题"+id+"已被替换为"+reflection.getNewId()));
        }
        //添加试题逻辑
        try {
            log.info("试题{}不存在，执行添加操作", id);
            //mongo->mysql 字段映射
            InsertQuestionReqV1 insertQuestionReqV1 = assertInsertObject(id);
            InsertCommonQuestionReqV1 insertCommonQuestionReqV1 = new InsertCommonQuestionReqV1();
            BeanUtils.copyProperties(insertQuestionReqV1, insertCommonQuestionReqV1);
            //子题迁移的时候，需要确定进入mysql时的复合题的newId，由于复合题迁移都会生成映射数据，所有必须查询修改multiId字段
            Long multiId = insertQuestionReqV1.getMultiId();
            if (multiId != null && multiId > 0L) {
                ReflectQuestion reflectQuestion = reflectQuestionService.findById(multiId.intValue());
                if (reflectQuestion != null) {
                    insertCommonQuestionReqV1.setMultiId(new Long(reflectQuestion.getNewId()));
                } else {
                    log.info("试题{}属于子题，其复合题{}还未做迁移操作！！", id, multiId);
                }
            }
            Map result = commonQuestionServiceV1.insertQuestion(insertCommonQuestionReqV1);
            Long questionId = Long.parseLong(String.valueOf(result.get("questionId")));
            //如果试题状态正常，则直接发布试题
            if(insertCommonQuestionReqV1.getStatus().equals(StatusEnum.NORMAL)){
                commonQuestionServiceV1.updateQuestionBizStatus(questionId, BizStatusEnum.PUBLISH.getValue());
            }
            //当为复合题的时候，迁移是重新生成试题id,需要添加映射关系
            if (questionId.intValue() != id.intValue()) {
                reflectQuestionService.insertRelation(id, questionId);
            }
            return questionId;
        } catch (BizException e) {
            log.info("试题迁移错误统计:{},原因：{}", id, e.getErrorResult().getMessage());
            throw e;
        } catch (Exception e1) {
            log.info("试题迁移错误统计:{},原因：{}", id, e1.getMessage());
            e1.printStackTrace();
            throw e1;
        }
    }

    /**
     * 同步试题信息
     *
     * @param questionId
     * @param paperId
     * @param sort
     * @param module
     */
    @Override
    public void syncQuestion(Integer questionId, Long paperId, Integer sort, String module) {
        //先删除原有的数据，在重新迁移
        commonQuestionServiceV1.deleteQuestionPhysical(questionId);
        //同步试题数据
        syncQuestion(questionId);
        //同步试题试卷模块信息(复合题没有绑定关系，同样不会参与试卷题序排序，所以使用题序作为是否是复合题的判断条件)
        bindQuestion(questionId, paperId, sort, module);
    }

    @Override
    public List<SelectQuestionRespV1> findQuestionByIds(List<Question> questions, List<Long> movedIds, Map<Integer, Integer> sortMap) {
        List<SelectQuestionRespV1> questionRespV1s = Lists.newArrayList();
        //题型查询
        Function<Integer, String> getName = (type -> {
            QuestionInfoEnum.QuestionTypeEnum[] values = QuestionInfoEnum.QuestionTypeEnum.values();
            for (QuestionInfoEnum.QuestionTypeEnum value : values) {
                if (value.getCode() == type) {
                    return value.getName();
                }
            }
            return "未知题型";
        });

        Map<Long, SelectCompositeQuestionRespV1> compositeMap = Maps.newHashMap();
        for (Question question : questions) {
            try {
                SelectQuestionRespV1 tempResp = commonQuestionServiceV1.convertQuestionMongo2DB(question);
                tempResp.setTypeName(getName.apply(tempResp.getQuestionType()));
                if (movedIds.contains(tempResp.getId())) {
                    tempResp.setMoveFlag(1);
                }
                tempResp.setSort(sortMap.get(question.getId()));
                Long multiId = tempResp.getMultiId();
                //如果是试卷中复合题的子题；子题作为复合题的子节点，嵌套进复合题中，复合题占位，之后再遇到该复合题的子题，拼到该复合题下
                if (multiId > 0L) {
                    if (compositeMap.get(multiId) == null) {
                        Question parentQuestion = oldQuestionService.findQuestion(multiId.intValue());
                        if(null == parentQuestion){
                            log.error("parent not exsited,id = {}",multiId);
                        }
                        SelectCompositeQuestionRespV1 parent = (SelectCompositeQuestionRespV1) commonQuestionServiceV1.convertQuestionMongo2DB(parentQuestion);
                        parent.setTypeName(getName.apply(parent.getQuestionType()));
                        //首次加载复合题部分时，判断是否被录入mysql（复合部分数据不会直接迁移，迁移过程，id必然会再映射表中留下数据）
                        if (movedIds.contains(multiId)) {
                            parent.setMoveFlag(1);
                        }
                        parent.setChildren(Lists.newArrayList(tempResp));
                        compositeMap.put(multiId, parent);
                        questionRespV1s.add(parent);
                    } else {
                        SelectCompositeQuestionRespV1 parent = compositeMap.get(multiId);
                        parent.getChildren().add(tempResp);
                    }
                } else {
                    questionRespV1s.add(tempResp);
                }
            } catch (Exception e) {
                log.error("question parse error,id={}", question.getId());
                e.printStackTrace();
            }

        }
        return questionRespV1s;
    }

    @Override
    public void duplicateQuestion(Integer questionId, Long paperId, Integer sort, String module, Long id) {
        reflectQuestionService.insertRelation(questionId, id);
        //同步试题试卷模块信息
        bindQuestion(questionId, paperId, sort, module);
    }

    @Override
    public Object findDuplicateQuestion(Integer questionId, Integer subjectFlag, Integer yearFlag) {
        Question question = oldQuestionService.findQuestion(questionId);
        if (question == null) {
            return Lists.newArrayList();
        }

        //如果试题是复合试题，则查询材料表数据
        Object duplicateQuestion = commonQuestionServiceV1.findDuplicateQuestion(question, subjectFlag, yearFlag);
        if (duplicateQuestion == null) {
            return Lists.newArrayList();
        }
        return duplicateQuestion;
    }

    @Override
    public void bindQuestion(int questionId, Long paperId, int sort, String moduleName) {
        if (sort > 0) {
            Example example = new Example(PaperQuestion.class);
            example.and().andEqualTo("questionId", questionId).andEqualTo("paperId", paperId).andEqualTo("sort", sort);
            List<PaperQuestion> paperQuestions = paperQuestionService.selectByExample(example);
            if (CollectionUtils.isNotEmpty(paperQuestions)) {
                log.info("绑定关系已建立，强制结束！");
                return;
            }
            int moduleId = paperEntityService.getModuleIdByName(paperId, moduleName);
            int i = paperQuestionService.savePaperQuestionWithSort(new Long(questionId), paperId, moduleId, sort, PaperInfoEnum.TypeInfo.ENTITY);
        }
    }


    /**
     * 同步数据结构处理，并对知识点和科目进行补充
     *
     * @param questionId
     * @return
     */
    private InsertQuestionReqV1 assertInsertObject(Integer questionId) {
        Question question = oldQuestionService.findQuestion(questionId.intValue());
        if (question == null) {
            throw new BizException(ErrorResult.create(2000001, "旧题库中无此题目"));
        }
        Integer type = question.getType();
        int subjectId = question.getSubject();
        Subject subject = teacherSubjectService.selectById(new Long(subjectId));
        if (subject == null) {
            throw new BizException(ErrorResult.create(2000001, "科目" + subjectId + "不存在"));
        }
        QuestionServiceV1 questionService = commonQuestionServiceV1.choiceService(QuestionInfoEnum.getSaveTypeByQuestionType(type));
        InsertQuestionReqV1 insertQuestionReqV1 = questionService.assertInsertReq(question);
        insertQuestionReqV1.setDifficultyLevel(question.getDifficult()>0?question.getDifficult(): DifficultyLevelEnum.GENERAL.getValue());
        //试题插入
        return insertQuestionReqV1;
    }
}

