package com.huatu.tiku.teacher.dao;

import com.huatu.tiku.entity.common.User;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author jbzm
 * @date 2018下午3:45
 **/
@Component
public interface UserMapper extends Mapper<User> {

    @Select(value = "select * from t_user where username=#{userName}")
    User findByUserName(String userName);
}
