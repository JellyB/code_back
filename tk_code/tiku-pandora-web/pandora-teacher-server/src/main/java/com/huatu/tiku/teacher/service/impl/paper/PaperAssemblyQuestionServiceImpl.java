package com.huatu.tiku.teacher.service.impl.paper;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.entity.teacher.PaperAssemblyQuestion;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.paper.PaperAssemblyQuestionMapper;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyQuestionService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/16
 */
@Service
public class PaperAssemblyQuestionServiceImpl extends BaseServiceImpl<PaperAssemblyQuestion> implements PaperAssemblyQuestionService {

    public PaperAssemblyQuestionServiceImpl() {
        super(PaperAssemblyQuestion.class);
    }

    @Autowired
    private PaperAssemblyQuestionMapper mapper;

    @Autowired
    private QuestionSearchService questionSearchService;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Override
    public int saveQuestionInfo(Long paperId, List<Long> questionIdList) {
        if (null == paperId || CollectionUtils.isEmpty(questionIdList)) {
            return 0;
        }
        //清除原有的数据
        WeekendSqls<PaperAssemblyQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(PaperAssemblyQuestion::getPaperId, paperId);
        Example example = Example.builder(PaperAssemblyQuestion.class)
                .where(sql)
                .build();
        mapper.deleteByExample(example);
        //保存新的数据
        List<PaperAssemblyQuestion> saveList = Lists.newArrayList();
        questionIdList.forEach(questionId -> {
            PaperAssemblyQuestion paperAssemblyQuestion = PaperAssemblyQuestion.builder()
                    .paperId(paperId)
                    .questionId(questionId)
                    .sort(saveList.size() + 1)
                    .build();
            saveList.add(paperAssemblyQuestion);
        });
        return insertAll(saveList);
    }

    @Override
    public List<QuestionSimpleInfo> list(Long paperId) {
        WeekendSqls<PaperAssemblyQuestion> sql = WeekendSqls.custom();
        sql.andEqualTo(PaperAssemblyQuestion::getPaperId, paperId);
        Example assemblyExample = Example.builder(PaperAssemblyQuestion.class)
                .where(sql)
                .build();
        List<PaperAssemblyQuestion> paperAssemblyQuestions = selectByExample(assemblyExample);
        List<Long> questionIdList = paperAssemblyQuestions.stream()
                .map(PaperAssemblyQuestion::getQuestionId)
                .collect(Collectors.toList());
        reflectQuestionDao.transId(questionIdList);
        List<QuestionSimpleInfo> simpleInfoList = questionSearchService.listAllByQuestionId(questionIdList);
        /**
         * update by lizhenjuan
         * 纠正试卷内试题排序不对问题
         **/
        //按照序号排序
        List<QuestionSimpleInfo> questionSimpleInfoList = simpleInfoList.stream().map(question -> {
            if (CollectionUtils.isEmpty(question.getChildren())) {
                //对于单题
                Optional<PaperAssemblyQuestion> collect = paperAssemblyQuestions.stream()
                        .filter(q -> q.getQuestionId().equals(question.getId()))
                        .findFirst();
                question.setSort(collect.get().getSort());
            } else {
                //对于复合题,顺序是第一个子题的顺序
                List<QuestionSimpleInfo> childrenQuestions = question.getChildren();
                List<QuestionSimpleInfo> childPaperQuestionList = childrenQuestions.stream().map(child -> {
                            Optional<PaperAssemblyQuestion> childCollect = paperAssemblyQuestions.stream().filter(paperQuestion -> paperQuestion.getQuestionId().equals(child.getId()))
                                    .findFirst();
                            if (childCollect.isPresent()) {
                                child.setSort(childCollect.get().getSort());
                            }
                            return child;
                        }
                ).collect(Collectors.toList());
                //子题按照sort升序排列
                List<QuestionSimpleInfo> paperQuestionSimpleInfos = childPaperQuestionList.stream().sorted(Comparator.comparing(QuestionSimpleInfo::getSort))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(paperQuestionSimpleInfos)) {
                    Integer sort = Integer.valueOf(paperQuestionSimpleInfos.get(0).getSort().toString());
                    if (CollectionUtils.isNotEmpty(childrenQuestions)) {
                        question.setSort(sort);
                    }
                }
                question.getChildren().clear();
                question.setChildren(paperQuestionSimpleInfos);
            }
            return question;
        }).collect(Collectors.toList());
        //试题外层整体排序
        List<QuestionSimpleInfo> result = questionSimpleInfoList.stream().sorted(Comparator.comparing(QuestionSimpleInfo::getSort)).collect(Collectors.toList());
        return result;
    }

}
