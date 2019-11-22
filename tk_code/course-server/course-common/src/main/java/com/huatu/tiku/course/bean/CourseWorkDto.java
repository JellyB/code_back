package com.huatu.tiku.course.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-07 5:21 PM
 **/

@NoArgsConstructor
@Getter
@Setter
public class CourseWorkDto {
    private String userName;
    private long syllabusId;

    @Builder
    public CourseWorkDto(String userName, long syllabusId) {
        this.userName = userName;
        this.syllabusId = syllabusId;
    }
}
