package com.huatu.tiku.match.service.v1.search;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-15 上午10:41
 **/
public interface SearchService {

    int TOURIST_USER_ID = -1;

    /**
     * 模考大赛列表查询入口
     * @param subject
     * @param page
     * @param size
     * @param filterHead
     * @return
     * @throws BizException
     */
    HashMap<String, Object> matchEntrance(int subject, int page, int size, Predicate<Match> filterHead) throws BizException;

    Object getUserMatchInfo(int subjectId, int paperId) throws BizException;
}
