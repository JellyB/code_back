package com.huatu.ztk.paper.service.v4.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.constants.teacher.EssayConstant;
import com.huatu.tiku.entity.teacher.EssayMockExam;
import com.huatu.ztk.paper.bean.EssayPaper;
import com.huatu.ztk.paper.bean.EstimateEssayPaper;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.dao.EssayPaperDao;
import com.huatu.ztk.paper.service.v4.EssayPaperService;
import com.huatu.ztk.paper.util.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class EssayPaperServiceImpl implements EssayPaperService {

    private static final Logger logger = LoggerFactory.getLogger(EssayPaperServiceImpl.class);

    @Autowired
    private EssayPaperDao essayPaperDao;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<EstimateEssayPaper> findUserFulMatch() {
        List<EstimateEssayPaper> mocks = essayPaperDao.findMockList();
        if (CollectionUtils.isNotEmpty(mocks)) {
            long l = System.currentTimeMillis();
            List<EstimateEssayPaper> collect = mocks.stream()
                    .filter(i -> i.getEndTime() + TimeUnit.MINUTES.toMillis(60) > l)
                    .collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                Optional<Map.Entry<String, List<EstimateEssayPaper>>> first = collect.stream().collect(Collectors.groupingBy(i -> DateUtil.getFormatDateString(i.getStartTime())))
                        .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                        .findFirst();
                if(first.isPresent()){
                    return first.get().getValue();
                }
            }

        }
        return Lists.newArrayList();
    }

    @Override
    public List<EssayPaper> findBasePaperList() {
        List<EssayPaper> papers = essayPaperDao.findBTruePaper();
        return papers;
    }
}
