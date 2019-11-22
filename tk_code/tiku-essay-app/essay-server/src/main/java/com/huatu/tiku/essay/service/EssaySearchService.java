package com.huatu.tiku.essay.service;

import com.huatu.tiku.common.bean.user.UserSession;

/**
 * @author jbzm
 * @date Create on 2018/2/5 11:33
 */
public interface EssaySearchService {
    Object searchQuestion(UserSession userSession, String content, int page, int pageSize, String cv, int terminal);

    Object searchPaper(String content, int page, int pageSize);

    void importPaper2Search(long paperId);

    Object searchQuestionV3( int userId,String content, int page, int pageSize,int type);

    void importQuestion2Search();
    
    void report2Sensors(String ucId,Integer searchCount,String keywords,int terminal);

    Object searchQuestionV4( int userId,String content, int page, int pageSize,int type);
}
