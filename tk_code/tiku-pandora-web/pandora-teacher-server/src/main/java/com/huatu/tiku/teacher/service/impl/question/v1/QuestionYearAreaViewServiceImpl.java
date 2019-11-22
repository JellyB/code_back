package com.huatu.tiku.teacher.service.impl.question.v1;

import com.google.common.collect.Lists;
import com.huatu.tiku.dto.QuestionYearAreaDTO;
import com.huatu.tiku.entity.question.QuestionYearAreaView;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.teacher.dao.QuestionYearAreaViewMapper;
import com.huatu.tiku.teacher.service.question.QuestionYearAreaViewService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/24
 */
@Service
public class QuestionYearAreaViewServiceImpl implements QuestionYearAreaViewService {

    @Autowired
    private QuestionYearAreaViewMapper mapper;

    @Override
    public QuestionYearAreaDTO selectByPrimaryKey(Long questionId) {
        Example example = Example.builder(QuestionYearAreaView.class).build();
        example.and().andEqualTo("questionId", questionId);
        List<QuestionYearAreaView> questionAreaYearList = mapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(questionAreaYearList)) {
            List<Integer> yearList = Lists.newArrayList();
            List<QuestionYearAreaDTO.Area> areaList = Lists.newArrayList();
            questionAreaYearList.forEach(questionYearAreaView -> {
                yearList.add(questionYearAreaView.getYear());
                areaList.add(QuestionYearAreaDTO.Area.builder()
                        .areaId(questionYearAreaView.getAreaId())
                        .name(questionYearAreaView.getAreaName())
                        .build());
            });
            return QuestionYearAreaDTO.builder()
                    .questionId(questionId)
                    .yearList(yearList)
                    .areaList(areaList)
                    .build();
        }
        return null;
    }

    @Override
    public List<Long> selectQuestionId(List<Long> areaIdList, Integer year) {
        WeekendSqls<QuestionYearAreaView> weekendSql = WeekendSqls.custom();
        if (BaseInfo.isNotDefaultSearchValue(year)) {
            weekendSql.andEqualTo(QuestionYearAreaView::getYear, year);
        }
        if (CollectionUtils.isNotEmpty(areaIdList)) {
            weekendSql.andIn(QuestionYearAreaView::getAreaId, areaIdList);
        }
        Example example = Example.builder(QuestionYearAreaView.class)
                .andWhere(weekendSql)
                .build();
        List<QuestionYearAreaView> questionAreaYearList = mapper.selectByExample(example);
        List<Long> questionIdList = questionAreaYearList.stream()
                .map(QuestionYearAreaView::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        return questionIdList;
    }
}
