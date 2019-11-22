package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/27
 * @描述
 */
@AllArgsConstructor
@Getter
public enum CourseExerciseSearchTypeEnum {
    QUESTION_ID(1, "单题ID",0),
    QUESTION_CONTENT(2, "单题标题",0),
    PAPER_ID(3, "套题ID",1),
    PAPER_CONTENT(4, "套题标题",1);

    private Integer value;
    private String title;
    private Integer type;

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(Integer type){
      this.type=type;

    }

    public static CourseExerciseSearchTypeEnum create(Integer value) {
        return (CourseExerciseSearchTypeEnum) EnumUtils.getEnum(values(), value);
    }


}
