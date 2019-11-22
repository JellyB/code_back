package com.huatu.tiku.teacher.service;

import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperSearchInfo;
import com.huatu.tiku.request.paper.InsertActivityPaperReq;
import com.huatu.tiku.request.paper.SelectActivityReq;
import com.huatu.tiku.request.paper.UpdateActivityPaperReq;
import com.huatu.tiku.service.BaseService;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
public interface PaperActivityService extends BaseService<PaperActivity> {
    /**
     * 查询考试配置
     *
     * @param activityIds
     * @return
     */
    List<PaperActivity> selectByIds(List<Long> activityIds);

    /**
     * 添加考试
     *
     * @param insertActivityPaperReq
     * @param paperId                需要绑定的实体卷ID
     */
    Map insertPaper(InsertActivityPaperReq insertActivityPaperReq, Long paperId);

    /**
     * 添加活动-绑定活动卷跟试实体卷
     *
     * @param paperActivity
     */
    void savePaper(PaperActivity paperActivity);


    /**
     * 活动卷及相关删除
     *
     * @param paperId
     */
    void deletePaper(Long paperId);

    /**
     * 修改试卷及其相关
     *
     * @param updateActivityPaperReq
     */
    void updatePaper(UpdateActivityPaperReq updateActivityPaperReq);

    /**
     * 查询相关属性
     *
     * @param id
     * @return
     */
    SelectActivityReq findById(Long id);

    /**
     * 解绑试卷中的某道试题
     *
     * @param paperId
     * @param questionId
     */
    void unBindWithQuestion(Long paperId, Long questionId);

    /**
     * 解绑试卷中的某一题序下的试题
     *
     * @param paperId
     * @param sort
     */
    void unBindWithSort(Long paperId, Integer sort);

    /**
     * 重置试卷试题及题序
     *
     * @param paperId
     * @param questionIds
     */
    void updatePaperQuestion(Long paperId, List<Long> questionIds);

    /**
     * 试卷绑定特定的试题到特定的题序
     *
     * @param paperId
     * @param questionId
     * @param sort
     * @param moduleName
     */
    void saveBindWithQuestion(Long paperId, Long questionId, Integer sort, String moduleName);


    /**
     * 活动卷绑定实体卷
     *
     * @param id
     * @param paperId
     */
    void bindEntityPaperId(Long id, Long paperId);

    /**
     * 统计活动数据
     *
     * @param paperId
     */
    Map countExamInfo(Long paperId);

    /**
     * 通过关联试卷查询活动卷id
     *
     * @param paperId 试题卷ID
     * @return 活动卷ID
     */
    List<Long> findByPaperId(Long paperId);


    /**
     * @param activityType  活动类型
     * @param activityState 活动状态
     * @param year          年份
     * @param areaIds       地区
     * @param activityName  活动名称
     * @return
     */
    List<HashMap<String, Object>> getActivityList(Integer activityType, Integer activityState, Integer year,
                                                  String areaIds, String activityName, List<Integer> subjectId,
                                                  String startTime, String endTime,int searchType);


    /**
     * 活动列表-查询活动报名人数
     *
     * @param id
     */
    Map<String, Integer> activityData(Long id, Long subjectId);

    /**
     * 修改活动发布状态
     *
     * @param activityId
     * @return
     */
    int updatePaperStatus(Long activityId);


    /**
     * 查询活动卷信息
     *
     * @param acticityId 试卷ID
     * @return 试卷详情
     */
    PaperSearchInfo paperDetail(Long acticityId);

    /**
     * 获取标签
     *
     * @param subjectId
     * @return
     */
    List<HashMap<Integer, String>> getTags(Long subjectId, Integer level);


    /**
     * 信息验证规则
     */
    BiConsumer<Long, Integer> createPaperQuestionValidate();

    /**
     * 信息验证规则
     */
    int saveActivityInfo(PaperActivity paperActivity);

    /**
     * 根据活动卷信息生成活动卷
     */
    void createActivityByPaperId();

    /**
     * 保存课程-课程大纲-阶段测试绑定关系
     *
     * @param courseId
     * @param syllabusId
     * @param paperInfo
     */
    void saveFormativePaper(int courseId, int syllabusId,
                            List<Long> paperInfo);

    /**
     * 单题算分,paperActivity存放总分=试卷所有试题总分
     * 不单题算分,取用户填写的总分
     *
     * @param paperActivity
     * @return
     */
    Double getScore(PaperActivity paperActivity);


    /**
     * 活动卷是否参与题源标识修改
     * @param id
     * @return
     */
    int updateSourceFlag(Long id);

    /**
     * 分页查询教育小程序的活动信息
     * @param activityTypeEnum
     * @param status
     * @param name
     * @param subjectId
     * @param startTime
     * @param endTime
     * @param tagId
     * @param paperId
     * @return
     */
    List<HashMap<String,Object>> getActivityListForEdu(ActivityTypeAndStatus.ActivityTypeEnum activityTypeEnum, int status, String name, int subjectId, long startTime, long endTime, int tagId, String paperId);

    /**
     * 根据活动卷id查询小程序二维码
     * @param id
     * @return
     */
	Object getQRCode(Long id, Long subjectId);
}

