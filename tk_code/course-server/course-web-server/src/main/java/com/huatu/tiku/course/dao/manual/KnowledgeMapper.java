package com.huatu.tiku.course.dao.manual;

import com.huatu.tiku.entity.knowledge.Knowledge;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * 描述：知识点 mapper
 *
 * @author biguodong
 * Create time 2019-03-12 9:12 PM
 **/
@Repository
public interface KnowledgeMapper extends Mapper<Knowledge> {

}
