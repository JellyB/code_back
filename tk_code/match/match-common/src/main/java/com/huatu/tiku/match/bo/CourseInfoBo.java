package com.huatu.tiku.match.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 下午5:39
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseInfoBo {

    private int classId;
    private String courseTitle;
    private long liveDate;
    private int price;
}
