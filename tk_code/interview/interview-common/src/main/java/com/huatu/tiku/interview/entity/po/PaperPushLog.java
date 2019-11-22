package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/5/16.
 * 课堂互动推送  记录
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_paper_push_log")
public class PaperPushLog extends BaseEntity{

    //课堂互动id
    private long paperId;
    //班级id
    private long classId;

}
