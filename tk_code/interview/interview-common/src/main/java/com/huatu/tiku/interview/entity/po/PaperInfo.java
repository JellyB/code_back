package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/11.
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_paper_info")
public class PaperInfo   extends BaseEntity{
    //试卷名称（标题）
    private String paperName;
    //班级id
    private long classId;
    //试卷类型(1单选 2多选 3排序 4简答)
    private int type;

    //考试类型(1课堂互动 2全真模考)
    private int examType;

}
