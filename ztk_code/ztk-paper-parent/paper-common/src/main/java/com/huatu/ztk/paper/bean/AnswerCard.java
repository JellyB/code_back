package com.huatu.ztk.paper.bean;

import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 答题卡
 * Created by shaojieyue on 4/21/16.
 */

@Data
@Document(collection = "ztk_answer_card")
public abstract class AnswerCard implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private long id;
    private long userId;//用户id
    private int subject;//知识点类目
    private int catgory;//考试科目
    private double score;//预测分数（学员得分）
    private double difficulty;//难度系数
    private String name;//答题卡名称
    private int rcount;//答题正确数量
    private int wcount;//答题错误数量
    private int ucount;//未做答题数量
    private int status;//答题卡状态 已完成，未做完
    private int type;//答题卡 类型
    private int terminal;//答题终端: pc,移动
    private int expendTime;//耗时
    private int speed;//平均答题速度
    private long createTime;//当作交卷时间使用
    private int lastIndex;//本次答题做到第几题
    private int remainingTime;//剩余时间
    private Long cardCreateTime;//当作交卷时间使用


    /**
     * 答题是否正确 0:没作答 1:正确 2:错误
     */
    private int[] corrects;//是否正确

    /**
     * 答题卡 答案组成
     * 单选:就是题的索引+1,如1,2,3,
     * 多选:则是索引组合,如:12,124,14
     */
    private String[] answers;//答题记录
    private int[] times;//每道题的耗时 单位是秒

    @Transient
    private List<QuestionPointTree> points;


    /**
     * 疑问 0：未标记  1：标记未有疑问
     */
    private int[] doubts;


    /**
     * 招警机考
     * 用户各模块完成情况 0初始化，1进行中 2结束
     */
    private Map<Integer, Integer> moduleStatus;
    /**
     * 招警机考
     * 用户各模块开始答题时间 -1默认，其他创建时间毫秒值
     */
    private Map<Integer, Long> moduleCreateTime;

    /**
     * 答题卡阶段，0初始化阶段，1交卷并且同步用户试题数据完成
     */
    private int stage;

    /**
     * 活动图标
     */
    @Transient
    private String iconUrl;

    @Transient
    private Integer hasGift = 0;//是否有活动信息 默认 没有活动信息

    /**
     * 阶段测试使用和大纲绑定
     */
    private Long syllabusId;
    /**
     * 7.1.141版本之后客户端取字符串分数
     */
    @Transient
    private String scoreStr;

}
