package com.huatu.tiku.teacher.dao.paper;

import com.huatu.tiku.entity.teacher.PaperAssembly;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.dao.provider.PaperAssemblyProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 组卷 - 试卷
 * Created by lijun on 2018/8/16
 */
@Repository
public interface PaperAssemblyMapper extends Mapper<PaperAssembly> {

    /**
     * 查询
     *
     * @param name      名称
     * @param beginTime 组卷开始时间
     * @param endTime   组卷结束时间
     * @param type      类型
     * @return 查询集合
     */
    @SelectProvider(type = PaperAssemblyProvider.class, method = "list")
    List<PaperAssembly> list(String name, String beginTime, String endTime, Long subjectId,PaperInfoEnum.PaperAssemblyType type);
}
