package com.huatu.tiku.teacher.dao;

import com.huatu.tiku.entity.tag.Tag;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author jbzm
 * @date 2018下午5:37
 **/
@Repository
public interface TagMapper extends Mapper<Tag> {

}
