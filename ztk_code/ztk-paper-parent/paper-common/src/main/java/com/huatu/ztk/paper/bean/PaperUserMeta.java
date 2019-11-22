package com.huatu.ztk.paper.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * 试题练习统计bean
 * Created by shaojieyue
 * Created time 2016-04-29 16:44
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_paper_user_meta")
public class PaperUserMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;//记录id
    private long currentPracticeId;//当前未完成的练习id(练习id)
    private int paperId;//试卷id
    private long uid;//用户id
    private int finishCount;//完成次数,交卷一次+1
    private List<Long> practiceIds;//已经练习的id列表，包含未完成的
    /**
     * 大纲Id
     */
    private long syllabusId;
    //小程序（未完成练习ID）转化为字符串
    @Transient
    private String currentPracticeIdStr;
    @Transient
    private List<String> practiceIdsStr;
    /**
     * 题库小程序增加用户分数
     */
    private Double score;
}
