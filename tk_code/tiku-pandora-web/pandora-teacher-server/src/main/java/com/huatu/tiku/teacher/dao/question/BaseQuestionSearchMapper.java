package com.huatu.tiku.teacher.dao.question;

import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.request.question.v1.BaseQuestionSearchReq;
import com.huatu.tiku.teacher.dao.provider.question.TeacherBaseQuestionSearchProvider;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lijun on 2018/7/16
 */
@Repository
public interface BaseQuestionSearchMapper {
    /**
     * 试题信息列表查询  默认字段使用 -1
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "list")
    List<HashMap<String, Object>> list(@Param("arg0") BaseQuestionSearchReq baseQuestionSearchReq);

    /**
     * 获取试题的简要信息
     *
     * @param questionId   试题ID
     * @param questionType 试题信息
     * @return
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionSimpleInfo")
    List<HashMap<String, Object>> getQuestionSimpleInfo(long questionId, int questionType);

    /**
     * 批量查询试题的简要信息
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionSimpleInfoForList")
    List<HashMap<String, Object>> getQuestionSimpleInfoForList(@Param("arg0") List<BaseQuestion> params);

//    /**
//     * 获取试题的来源信息
//     */
//    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionSourceInfo")
//    List<HashMap<String, Object>> getQuestionSourceInfo(@Param("arg0") long questionId);

    /**
     * 获取试题标签信息
     *
     * @param questionId 试题ID
     * @return "id,name"->tagID,TagName
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionTagInfo")
    List<HashMap<String, Object>> getQuestionTagInfo(@Param("arg0") long questionId);

    /**
     * 通过ID 批量查询组装数据
     *
     * @param questionIds 试题ID 集合
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "listAllByQuestionId")
    List<HashMap<String, Object>> listAllByQuestionId(@Param("arg0") List<Long> questionIds);

    /**
     * 查询某个科目下的 MongoDB数据
     * 试题ID和knowledgeId作为column
     *
     * @param subjectId
     * @return
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "listAllMongoDBQuestionBySubject")
    List<HashMap<String, Object>> listAllMongoDBQuestionBySubject(@Param("arg0") long subjectId);

    /**
     * 分组查询复合题子题个数，且返回子题个数小于5的复合题
     *
     * @return
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "findQuestionIdByMultiId")
    List<HashMap<String, Object>> findQuestionIdByMultiId();

    /**
     * 批量查询试题来源
     *
     * @param questionIds
     * @return
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionSourceForList")
    List<HashMap<String, Object>> getQuestionSourceForList(@Param("arg1") List<Long> questionIds);

    /**
     * 批量查询试题来源
     *
     * @param questionIds
     * @return
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionSourceForListByType")
    List<HashMap<String, Object>> getQuestionSourceForListByType(List<Long> questionIds,List<ActivityTypeAndStatus.ActivityTypeEnum> types);

    /**
     * 批量查询试题来源
     *
     * @param questionIds
     * @return
     */
    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "getQuestionSourceForListByTypeWithEntity")
    List<HashMap<String, Object>> getQuestionSourceForListByTypeWithEntity(List<Long> questionIds,List<ActivityTypeAndStatus.ActivityTypeEnum> types,Boolean entityFlag);

    /**
     * 可以查询已经被删除的试题
     *
     * @param questionId
     * @return
     */

    @SelectProvider(type = TeacherBaseQuestionSearchProvider.class, method = "findBaseQuestion")
    HashMap findBaseQuestion(@Param("arg0") Long questionId);


}