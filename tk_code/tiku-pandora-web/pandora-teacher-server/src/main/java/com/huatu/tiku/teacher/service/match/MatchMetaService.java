package com.huatu.tiku.teacher.service.match;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模考大赛数据持久化处理
 * +++ 模考大赛数据统计
 * Created by huangqingpeng on 2018/10/16.
 */
public interface MatchMetaService {

    /**
     * 持久化模考大赛
     * @param matchId
     * @return
     */
    Object persistence(int matchId);

    Object metaEnroll(int paperId);

    File metaResult(int paperId) ;

    Map metaAllTime(int paperId);

    /**
     * 教育小程序提供参考用户数据
     * @param paperId
     * @return
     */
    List<MatchUserMeta> getMetaForEdu(int paperId);

    public List<HashMap> assemblingEduMeta(List<MatchUserMeta> metas);
}
