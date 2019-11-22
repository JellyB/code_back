package com.huatu.tiku.match.service.impl.v1.report;

import com.google.common.collect.Lists;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.bo.report.ReportBo;
import com.huatu.tiku.match.bo.report.ReportListBo;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.service.impl.v1.paper.AnswerCardUtil;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.report.ReportService;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Match;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 描述：我的报告service
 *
 * @author biguodong
 * Create time 2018-10-24 下午5:52
 **/
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private MatchUserMetaService matchUserMetaService;

    @Autowired
    private MatchDao matchDao;

    /**
     * 查询模考大赛报告列表
     *
     * @param userId
     * @param tagId
     * @param subject
     * @return
     * @throws BizException
     */
    @Override
    public Object myReportList(int userId, int tagId, int subject, String cv, int terminal) throws BizException {
        ReportBo reportBo = new ReportBo();
        List<ReportListBo> matchReportBos = Lists.newArrayList();
        //用户参加的模考大赛信息
        List<MatchUserMeta> availableMatchMeta = matchUserMetaService.getAvailableMatchMeta(userId, tagId, subject);

        if (CollectionUtils.isEmpty(availableMatchMeta)) {
            reportBo.setList(Lists.newArrayList());
            return reportBo;
        }
        Line matchLine = matchUserMetaService.getMatchLine(availableMatchMeta);
        //版本限制
        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            AnswerCardUtil.handlerLine(matchLine, AnswerCardUtil.transInt);
        }
        reportBo.setLine(matchLine);

        for (MatchUserMeta matchUserMeta : availableMatchMeta) {
            ReportListBo reportListBo = new ReportListBo();
            reportListBo.setName(matchUserMeta.getName());
            reportListBo.setPracticeId(matchUserMeta.getPracticeId());
            reportListBo.setStartTime(matchUserMeta.getCardCreateTime().getTime());
            reportListBo.setPaperId(matchUserMeta.getMatchId());
            reportListBo.setTotal(matchUserMeta.getRankCount() == null ? 1 : matchUserMeta.getRankCount());
            matchReportBos.add(reportListBo);
        }
        if(terminal == TerminalType.WEI_XIN_APPLET){
            List<Integer> ids = matchReportBos.stream().map(ReportListBo::getPaperId).collect(Collectors.toList());
            List<Match> matches = matchDao.findByIds(ids);
            for (ReportListBo matchReportBo : matchReportBos) {
                Optional<Match> first = matches.stream().filter(match -> match.getPaperId() == matchReportBo.getPaperId()).findFirst();
                if(first.isPresent()){
                    Match match = first.get();
                    matchReportBo.setCourseId(match.getCourseId());
                    matchReportBo.setCourseName(match.getCourseInfo());
                }
            }
        }
        reportBo.setList(matchReportBos);
        return reportBo;
    }
}
