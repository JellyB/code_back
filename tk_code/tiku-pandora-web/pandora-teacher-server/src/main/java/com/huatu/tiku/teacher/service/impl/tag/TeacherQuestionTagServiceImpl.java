package com.huatu.tiku.teacher.service.impl.tag;

import com.huatu.tiku.entity.tag.QuestionTag;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.tag.TeacherQuestionTagService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
@Service
public class TeacherQuestionTagServiceImpl extends BaseServiceImpl<QuestionTag> implements TeacherQuestionTagService {
    public TeacherQuestionTagServiceImpl() {
        super(QuestionTag.class);
    }

    /**
     * 查询标签绑定的试题数量
     * @param id
     * @return
     */
    public Object questionCount(Long id){
        Example example = new Example(QuestionTag.class);
        example.and().andEqualTo("tagId", id);
        List<QuestionTag> questionTags = selectByExample(example);
        if (CollectionUtils.isNotEmpty(questionTags)){
            return questionTags.size();
        }
        return 0;
    }

}

