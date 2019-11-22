package com.huatu.tiku.essay.service.impl;

import java.util.List;
import java.util.Map;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayFileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.service.EssayWeiXinService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.resp.EssayMockExamAnswerVO;
import com.huatu.tiku.essay.vo.resp.EssayMockSimpleAnswerVO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EssayWeiXinServiceImpl implements EssayWeiXinService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Autowired
    private EssayUserAnswerQuestionDetailedScoreRepository essayUserAnswerQuestionDetailedScoreRepository;
    @Autowired
    private BjyHandler bjyHandler;

    @Autowired
    private EssayMockExamRepository essayMockExamRepository;

    @Autowired
    private EssayMaterialRepository essayMaterialRepository;

    @Autowired
    private EssayFileService essayFileService;
    final private String redisAnswer = "redisAnswer";
    final private String mysqlAnswer = "mysqlAnswer";


    public static String GETMOCKPDFURL = "https://ns.huatu.com/e/api/wxApi/getMockPaperPdf";

    @Override
    public void bindPaper2Mock(Long paperId, Long mockId) {

        // 查询模考信息
        EssayMockExam mockExam = essayMockExamRepository.findOne(mockId);
        if (mockExam == null) {
            throw new BizException(ErrorResult.create(1000001, "模考信息不存在"));
        }
        EssayPaperBase paper = essayPaperBaseRepository.findOne(paperId);
        if (paper == null || paper.getBizStatus() != 0) {
            throw new BizException(ErrorResult.create(1000001, "真题卷已经上线"));
        }
        // 删除旧的模考试题信息
        int updateQuestionCount = essayQuestionBaseRepository.updateStatus(mockId);
        log.info("删除旧模考绑定试题个数:{}", updateQuestionCount);
        // 删除旧模考材料信息
        int modifyMaterialCount = essayMaterialRepository.modifyByPaperId(mockId);
        log.info("删除旧模考绑定材料个数:{}", modifyMaterialCount);
        // 将真题卷中的试题信息绑定到模考卷中
        int updateQuestionPaper2Mock = essayQuestionBaseRepository.updatePaper2Mock(paperId, mockId);
        log.info("绑定到新模考的试题个数:{}", updateQuestionPaper2Mock);
        // 将真题卷中的材料信息绑定到模考卷中
        int updateMaterialPaper2Mock = essayMaterialRepository.updatePaper2Mock(paperId, mockId);
        log.info("绑定到新模考的材料个数:{}", updateMaterialPaper2Mock);
        // 删除真题卷
        int updateStatus = essayPaperBaseRepository.updateStatusById(-1, paperId);

        log.info("删除真题卷状态:{}", updateStatus);
        log.error("-----------------开始同步试卷pdf信息！");
        ResponseEntity<Object> forEntity = restTemplate.getForEntity(GETMOCKPDFURL + "?paperId=" + mockId,
                Object.class);
        log.error("-----------------试卷pdf信息地址为:{}", forEntity.getBody());

    }

    @Override
    public Object compareRedisAndMysqlMockInfo(Long paperId) {
        // 获取所有试卷答题卡
        List<Map<String, Object>> ret = Lists.newArrayList();
        List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByPaperBaseIdAndStatus(paperId,
                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
        paperAnswerList.forEach(paperAnswer -> {
            Map<Long, EssayQuestionAnswer> essayQuestionAnswerRedisMap = Maps.newHashMap();
            // 查询所有试题答题卡
            // 从缓存中获取 答题卡相关信息
            String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, paperAnswer.getUserId());
            EssayMockExamAnswerVO examAnswerVO = (EssayMockExamAnswerVO) redisTemplate.opsForValue().get(examAnswerKey);
            if (examAnswerVO != null) {
                List<EssayQuestionAnswer> essayQuestionAnswerListRedis = examAnswerVO.getEssayQuestionAnswerList();
                essayQuestionAnswerListRedis.forEach(essayQuestionAnswerRedis -> {
                    essayQuestionAnswerRedisMap.put(essayQuestionAnswerRedis.getId(), essayQuestionAnswerRedis);
                });
                List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus(
                        paperAnswer.getId(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        new Sort(Sort.Direction.ASC, "questionDetailId"));
                questionAnswerList.forEach(questionAnswer -> {
                    Map<String, Object> differentMap = Maps.newHashMap();
                    String content = questionAnswer.getContent();
                    EssayQuestionAnswer essayQuestionAnswerRedis = essayQuestionAnswerRedisMap.get(questionAnswer.getId());
                    if (essayQuestionAnswerRedis != null) {
                        String redisContent = essayQuestionAnswerRedis.getContent();
                        if (StringUtils.isNotBlank(content)) {
                            // 数据库中答案不为空
                            if (redisContent == null || !content.equals(redisContent)) {
                                // redis中缓存缺失
                                differentMap.put(mysqlAnswer,
                                        EssayMockSimpleAnswerVO.builder().content(content)
                                                .questionAnswerId(questionAnswer.getId()).build());
                                differentMap.put(redisAnswer,
                                        EssayMockSimpleAnswerVO.builder().content(redisContent)
                                                .questionAnswerId(essayQuestionAnswerRedis.getId()).build());
                                ret.add(differentMap);
                            }
                        }
                    } else {
                        // redis中缓存缺失
                        if (StringUtils.isNotBlank(content)) {
                            differentMap.put(mysqlAnswer, EssayMockSimpleAnswerVO.builder().content(content)
                                    .questionAnswerId(questionAnswer.getId()).build());
                            differentMap.put(redisAnswer,
                                    EssayMockSimpleAnswerVO.builder().content(null)
                                            .questionAnswerId(null).build());
                            ret.add(differentMap);
                        }
                    }
                });
            } else {
                Map<String, Object> differentPaperMap = Maps.newHashMap();
                differentPaperMap.put("mysqlPaperAnswer", paperAnswer.getId());
                ret.add(differentPaperMap);
            }
        });
        return ret;
    }

}
