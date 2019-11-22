package com.huatu.tiku.match.service.v1.search;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-16 下午2:01
 **/
public interface WhiteListService {


    /**
     * 检查用户是否在白名单列表中
     * @param userId
     * @return
     */
    Boolean isWhiteMember(int userId);
}
