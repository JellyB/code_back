package com.huatu.tiku.teacher.dao.paper;

import com.huatu.tiku.entity.teacher.PaperActivitySubject;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@Repository
public interface PaperActivitySubjectMapper extends Mapper<PaperActivitySubject> {
    List<Map<String,Long>> countGroupBySubject();
}

