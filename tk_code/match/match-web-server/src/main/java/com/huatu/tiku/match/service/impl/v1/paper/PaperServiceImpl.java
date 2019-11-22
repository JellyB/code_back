package com.huatu.tiku.match.service.impl.v1.paper;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.tiku.match.dao.document.PaperDao;
import com.huatu.tiku.match.enums.PaperInfoEnum;
import com.huatu.tiku.match.enums.util.EnumUtil;
import com.huatu.tiku.match.service.v1.paper.PaperService;
import com.huatu.ztk.paper.bean.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by lijun on 2018/10/19
 */
@Service
public class PaperServiceImpl implements PaperService {

    /**
     * 用以在本机缓存试卷信息
     */
    private final static Cache<Integer, Paper> PAPER_CACHE = CacheBuilder.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Autowired
    private PaperDao paperDao;

    @Override
    public PaperInfoEnum.PaperTypeEnum getPaperTypeById(int paperId) {
        Paper paper = findPaperCacheById(paperId);
        if (null == paper) {
            return PaperInfoEnum.PaperTypeEnum.DEFAULT;
        }
        return EnumUtil.create(paper.getType(), PaperInfoEnum.PaperTypeEnum.class);
    }

    @Override
    public List<Integer> getPaperQuestionIdList(int paperId) {
        Paper paper = findPaperCacheById(paperId);
        if (null != paper) {
            return paper.getQuestions();
        }
        return Lists.newArrayList();
    }

    /**
     * 通过ID 查询试卷信息
     *
     * @param paperId 试卷ID
     * @return 试卷信息
     */
    @Override
    public Paper findPaperCacheById(int paperId) {
        Paper paperCacheIfPresent = PAPER_CACHE.getIfPresent(paperId);
        if (null != paperCacheIfPresent) {
            return paperCacheIfPresent;
        }
        Paper daoPaperById = paperDao.findPaperById(paperId);
        if (null != daoPaperById) {
            PAPER_CACHE.put(daoPaperById.getId(), daoPaperById);
            return PAPER_CACHE.getIfPresent(paperId);
        }
        return null;
    }
}
