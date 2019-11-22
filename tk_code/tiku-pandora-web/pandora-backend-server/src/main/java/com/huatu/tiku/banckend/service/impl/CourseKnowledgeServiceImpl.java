package com.huatu.tiku.banckend.service.impl;

import com.ctrip.framework.apollo.core.utils.StringUtils;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.dto.request.BatchKnowledgeByCourseReqVO;
import com.huatu.tiku.entity.CourseKnowledge;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/6/11
 *
 * 知识点绑定: courseType 录播：0 直播 ：1
 */
@Service
public class CourseKnowledgeServiceImpl extends BaseServiceImpl<CourseKnowledge> implements CourseKnowledgeService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public CourseKnowledgeServiceImpl() {
        super(CourseKnowledge.class);
    }

    @Transactional(value = "backendTransactionManager")
    @Override
    public void edit(Integer courseType, long courseId, String knowledgePointIds) {
        List<String> knowledgePointList;
        if (StringUtils.isBlank(knowledgePointIds) ||
                CollectionUtils.isEmpty(knowledgePointList = Arrays.asList(knowledgePointIds.split(",")))) {
            //当前没有知识点信息，移除所有绑定关系
            removeAllByCourseId(courseType, courseId);
            return;
        }
        //新增数据方法
        Consumer<String> addData = (knowledgePointId) -> {
            CourseKnowledge courseKnowledge = CourseKnowledge.builder()
                    .courseId(courseId)
                    .courseType(courseType)
                    .knowledgeId(Long.parseLong(knowledgePointId))
                    .build();
            save(courseKnowledge);
        };
        List<Long> existData = getListByCourseId(courseType, courseId);
        if (CollectionUtils.isNotEmpty(existData)) {
            //编辑处理 - 如果当前有数据,移除被删掉的数据
            List<Long> deleteIds = existData.stream()
                    .filter(existDataKnowledgeId -> !knowledgePointList.contains(String.valueOf(existDataKnowledgeId)))
                    .collect(Collectors.toList());
            //根据ID 批量删除
            deleteByPrimaryKeys(deleteIds);
            //处理新增的数据
            knowledgePointList.stream()
                    .filter(data -> !existData.stream().anyMatch(existDataKnowledgeId -> String.valueOf(existDataKnowledgeId).equals(data)))
                    .forEach(addData);
        } else {
            //新增处理
            knowledgePointList.stream().forEach(addData);
        }
    }

    /**
     * 查询课程知识点关联
     * @param courseType 课程类型
     * @param courseId   课程ID
     * @return
     */
    @Override
    public List<Long> getListByCourseId(Integer courseType, long courseId) {
        Example example = buildExampleByCourseId(courseType, courseId);
        List<CourseKnowledge> listByCourseId = selectByExample(example);
        return listByCourseId.stream()
                .map(CourseKnowledge::getKnowledgeId)
                .collect(Collectors.toList());

    }

    /**
     * 查询课程知识点关联(批量)
     * @param list
     * @return
     */
    @Override
    public List<BatchKnowledgeByCourseReqVO> getBatchListByCourseId(List<BatchKnowledgeByCourseReqVO> list) {
        for(BatchKnowledgeByCourseReqVO vo:list){
            Example example = buildExampleByCourseId(vo.getCourseType(), vo.getCourseId());
            List<CourseKnowledge> listByCourseId = selectByExample(example);
            List<Long> points = listByCourseId.stream()
                    .map(CourseKnowledge::getKnowledgeId)
                    .collect(Collectors.toList());
            vo.setPoints(points);
        }
        return list;
    }
    @Override
    public void removeAllByCourseId(Integer courseType, long courseId) {
        Example example = buildExampleByCourseId(courseType, courseId);
        deleteByExample(example);
    }



    /**
     * 通过 courseId 构建查询条件
     *
     * @param courseId 课程ID
     */
    private static Example buildExampleByCourseId(Integer courseType, long courseId) {
        WeekendSqls<CourseKnowledge> sql = WeekendSqls.custom();
        sql.andEqualTo(CourseKnowledge::getCourseId, courseId);
        sql.andEqualTo(CourseKnowledge::getCourseType, courseType);
        Example example = Example.builder(CourseKnowledge.class)
                .where(sql)
                .build();
        return example;
    }


    /**
     * 关联试题-》关联知识点
     */
    @Override
    public void boundKnowledgeByQuestion(Long courseId, Integer courseType, Long questionId){
        //题目的知识点
        Set<Long> questionPoints = new HashSet<>();
        Criteria criteria = Criteria.where("id").is(questionId)
                .and("status").is(QuestionStatus.AUDIT_SUCCESS);
        List<Question> questionList = mongoTemplate.find(new Query(criteria), Question.class, "ztk_question_new");
        if(CollectionUtils.isNotEmpty(questionList)){
            Question question = questionList.get(0);
            List<KnowledgeInfo> pointList = question.getPointList();

            if (CollectionUtils.isNotEmpty(pointList)) {
                questionPoints = pointList.stream().map(vo -> vo.getPoints().get(2).longValue()).collect(Collectors.toSet());
            }else if(question instanceof GenericQuestion){
                Long questionPoint = ((GenericQuestion) question).getPoints().get(2).longValue();
                questionPoints.add(questionPoint);
            }
        }
        //课件的知识点
        List<Long> coursePoints = getListByCourseId(courseType, courseId);

        //课件关联知识点
        Collection subtract = CollectionUtils.subtract(questionPoints, coursePoints);
        if (CollectionUtils.isNotEmpty(subtract)) {
            subtract.forEach(pointId -> {
                CourseKnowledge courseKnowledge = CourseKnowledge.builder()
                        .courseId(courseId)
                        .courseType(courseType)
                        .knowledgeId(Long.parseLong(pointId.toString()))
                        .build();
                save(courseKnowledge);
            });
        }

    }
}
