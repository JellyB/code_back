package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayRuleService;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.util.admin.EssayConvertUtil;
import com.huatu.tiku.essay.vo.admin.*;
import com.huatu.tiku.essay.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.AdminKeyPhraseTypeConstant.APPLICATION_KEYPHRASE_TYPE;
import static com.huatu.tiku.essay.constant.status.AdminKeyPhraseTypeConstant.APPLICATION_KEYPHRASE_WITH_DESC_TYPE;
import static com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITH_DESC;

/**
 * Created by huangqp on 2017\12\6 0006.
 */
@Slf4j
@Service
public class EssayRuleServiceImpl implements EssayRuleService {
    @Autowired
    EssayStandardAnswerFormatRepository essayStandardAnswerFormatRepository;
    @Autowired
    EssayStandardAnswerKeyPhraseRepository essayStandardAnswerKeyPhraseRepository;
    @Autowired
    EssayStandardAnswerKeyWordRepository essayStandardAnswerKeyWordRepository;
    @Autowired
    EssayStandardAnswerRuleSpecialStripRepository essayStandardAnswerRuleSpecialStripRepository;
    @Autowired
    EssayStandardAnswerRuleStripSegmentalRepository essayStandardAnswerRuleStripSegmentalRepository;
    @Autowired
    EssayStandardAnswerRuleWordNumRepository essayStandardAnswerRuleWordNumRepository;
    @Autowired
    EssayStandardAnswerRuleRepository essayStandardAnswerRuleRepository;
    @Autowired
    EssayStandardAnswerSplitWordRepository essayStandardAnswerSplitWordRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    RestTemplate restTemplate;

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<EssayStandardAnswerFormatVO> findAnswerFormatsByQuestion(long questionId) {
        //获取格式表信息
        List<EssayStandardAnswerFormat> result = essayStandardAnswerFormatRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());

        List<EssayStandardAnswerFormatVO> list = Lists.newArrayList();

        List<EssayStandardAnswerKeyWord> keyWordList = essayStandardAnswerKeyWordRepository.findByQuestionDetailIdAndBizStatusAndStatusInOrderByIdAsc(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());

        Map<Long, List<EssayStandardAnswerKeyWordVO>> childMap = Maps.newHashMap();
        Map<Long, EssayStandardAnswerKeyWordVO> keyWordMap = Maps.newHashMap();
        Map<Long, Set<Long>> idsMap = Maps.newLinkedHashMap();

        for (EssayStandardAnswerKeyWord keyWord : keyWordList) {
            EssayStandardAnswerKeyWordVO keyWordVO = new EssayStandardAnswerKeyWordVO();
            BeanUtils.copyProperties(keyWord, keyWordVO);
            //各种格式关键词的近义词
            if (EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD == keyWord.getType()) {
                long correspondingId = keyWord.getCorrespondingId();
                if (childMap.get(correspondingId) == null) {
                    List<EssayStandardAnswerKeyWordVO> childList = Lists.newLinkedList();
                    childList.add(keyWordVO);
                    childMap.put(correspondingId, childList);
                } else {
                    List<EssayStandardAnswerKeyWordVO> childList = childMap.get(correspondingId);
                    childList.add(keyWordVO);
                }
            }
            if (EssayAnswerKeyWordConstant.QUESTION_FROM_TITLE_CHILD_KEYWORD == keyWord.getType() ||
                    EssayAnswerKeyWordConstant.QUESTION_FROM_APPELLATION_CHILD_KEYWORD == keyWord.getType() ||
                    EssayAnswerKeyWordConstant.QUESTION_FROM_INSCRIBE_CHILD_KEYWORD == keyWord.getType()) {
                keyWordMap.put(keyWord.getId(), keyWordVO);
                if (idsMap.get(keyWord.getCorrespondingId()) == null) {
                    Set<Long> ids = Sets.newHashSet();
                    ids.add(keyWord.getId());
                    idsMap.put(keyWord.getCorrespondingId(), ids);
                } else {
                    Set<Long> ids = idsMap.get(keyWord.getCorrespondingId());
                    ids.add(keyWord.getId());
                }
            }
        }
        for (Map.Entry<Long, EssayStandardAnswerKeyWordVO> entry : keyWordMap.entrySet()) {
            EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO = entry.getValue();
            essayStandardAnswerKeyWordVO.setSimilarWordVOList(childMap.get(entry.getKey()));
        }
        for (EssayStandardAnswerFormat essayStandardAnswerFormat : result) {
            EssayStandardAnswerFormatVO target = new EssayStandardAnswerFormatVO();
            BeanUtils.copyProperties(essayStandardAnswerFormat, target);
            long parentId = essayStandardAnswerFormat.getId();
//            if (idsMap.get(parentId) == null) {
//                continue;
//            }
            if(idsMap.get(parentId) != null) {
            	List<EssayStandardAnswerKeyWordVO> resultChildList = Lists.newLinkedList();
                for (long id : idsMap.get(parentId)) {
                    EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO = keyWordMap.get(id);
                    if (null == essayStandardAnswerKeyWordVO.getSimilarWordVOList()) {
                        essayStandardAnswerKeyWordVO.setSimilarWordVOList(Lists.newArrayList());
                    }
                    resultChildList.add(essayStandardAnswerKeyWordVO);
                }
                target.setChildKeyWords(resultChildList);
            }
            list.add(target);
        }
        return list;
    }

    @Override
    public List<EssayStandardAnswerKeyPhraseVO> findAnswerKeyPhraseByQuestion(long questionId, int type, long pid) {
        List<EssayStandardAnswerKeyPhrase> result = new LinkedList<>();

        if (APPLICATION_KEYPHRASE_WITH_DESC_TYPE != type) {
            result = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatus(questionId,
                    type,
                    EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                    EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        }

        if (APPLICATION_KEYPHRASE_WITH_DESC_TYPE == type) {
            //有描述的关键句
            result = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatusAndPid(
                    questionId,
                    APPLICATION_KEYPHRASE_TYPE,
                    EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                    EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus(),
                    pid);
            if (CollectionUtils.isNotEmpty(result)) {
                //近似句
                List<EssayStandardAnswerKeyPhrase> similarKeyPhrases = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatusAndPidIn(
                        questionId,
                        APPLICATION_KEYPHRASE_TYPE,
                        EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                        EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus(),
                        result.stream()
                                .map(EssayStandardAnswerKeyPhrase::getId)
                                .collect(Collectors.toList())
                );
                result.addAll(similarKeyPhrases);
            }
        }
        //查询试题下关键句下关键词和近义词
        List<EssayStandardAnswerKeyWordVO> keyWordList = findAnswerKeyWordByQuestion(questionId, EssayAnswerKeyWordConstant.QUESTION_KEYPHRASE_CHILD_KEYWORD, 0L);
        //关键句返回对象
        List<EssayStandardAnswerKeyPhraseVO> list = Lists.newLinkedList();
        if (CollectionUtils.isNotEmpty(result)) {
            //近似句
            for (EssayStandardAnswerKeyPhrase keyPhrase : result) {
                //转换数据格式
                if (keyPhrase.getPid() == pid) {
                    EssayStandardAnswerKeyPhraseVO keyPhraseVO = getKeyPhraseDetail(keyPhrase, keyWordList);
                    //查询近似句信息
                    LinkedList<EssayStandardAnswerKeyPhraseVO> similarPhraseList = new LinkedList<>();
                    for (EssayStandardAnswerKeyPhrase similarPhrase : result) {
                        if (similarPhrase.getPid() == keyPhrase.getId()) {
                            EssayStandardAnswerKeyPhraseVO similarPhraseVO = getKeyPhraseDetail(similarPhrase, keyWordList);
                            similarPhraseList.add(similarPhraseVO);
                        }
                    }
                    keyPhraseVO.setSimilarPhraseList(similarPhraseList);
                    list.add(keyPhraseVO);
                }
            }
        }
        return list;
    }

    private EssayStandardAnswerKeyPhraseVO getKeyPhraseDetail
            (EssayStandardAnswerKeyPhrase keyPhrase, List<EssayStandardAnswerKeyWordVO> keyWordList) {
        EssayStandardAnswerKeyPhraseVO keyPhraseVO = new EssayStandardAnswerKeyPhraseVO();
        BeanUtils.copyProperties(keyPhrase, keyPhraseVO);
        LinkedList<EssayStandardAnswerKeyWordVO> keyWordVOList = new LinkedList<>();
        //填充关键句的 关键词列表
        if (CollectionUtils.isNotEmpty(keyWordList)) {
            for (EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO : keyWordList) {
                if (keyPhrase.getId() == essayStandardAnswerKeyWordVO.getCorrespondingId()) {
                    keyWordVOList.add(essayStandardAnswerKeyWordVO);
                }
            }
        }
        keyPhraseVO.setKeyWordVOList(keyWordVOList);
        return keyPhraseVO;
    }

    @Override
    public List<EssayStandardAnswerKeyWordVO> findAnswerKeyWordByQuestion(long questionId, int type, long pid) {
        //查询这个试题的所有关键词和近义词（部分近义词是不包含在该次查询结果中的，需要排除）
        Integer[] types = {type, EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD};
        List<EssayStandardAnswerKeyWord> result = essayStandardAnswerKeyWordRepository.findByQuestionDetailIdAndBizStatusAndStatusAndTypeInOrderByTypeAsc(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus(),
                types);

        if (QUESTION_PARENT_KEYWORD_WITH_DESC == type) {
            result = result.stream().filter(keyWord -> null != keyWord &&
                    ((keyWord.getCorrespondingId() == pid && QUESTION_PARENT_KEYWORD_WITH_DESC == keyWord.getType()) || QUESTION_PARENT_KEYWORD_WITH_DESC != keyWord.getType()))
                    .collect(Collectors.toList());
        }
        //key 为跟关键词的id,value为跟关键词对象，其中近义词会组合在对象中
        Map<Long, EssayStandardAnswerKeyWordVO> mapData = Maps.newHashMap();
        //key 为近义词的上级id,value为近义词对象(近义词的关联id，都是关键词，所以可以通过关联的关键词分组)
        Map<Long, List<EssayStandardAnswerKeyWordVO>> childMap = Maps.newHashMap();
        for (EssayStandardAnswerKeyWord keyWord : result) {
            //数据结构变动
            EssayStandardAnswerKeyWordVO keyWordVO = new EssayStandardAnswerKeyWordVO();
            BeanUtils.copyProperties(keyWord, keyWordVO);
            //如果是根关键词类型，存储到mapData
            if (type == keyWord.getType()) {
                mapData.put(keyWord.getId(), keyWordVO);
            }
            //如果是近义词，分组存储
            if (EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD == keyWord.getType()) {
                if (childMap.get(keyWord.getCorrespondingId()) == null) {
                    List<EssayStandardAnswerKeyWordVO> list = Lists.newArrayList();
                    list.add(keyWordVO);
                    childMap.put(keyWord.getCorrespondingId(), list);
                } else {
                    List<EssayStandardAnswerKeyWordVO> list = childMap.get(keyWord.getCorrespondingId());
                    list.add(keyWordVO);
                }
            }
        }
        List<EssayStandardAnswerKeyWordVO> resultList = Lists.newArrayList();
        for (Map.Entry<Long, EssayStandardAnswerKeyWordVO> entry : mapData.entrySet()) {
            //根关键词的id,将分组结果合并到跟关键词下
            Long key = entry.getKey();
            EssayStandardAnswerKeyWordVO keyWordVO = entry.getValue();
            keyWordVO.setSimilarWordVOList(childMap.get(key));
            resultList.add(keyWordVO);
        }
        return resultList;
    }

    @Override
    public List<EssayStandardAnswerRuleSpecialStripVO> findAnswerRuleSpecialStripByQuestion(long questionId) {
        List<EssayStandardAnswerRuleSpecialStrip> result = essayStandardAnswerRuleSpecialStripRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        List<EssayStandardAnswerRuleSpecialStripVO> list = Lists.newArrayList();
        for (EssayStandardAnswerRuleSpecialStrip specialStrip : result) {
            EssayStandardAnswerRuleSpecialStripVO stripVO = new EssayStandardAnswerRuleSpecialStripVO();
            BeanUtils.copyProperties(specialStrip, stripVO);
            list.add(stripVO);
        }
        list.sort((a, b) -> (int) (a.getId() - b.getId()));
        return list;
    }

    @Override
    public List<EssayStandardAnswerRuleStripSegmentalVO> findAnswerRuleStripSegmentalByQuestion(long questionId) {
        List<EssayStandardAnswerRuleStripSegmental> result = essayStandardAnswerRuleStripSegmentalRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        List<EssayStandardAnswerRuleStripSegmentalVO> list = Lists.newArrayList();
        BeanUtils.copyProperties(list, result);
        return list;
    }

    @Override
    public List<EssayStandardAnswerRuleWordNumVO> findAnswerRuleWordNumByQuestion(long questionId) {
        List<EssayStandardAnswerRuleWordNum> result = essayStandardAnswerRuleWordNumRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        List<EssayStandardAnswerRuleWordNumVO> list = Lists.newArrayList();
        BeanUtils.copyProperties(list, result);
        return list;
    }

    @Override
    public EssayStandardAnswerFormatVO addAnswerFormat(EssayStandardAnswerFormatVO essayStandardAnswerFormat, int uid) {
        EssayStandardAnswerFormatVO resultVO = new EssayStandardAnswerFormatVO();
        EssayStandardAnswerFormat target = new EssayStandardAnswerFormat();
        BeanUtils.copyProperties(essayStandardAnswerFormat, target);
        target.setCreator(uid + "");
        target.setGmtCreate(new Date());
        EssayStandardAnswerFormat result = essayStandardAnswerFormatRepository.save(target);
        List<EssayStandardAnswerKeyWordVO> list = essayStandardAnswerFormat.getChildKeyWords();
        BeanUtils.copyProperties(result, resultVO);
        if (CollectionUtils.isEmpty(list)) {
            return resultVO;
        }
        List<EssayStandardAnswerKeyWordVO> childList = Lists.newArrayList();
        for (EssayStandardAnswerKeyWordVO keyWordVO : list) {
            keyWordVO.setCorrespondingId(result.getId());
            childList.add(addAnswerKeyWord(keyWordVO, uid, keyWordVO.getType()));
        }
        resultVO.setChildKeyWords(childList);
        return resultVO;
    }

    /**
     * 处理单个关键句
     *
     * @param keyPhraseVO
     * @param uid
     * @return
     */
    @Override
    public EssayStandardAnswerKeyPhraseVO addAnswerKeyPhrase(EssayStandardAnswerKeyPhraseVO keyPhraseVO, int uid) {
        //处理关键句
        EssayStandardAnswerKeyPhraseVO resultVO = saveKeyPhase(keyPhraseVO, uid);
        //处理关键句的近似句
        List<EssayStandardAnswerKeyPhraseVO> similarPhraseList = keyPhraseVO.getSimilarPhraseList();
        if (CollectionUtils.isNotEmpty(similarPhraseList)) {
            for (EssayStandardAnswerKeyPhraseVO similarPhrase : similarPhraseList) {
                similarPhrase.setPid(resultVO.getId());
                similarPhrase.setType(APPLICATION_KEYPHRASE_TYPE);
                List<EssayStandardAnswerKeyWordVO> keyWordVOList = similarPhrase.getKeyWordVOList();
                if (CollectionUtils.isNotEmpty(keyWordVOList)) {
                    for (EssayStandardAnswerKeyWordVO keyWordVO : keyWordVOList) {
                        keyWordVO.setQuestionDetailId(resultVO.getQuestionDetailId());
                        //近似词
                        List<EssayStandardAnswerKeyWordVO> similarWordVOList = keyWordVO.getSimilarWordVOList();
                        if (CollectionUtils.isNotEmpty(similarWordVOList)) {
                            for (EssayStandardAnswerKeyWordVO similarWordVO : similarWordVOList) {
                                similarWordVO.setQuestionDetailId(resultVO.getQuestionDetailId());
                            }
                        }
                    }
                }
                similarPhrase = saveKeyPhase(similarPhrase, uid);
            }
        }
        resultVO.setSimilarPhraseList(similarPhraseList);
        return resultVO;
    }

    private EssayStandardAnswerKeyPhraseVO saveKeyPhase(EssayStandardAnswerKeyPhraseVO keyPhraseVO, int uid) {
        //保存关键句信息
        EssayStandardAnswerKeyPhrase keyPhrase = new EssayStandardAnswerKeyPhrase();
        BeanUtils.copyProperties(keyPhraseVO, keyPhrase);
        keyPhrase.setCreator(uid + "");
        keyPhrase.setGmtCreate(new Date());
        if (keyPhrase.getType() <= 0) {
            return new EssayStandardAnswerKeyPhraseVO();
        }
        EssayStandardAnswerKeyPhrase result = essayStandardAnswerKeyPhraseRepository.save(keyPhrase);
        //发送消息队列处理关键句(批改端 ： 切分关键词)
        sendMsgToKeyPhraseQueue(result);
        EssayStandardAnswerKeyPhraseVO resultVO = new EssayStandardAnswerKeyPhraseVO();
        BeanUtils.copyProperties(result, resultVO);
        //保存关键词信息
        List<EssayStandardAnswerKeyWordVO> keyWordVOList = keyPhraseVO.getKeyWordVOList();
        LinkedList<EssayStandardAnswerKeyWordVO> newKeyWordVOList = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(keyWordVOList)) {
            for (EssayStandardAnswerKeyWordVO keyWordVO : keyWordVOList) {
                keyWordVO.setCorrespondingId(result.getId());
                //添加关键词到数据库
                EssayStandardAnswerKeyWordVO keyWord = addAnswerKeyWord(keyWordVO, uid, EssayAnswerKeyWordConstant.QUESTION_KEYPHRASE_CHILD_KEYWORD);
                newKeyWordVOList.add(keyWord);
            }
        }
        resultVO.setKeyWordVOList(newKeyWordVOList);
        return resultVO;
    }

    /**
     * 发送处理关键句的消息队列
     *
     * @param result
     */
    private void sendMsgToKeyPhraseQueue(EssayStandardAnswerKeyPhrase result) {
        try {
            Map sendMap = Maps.newHashMap();
            sendMap.put("id", result.getId());
            sendMap.put("item", result.getItem());
            sendMap.put("type", result.getType());
            rabbitTemplate.convertAndSend(SystemConstant.ESSAY_STANDARD_ANSWER_KEY_PHRASE_QUEUE, sendMap);
            log.info("sending mq , id={},item={},type={}", result.getId(), result.getItem(), result.getType());
        } catch (Exception e) {
            log.warn("发送消息队列失败，keyPhraseId = {}", result.getId());
        }
    }

    /**
     * @param keyWordVO 关键词
     * @param uid       用户id
     * @param type      关键词的类型
     * @return
     */
    @Override
    @Transactional
    public EssayStandardAnswerKeyWordVO addAnswerKeyWord(EssayStandardAnswerKeyWordVO keyWordVO, int uid, int type) {
        EssayStandardAnswerKeyWordVO resultVO = new EssayStandardAnswerKeyWordVO();
        //处理 关键词(将关键词的PO数据得到，该关键词的近义词未处理)
        entityManager.clear();
        EssayStandardAnswerKeyWord keyWord = getEssayStandardAnswerKeyWordVO(keyWordVO, uid);
        //设定关键词的类型，由参数决定
        keyWord.setType(type);
        if (EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC == type) {
            keyWord.setCorrespondingId(keyWord.getQuestionDetailId());
        }

        //如果是有描述的关键词，correspondingId是描述的id（keyPhrase表的id）
        if (EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITH_DESC == type) {
            keyWord.setCorrespondingId(keyWord.getCorrespondingId());
            keyWord.setType(QUESTION_PARENT_KEYWORD_WITH_DESC);
        }
        EssayStandardAnswerKeyWord afterKeyWord = essayStandardAnswerKeyWordRepository.save(keyWord);

        BeanUtils.copyProperties(afterKeyWord, resultVO);
        List<EssayStandardAnswerKeyWordVO> similarWordVOList = keyWordVO.getSimilarWordVOList();
        LinkedList<EssayStandardAnswerKeyWordVO> newSimilarWordVOList = new LinkedList<>();
        //处理 关键词的近义词
        if (CollectionUtils.isNotEmpty(similarWordVOList)) {
            for (EssayStandardAnswerKeyWordVO similarKeyWordVO : similarWordVOList) {
                EssayStandardAnswerKeyWord similarWord = getEssayStandardAnswerKeyWordVO(similarKeyWordVO, uid);
                similarWord.setQuestionDetailId(keyWord.getQuestionDetailId());
                similarWord.setType(EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD);
                similarWord.setCorrespondingId(afterKeyWord.getId());
                similarWord = essayStandardAnswerKeyWordRepository.save(similarWord);
                EssayStandardAnswerKeyWordVO similarKeyWordVOAfter = new EssayStandardAnswerKeyWordVO();
                BeanUtils.copyProperties(similarWord, similarKeyWordVOAfter);
                newSimilarWordVOList.add(similarKeyWordVOAfter);
            }
            resultVO.setSimilarWordVOList(newSimilarWordVOList);
        }

        return resultVO;
    }

    private EssayStandardAnswerKeyWord getEssayStandardAnswerKeyWordVO(EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWord, int uid) {
        EssayStandardAnswerKeyWord target = new EssayStandardAnswerKeyWord();
        BeanUtils.copyProperties(essayStandardAnswerKeyWord, target);
        target.setCreator(uid + "");
        target.setGmtCreate(new Date());
        target.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        return target;
    }

    /**
     * 删除试题中的所有特殊分条规则记录
     *
     * @param questionDetailId
     * @param uid
     * @return
     */
    @Transactional
    public boolean delAnswerRuleSpecialStrip(long questionDetailId, int uid) {
        List<EssayStandardAnswerRuleSpecialStrip> specialStrips = essayStandardAnswerRuleSpecialStripRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionDetailId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        for (EssayStandardAnswerRuleSpecialStrip specialStrip : specialStrips) {
            specialStrip.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
            specialStrip.setModifier(uid + "");
            specialStrip.setGmtModify(new Date());
        }
        List<EssayStandardAnswerRuleSpecialStrip> result = essayStandardAnswerRuleSpecialStripRepository.save(specialStrips);
        log.info("删除特殊规则共：" + result.size() + "条");
        return true;
    }

    @Override
    public EssayStandardAnswerRuleSpecialStripVO addAnswerRuleSpecialStrip(EssayStandardAnswerRuleSpecialStripVO essayStandardAnswerRuleSpecialStrip, int uid) {
        EssayStandardAnswerRuleSpecialStrip target = new EssayStandardAnswerRuleSpecialStrip();
        BeanUtils.copyProperties(essayStandardAnswerRuleSpecialStrip, target);
        target.setCreator(uid + "");
        target.setGmtCreate(new Date());
        target.setStatus(1);
        EssayStandardAnswerRuleSpecialStrip result = essayStandardAnswerRuleSpecialStripRepository.save(target);
        EssayStandardAnswerRuleSpecialStripVO resultVO = new EssayStandardAnswerRuleSpecialStripVO();
        BeanUtils.copyProperties(result, resultVO);
        return resultVO;
    }

    @Override
    public EssayStandardAnswerRuleStripSegmentalVO addAnswerRuleStripSegmental(EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental, int uid) {
        EssayStandardAnswerRuleStripSegmental target = new EssayStandardAnswerRuleStripSegmental();
        BeanUtils.copyProperties(target, essayStandardAnswerRuleStripSegmental);
        target.setCreator(uid + "");
        target.setGmtCreate(new Date());
        EssayStandardAnswerRuleStripSegmental result = essayStandardAnswerRuleStripSegmentalRepository.save(target);
        EssayStandardAnswerRuleStripSegmentalVO resultVO = new EssayStandardAnswerRuleStripSegmentalVO();
        BeanUtils.copyProperties(result, resultVO);
        return resultVO;
    }

    @Override
    public EssayStandardAnswerRuleWordNumVO addAnswerRuleWordNum(EssayStandardAnswerRuleWordNumVO essayStandardAnswerRuleWordNum, int uid) {
        EssayStandardAnswerRuleWordNum target = new EssayStandardAnswerRuleWordNum();
        BeanUtils.copyProperties(target, essayStandardAnswerRuleWordNum);
        target.setCreator(uid + "");
        target.setGmtCreate(new Date());
        EssayStandardAnswerRuleWordNum result = essayStandardAnswerRuleWordNumRepository.save(target);
        EssayStandardAnswerRuleWordNumVO resultVO = new EssayStandardAnswerRuleWordNumVO();
        BeanUtils.copyProperties(result, resultVO);
        return resultVO;
    }

    @Override
    public EssayStandardAnswerRuleWordNumVO updateAnswerRuleWordNum(EssayStandardAnswerRuleWordNumVO essayStandardAnswerRuleWordNum, int uid) {
        EssayStandardAnswerRuleWordNum target = new EssayStandardAnswerRuleWordNum();
        BeanUtils.copyProperties(target, essayStandardAnswerRuleWordNum);
        target.setModifier(uid + "");
        target.setGmtModify(new Date());
        EssayStandardAnswerRuleWordNum result = essayStandardAnswerRuleWordNumRepository.save(target);
        EssayStandardAnswerRuleWordNumVO resultVO = new EssayStandardAnswerRuleWordNumVO();
        BeanUtils.copyProperties(result, resultVO);
        return resultVO;
    }

    @Override
    public EssayStandardAnswerRuleStripSegmentalVO updateAnswerRuleStripSegmental(EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental, int uid) {
        EssayStandardAnswerRuleStripSegmental target = new EssayStandardAnswerRuleStripSegmental();
        BeanUtils.copyProperties(target, essayStandardAnswerRuleStripSegmental);
        target.setModifier(uid + "");
        target.setGmtModify(new Date());
        EssayStandardAnswerRuleStripSegmental result = essayStandardAnswerRuleStripSegmentalRepository.save(target);
        EssayStandardAnswerRuleStripSegmentalVO resultVO = new EssayStandardAnswerRuleStripSegmentalVO();
        BeanUtils.copyProperties(result, resultVO);
        return resultVO;
    }

    @Override
    public EssayStandardAnswerRuleSpecialStripVO updateAnswerRuleSpecialStrip(EssayStandardAnswerRuleSpecialStripVO essayStandardAnswerRuleSpecialStrip, int uid) {
        EssayStandardAnswerRuleSpecialStrip target = new EssayStandardAnswerRuleSpecialStrip();
        BeanUtils.copyProperties(target, essayStandardAnswerRuleSpecialStrip);
        target.setModifier(uid + "");
        target.setGmtModify(new Date());
        EssayStandardAnswerRuleSpecialStrip result = essayStandardAnswerRuleSpecialStripRepository.save(target);
        EssayStandardAnswerRuleSpecialStripVO resultVO = new EssayStandardAnswerRuleSpecialStripVO();
        BeanUtils.copyProperties(result, resultVO);
        return resultVO;
    }

    @Override
    public EssayStandardAnswerKeyWordVO updateAnswerKeyWord(EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWord, int uid) {
        return addAnswerKeyWord(essayStandardAnswerKeyWord, uid, EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC);
    }

    @Override
    public EssayStandardAnswerKeyPhraseVO updateAnswerKeyPhrase(EssayStandardAnswerKeyPhraseVO essayStandardAnswerKeyPhrase, int uid) {
        return addAnswerKeyPhrase(essayStandardAnswerKeyPhrase, uid);
    }

    @Override
    public EssayStandardAnswerFormatVO updateAnswerFormat(EssayStandardAnswerFormatVO essayStandardAnswerFormat, int uid) {
        return addAnswerFormat(essayStandardAnswerFormat, uid);
    }

    /**
     * 查询试题id下的所有普通扣分规则
     *
     * @param questionId
     * @return
     */
    @Override
    public List<EssayStandardAnswerRuleVO> findCommonAnswerRule(long questionId) {
        List<EssayStandardAnswerRule> ruleList = essayStandardAnswerRuleRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        List<EssayStandardAnswerRuleVO> result = Lists.newLinkedList();
        for (EssayStandardAnswerRule rule : ruleList) {
            EssayStandardAnswerRuleVO resultVO = new EssayStandardAnswerRuleVO();
            BeanUtils.copyProperties(rule, resultVO);
            result.add(resultVO);
        }
        result.sort((a, b) -> (int) (a.getId() - b.getId()));
        return result;
    }

    /**
     * 修改试题普通规则
     *
     * @param essayStandardAnswerRuleVO
     * @return
     */
    @Override
    public EssayStandardAnswerRuleVO addCommonAnswerRule(EssayStandardAnswerRuleVO essayStandardAnswerRuleVO, int uid) {
        EssayStandardAnswerRule essayStandardAnswerRule = new EssayStandardAnswerRule();
        EssayStandardAnswerRuleVO result = new EssayStandardAnswerRuleVO();
        BeanUtils.copyProperties(essayStandardAnswerRuleVO, essayStandardAnswerRule);
        essayStandardAnswerRule.setCreator(uid + "");
        essayStandardAnswerRule.setGmtCreate(new Date());
        EssayStandardAnswerRule rule = essayStandardAnswerRuleRepository.save(essayStandardAnswerRule);
        BeanUtils.copyProperties(rule, result);
        //如果扣分规则为字数限制，将字数限制录入试题表中
        if (essayStandardAnswerRule.getType() == 1) {
            setQuestionLimitNum(essayStandardAnswerRule);
        }
        return result;
    }

    public boolean delCommonAnswerRule(long questionDetailId, int uid) {
        List<EssayStandardAnswerRule> rules = essayStandardAnswerRuleRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionDetailId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        for (EssayStandardAnswerRule rule : rules) {
            rule.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
            rule.setModifier(uid + "");
            rule.setGmtModify(new Date());
        }
        List<EssayStandardAnswerRule> result = essayStandardAnswerRuleRepository.save(rules);
        log.info("删除普通规则共：" + result.size() + "条");
        return true;
    }

    @Override
    public void delAllDeductRuleByQuestion(long questionDetailId, int uid) {
        delAnswerRuleSpecialStrip(questionDetailId, uid);
        delCommonAnswerRule(questionDetailId, uid);
    }

//    @Override
//    public void delAllKeyRuleByQuestion(long questionDetailId, int uid) {
//        //查询试题下所有的关键词
//        List<EssayStandardAnswerKeyWord> keyWordList = essayStandardAnswerKeyWordRepository.findByQuestionDetailIdAndBizStatusAndStatusInOrderByIdAsc(questionDetailId,
//                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
//                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
//        //查询试卷下所有的关键句（1为应用文关键句，2为议论文中心思想，3为议论文主题）分情况处理
//        List<EssayStandardAnswerKeyPhrase> keyPhraseList = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionDetailId,
//                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
//                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
//
//        List<Long> relationIds = Lists.newLinkedList();      //所有议论文主题id
//        List<EssayStandardAnswerKeyWord> list = Lists.newLinkedList();  //需要置为删除状态的关键词集合
//        Set<Long> keyPhraseIds = Sets.newHashSet();     //关键句id集合
//        for (EssayStandardAnswerKeyPhrase keyPhrase : keyPhraseList) {
//            keyPhraseIds.add(keyPhrase.getId());
//            keyPhrase.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
//            if (AdminKeyPhraseTypeConstant.ARGUMENTATION_TOPIC_SUBJECT_TYPE == keyPhrase.getType()) {
//                relationIds.add(keyPhrase.getId());
//            }
//        }
//        //查询分词情况(按照关键句类型为3的关键句id查询)
//        List<EssayStandardAnswerSplitWord> splitWords = essayStandardAnswerSplitWordRepository.findByRelationIdInAndBizStatusAndStatus(relationIds,
//                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
//                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
//        for (EssayStandardAnswerSplitWord word : splitWords) {
//            word.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
//        }
//        essayStandardAnswerSplitWordRepository.save(splitWords);
//        essayStandardAnswerKeyPhraseRepository.save(keyPhraseList);
//        Set<Long> keyWordIds = Sets.newHashSet();
//        for (EssayStandardAnswerKeyWord keyWord : keyWordList) {
//            if (keyPhraseIds.contains(keyWord.getCorrespondingId()) && EssayAnswerKeyWordConstant.QUESTION_KEYPHRASE_CHILD_KEYWORD == keyWord.getType()) {
//                list.add(keyWord);
//                keyWord.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
//                keyWordIds.add(keyWord.getId());
//            }
//            if (EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD_WITHOUT_DESC == keyWord.getType()) {
//                list.add(keyWord);
//                keyWord.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
//                keyWordIds.add(keyWord.getId());
//            }
//        }
//        for (EssayStandardAnswerKeyWord keyWord : keyWordList) {
//            if (EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD == keyWord.getType() &&
//                    keyWordIds.contains(keyWord.getCorrespondingId())) {
//                keyWord.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
//                list.add(keyWord);
//            }
//        }
//        essayStandardAnswerKeyWordRepository.save(list);
//    }

    @Override
    public void delEssayAnswerFormatByQuestion(long questionDetailId, int uid) {
        List<EssayStandardAnswerFormat> formats = essayStandardAnswerFormatRepository.findByQuestionDetailIdAndBizStatusAndStatus(questionDetailId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        Set<Long> ids = Sets.newHashSet();
        for (EssayStandardAnswerFormat format : formats) {
            format.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
            ids.add(format.getId());
        }
        List<EssayStandardAnswerKeyWord> keyWords = Lists.newLinkedList();
        Set<Long> parentIds = Sets.newHashSet();
        List<EssayStandardAnswerKeyWord> keyWordList = essayStandardAnswerKeyWordRepository.findByQuestionDetailIdAndBizStatusAndStatusInOrderByIdAsc(questionDetailId,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        for (EssayStandardAnswerKeyWord keyWord : keyWordList) {
            if (EssayAnswerKeyWordConstant.QUESTION_FROM_TITLE_CHILD_KEYWORD == keyWord.getType() ||
                    EssayAnswerKeyWordConstant.QUESTION_FROM_APPELLATION_CHILD_KEYWORD == keyWord.getType() ||
                    EssayAnswerKeyWordConstant.QUESTION_FROM_INSCRIBE_CHILD_KEYWORD == keyWord.getType()) {
                ids.add(keyWord.getCorrespondingId());
                keyWord.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
                keyWords.add(keyWord);
                parentIds.add(keyWord.getId());
            }
        }
        for (EssayStandardAnswerKeyWord keyWord : keyWordList) {
            if (EssayAnswerKeyWordConstant.QUESTION_KEYWORD_CHILD_KEYWORD == keyWord.getType() &&
                    parentIds.contains(keyWord.getCorrespondingId())) {
                keyWord.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
                keyWords.add(keyWord);
            }
        }
        essayStandardAnswerFormatRepository.save(formats);
        essayStandardAnswerKeyWordRepository.save(keyWords);
    }

    @Override
    public AdminQuestionTopicVO addQuestionTopicVO(AdminQuestionTopicVO adminQuestionTopicVO, int uid) {
        String userId = uid + "";
        EssayStandardAnswerKeyPhrase essayStandardAnswerKeyPhrase = new EssayStandardAnswerKeyPhrase();
        BeanUtils.copyProperties(adminQuestionTopicVO, essayStandardAnswerKeyPhrase);
        essayStandardAnswerKeyPhrase.setType(AdminKeyPhraseTypeConstant.ARGUMENTATION_TOPIC_SUBJECT_TYPE);
        essayStandardAnswerKeyPhrase.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        essayStandardAnswerKeyPhrase.setBizStatus(EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus());
        essayStandardAnswerKeyPhrase.setCreator(userId);
        essayStandardAnswerKeyPhrase.setGmtCreate(new Date());
        EssayStandardAnswerKeyPhrase keyPhrase = essayStandardAnswerKeyPhraseRepository.save(essayStandardAnswerKeyPhrase);
        //消息队列处理主题关键句的切词
        sendMsgToKeyPhraseQueue(keyPhrase);
        AdminQuestionTopicVO resultVO = new AdminQuestionTopicVO();
        BeanUtils.copyProperties(keyPhrase, resultVO);
        List<EssayStandardAnswerSplitWordVO> wordList = adminQuestionTopicVO.getSplitWordList();
        List<EssayStandardAnswerSplitWordVO> childList = Lists.newLinkedList();
        for (EssayStandardAnswerSplitWordVO wordVO : wordList) {
            if (StringUtils.isBlank(wordVO.getItem())) {
                continue;
            }
            EssayStandardAnswerSplitWordVO childVO = new EssayStandardAnswerSplitWordVO();
            EssayStandardAnswerSplitWord word = new EssayStandardAnswerSplitWord();
            BeanUtils.copyProperties(wordVO, word);
            word.setType(4); //表示是主题的重要词
            word.setRelationId(keyPhrase.getId());
            word.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
            word.setBizStatus(EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus());
            word.setCreator(userId);
            word.setGmtCreate(new Date());
            EssayStandardAnswerSplitWord result = essayStandardAnswerSplitWordRepository.save(word);
            BeanUtils.copyProperties(result, childVO);
            childList.add(childVO);
        }
        resultVO.setSplitWordList(childList);
        return resultVO;
    }

    @Override
    public AdminQuestionTopicVO findAnswerTopicByQuestion(long questionDetailId) {
        AdminQuestionTopicVO result = new AdminQuestionTopicVO();
        List<EssayStandardAnswerKeyPhrase> keyPhrases = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatus(questionDetailId,
                AdminKeyPhraseTypeConstant.ARGUMENTATION_TOPIC_SUBJECT_TYPE,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus()
        );
        if (CollectionUtils.isEmpty(keyPhrases)) {
            return result;
        }
        //一个议论文最多只有一个主体
        BeanUtils.copyProperties(keyPhrases.get(0), result);
        List<EssayStandardAnswerSplitWord> splitWords = essayStandardAnswerSplitWordRepository.findByRelationIdAndBizStatusAndStatus(result.getId(),
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        List<EssayStandardAnswerSplitWordVO> splitWordList = Lists.newLinkedList();
        for (EssayStandardAnswerSplitWord splitWord : splitWords) {
            //主体主要分词即为类型4
            if (4 == splitWord.getType()) {
                EssayStandardAnswerSplitWordVO child = new EssayStandardAnswerSplitWordVO();
                BeanUtils.copyProperties(splitWord, child);
                splitWordList.add(child);
            }
        }
        result.setSplitWordList(splitWordList);
        return result;
    }


    /**
     * 通过问题的字数限制扣分规则补充题目字数限制属性
     *
     * @param answerRule
     */
    public void setQuestionLimitNum(EssayStandardAnswerRule answerRule) {
        long detailId = answerRule.getQuestionDetailId();
        int max = answerRule.getMaxNum();
        int min = answerRule.getMinNum();
        EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(detailId);
        essayQuestionDetail.setInputWordNumMax(max);
        essayQuestionDetail.setInputWordNumMin(min);
        essayQuestionDetailRepository.save(essayQuestionDetail);
        log.info("----设置试题的字数限制----，detailId = {},min={},max={}", detailId, min, max);
    }

    @Override
    public ResponseVO refresh() {

        //查询所有的应用文
        List<EssayQuestionDetail> essayQuestionDetails = essayQuestionDetailRepository.findByStatusAndType(1, 4);
        if (CollectionUtils.isNotEmpty(essayQuestionDetails)) {
            log.info("共有应用文题目数：{}", essayQuestionDetails.size());
            for (EssayQuestionDetail detail : essayQuestionDetails) {
                List<EssayStandardAnswerRule> ruleList = essayStandardAnswerRuleRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatus(detail.getId(),
                        EssayAnswerReductRuleConstant.STRIP_SEGMENTAL_RANGE,
                        EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                        EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());

                if (CollectionUtils.isNotEmpty(ruleList)) {
                    EssayStandardAnswerRule answerRule = ruleList.get(0);

                    answerRule.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.DELETED.getStatus());
                    answerRule.setModifier("zxtest0316");
                    essayStandardAnswerRuleRepository.save(answerRule);

//                    String standardAnswerKey = RedisKeyConstant.getStandardAnswerKey(detail.getId());
//                    redisTemplate.delete(standardAnswerKey);
//                    log.info("清除标准答案缓存成功，key值:"+standardAnswerKey);
                    ResponseEntity<ResponseMsg> forEntity = restTemplate.postForEntity("http://192.168.10.34:8090/cr/init/toRedis?id=" + detail.getId(), null,
                            ResponseMsg.class);
                    ResponseMsg body = forEntity.getBody();
                    if (null != body && null != body.getData()) {
                        if (!(boolean) body.getData()) {
                            log.warn("刷新缓存失败111。id：{}", detail.getId());
                            throw new BizException(EssayErrors.REDIS_REFRESH_ERROR);
                        }
                    } else {
                        log.warn("刷新缓存失败222。id：{}", detail.getId());
                        throw new BizException(EssayErrors.REDIS_REFRESH_ERROR);
                    }
                }
            }
        }
        return ResponseVO.builder().flag(true).build();

    }

    /**
     * 添加带描述的关键词
     *
     * @param adminQuestionKeyWordWithDescVO
     * @param uid
     * @return
     */
    @Override
    public AdminQuestionKeyWordWithDescVO addAnswerKeyWordWithDesc(AdminQuestionKeyWordWithDescVO adminQuestionKeyWordWithDescVO, int uid) {
        EssayStandardAnswerKeyPhrase keyPhrase = new EssayStandardAnswerKeyPhrase();
        BeanUtils.copyProperties(adminQuestionKeyWordWithDescVO, keyPhrase);
        //关键词描述type是5,position是2（默认）
        keyPhrase.setPosition(2);
        keyPhrase.setType(AdminKeyPhraseTypeConstant.DESC_OF_KEYWORD);
        keyPhrase.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());

        //保存关键词描述
        EssayStandardAnswerKeyPhrase save = essayStandardAnswerKeyPhraseRepository.save(keyPhrase);

        //保存有描述的关键词
        List<AdminQuestionKeyWordVO> keyWordList = adminQuestionKeyWordWithDescVO.getKeyWordList();
        if (CollectionUtils.isNotEmpty(keyWordList)) {
            keyWordList.forEach(keyWordVO -> {
                EssayStandardAnswerKeyWordVO essayStandardAnswerKeyWordVO = EssayConvertUtil.convertKeyWordPre2VO(keyWordVO);
                essayStandardAnswerKeyWordVO.setQuestionDetailId(keyPhrase.getQuestionDetailId());
                essayStandardAnswerKeyWordVO.setCorrespondingId(save.getId());
                addAnswerKeyWord(essayStandardAnswerKeyWordVO, uid, QUESTION_PARENT_KEYWORD_WITH_DESC);
            });
        }

        return null;
    }

    @Override
    public AdminQuestionKeyPhraseWithDescVO addAnswerKeyPhraseWithDesc(AdminQuestionKeyPhraseWithDescVO adminQuestionKeyPhraseWithDescVO, int uid) {

        EssayStandardAnswerKeyPhrase keyPhrase = new EssayStandardAnswerKeyPhrase();
        BeanUtils.copyProperties(adminQuestionKeyPhraseWithDescVO, keyPhrase);
        //关键句描述type是5,position是2（默认）
        keyPhrase.setPosition(2);
        keyPhrase.setStatus(EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());
        keyPhrase.setType(AdminKeyPhraseTypeConstant.DESC_OF_KEYPHASE);
        //保存关键句描述
        EssayStandardAnswerKeyPhrase save = essayStandardAnswerKeyPhraseRepository.save(keyPhrase);

        long questionDetailId = keyPhrase.getQuestionDetailId();
        //保存有描述的关键句
        List<AdminQuestionKeyPhraseVO> keyPhraseList = adminQuestionKeyPhraseWithDescVO.getKeyPhraseList();
        if (CollectionUtils.isNotEmpty(keyPhraseList)) {
            keyPhraseList.forEach(keyPhraseVO -> {
                keyPhraseVO.setType(APPLICATION_KEYPHRASE_TYPE);
                keyPhraseVO.setQuestionDetailId(questionDetailId);
                keyPhraseVO.setPid(save.getId());
                EssayStandardAnswerKeyPhraseVO target = EssayConvertUtil.convertKeyPhrasePre2VO(keyPhraseVO);
                addAnswerKeyPhrase(target, uid);
            });
        }
        return null;
    }

    /**
     * 查询有描述的关键词
     *
     * @param questionDetailId
     * @return
     */
    @Override
    public List<AdminQuestionKeyWordWithDescVO> findAnswerKeyWordByQuestionWithDesc(long questionDetailId) {
        List<EssayStandardAnswerKeyPhrase> keyWordDescList = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatus(questionDetailId,
                AdminKeyPhraseTypeConstant.DESC_OF_KEYWORD,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());

        if (CollectionUtils.isNotEmpty(keyWordDescList)) {
            LinkedList<AdminQuestionKeyWordWithDescVO> keyWordWithDescVOList = new LinkedList<>();
            keyWordDescList.forEach(keyWordDesc -> {
                //查询
                List<EssayStandardAnswerKeyWordVO> answerKeyWordListByQuestion = findAnswerKeyWordByQuestion(questionDetailId, QUESTION_PARENT_KEYWORD_WITH_DESC, keyWordDesc.getId());
                answerKeyWordListByQuestion.sort((a, b) -> (int) (a.getId() - b.getId()));
                //格式转换
                List<AdminQuestionKeyWordVO> adminQuestionKeyWordVOS = EssayConvertUtil.convertBatchKeyWordVO2Pre(answerKeyWordListByQuestion);
                AdminQuestionKeyWordWithDescVO keyWordWithDescVO = AdminQuestionKeyWordWithDescVO.builder()
                        .keyWordList(adminQuestionKeyWordVOS)
                        .item(keyWordDesc.getItem())
                        .score(keyWordDesc.getScore())
                        .id(keyWordDesc.getId())
                        .build();

                keyWordWithDescVOList.add(keyWordWithDescVO);
            });

            return keyWordWithDescVOList;
        } else {
            return null;
        }

    }

    /**
     * 查询有描述的关键句
     *
     * @param questionDetailId
     * @return
     */
    @Override
    public List<AdminQuestionKeyPhraseWithDescVO> findAnswerKeyPhraseByQuestionWithDesc(long questionDetailId) {
        List<EssayStandardAnswerKeyPhrase> keyPhraseDescList = essayStandardAnswerKeyPhraseRepository.findByQuestionDetailIdAndTypeAndBizStatusAndStatus(questionDetailId,
                AdminKeyPhraseTypeConstant.DESC_OF_KEYPHASE,
                EssayAnswerRuleConstant.EssayAnswerRuleBizStatusEnum.ONLINE.getBizStatus(),
                EssayAnswerRuleConstant.EssayAnswerRuleStatusEnum.NORMAL.getStatus());

        if (CollectionUtils.isNotEmpty(keyPhraseDescList)) {
            LinkedList<AdminQuestionKeyPhraseWithDescVO> keyPhraseWithDescVOList = new LinkedList<>();
            for (EssayStandardAnswerKeyPhrase keyPhrasedWithDesc : keyPhraseDescList) {
                List<EssayStandardAnswerKeyPhraseVO> answerKeyPhraseByQuestion = findAnswerKeyPhraseByQuestion(questionDetailId, APPLICATION_KEYPHRASE_WITH_DESC_TYPE, keyPhrasedWithDesc.getId());
                //格式转换
                List<AdminQuestionKeyPhraseVO> adminQuestionKeyPhraseVOS = EssayConvertUtil.convertBatchKeyPhraseVO2Pre(answerKeyPhraseByQuestion);
                AdminQuestionKeyPhraseWithDescVO keyPhraseWithDescVO = AdminQuestionKeyPhraseWithDescVO.builder()
                        .keyPhraseList(adminQuestionKeyPhraseVOS)
                        .item(keyPhrasedWithDesc.getItem())
                        .score(keyPhrasedWithDesc.getScore())
                        .id(keyPhrasedWithDesc.getId())
                        .build();

                keyPhraseWithDescVOList.add(keyPhraseWithDescVO);
            }

            return keyPhraseWithDescVOList;
        } else {
            return null;
        }
    }


}
