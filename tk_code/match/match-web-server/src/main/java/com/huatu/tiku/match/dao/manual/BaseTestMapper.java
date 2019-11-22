package com.huatu.tiku.match.dao.manual;

import com.huatu.tiku.match.dao.manual.provider.BaseTestProvider;
import com.huatu.tiku.match.bean.entity.BaseTest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 基础测试类 - 自定义sql 语句示例
 * Created by lijun on 2018/10/11
 */
@Repository
public interface BaseTestMapper extends Mapper<BaseTest> {

    /**
     * 查询所有
     */
    @Select("select * from area")
    List<BaseTest> listAll();

    /**
     * 根据状态查询所有
     */
    @SelectProvider(type = BaseTestProvider.class, method = "listAllByState")
    List<BaseTest> listAllByState(@Param("state") Integer state);
}
