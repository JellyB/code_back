package com.huatu.ztk.paper.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.huatu.ztk.knowledge.bean.Module;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 试卷bean
 * Created by shaojieyue on 4/20/16.
 */

@Document(collection = "ztk_essay_paper")
@Data
public class EssayPaper implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int id; //试卷id
    private String name; //试卷名字
    private int year; //年份
    private int area;//所属区域
    private int time;//答题时间
    private int score;//分数
    private int type;
    private int status;
    private int catgory;//科目
    @Getter(onMethod = @__({@JsonIgnore}))
    private String createdBy;//创建人
    @Getter(onMethod = @__({@JsonIgnore}))
    private Date createTime;//创建时间

}
