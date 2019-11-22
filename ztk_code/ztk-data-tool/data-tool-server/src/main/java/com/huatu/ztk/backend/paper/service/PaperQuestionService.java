package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.paper.bean.*;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.question.common.error.QuestionError;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.*;
import com.itextpdf.text.BadElementException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by linkang on 3/14/17.
 */

@Service
public class PaperQuestionService {

    private static final Logger logger = LoggerFactory.getLogger(PaperQuestionService.class);

    @Autowired
    private PaperDao paperDao;


    @Autowired
    private PaperQuestionDao paperQuestionDao;


    @Autowired
    private PaperService paperService;

    @Autowired
    private QuestionService questionService;


    @Autowired
    private QuestionDao questionDao;

    /**
     * 将题目插入试卷
     */
    public void insertQuestion(QuestionExtend target) throws BizException{
        Paper paper = paperDao.findById(target.getPaperId());

        List<Integer> oldQuestions = paper.getQuestions();

        if (CollectionUtils.isEmpty(oldQuestions)) {

            List<Module> modules = paper.getModules();

            for (Module module : modules) {
                if (module.getCategory() == target.getModuleId()) {
                    module.setQcount(1);
                }
            }
            paper.setModules(modules);
            paper.setQuestions(Arrays.asList(target.getQid()));
            paper.setQcount(1);
            paperDao.update(paper);
            return;
        }

        if (oldQuestions.contains(target.getQid())) {
            throw new BizException(PaperErrors.EXISTS_QID);
        }


        //已经排好序号的题目extend
        List<QuestionExtend> totalExtendList = paperQuestionDao.findExtendBath(oldQuestions);

        //moduleId,extend  map
        ArrayListMultimap<Integer, QuestionExtend> map = ArrayListMultimap.create();

        for (QuestionExtend extend : totalExtendList) {
            map.put(extend.getModuleId(), extend);
        }

        List<Module> modules = paper.getModules();

        //新的试题id列表
        List<Integer> newQuestions = new ArrayList<>();

        for (int i = 0; i < modules.size(); i++) {

            Module module = modules.get(i);
            List<QuestionExtend> extendList = map.get(module.getCategory());

            if (module.getCategory() == target.getModuleId()) {
                module.setQcount(module.getQcount() + 1);
                //先插入再排序
                extendList.add(target);
            }

            extendList.sort((a, b) -> (a.getSequence() > b.getSequence() ? 1 : -1));
            extendList.forEach(e -> newQuestions.add(e.getQid()));
        }

        paper.setModules(modules);
        paper.setQuestions(newQuestions);
        paper.setQcount(newQuestions.size());

        updateBigQustionsAndPaper(paper);
    }

    /**
     * 试题查看
     *
     * @param id
     * @return
     * @throws BizException
     */
    public Object findQuestionById(int id) throws BizException {
        Question question = paperQuestionDao.findQuestionById(id);

        if (question instanceof GenericQuestion
                || question instanceof GenericSubjectiveQuestion) {
            return getSingle(question);
        } else {
            return getMultiObjective(question);
        }
    }

    /**
     * 组装单一客观题，单一主观题
     *
     * @param question
     * @return
     * @throws BizException
     */
    public PaperQuestionBean getSingle(Question question) throws BizException {

        QuestionExtend qe = paperQuestionDao.findExtendById(question.getId());
        PaperQuestionBean bean = PaperQuestionBean.builder()
                .tikuType(question instanceof GenericSubjectiveQuestion ?
                        TikuQuestionType.SINGLE_SUBJECTIVE : TikuQuestionType.SINGLE_OBJECTIVE)
                .question(question)
                .score(question.getScore())
                .extend(qe == null ? new QuestionExtend() : qe)
                .childrens(null)
                .build();
        return bean;
    }

    /**
     * 组装复合题
     *
     * @param question
     * @return
     */
    private PaperQuestionBean getMultiObjective(Question question) throws BizException {

        List<Question> childrens = null;

        if (question instanceof CompositeQuestion) {
            CompositeQuestion parent = (CompositeQuestion) question;
            childrens = paperQuestionDao.findBath(parent.getQuestions());
        } else if (question instanceof CompositeSubjectiveQuestion) {
            CompositeSubjectiveQuestion parent = (CompositeSubjectiveQuestion) question;
            childrens = paperQuestionDao.findBath(parent.getQuestions());
        }

        List<PaperQuestionBean> childrenBeans = childrens.stream().map(i -> {
            try {
                return getSingle(i);
            } catch (BizException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());

        QuestionExtend qe = paperQuestionDao.findExtendById(question.getId());

        PaperQuestionBean bean = PaperQuestionBean.builder()
                .tikuType(question instanceof CompositeQuestion ?
                        TikuQuestionType.MULTI_OBJECTIVE : TikuQuestionType.MULTI_SUBJECTIVE)
                .question(question)
                .childrens(childrenBeans)
                .extend(qe == null ? new QuestionExtend() : qe)
                .build();
        return bean;
    }

    /**
     * 判断题序是否已经被占用了
     *
     * @param paperId
     * @param sequence
     * @return
     */
    public void judgeDuplication(int paperId, int sequence) throws BizException {
        logger.info("得到paperId",paperId);
        Paper paper = paperDao.findById(paperId);

        List<Integer> qids = paper.getQuestions();

        if (CollectionUtils.isNotEmpty(qids)) {
            //已经排好序号的题目extend
            List<QuestionExtend> totalExtendList = paperQuestionDao.findExtendBath(qids);
            System.out.println("paperId:" + paperId + "---sequence:" + sequence);

            for (int i = 0; i < totalExtendList.size(); i++) {
                int sequenceOld = Math.round(totalExtendList.get(i).getSequence());
                System.out.println("第" + i + "个：" + sequenceOld);
                if (sequence == sequenceOld) {
                    throw new BizException(QuestionError.SEQUENCE_ALEADY_EXIT);
                }
            }
        }
    }


    /**
     * 真题试卷添加试题界面
     * 获取试题数据
     *
     * @param paperId
     * @return
     */
    public Object findPaperQuestionList(int paperId) throws BizException{
        Paper paper = paperDao.findById(paperId);

        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }


        List<Module> modules = paper.getModules();

        if (CollectionUtils.isEmpty(modules)) {
            return null;
        }


        List<PracticeModuleBean> moduleBeanList = new ArrayList<>();

        List<Integer> questions = paper.getQuestions();


        Map<Integer, Question> questionMap = Maps.newHashMap();
        Map<Integer, QuestionExtend> extendMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(questions)) {
            List<Question> bath = paperQuestionDao.findBath(questions);
            questionMap = bath.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));

            List<QuestionExtend> extendBath = paperQuestionDao.findExtendBath(questions);
            extendMap = extendBath.stream().collect(Collectors.toMap(i -> i.getQid(), i -> i));
        }

        int index = 0;
        for (Module module : modules) {
            if (module.getQcount() == 0) {
                PracticeModuleBean bean = PracticeModuleBean.builder()
                        .name(module.getName())
                        .id(module.getCategory())
                        .practiceSorts(Lists.newArrayList())
                        .build();
                moduleBeanList.add(bean);
                continue;
            }

            if (CollectionUtils.isEmpty(questions)) {
                continue;
            }


            //模块下所有试题id
            List<Integer> qids = questions.subList(index, index + module.getQcount());

            List<PracticeSort> sorts = new ArrayList<>();

            Set<Integer> parentIds = Sets.newHashSet();

            for (Integer qid : qids) {
                Question question = questionMap.get(qid);

                int parentId = findParentId(question);

                if (parentIds.contains(parentId)) {
                    continue;
                }


                PracticeSort practiceSort = new PracticeSort();
                if (parentId > 0) {
                    parentIds.add(parentId);

                    Question parent = paperQuestionDao.findQuestionById(parentId);
                    List<Integer> childrenIds = null;


                    if (parent instanceof CompositeSubjectiveQuestion) {
                        childrenIds = ((CompositeSubjectiveQuestion) parent).getQuestions();
                    } else {
                        childrenIds = ((CompositeQuestion) parent).getQuestions();
                    }

                    //过滤掉不在该试卷里的子试题
                    List<Integer> cids = childrenIds.stream().filter(i -> qids.contains(i)).collect(Collectors.toList());

                    List<QuestionExtend> childrenExtends = new ArrayList<>();

                    for (Integer cid : cids) {
                        childrenExtends.add(extendMap.get(cid));
                    }

                    //所有子试题的题序
                    List<String> newSeqs = childrenExtends.stream().map(i -> getSeqString(i.getSequence())).collect(Collectors.toList());

                    practiceSort.setName(StringUtils.join(newSeqs, '-'));
                    practiceSort.setQid(parentId);

                } else {
                    QuestionExtend extend = extendMap.get(qid);
                    practiceSort.setName(getSeqString(extend.getSequence()));
                    practiceSort.setQid(qid);
                }

                sorts.add(practiceSort);
            }


            PracticeModuleBean bean = PracticeModuleBean.builder()
                    .name(module.getName())
                    .id(module.getCategory())
                    .practiceSorts(sorts)
                    .build();
            moduleBeanList.add(bean);

            index += module.getQcount();
        }

        return moduleBeanList;
    }

    private String getSeqString(Float seq) {
        if (seq % 1 == 0) {
            return String.valueOf(seq.intValue());
        } else {
            return String.valueOf(seq);
        }
    }

    /**
     * 从试卷中删除题目
     *
     * @param paperId
     * @param questionId
     * @throws BizException
     */
    public void delQuestion(int paperId, int questionId) throws BizException {
        Paper paper = paperDao.findById(paperId);
        Question question = paperQuestionDao.findQuestionById(questionId);

        List<Integer> removeQids = Arrays.asList(questionId);
        if (question instanceof CompositeQuestion && ((CompositeQuestion) question).getQuestions()!=null) {
            removeQids.addAll(((CompositeQuestion) question).getQuestions());
//            removeQids = ((CompositeQuestion) question).getQuestions();
        } else if (question instanceof CompositeSubjectiveQuestion &&((CompositeSubjectiveQuestion) question).getQuestions()!=null) {
            removeQids.addAll(((CompositeSubjectiveQuestion) question).getQuestions());
        }

        //取得模块id
        int moduleId = getQuestionModuleId(paper, removeQids.get(0));

        if (moduleId < 0) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        List<Module> modules = paper.getModules();
        Module module = modules.stream().filter(i -> i.getCategory() == moduleId).findAny().orElse(null);
        //模块试题数-1
        module.setQcount(module.getQcount() - removeQids.size());

        List<Integer> questions = paper.getQuestions();

        final List<Integer> tmpQids = removeQids;

        //从试题列表中删除
        questions.removeIf(i -> tmpQids.contains(i));


        List<Integer> bigQuestions = paper.getBigQuestions();

        if (CollectionUtils.isNotEmpty(bigQuestions)) {
            bigQuestions.removeIf(qid -> qid == questionId);
            paper.setBigQuestions(bigQuestions);
        }

        paper.setModules(modules);
        paper.setQuestions(questions);
        paper.setQcount(questions.size());
        paperDao.update(paper);
    }

    public int getQuestionModuleId(Paper paper, int questionId) {
        List<ModuleBean> moduleBeanList = paperService.getModuleBeanList(paper);

        //试题id，模块id map
        Map<Integer, Integer> qidModuleIdMap = new HashMap<>();
        for (ModuleBean bean : moduleBeanList) {
            if (MapUtils.isEmpty(bean.getQuestions())) {
                continue;
            }

            Collection<Integer> qids = bean.getQuestions().values();
            qids.forEach(q -> qidModuleIdMap.put(q, bean.getId()));
        }
        return qidModuleIdMap.getOrDefault(questionId, -1);
    }


    /**
     * 校对删除试题
     * @param paperId
     * @param questionId
     * @throws BizException
     */
    public void deleteProofQuestion(int paperId, int questionId,String account,int id) throws BizException, IOException, BadElementException {
        Paper paper = paperDao.findById(paperId);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        if (paper.getStatus()==BackendPaperStatus.ONLINE
                ||paper.getStatus()==BackendPaperStatus.AUDIT_SUCCESS||paper.getStatus()==BackendPaperStatus.OFFLINE
                ||paper.getStatus()==BackendPaperStatus.ING) {//上线后删除
            Map<String, Object> map = Maps.newHashMap();
            Question question=questionService.findQuestinbyId(questionId);
            if(question==null){
                throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
            }
            //获取父集试题信息
            int parentId = findParentId(question);

            //取得模块id
            int moduleId = getQuestionModuleId(paper, questionId);
            map.put("questionId",parentId<=0?questionId:parentId);
            map.put("type",question.getType());
            map.put("module",moduleId);
            map.put("subject",question.getSubject());
            map.put("paperId",paperId);
            map.put("subSign",questionId);
            questionService.editApplyQuestion(JsonUtil.toJson(map),account,id);
        } else { //其它状态的试卷,从试卷中删除,如果是大题的子题,从大题中删除
            delQuestion(paperId, questionId);

            deleteChildQustion(questionId);
        }
    }

    /**
     * 从大题中删除一个子题
     * @param childId
     */
    private void deleteChildQustion(int childId) {
        Question question = paperQuestionDao.findQuestionById(childId);
        int parentId = findParentId(question);
        if (parentId > 0) {
            Question parent = paperQuestionDao.findQuestionById(parentId);
            if (parent instanceof CompositeQuestion) {
                CompositeQuestion compositeQuestion = (CompositeQuestion) parent;
                List<Integer> childIds = compositeQuestion.getQuestions();
                childIds.removeIf(qid -> qid == childId);

                compositeQuestion.setQuestions(childIds);

            } else if (parent instanceof CompositeSubjectiveQuestion) {
                CompositeSubjectiveQuestion parentObj = (CompositeSubjectiveQuestion) parent;
                List<Integer> childIds = parentObj.getQuestions();
                childIds.removeIf(qid -> qid == childId);

                parentObj.setQuestions(childIds);
            }

            //更新mongo
            questionDao.updateQuestion(parent);
        }
    }

    /**
     * 获得大题id
     * @param question
     * @return
     */
    public int findParentId(Question question) {
        int parentId = 0;
        if (question instanceof GenericQuestion) {
            parentId = ((GenericQuestion) question).getParent();
        } else if (question instanceof GenericSubjectiveQuestion){
            parentId = ((GenericSubjectiveQuestion) question).getParent();
        }
        return parentId;
    }


    /**
     * 更新试卷的大题id列表
     * @param paper
     */
    public void updateBigQustionsAndPaper(Paper paper) {
        logger.info("update paper big questions start.paper id={}",paper.getId());
        List<Integer> qids = paper.getQuestions();
        if(CollectionUtils.isNotEmpty(qids)){
            Map<Integer, Question> questionMap = paperQuestionDao.findBath(qids)
                    .stream()
                    .collect(Collectors.toMap(i -> i.getId(), i -> i));

            Set<Integer> bigIdSet = new LinkedHashSet<>();
            for (Integer qid : qids) {
                Question obj = questionMap.get(qid);
                int parentId = findParentId(obj);
                if (parentId > 0 && !bigIdSet.contains(parentId)) {
                    bigIdSet.add(parentId);
                } else if (parentId == 0) {
                    bigIdSet.add(obj.getId());
                }
            }
            paper.setBigQuestions(Lists.newArrayList(bigIdSet));
        }else {
            paper.setBigQuestions(Lists.newArrayList());
        }
        paperDao.update(paper);
    }
}
