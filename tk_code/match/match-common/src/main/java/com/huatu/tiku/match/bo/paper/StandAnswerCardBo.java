package com.huatu.tiku.match.bo.paper;

import com.huatu.tiku.match.bo.GiftBo;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.CardUserMeta;
import com.huatu.ztk.paper.bean.MatchCardUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-11 上午11:49
 **/
@Data
public class StandAnswerCardBo implements Serializable{


    private static final long serialVersionUID = 1L;
    private long id;
    private long userId;
    private int subject;
    private int catgory;
    private double score;
    private double difficulty;
    private String name;
    private int rcount;
    private int wcount;
    private int ucount;
    private int status;
    private int type;
    private int terminal;
    private int expendTime;
    private int speed;
    private long createTime;
    private int lastIndex;
    private int remainingTime;
    private Long cardCreateTime;
    private int[] corrects;
    private String[] answers;
    private int[] times;
    private List<QuestionPointTree> points;
    private int[] doubts;
    private Map<Integer, Integer> moduleStatus;
    private Map<Integer, Long> moduleCreateTime;
    private String iconUrl;
    private Integer hasGift = 0;
    private Paper paper;
    private CardUserMeta cardUserMeta;
    private MatchCardUserMeta matchMeta;
    private String idStr;
    private Long currentTime;
    /**
     * 大礼包信息
     */
    private GiftBo giftInfo;
    /**
     * 字符串分数
     */
    private String scoreStr;

}
