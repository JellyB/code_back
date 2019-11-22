package com.huatu.tiku.teacher.dao.paper;

import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.teacher.dao.provider.paper.PaperEntityProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@Repository
public interface PaperEntityMapper extends Mapper<PaperEntity> {

    /**
     * 获取各个科目下的试题数量
     */
    List<Map<String, Long>> countGroupBySubject();

    /**
     * 列表查询
     *
     * @param mode        试卷属性
     * @param year        年份
     * @param specialFlag 是否是特等教师
     * @param missFlag    是否残缺
     * @param subjectId   科目ID
     * @param bizStatus   试卷状态
     * @param areaIds     区域ID 数组
     * @param name        试卷名称
     */
    @SelectProvider(type = PaperEntityProvider.class, method = "list")
    List<HashMap<String, Object>> list(int mode, int year, int specialFlag, int missFlag, long subjectId, int bizStatus, String areaIds, String name);

    /**
     * 获取试题卷的考试时间
     */
    @SelectProvider(type = PaperEntityProvider.class, method = "getEntityPaperTime")
    List<String> getEntityPaperTime();
}

