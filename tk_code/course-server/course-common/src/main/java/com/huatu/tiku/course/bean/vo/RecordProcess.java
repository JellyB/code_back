package com.huatu.tiku.course.bean.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：录播上传进度
 *
 * @author biguodong
 * Create time 2019-03-15 10:18 AM
 **/

@NoArgsConstructor
@Getter
@Setter
public class RecordProcess {

    private long syllabusId;
    private String userName;
    private int userId;
    private int terminal;
    private String cv;
    private int subject;

    @Builder
    public RecordProcess(long syllabusId, String userName, int userId, int terminal, String cv, int subject) {
        this.syllabusId = syllabusId;
        this.userName = userName;
        this.userId = userId;
        this.terminal = terminal;
        this.cv = cv;
        this.subject = subject;
    }
}
