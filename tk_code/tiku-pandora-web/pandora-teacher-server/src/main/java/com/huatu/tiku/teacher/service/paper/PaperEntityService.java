package com.huatu.tiku.teacher.service.paper;

import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.BaseService;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by lijun on 2018/8/2
 */
public interface PaperEntityService extends BaseService<PaperEntity> {

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
    List<HashMap<String, Object>> list(int mode, int year, int specialFlag, int missFlag, long subjectId, int bizStatus, String areaIds, String name);

    /**
     * 根据模块ID 获取模块名称
     *
     * @param paperId    试卷ID
     * @param moduleName 模块名称
     * @return
     */
    int getModuleIdByName(Long paperId, String moduleName);

    /**
     * 存储模块信息
     *
     * @param paperId     试卷ID
     * @param moduleNames 模块名称
     * @return 操作成功数
     */
    List<PaperModuleInfo> saveModuleInfo(Long paperId, List<String> moduleNames);

    /**
     * 删除模块信息
     *
     * @param paperId    试卷ID
     * @param moduleName 模块名称
     * @return 操作成功数
     */
    int deleteModuleInfo(Long paperId, List<String> moduleName);

    /**
     * 删除模块信息
     *
     * @param paperId   试卷ID
     * @param moduleIds 模块名称
     * @return 操作成功数
     */
    int deleteModuleInfoByIdList(Long paperId, List<Integer> moduleIds);

    /**
     * 批量修改
     *
     * @param paperId        试卷ID
     * @param moduleInfoList 模块信息
     * @return 操作成功数量
     */
    int updateModuleInfo(Long paperId, List<PaperModuleInfo> moduleInfoList);

    /**
     * 保存试题卷 - 处理区域信息
     */
    int savePaper(PaperEntity paperEntity);

    /**
     * 试卷详情 - 处理区域信息
     */
    PaperEntity detail(long paperId);

    /**
     * 删除试卷信息
     */
    int deletePaper(Long paperId);

    /**
     * 添加实体卷- 处理区域信息 - 携带paperId
     */
    int insertPaper(PaperEntity paperEntity);

    /**
     * 修改 试卷发布状态
     */
    int updateReleaseStatus(Long paperId);

    /**
     * 获取试题卷所有的考试时间
     */
    List<String> getEntityPaperTime();

    /**
     * 验证试卷、模块信息方法
     */
    BiConsumer<Long, Integer> createPaperQuestionValidate();

    /**
     * 修改试卷模块的排列顺序（不能修改模块的名称）
     * @param paperId
     * @param moduleInfoList
     * @return
     */
    int changeModuleInfo(long paperId, List<PaperModuleInfo> moduleInfoList);

    /**
     * 修改试卷题源标识
     * @param paperId
     * @return
     */
    int updateSourceFlag(long paperId);
}
