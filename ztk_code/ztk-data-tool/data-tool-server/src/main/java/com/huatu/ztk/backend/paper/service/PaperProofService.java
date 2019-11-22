package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.bean.ModuleBean;
import com.huatu.ztk.backend.paper.bean.PaperQuestionBean;
import com.huatu.ztk.backend.paper.bean.TikuQuestionType;
import com.huatu.ztk.backend.paper.controller.PaperController;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.*;
import com.itextpdf.text.BadElementException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 试卷试题关系 controller
 * Created by linkang on 3/9/17.
 */

@Service
public class PaperProofService {

    private static final Logger logger = LoggerFactory.getLogger(PaperController.class);

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private PaperQuestionDao paperQuestionDao;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    /**
     * 查询试卷全部试题数据
     *
     * @param paperId  试卷id
     * @param page     页码
     * @param size     试题个数
     * @param moduleId 模块id，0：全部模块
     * @return
     * @throws BizException
     */
    public Map findPaperProof(int paperId, int page, int size, int moduleId) throws BizException {

        Paper paper = paperDao.findById(paperId);

        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        List<Integer> totalQids = paper.getQuestions();
        if (CollectionUtils.isEmpty(totalQids)) {
            return Maps.newHashMap();
        }

        List<ModuleBean> moduleBeanList = paperService.getModuleBeanList(paper);

        ModuleBean moduleBean = null;

        //全部模块
        if (moduleId == 0) {

            Map total = new HashMap();
            for (ModuleBean bean : moduleBeanList) {
                if (MapUtils.isNotEmpty(bean.getQuestions())) {
                    total.putAll(bean.getQuestions());
                }
            }
            moduleBean = ModuleBean.builder()
                    .questions(total)
                    .build();

        } else {
            moduleBean = moduleBeanList.stream()
                    .filter(i -> i.getId() == moduleId)
                    .findAny()
                    .orElse(null);
        }

        if (moduleBean == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }


        //题序,试题id map
        Map<Integer, Integer> indexQidMap = moduleBean.getQuestions();

        if (MapUtils.isEmpty(indexQidMap)) {
            return Maps.newHashMap();
        }

        //分页
        List<Integer> indexs = indexQidMap.keySet().stream().skip((page - 1) * size).limit(size)
                .collect(Collectors.toList());

        //页面需要的题序，试题id map
        Map<Integer, Integer> newIndexQidMap = new LinkedHashMap<>();

        for (Integer index : indexs) {
            newIndexQidMap.put(index, indexQidMap.get(index));
        }

        ArrayList<PaperQuestionBean> questionList = new ArrayList<>();

        Map<Integer, Question> questionMap = paperQuestionDao.findBath(newIndexQidMap.values().stream().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(i -> i.getId(), i -> i));

        Map<Integer, QuestionExtend> extendMap = paperQuestionDao.findExtendBath(newIndexQidMap.values().stream().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(i -> i.getQid(), i -> i));

        //存放已经处理过的试题id
        Set<Integer> finishSet = new HashSet<>();


        Map<Integer, Integer> qidIndexMap = newIndexQidMap.keySet()
                .stream()
                .collect(Collectors.toMap(i -> newIndexQidMap.get(i), i -> i));


        //key：下标，value：qid
        for (Map.Entry<Integer, Integer> entry : newIndexQidMap.entrySet()) {
            Integer qid = entry.getValue();

            //已经处理过
            if (finishSet.contains(qid)) {
                continue;
            }

            Question currentQuestion = questionMap.get(qid);
            if (currentQuestion != null) {

                int parentId = paperQuestionService.findParentId(currentQuestion);

                //子试题id
                List<Integer> childrenIds = null;

                //复合题材料
                List<String> materials = new ArrayList<>();

                List<PaperQuestionBean> childrens = new ArrayList<>();

                if (parentId > 0) {
                    Question parent = paperQuestionDao.findQuestionById(parentId);

                    if (parent instanceof CompositeQuestion) {

                        //复合客观题材料
                        CompositeQuestion compositeQuestion = (CompositeQuestion) parent;
                        materials.add(compositeQuestion.getMaterial());
                        childrenIds = compositeQuestion.getQuestions();
                    } else if (parent instanceof CompositeSubjectiveQuestion) {
                        if (CollectionUtils.isNotEmpty(parent.getMaterials())) {
                            //复合主观题，材料
                            materials.addAll(parent.getMaterials());
                        }

                        childrenIds = ((CompositeSubjectiveQuestion) parent).getQuestions();
                    }

                    //移除不在页面中的子试题id
                    childrenIds.removeIf(i -> !newIndexQidMap.values().contains(i));

                    for (int childId : childrenIds) {

                        Question child = questionMap.get(childId);

                        QuestionExtend childExtend = extendMap.get(childId);

                        PaperQuestionBean childBean = PaperQuestionBean.builder()
                                .tikuType(child instanceof GenericQuestion ?
                                        TikuQuestionType.SINGLE_OBJECTIVE : TikuQuestionType.SINGLE_SUBJECTIVE)
                                .question(child)
                                .index(qidIndexMap.get(childId))
                                .score(child.getScore())
                                .extend(childExtend != null ? childExtend : new QuestionExtend())
                                .build();
                        childrens.add(childBean);

                        finishSet.add(childId);
                    }

                    QuestionExtend extend = paperQuestionDao.findExtendById(parentId);
                    PaperQuestionBean bean = PaperQuestionBean.builder()
                            .tikuType(parent instanceof CompositeQuestion ?
                                    TikuQuestionType.MULTI_OBJECTIVE : TikuQuestionType.MULTI_SUBJECTIVE)
                            .question(parent)
                            .extend(extend != null ? extend : new QuestionExtend())
                            .childrens(childrens)
                            .build();
                    questionList.add(bean);
                } else {
                    QuestionExtend extend = extendMap.get(qid);
                    PaperQuestionBean bean = PaperQuestionBean.builder()
                            .tikuType(currentQuestion instanceof GenericQuestion ?
                                    TikuQuestionType.SINGLE_OBJECTIVE : TikuQuestionType.SINGLE_SUBJECTIVE)
                            .question(currentQuestion)
                            .extend(extend != null ? extend : new QuestionExtend())
                            .score(currentQuestion.getScore())
                            .index(entry.getKey())
                            .childrens(childrens)
                            .build();
                    questionList.add(bean);
                }
            }
        }

        Map result = new HashMap();
        result.put("questions", questionList);
        result.put("totalCount", moduleBean.getQuestions().keySet().size());
        result.put("paperType", paper.getType());
        return result;
    }


    /**
     * 校对
     * 删除试卷某一模块中的试题
     *
     * @param paperId
     * @param questionId
     * @throws BizException
     */
    public void deleteProofQuestion(int paperId, int questionId,String account,int id) throws BizException, IOException, BadElementException {
        paperQuestionService.deleteProofQuestion(paperId, questionId,account,id);
    }


}
