package com.huatu.tiku.match.service.impl.v1.enroll;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.common.TimeGapConstant;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.dto.enroll.EnrollDTO;
import com.huatu.tiku.match.service.v1.enroll.EnrollService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaHandlerService;
import com.huatu.tiku.match.service.v1.reward.PaperRewardService;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.common.MatchErrors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 描述：模考大赛报名
 *
 * @author biguodong
 * Create time 2018-10-19 上午9:37
 **/

@Service
@Slf4j
public class EnrollServiceImpl implements EnrollService{

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private PaperRewardService paperRewardService;

    @Autowired
    private MetaHandlerService metaHandlerService;

    @Autowired
    private MatchUserMetaService matchUserMetaService;
    
    @Override
    public Object enroll(int matchId, int userId, String userName, int positionId, Long schoolId, String schoolName) throws BizException{
        log.info("match enroll matchId={},userId={},positionId={}", matchId, userId, positionId);
        final long currentTime = System.currentTimeMillis();
        Match match = matchDao.findById(matchId);
        if (match == null) {
            throw new BizException(ErrorResult.create(1000103,"资源未发现"));
        }
        saveTestEnrollInfo(matchId, userId, positionId, match, currentTime, schoolId, schoolName);
        paperRewardService.sendEnrollMsg(userId, userName,  matchId);
        return SuccessMessage.create("报名成功!");
    }



    /**
     * 保存模考大赛行测报名逻辑
     *
     * @param paperId
     * @param userId
     * @param positionId
     * @param match
     * @throws BizException
     */
    private void saveTestEnrollInfo(int paperId, int userId, int positionId, Match match, long currentTime, Long schoolId, String schoolName) throws BizException {
        /**
         * 模考大赛考试开始30分钟后，不能报名
         */
        if (currentTime - match.getStartTime() > TimeUnit.MINUTES.toMillis(TimeGapConstant.THIRTY_MINUTES)) {
            throw new BizException(ErrorResult.create(10031007, "已错过模考大赛"));
        }
        boolean enableEnroll = isEnableEnroll(userId, paperId);
        if(!enableEnroll){
            throw new com.huatu.common.exception.BizException(ErrorResult.create(5000000, "报名条件不满足"));
        }
        EnrollDTO enrollDTO = new EnrollDTO();
        enrollDTO.setPaperId(paperId);
        enrollDTO.setUserId(userId);
        enrollDTO.setPositionId(positionId);
        enrollDTO.setEnrollTime(currentTime);
        if(null!=schoolId&&schoolId>0){
            enrollDTO.setSchoolId(schoolId.intValue());
        }
        if(StringUtils.isNotBlank(schoolName)){
            enrollDTO.setSchoolName(schoolName);
        }
        if (match.getEssayPaperId()>0) {
            enrollDTO.setEssayPaperId(match.getEssayPaperId());
        }else{
            enrollDTO.setEssayPaperId(0L);
        }
        metaHandlerService.saveEnrollInfo(enrollDTO );
    }

    private boolean isEnableEnroll(Integer userId, Integer paperId) {
        MatchUserMeta matchUserEnrollInfo = matchUserMetaService.findMatchUserEnrollInfo(userId, paperId);
        if(null == matchUserEnrollInfo || matchUserEnrollInfo.getPracticeId() < 0){
            return true;
        }
        return false;
    }
}
