package com.huatu.tiku.match.service.v1.paper;

import com.huatu.ztk.paper.bean.PaperUserMeta;

import java.util.List;

/**
 * Created by huangqingpeng on 2019/2/25.
 */
public interface PaperUserMetaService {

    /**
     * 获取用户试卷统计ID
     * @param uid
     * @param paperId
     * @return
     */
    String getId(long uid,int paperId);

    /**
     * 批量查询用户的试卷的统计信息
     * @param uid
     * @param paperIds
     * @return
     */
    List<PaperUserMeta> findBatch(long uid, List<Integer> paperIds);

    /**
     * 查询用户某一套试卷的统计信息
     * @param uid
     * @param paperId
     * @return
     */
    PaperUserMeta findById(long uid , int paperId);

    /**
     * 试卷统计中添加未完成的练习ID
     * @param userId
     * @param paperId
     * @param practiceId
     */
    void addUndoPractice(long userId, int paperId, long practiceId);

    /**
     * 试卷统计中添加完成的练习ID
     * @param userId
     * @param paperId
     * @param practiceId
     */
    void addFinishPractice(long userId, int paperId, long practiceId);
}
