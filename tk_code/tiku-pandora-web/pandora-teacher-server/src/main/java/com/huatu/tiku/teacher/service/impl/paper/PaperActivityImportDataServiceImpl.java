package com.huatu.tiku.teacher.service.impl.paper;

import com.google.common.collect.Maps;
import com.huatu.tiku.cop.service.SchoolService;
import com.huatu.tiku.cop.service.impl.SchoolServiceImpl;
import com.huatu.tiku.dto.ExcelView;
import com.huatu.tiku.dto.MatchResultVO;
import com.huatu.tiku.dto.PaperResultView;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.activity.PaperAnswerCardService;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.tiku.teacher.service.paper.PaperActivityImportDataService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.PaperType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/24
 * @描述
 */
@Service
public class PaperActivityImportDataServiceImpl implements PaperActivityImportDataService {

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PaperActivityService paperActivityService;

    @Autowired
    private MatchUserMetaService matchUserMetaService;

    @Autowired
    private SchoolService schoolService;

    public static final Logger logger = LoggerFactory.getLogger(SchoolServiceImpl.class);

    /**
     * 学员考试数据导出（除了真题演练之外的所有活动）
     *
     * @param paperId
     * @return
     */
    @Override
    public ModelAndView importUserExamData(int paperId) {

        List<MatchResultVO> list = new LinkedList<>();
        List<Long> userIds;
        String paperName = "";
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(Long.valueOf(paperId));

        if (null != paperActivity) {
            paperName = paperActivity.getName();
            List<MatchResultVO> matchResultVOList = new ArrayList<>();
            if (paperActivity.getType() == PaperType.MATCH) {
                //模考大赛
                List<com.huatu.tiku.match.bean.entity.MatchUserMeta> matchUserMetas = matchUserMetaService.findByMatchId(paperId);
                List<com.huatu.tiku.match.bean.entity.MatchUserMeta> matchUserMetaInfo = matchUserMetas.stream().filter(matchUserMeta -> matchUserMeta.getPracticeId() > 0L).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(matchUserMetaInfo)) {
                    matchResultVOList = matchUserMetaInfo.stream().map(matchUserMeta -> {
                        MatchResultVO matchResultVO = MatchResultVO.builder().endTime(matchUserMeta.getSubmitTime() == null ? "" : matchUserMeta.getSubmitTime().toString())
                                .score(matchUserMeta.getScore() == null ? 0D : matchUserMeta.getScore())
                                .userId(matchUserMeta.getUserId())
                                .positionName(matchUserMeta.getPositionName())
                                .positionId(matchUserMeta.getPositionId())
                                .build();
                        return matchResultVO;
                    }).collect(Collectors.toList());
                }
            } else {
                List<AnswerCard> userAnswerCards = paperAnswerCardService.getUserAnswerCardByPaperId(Long.valueOf(paperId));
                matchResultVOList = userAnswerCards.stream().map(answerCard -> {
                    MatchResultVO matchResultVO = MatchResultVO.builder().endTime(DateFormatUtils.format(new Date(answerCard.getCreateTime()), "yyyy-MM-dd HH:mm:ss"))
                            .score(answerCard.getScore())
                            .userId(answerCard.getUserId())
                            .build();
                    return matchResultVO;
                }).collect(Collectors.toList());
            }

            //组装倒出数据
            if (CollectionUtils.isNotEmpty(matchResultVOList)) {
                //获取用户信息
                userIds = matchResultVOList.stream().map(answerCard -> answerCard.getUserId()).collect(Collectors.toList());
                List<Map<String, Object>> userInfoList = schoolService.getUserInfo(userIds);
                if (CollectionUtils.isNotEmpty(userInfoList)) {
                    for (MatchResultVO matchResult : matchResultVOList) {
                        Map userInfoMap = Maps.newHashMap();
                        List<Map<String, Object>> collect = userInfoList.stream().filter(map -> Long.valueOf(map.get("id").toString()) == matchResult.getUserId())
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(collect)) {
                            userInfoMap = collect.get(0);
                            String nick = userInfoMap.get("nick") == null ? "" : userInfoMap.get("nick").toString();
                            String mobile = userInfoMap.get("mobile") == null ? "" : userInfoMap.get("mobile").toString();
                            String name = userInfoMap.get("name") == null ? "" : userInfoMap.get("name").toString();

                            MatchResultVO matchResultVO = MatchResultVO.builder()
                                    .userId(matchResult.getUserId())
                                    .score(matchResult.getScore())
                                    .endTime(matchResult.getEndTime())
                                    .nick(nick)
                                    .mobile(mobile)
                                    .name(name)
                                    .positionName(matchResult.getPositionName())
                                    .positionId(matchResult.getPositionId())
                                    .build();
                            list.add(matchResultVO);
                        }
                    }
                    list.sort((a, b) -> (int) (a.getUserId() - b.getUserId()));
                }
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", paperId + "-" + paperName);
        ExcelView excelView = new PaperResultView();
        return new ModelAndView(excelView, map);
    }
}
