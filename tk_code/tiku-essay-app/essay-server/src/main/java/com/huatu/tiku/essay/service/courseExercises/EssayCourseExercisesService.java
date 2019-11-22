package com.huatu.tiku.essay.service.courseExercises;

import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExerciseEditVo;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExerciseQuestionVO;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExerciseSearchVo;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExercisesRepVo;
import com.huatu.tiku.essay.vo.resp.courseExercises.ExercisesListVO;

import java.util.List;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述
 */
public interface EssayCourseExercisesService {

    /**
     * 根据课程id和用户昵称获取退款金额
     *
     * @param userName
     * @param courseId
     * @return
     */
    Object getRefundMoney(String userName, long courseId);

    /**
     * 待绑定试题列表

     * @return
     */
    PageUtil<Object> getQuestionList(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo);

    /**
     * courseExercisesRepVo
     */
    Object saveCourseExercise(AdminCourseExercisesRepVo courseExercisesRepVo);


    /**
     * @param courseWareId 课件ID
     * @return
     */
    List<AdminCourseExerciseQuestionVO> getSelectedQuestionList( Long courseWareId,int courseType);


    /**
     * 编辑练习题序号
     *
     * @param exerciseEditVos
     * @return
     */
    Object editCourseExercise(List<AdminCourseExerciseEditVo> exerciseEditVos);

    /**
     * 删除课后作业
     *
     * @param id
     * @return
     */
    Object delCourseExercise(Long id);

    /**
     * 多题列表
     *
     * @param userId
     * @param courseWareId
     * @param courseType
     * @return
     */
    ExercisesListVO getCourseExerciseQuestionList(Integer userId, Long courseWareId, Integer courseType, Long syllabusId);

    /**
     * 切换科目,清除绑定数据
     *
     * @param courseWareId
     * @return
     */
    Object cleanBindData(Long courseWareId,int courseType);

    /**
     * 绑定练习题，通知 php
     * @param afterExercisesNum
     * @param courseWardId
     * @param courseType
     * @param questionType
     */
    void noticePHPUpdateCourseNum(int afterExercisesNum, Long courseWardId, int courseType, int questionType);

}
