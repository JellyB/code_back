package com.huatu.tiku.course.bean.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-08-29 5:28 PM
 **/

@NoArgsConstructor
@Getter
@Setter
public class AnswerCardInfo {

    /**
     * 1 行测 2 申论
     */
    private int status;
    private int wcount;
    private int ucount;
    private int rcount;
    private int qcount;
    private long  id;

    @Builder
    public AnswerCardInfo(int status, int wcount, int ucount, int rcount, int qcount, long id) {
        this.status = status;
        this.wcount = wcount;
        this.ucount = ucount;
        this.rcount = rcount;
        this.qcount = qcount;
        this.id = id;
    }
}
