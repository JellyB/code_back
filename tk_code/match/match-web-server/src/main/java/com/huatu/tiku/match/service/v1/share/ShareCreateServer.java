package com.huatu.tiku.match.service.v1.share;

import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.commons.exception.BizException;

/**
 * Created by huangqingpeng on 2019/1/11.
 */
public interface ShareCreateServer {

    /**
     * 单个模考大赛分享信息生成
     *
     * @param paperId
     * @param userId
     * @param userName
     * @param token
     * @return
     */
    Share buildShareInfo(int paperId, int userId, String userName, String token,String cv,int terminal) throws BizException;
}
