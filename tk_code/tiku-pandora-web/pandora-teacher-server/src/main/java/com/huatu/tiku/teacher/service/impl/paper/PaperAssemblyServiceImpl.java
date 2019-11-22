package com.huatu.tiku.teacher.service.impl.paper;

import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.entity.teacher.PaperAssembly;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.paper.PaperAssemblyMapper;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/16
 */
@Service
public class PaperAssemblyServiceImpl extends BaseServiceImpl<PaperAssembly> implements PaperAssemblyService {

    public PaperAssemblyServiceImpl() {
        super(PaperAssembly.class);
    }

    @Autowired
    private PaperAssemblyQuestionService assemblyQuestionService;

    @Autowired
    private PaperAssemblyMapper mapper;

    @Transactional
    @Override
    public int savePaperAssemblyInfo(PaperAssembly paperAssembly) {
        Integer save = save(paperAssembly);
        String questionIds = paperAssembly.getQuestionIds();
        if (StringUtils.isNotBlank(questionIds)) {
            List<Long> saveQuestionIdList = Arrays.stream(questionIds.split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            assemblyQuestionService.saveQuestionInfo(paperAssembly.getId(), saveQuestionIdList);
        }
        return save;
    }

    @Override
    public PaperAssembly detailWithQuestion(Long paperId) {
        if (null == paperId) {
            return null;
        }
        PaperAssembly paperAssembly = selectByPrimaryKey(paperId);
        if (null != paperAssembly) {
            List<QuestionSimpleInfo> list = assemblyQuestionService.list(paperId);
            paperAssembly.setQuestionSimpleInfoList(list);
        }
        return paperAssembly;
    }

    @Override
    public List<PaperAssembly> list(String name, String beginTime, String endTime, Long subjectId, PaperInfoEnum.PaperAssemblyType type) {
        return mapper.list(name, beginTime, endTime, subjectId, type);
    }


    /**
     * 删除组卷
     *
     * @param id
     * @return
     */
    @Override
    public int deleteAssembly(Long id) {
        return deleteByPrimaryKey(id);
    }
}
