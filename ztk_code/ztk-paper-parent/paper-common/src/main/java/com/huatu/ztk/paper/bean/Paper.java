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

@Document(collection = "ztk_paper")
@Data
public class Paper implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private int id; //试卷id
    private String name; //试卷名字
    private int year; //年份
    private int area;//所属区域
    private int time;//答题时间
    private int score;//分数
    private int passScore;//及格线
    private int qcount;//题量
    private double difficulty;//难度系数
    private int type;
    private List<Module> modules;//模块列表
    private int status;
    private int catgory;//科目
    private List<Integer> questions;//单题id+复合题子题id
    @Getter(onMethod = @__({@JsonIgnore}))
    private int createdBy;//创建人
    @Getter(onMethod = @__({@JsonIgnore}))
    private Date createTime;//创建时间
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private PaperUserMeta userMeta;//答题统计数据
    private PaperMeta paperMeta;
    private List<Integer> bigQuestions;//大题id列表（包含单题+复合题主题id）
    private List<Double> scores;       //每道小题的分数
    private Integer scoreFlag;      //null或者0平均分做分数计算，1使用单题自有的分数做统计计算
    private String paperName;  //小程序需要展示的名称(考试类型+考试科目+地区)
    private int categoryId;   //小程序使用-考试类型
    private List<Integer> areaIds;   //题库小程序支持多地区

}
