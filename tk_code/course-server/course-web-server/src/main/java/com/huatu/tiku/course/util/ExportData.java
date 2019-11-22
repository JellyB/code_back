package com.huatu.tiku.course.util;

import com.github.crab2died.annotation.ExcelField;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-01 6:28 PM
 **/
@Getter
@Setter
@NoArgsConstructor
public class ExportData{
    @ExcelField(title = "课程id", order = 1)
    private String courseId;
    @ExcelField(title = "课件id", order = 2)
    private Integer lessonId;
    @ExcelField(title = "参与人数", order = 3)
    private Integer cnt;

    @Builder
    public ExportData(String courseId, Integer lessonId, Integer cnt) {
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.cnt = cnt;
    }
}