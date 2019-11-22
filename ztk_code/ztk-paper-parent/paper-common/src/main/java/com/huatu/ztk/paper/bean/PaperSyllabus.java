package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/20
 * @描述 课程大纲绑定阶段测试
 */

@Document(collection = "ztk_paper_syllabus")
@Data
@Builder
public class PaperSyllabus implements Serializable {

    @Id
    private int id;        //id
    private int courseId;   //课程ID
    private int syllabusId;//课程大纲ID
    private List<Long> paperId;    // 试卷ID
    private int startTimeIsEffective;//考试时间是否起作用 0 否;1 是
    private Date createTime;  //创建时间
    private Date updateTIme;  //更新时间

}