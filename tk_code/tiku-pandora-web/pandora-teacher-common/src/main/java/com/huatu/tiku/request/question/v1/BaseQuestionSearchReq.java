package com.huatu.tiku.request.question.v1;

import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.request.PageBaseReq;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lijun on 2018/9/18
 */
@Data
@NoArgsConstructor
public class BaseQuestionSearchReq extends PageBaseReq {

    /**
     * 试题ID
     */
    private Integer questionId = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 内容
     */
    private String content = BaseInfo.SEARCH_INPUT_DEFAULT;

    /**
     * 内容检索策略
     * searchType 传入 1 << 1 + 1 << 2 + 1 << 3 分别代表 题干、选项、材料 是否需要作为搜索条件
     */
    private Integer contentSearchType = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     *
     */
    private Integer questionType = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 是否为模拟题
     */
    private Integer mode = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 是否为废题
     */
    private Integer availFlag = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 难度
     */
    private Integer difficultyLevel = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 科目
     */
    private Long subject = BaseInfo.SEARCH_DEFAULT_LONG;

    /**
     * 区域ID
     */
    private String areaId = BaseInfo.SEARCH_INPUT_DEFAULT;

    /**
     * 年份
     */
    private Integer year = BaseInfo.SEARCH_DEFAULT_INTEGER;


    /**
     * 是否有来源
     */
    private Integer sourceFlag = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 试题状态
     */
    private Integer bizStatus = BaseInfo.SEARCH_DEFAULT_INTEGER;

    /**
     * 知识点
     */
    private Long knowledgeId = BaseInfo.SEARCH_DEFAULT_LONG;
    private String knowledgeIds = BaseInfo.SEARCH_INPUT_DEFAULT;

    /**
     * 标签
     */
    private String tagIds = BaseInfo.SEARCH_INPUT_DEFAULT;

}
