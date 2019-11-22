package com.huatu.tiku.teacher.service.impl.subject;

import com.huatu.tiku.entity.subject.SubjectQuestionType;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.subject.SubjectQuestionTypeService;
import org.springframework.stereotype.Service;

/**
 * Created by huangqp on 2018\6\15 0015.
 */
@Service
public class SubjectQuestionTypeServiceImpl extends BaseServiceImpl<SubjectQuestionType> implements SubjectQuestionTypeService{
    public SubjectQuestionTypeServiceImpl() {
        super(SubjectQuestionType.class);
    }
}

