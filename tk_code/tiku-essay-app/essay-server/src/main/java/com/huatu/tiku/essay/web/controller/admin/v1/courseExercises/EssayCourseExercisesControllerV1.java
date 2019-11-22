package com.huatu.tiku.essay.web.controller.admin.v1.courseExercises;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.essay.essayEnum.CourseExerciseSearchTypeEnum;
import com.huatu.tiku.essay.service.courseExercises.EssayCourseExercisesService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExerciseCleanDataVo;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExerciseEditVo;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExerciseSearchVo;
import com.huatu.tiku.essay.vo.admin.courseExercise.AdminCourseExercisesRepVo;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 课后作业相关
 *
 * @author zhangchong
 */
@RestController
@Slf4j
@RequestMapping("/end/v1/courseExercise")
public class EssayCourseExercisesControllerV1 {

    @Autowired
    private EssayCourseExercisesService essayCourseExercisesService;

    /**
     * 获取课程对应退款金额
     *
     * @param userName
     * @param courseId
     * @return
     */

    @LogPrint
    @GetMapping("/getRefundMoney")
    public Object getRefundMoney(String userName, long courseId) {

        return essayCourseExercisesService.getRefundMoney(userName, courseId);
    }


    /**
     * 待绑定试题列表
     *
     * @return
     */
    @LogPrint
    @GetMapping("/question/list")
    public Object getQuestionList(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo) {

        return essayCourseExercisesService.getQuestionList(adminCourseExerciseSearchVo);
    }


    /**
     * 已选题目列表
     *
     * @param courseWareId 课件ID
     * @return
     */
    @GetMapping("/selected/list")
    public Object getSelectedQuestionList(@RequestParam Long courseWareId,
                                          @RequestParam int courseType) {

        return essayCourseExercisesService.getSelectedQuestionList(courseWareId, courseType);
    }


    /**
     * 绑定课后作业
     *
     * @param courseExercisesRepVo
     * @return
     */
    @PostMapping("save")
    public Object saveBindRelation(@RequestBody @Validated AdminCourseExercisesRepVo courseExercisesRepVo) {
        log.info("绑定课后作业添加参数:{}", JsonUtil.toJson(courseExercisesRepVo));
        essayCourseExercisesService.saveCourseExercise(courseExercisesRepVo);
        return SuccessMessage.create("绑定成功!");
    }


    /**
     * 编辑关联练习题目
     *
     * @param exerciseEditVos
     * @return
     */
    @PutMapping("edit")
    public Object editBindRelation(@RequestBody List<AdminCourseExerciseEditVo> exerciseEditVos) {

        essayCourseExercisesService.editCourseExercise(exerciseEditVos);
        return SuccessMessage.create("编辑成功!");
    }

    /**
     * 删除(撤销按钮)
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Object delBindRelation(@PathVariable Long id) {
        return essayCourseExercisesService.delCourseExercise(id);
    }

    /**
     * 切换科目，清除数据
     *
     * @return
     */
    @PostMapping("change/subject")
    public Object cleanBindData(@RequestBody AdminCourseExerciseCleanDataVo adminCourseExerciseCleanDataVo) {
        Long courseWareId = adminCourseExerciseCleanDataVo.getCourseWareId();
        Integer courseType = adminCourseExerciseCleanDataVo.getCourseType();
        essayCourseExercisesService.cleanBindData(courseWareId, courseType);
        return SuccessMessage.create("清除数据成功!");
    }

    /**
     * 根据题目类型获取枚举值
     *
     * @param type
     * @return
     */
    @GetMapping("searchType")
    public Object getQuestionSearchType(@RequestParam Integer type) {

        CourseExerciseSearchTypeEnum[] values = CourseExerciseSearchTypeEnum.values();
        List<HashMap> mapList = Arrays.stream(values).filter(searType -> searType.getType() == type)
                .map(searTypeEnum -> {
                    HashMap map = new HashMap();
                    map.put("value", searTypeEnum.getValue());
                    map.put("text", searTypeEnum.getTitle());
                    return map;
                }).collect(Collectors.toList());
        return mapList;
    }
}
