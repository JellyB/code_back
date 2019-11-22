package com.huatu.tiku.teacher.service.impl.question;

import com.google.common.collect.Maps;
import com.huatu.tiku.entity.common.QuestionType;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.question.QuestionTypeService;
import com.huatu.tiku.teacher.service.subject.SubjectQuestionTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\15 0015.
 */
@Service
public class QuestionTypeServiceImpl extends BaseServiceImpl<QuestionType> implements QuestionTypeService{

    @Autowired
    private SubjectQuestionTypeService subjectQuestionTypeService;
    public QuestionTypeServiceImpl() {
        super(QuestionType.class);
    }

    @Override
    public Object findTypeBySubject(Long subjectId) {
//        Example example = new Example(SubjectQuestionType.class);
//        example.and().andEqualTo("subjectId",subjectId);
//        List<SubjectQuestionType> relations = subjectQuestionTypeService.selectByExample(example);
//        List<Long> typeIds ;
//        if(CollectionUtils.isNotEmpty(relations)){
//            typeIds = relations.stream().map(i -> i.getQuestionType()).collect(Collectors.toList());
//        }else{
//            typeIds = Arrays.stream(QuestionInfoEnum.QuestionTypeEnum.values()).map(QuestionInfoEnum.QuestionTypeEnum::getCode)
//                    .map(Long::new)
//                    .collect(Collectors.toList());
//        }
//        Example questionTypeExample = new Example(QuestionType.class);
//        questionTypeExample.and().andIn("id",typeIds);
//        List<QuestionType> questionTypes = selectByExample(questionTypeExample);
//        if(CollectionUtils.isEmpty(questionTypes)){
//            return Lists.newArrayList();
//        }
//        questionTypes.sort(Comparator.comparingLong(i->i.getId()));
//        return questionTypes.stream().map(i->{
//            HashMap<Object, Object> typeMap = Maps.newHashMap();
//            typeMap.put("id",i.getId());
//            typeMap.put("name",i.getName());
//            typeMap.put("type",i.getBizType().intValue());
//            return typeMap;
//        }).collect(Collectors.toList());
        QuestionInfoEnum.SubjectQuestionTypeEnum subjectQuestionTypeEnum = QuestionInfoEnum.SubjectQuestionTypeEnum.create(subjectId);
        List<QuestionInfoEnum.QuestionTypeEnum> values = subjectQuestionTypeEnum.getValue();
        return values.stream().map(i->{
            HashMap<Object, Object> typeMap = Maps.newHashMap();
            typeMap.put("id",i.getCode());
            typeMap.put("name",i.getName());
            typeMap.put("type",i.getQuestionSaveTypeEnum().getCode());
            return typeMap;
        }).collect(Collectors.toList());
    }

    @Override
    public Long findIdByName(String name) {
//        Example example = new Example(QuestionType.class);
//        example.and().andEqualTo("name",name);
//        List<QuestionType> questionTypes = selectByExample(example);
//        if(CollectionUtils.isEmpty(questionTypes)){
//            return -1L;
//        }
//        return questionTypes.get(0).getId();
        if(StringUtils.isBlank(name)){
            return -1L;
        }
        for (QuestionInfoEnum.QuestionTypeEnum questionTypeEnum : QuestionInfoEnum.QuestionTypeEnum.values()) {
            if(questionTypeEnum.getName().equals(name)){
                return new Long(questionTypeEnum.getCode());
            }
        }
        return -1L;
    }
}