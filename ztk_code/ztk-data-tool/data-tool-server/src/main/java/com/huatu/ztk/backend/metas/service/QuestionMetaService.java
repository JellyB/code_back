package com.huatu.ztk.backend.metas.service;

import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.question.dao.PointDao;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\8 0008.
 */
@Service
public class QuestionMetaService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionMetaService.class);
    @Autowired
    PointDao pointDao;

    /**
     * 获取科目的最大知识点情况
     * @param subjectId
     * @return
     */
    public List<QuestionPointTreeMin>  getErrorQuestionByModule(Integer subjectId){
        return pointDao.findAllPonitsBySubject(subjectId).stream().filter(i->i.getParent()==0).collect(Collectors.toList());
    }
}

