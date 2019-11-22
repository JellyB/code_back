package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.paper.bean.EssayPaper;
import com.huatu.ztk.paper.bean.EstimateEssayPaper;

import java.util.List;

public interface EssayPaperService {

    List<EstimateEssayPaper> findUserFulMatch();


    List<EssayPaper> findBasePaperList();
}
