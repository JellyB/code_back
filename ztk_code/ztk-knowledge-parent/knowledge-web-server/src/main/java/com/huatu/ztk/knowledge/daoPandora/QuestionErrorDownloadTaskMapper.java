package com.huatu.ztk.knowledge.daoPandora;

import com.huatu.tiku.entity.download.QuestionErrorDownloadTask;
import com.huatu.ztk.knowledge.daoPandora.provider.QuestionErrorDownTaskProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangqingpeng
 * @title: QuestionErrorDownloadTaskMapper
 * @description: TODO
 * @date 2019-09-2416:15
 */
@Repository
public interface QuestionErrorDownloadTaskMapper extends Mapper<QuestionErrorDownloadTask> {


    /**
     * 分页查询用户的错题下载订单列表
     * @param uid
     * @param page
     * @param size
     * @return
     */
    @SelectProvider(type = QuestionErrorDownTaskProvider.class,method = "findOrderByUserId")
    List<HashMap> findOrderByUserId(long uid, int page, int size);

    /**
     * 查询用户错题下载订单总量
     * @param userId
     * @return
     */
    @SelectProvider(type = QuestionErrorDownTaskProvider.class,method = "countOrderByUserId")
    Long countOrderByUserId(@Param("userId") long userId);

    /**
     * 下载列表查询
     * @param uid
     * @param subject
     * @param page
     * @param size
     * @return
     */
    @SelectProvider(type = QuestionErrorDownTaskProvider.class,method = "findDownList")
    List<HashMap> findDownList(long uid, int subject, int page, int size);

    @SelectProvider(type = QuestionErrorDownTaskProvider.class,method = "countDownList")
    Map countDownList(long uid, int subject);
}
