package com.huatu.tiku.response.question;

import com.huatu.tiku.response.BaseResp;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Data
public class BaseQuestionResp extends BaseResp {
    /**
     * 试题id
     */
    private Long questionId;
    /**
     * 试题类型
     */
    private Integer questionType;
    /**
     * 题干
     */
    private String stem;
    /**
     * 标签名称
     */
    private List<String> tagNames;
    /**
     * 年份
     */
    private Integer bizStatus;
    /**
     * 创建时间
     */
    private Date gmtCreate;
    /**
     * 修改时间
     */
    private Date gmtModify;
    /**
     * 修改者
     */
    private Integer modifierId;
    /**
     * 创建者
     */
    private Integer creatorId;


}
