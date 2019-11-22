package com.huatu.tiku.match.service.impl.v1.search;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.common.TimeGapConstant;
import com.huatu.tiku.match.service.v1.search.SearchTemplate;
import com.huatu.tiku.match.util.UserInfoHolder;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-14 下午3:29
 **/
@Component
@Slf4j
public class SearchHandler {

    private SearchTemplate searchTemplate;

    @Autowired
    private TouristListStrategy touristListStrategy;

    @Autowired
    private TokenListStrategy tokenListStrategy;

    public void setSearchTemplate(SearchTemplate searchTemplate) {
        this.searchTemplate = searchTemplate;
    }

    /**
     * 默认的模考大赛首页筛选方式
     */
    public static final Predicate<Match> DEFAULT_HEAD_FILTER = match -> match.getEndTime() + TimeUnit.MINUTES.toMillis(TimeGapConstant.THIRTY_MINUTES) >= System.currentTimeMillis();
    private HashMap<String, Object> doSearch(int subjectId, int page, int size, Predicate<Match> filterHead) throws BizException {
        return searchTemplate.matchEntrance(subjectId, page, size, filterHead);
    }

    /**
     * 模考search 入口
     *
     * @param userSession
     * @param subjectId
     * @param page
     * @param size
     * @param filterHead
     * @return
     */

    public Object dealSearch(UserSession userSession, int subjectId, int page, int size, Predicate<Match> filterHead) throws BizException {
        if (null == userSession) {
            UserInfoHolder.set(SearchTemplate.TOURIST_USER_ID);
            setSearchTemplate(touristListStrategy);
            return doSearch(subjectId, page, size, filterHead);
        } else {
            int userId = userSession.getId();
            if (subjectId < 0) {
                subjectId = userSession.getSubject();
            }
            UserInfoHolder.set(userId);
            setSearchTemplate(tokenListStrategy);
            return doSearch(subjectId, page, size, filterHead);
        }
    }

    public Object dealSearchById(UserSession userSession, int subjectId, int paperId) throws BizException {
        if(null == userSession){
            UserInfoHolder.set(SearchTemplate.TOURIST_USER_ID);
            setSearchTemplate(touristListStrategy);
        }else{
            UserInfoHolder.set(userSession.getId());
            setSearchTemplate(tokenListStrategy);
            if(subjectId < 0){
                subjectId = userSession.getId();
            }
        }
        return searchTemplate.getUserMatchInfo(subjectId, paperId);
    }
}
