package com.huatu.tiku.match.service.impl.v1.search;

import com.huatu.tiku.match.bo.MatchBo;
import com.huatu.tiku.match.bo.MatchUserMetaBo;
import com.huatu.tiku.match.service.v1.search.SearchTemplate;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 描述：游客模式返回列表
 *
 * @author biguodong
 * Create time 2019-01-14 下午3:31
 **/

@Service(value = "touristListStrategy")
@Slf4j
public class TouristListStrategy extends SearchTemplate{


    /**
     * 处理用户报名信息
     *
     * @param matchId
     * @return
     * @throws BizException
     */
    @Override
    protected MatchUserMetaBo dealUserMatchUserMetaBo(int matchId){
        return null;
    }

    /**
     * 白名单处理
     *
     * @param matchBos
     */
    @Override
    protected void filterWhiteList(List<MatchBo> matchBos) {
        return;
    }
}
