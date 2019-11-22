package com.huatu.tiku.essay.service;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.vo.report.MatchUserMeta;
import com.huatu.tiku.essay.util.PageUtil;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2017/12/15.
 * 模考相关service
 */
public interface EssayMatchService {
    /**
     * 查询当天可用模考
     * @return
     */
    List<EssayMockExam> getCurrent();

    /**
     * 单独的申论模考大赛报名
     * @param paperId
     * @param userSession
     * @param positionId
     */
    void enroll(long paperId, UserSession userSession, int positionId);

    void saveEnrollToMysql(int positionId, long paperId, int userId);

    MatchUserMeta findMatchUserMeta(int userId, long paperId);

    PageUtil getCurrentPage(int page, int pageSize);

    Map<String,Map> getMockCourseList(String mockCourseIds);
}
