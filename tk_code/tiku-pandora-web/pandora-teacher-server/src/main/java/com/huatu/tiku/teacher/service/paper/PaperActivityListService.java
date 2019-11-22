package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/14
 * @描述
 */

public interface PaperActivityListService   extends BaseService<PaperActivity> {


    /**
     * 保存模块信息
     *
     * @param activityId     试卷ID
     * @param moduleNames 模块名称
     * @return
     */
    List<PaperModuleInfo> saveModuleInfo(Long activityId, List<String> moduleNames);


    /**
     * 删除模块信息
     *
     * @param paperId   试卷ID
     * @param moduleIds 模块名称
     * @return
     */
    int deleteModuleInfoByIdList(Long paperId, List<Integer> moduleIds);

    /**
     * 批量修改
     * @param paperId 试卷ID
     * @param moduleInfoList 模块信息
     * @return
     */
    int updateModuleInfo(Long paperId, List<PaperModuleInfo> moduleInfoList);

    /**
     * 通过模块名称获取模块id
     * @param paperId
     * @param moduleName
     * @return
     */
     int getModuleIdByName(Long paperId, String moduleName);

    /**
     * 修改试卷模块的排列顺序（不能修改模块的名称）
     * @param activityId
     * @param moduleInfoList
     * @return
     */
    int changeModuleInfo(long activityId, List<PaperModuleInfo> moduleInfoList);
}
