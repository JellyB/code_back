package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-28 下午5:16
 **/

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PastMatchCourseInfo implements Serializable{

    private String advertUrl;
    private int collectionCourseId;
    private int subjectId;
    private int tag;
}
