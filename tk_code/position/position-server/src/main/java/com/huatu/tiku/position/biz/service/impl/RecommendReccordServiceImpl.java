package com.huatu.tiku.position.biz.service.impl;

import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.domain.RecommendReccord;
import com.huatu.tiku.position.biz.respository.PositionRepository;
import com.huatu.tiku.position.biz.respository.RecommendReccordRepository;
import com.huatu.tiku.position.biz.service.RecommendReccordService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
@Service
public class RecommendReccordServiceImpl extends BaseServiceImpl<RecommendReccord,Long> implements RecommendReccordService {

    private final RecommendReccordRepository recommendReccordRepository;

    private final PositionRepository positionRepository;

    public RecommendReccordServiceImpl(RecommendReccordRepository recommendReccordRepository, PositionRepository positionRepository) {
        this.recommendReccordRepository = recommendReccordRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public void saveX(Long id) {
        List<RecommendReccord> byUserId = recommendReccordRepository.findByUserId(id);
        if(null!=byUserId&&!byUserId.isEmpty()){  //存在更新
            RecommendReccord recommendReccord = byUserId.get(0);
            recommendReccord.setCount(recommendReccord.getCount()+1);
            recommendReccordRepository.save(recommendReccord);
            return;
        }//不存在插入记录
        RecommendReccord recommendReccord=new RecommendReccord();
        recommendReccord.setUserId(id);
        recommendReccord.setCount(1);
        try {
            recommendReccordRepository.save(recommendReccord);
        } catch (Exception e) {
        }
    }

    /**
     * 检查更新 并进行推送
     * @param id
     */
    @Override
    public Integer checkUpdate(Long id) {
        List<RecommendReccord> recommendReccords = recommendReccordRepository.findByUserId(id);
        if(null==recommendReccords||!recommendReccords.isEmpty()){ //有记录检查更新
            RecommendReccord recommendReccord = recommendReccords.get(0);
            Date lastModifiedDate = recommendReccord.getLastModifiedDate();//最后一次修改日期
            if(null!=lastModifiedDate) {
                return positionRepository.countByLastModifiedDateIsAfter(lastModifiedDate);
            }
        }
        return 0;
    }
}
