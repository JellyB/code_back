package com.huatu.tiku.teacher.service.match;

import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.service.BaseService;

/**
 * 模考大赛试题统计信息处理
 * Created by huangqingpeng on 2018/10/16.
 */
public interface MatchQuestionMetaService extends BaseService<MatchQuestionMeta> {

    /**
     * 试题统计信息持久化到mysql
     *
     * @param matchPaperId
     * @return
     */
    int persistenceByPaperId(int matchPaperId);
}
