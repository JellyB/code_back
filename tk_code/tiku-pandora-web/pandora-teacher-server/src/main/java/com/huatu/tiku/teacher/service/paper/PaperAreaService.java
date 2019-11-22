package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.teacher.PaperArea;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
public interface PaperAreaService extends BaseService<PaperArea> {

    /**
     * 修改试卷-区域信息
     *
     * @param paperId   试卷ID
     * @param areaIds   区域ID 合集
     * @param typeInfo 试卷类型
     * @return
     */
    int savePaperAreaInfo(Long paperId, List<Long> areaIds, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 新增试卷-区域信息
     *
     * @param paperId   试卷ID
     * @param areaIds   区域ID 合集
     * @param typeInfo 试卷类型
     * @return
     */
    int insertPaperAreaInfo(Long paperId, List<Long> areaIds, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 删除试卷-区域信息
     *
     * @param paperId   试卷ID
     * @param typeInfo 试卷类型
     * @return
     */
    int deletePaperAreaInfo(Long paperId, PaperInfoEnum.TypeInfo typeInfo);

    /**
     * 获取试卷-区域信息
     *
     * @param paperId   试卷ID
     * @param typeInfo 试卷类型
     * @return
     */
    List<PaperArea> list(Long paperId, PaperInfoEnum.TypeInfo typeInfo);
}

