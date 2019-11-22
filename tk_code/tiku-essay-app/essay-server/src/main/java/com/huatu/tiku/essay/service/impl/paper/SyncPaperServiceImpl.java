package com.huatu.tiku.essay.service.impl.paper;

import com.huatu.tiku.essay.constant.status.EssayMockExamConstant;
import com.huatu.tiku.essay.constant.status.EssayPaperBaseConstant;
import com.huatu.tiku.essay.document.EssayPaperDao;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.paper.SyncPaperService;
import com.huatu.ztk.paper.bean.EssayPaper;
import com.huatu.ztk.paper.bean.EstimateEssayPaper;
import com.huatu.ztk.paper.common.PaperStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Service
@Slf4j
public class SyncPaperServiceImpl implements SyncPaperService {

    @Autowired
    private EssayPaperDao essayPaperDao;

    @Autowired
    private EssayPaperService essayPaperService;

    @Autowired
    private EssayMockExamService essayMockExamService;


    @Override
    @Async
    public void syncPaperInfo(long paperId) {
        EssayPaperBase paperBase = essayPaperService.findPaperInfoById(paperId);
        int type = paperBase.getType();
        switch (type){
            case 0:
                EstimateEssayPaper estimateEssayPaper = new EstimateEssayPaper();
                fillPaperBaseInfo().accept(paperBase,estimateEssayPaper);
                EssayMockExam mock = essayMockExamService.getMock(paperId);
                estimateEssayPaper.setEndTime(mock.getEndTime().getTime());
                estimateEssayPaper.setStartTime(mock.getStartTime().getTime());
                estimateEssayPaper.setOnlineTime(mock.getStartTime().getTime());
                estimateEssayPaper.setOfflineTime(mock.getEndTime().getTime());
                estimateEssayPaper.setStatus(getMockPaperStatus().apply(mock.getStatus(),mock.getBizStatus()));
                essayPaperDao.save(estimateEssayPaper);
                break;
            case 1:
                EssayPaper essayPaper = new EssayPaper();
                fillPaperBaseInfo().accept(paperBase,essayPaper);
                essayPaperDao.save(essayPaper);
        }
    }

    BiConsumer<EssayPaperBase, EssayPaper> fillPaperBaseInfo() {
        return ((essayPaperBase, essayPaper) -> {
            try {
                essayPaper.setId((int) essayPaperBase.getId());
                essayPaper.setName(essayPaperBase.getName());
                essayPaper.setYear(Integer.parseInt(essayPaperBase.getPaperYear()));
                essayPaper.setArea((int) essayPaperBase.getAreaId());
                essayPaper.setTime(essayPaperBase.getLimitTime());
                essayPaper.setScore((int) essayPaperBase.getScore());
                essayPaper.setType(essayPaperBase.getType());
                essayPaper.setStatus(getEssayPaperStatus().apply(essayPaperBase.getStatus(),essayPaperBase.getBizStatus()));
                essayPaper.setCatgory(14);
                essayPaper.setCreatedBy(essayPaperBase.getCreator());
                essayPaper.setCreateTime(essayPaperBase.getGmtCreate());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

    /**
     * 试卷mongo状态获取
     *
     * @return
     */
    BiFunction<Integer, Integer, Integer> getEssayPaperStatus() {
        return ((status, bizStatus) -> {
            if (EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus() == bizStatus) {
                if (EssayPaperBaseConstant.EssayPaperStatusEnum.DELETED.getStatus() == status) {
                    return PaperStatus.DELETED;
                } else {
                    return PaperStatus.CREATED;
                }
            }else{
                return PaperStatus.AUDIT_SUCCESS;
            }
        });
    }

    /**
     * 试卷mongo状态获取
     *
     * @return
     */
    BiFunction<Integer, Integer, Integer> getMockPaperStatus() {
        return ((status, bizStatus) -> {
            if (EssayMockExamConstant.EssayMockExamBizStatusEnum.ONLINE.getBizStatus() > bizStatus) {
                if (EssayMockExamConstant.EssayMockExamStatusEnum.DELETED.getStatus() == status) {
                    return PaperStatus.DELETED;
                } else {
                    return PaperStatus.CREATED;
                }
            }else{
                return PaperStatus.AUDIT_SUCCESS;
            }
        });
    }
}
