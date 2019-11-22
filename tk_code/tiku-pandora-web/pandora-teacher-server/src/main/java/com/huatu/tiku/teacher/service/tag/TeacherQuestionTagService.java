package com.huatu.tiku.teacher.service.tag;

import com.huatu.tiku.entity.tag.QuestionTag;
import com.huatu.tiku.service.BaseService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
public interface TeacherQuestionTagService extends BaseService<QuestionTag> {

    Object questionCount(Long id);
}
