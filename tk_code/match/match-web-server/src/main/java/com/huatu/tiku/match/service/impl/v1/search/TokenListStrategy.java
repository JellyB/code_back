package com.huatu.tiku.match.service.impl.v1.search;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.bo.MatchBo;
import com.huatu.tiku.match.bo.MatchUserMetaBo;
import com.huatu.tiku.match.common.TimeGapConstant;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.tiku.match.enums.MatchStatusEnum;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.search.SearchTemplate;
import com.huatu.tiku.match.service.v1.search.WhiteListService;
import com.huatu.tiku.match.util.UserInfoHolder;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 描述：登录用户返回列表
 *
 * @author biguodong
 * Create time 2019-01-14 下午3:29
 **/


@Service(value = "tokenListStrategy")
@Slf4j
public class TokenListStrategy extends SearchTemplate{

    @Autowired
    private MatchUserMetaService matchUserMetaService;

    @Autowired
    private WhiteListService whiteListService;

    /**
     * 处理用户报名信息
     *
     * @param matchId
     * @return
     * @throws BizException
     */
    @Override
    protected MatchUserMetaBo dealUserMatchUserMetaBo(int matchId) {
        MatchUserMeta matchUserMeta = matchUserMetaService.findMatchUserEnrollInfo(UserInfoHolder.get(), matchId);
        MatchUserMetaBo matchUserMetaBo = null;

        if (null != matchUserMeta) {
			log.info("userId:{},area:{}", UserInfoHolder.get(), matchUserMeta.getPositionId());
            matchUserMetaBo = MatchUserMetaBo.builder().build();
            BeanUtils.copyProperties(matchUserMeta, matchUserMetaBo);
            matchUserMetaBo.setSubmitFlag(checkIsSubmitted(matchUserMeta));
        }
        return matchUserMetaBo;
    }
    /**
     * 判断用户答题卡是交卷
     *
     * @return
     */
    private boolean checkIsSubmitted(MatchUserMeta matchUserMeta) {
        Integer isAnswer = matchUserMeta.getIsAnswer();
        if (null != isAnswer && isAnswer.equals(MatchInfoEnum.AnswerStatus.SUBMIT.getKey())) {
            return true;
        }
        return false;
    }

    /**
     * 白名单处理
     *
     * @param matchBos
     */
    @Override
    protected void filterWhiteList(final List<MatchBo> matchBos) {
        /**
         * 此部分代码只作为 内部线上数据测试使用,与原始业务逻辑相违背.
         * 白名单 状态值 矫正
         */
        if (whiteListService.isWhiteMember(UserInfoHolder.get())) {
            final long currentTimeMillis = System.currentTimeMillis();
            List<MatchBo> tmp = matchBos.stream().map(matchBo -> {
//                if (MatchStatusEnum.in(matchBo.getStatus(), MatchStatusEnum.ENROLL, MatchStatusEnum.START_UNAVAILABLE)) {
//                    /**
//                     * 当前不能考试时, 修改状态 成可考试状态
//                     */
//                    matchBo.setStatus(MatchStatusEnum.START_AVAILABLE.getKey());
//                }
//                if (matchBo.getStatus() == MatchStatusEnum.REPORT_UNAVAILABLE.getKey()) {
//                    /**
//                     * 当前不能查看报告,修改状态 成 可查看报告
//                     */
//                    matchBo.setStatus(MatchStatusEnum.REPORT_AVAILABLE.getKey());
//                }
                /**
                 * 考试开始前30分钟以前，报名的白名单用户，可以进入预览试题的状态
                 */
                long end = currentTimeMillis + TimeUnit.MINUTES.toMillis(TimeGapConstant.THIRTY_MINUTES);
                if(matchBo.getStatus() == MatchStatusEnum.ENROLL.getKey() && end < matchBo.getStartTime()){
                    matchBo.setStatus(MatchStatusEnum.WHITE_PRE_LOOK.getKey());
                }

                return matchBo;
            }).collect(Collectors.toList());
            matchBos.clear();
            matchBos.addAll(tmp);
        }
    }
}
