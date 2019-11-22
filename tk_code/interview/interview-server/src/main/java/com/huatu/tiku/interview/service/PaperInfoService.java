package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.po.PaperInfo;
import com.huatu.tiku.interview.entity.vo.request.PaperAllInfoVo;
import com.huatu.tiku.interview.util.common.PageUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by junli on 2018/4/11.
 */
public interface PaperInfoService {

    /**
     * 查询一条数据
     */
    PaperInfo findById(long id);

    /**
     * 推送
     */
    void push(long id);

    /**
     * 新增/修改试卷信息
     *
     * @param paperAllInfoVo 试卷信息
     */
    void save(PaperAllInfoVo paperAllInfoVo);

    /**
     * 移除试卷信息
     *
     * @param paperId 移除试卷信息
     */
    void delete(long paperId);

    /**
     * 获取详情
     */
    PaperAllInfoVo detail(long id);

    /**
     * 列表查询
     */
    PageUtil<PaperInfo> list(int page, int pageSize, int type, String paperName);

    /**
     * 试卷统计信息
     */
    Map<String, Object> meta(long id);

    void pushV2(long id, long classId,long adminId);

    Map<String,Object> metaV2(long id, long adminId,long classId,long pushId);

    List<Map<String,Object>> findUserAnswer(String openId, long pushId);
}
