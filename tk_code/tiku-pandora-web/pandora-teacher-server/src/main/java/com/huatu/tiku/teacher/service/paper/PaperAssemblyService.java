package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.teacher.PaperAssembly;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by lijun on 2018/8/16
 */
public interface PaperAssemblyService extends BaseService<PaperAssembly> {

    /**
     * 保存试卷信息
     */
    int savePaperAssemblyInfo(PaperAssembly paperAssembly);

    /**
     * 详情 - 携带试题信息
     */
    PaperAssembly detailWithQuestion(Long paperId);

    /**
     * 列表查询
     *
     * @param name      名称
     * @param beginTime 组卷时间段 - 开始
     * @param endTime   组卷时间段 - 结束
     * @param type      试卷类型
     * @return 信息
     */
    List<PaperAssembly> list(String name, String beginTime, String endTime, Long subjectId,PaperInfoEnum.PaperAssemblyType type);

    /**
     * 删除组卷
     * @param id
     * @return
     */
     int deleteAssembly(Long id);
}
