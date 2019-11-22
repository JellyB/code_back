package com.huatu.ztk.backend.paper.service;

import com.huatu.ztk.backend.paper.bean.ModuleBean;
import com.huatu.ztk.backend.paper.bean.SmartPaperBean;
import com.huatu.ztk.backend.paper.bean.SmartPaperModuleBean;
import com.huatu.ztk.backend.paper.bean.SmartPaperModulePoint;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PracticeDao;
import com.huatu.ztk.backend.paper.dao.PracticeSmartDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.DifficultGrade;
import com.huatu.ztk.question.common.QuestionStatus;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PracticeSmartService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeSmartService.class);

    @Autowired
    private PracticeSmartDao practiceSmartDao;

    @Autowired
    private PracticeDao practiceDao;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private PaperService paperService;

    private final static Map difficultMap = new LinkedHashMap();

    private final static Map publishMap = new LinkedHashMap();

    static {
        difficultMap.put(DifficultGrade.SO_EASY, "简单");
        difficultMap.put(DifficultGrade.EASY, "较易");
        difficultMap.put(DifficultGrade.GENERAL, "中等");
        difficultMap.put(DifficultGrade.DIFFICULT, "较难");
        difficultMap.put(DifficultGrade.SO_DIFFICULT, "困难");

        publishMap.put(QuestionStatus.AUDIT_SUCCESS, "审核已发布");
        publishMap.put(QuestionStatus.AUDIT_SUCCESS_NOT_ISSUED, "审核未发布");
    }

//    Cache<Integer, Map> pointQidsCache = CacheBuilder.newBuilder()
//            .expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * @param smartPaperBean
     * @throws BizException
     */
    public void makeSmartPaper(SmartPaperBean smartPaperBean) throws BizException {
        long stime = System.currentTimeMillis();
        check(smartPaperBean);

        EstimatePaper paper = practiceDao.findById(smartPaperBean.getPaperId());

        List<Integer> paperQids = new ArrayList<>();
        final int subject = paper.getCatgory();

        int[] difficultRatio = smartPaperBean.getDifficultRatio();
        int[] publishRatio = smartPaperBean.getPublishRatio();

        List<Module> paperModules = new ArrayList<>();

        Set<Integer> difficults = new HashSet<>();
        for (int i = 0; i < difficultRatio.length; i++) {
            difficults.add(getDifficultValue(i));
        }

        Set<Integer> pointIds = new HashSet<>();
        for (SmartPaperModuleBean moduleBean : smartPaperBean.getModules()) {
            for (SmartPaperModulePoint point : moduleBean.getPoints()) {
                pointIds.add(point.getId());
            }
        }

        long stime1 = System.currentTimeMillis();
        List<Question> allQuestions = practiceSmartDao.findAllPoints(subject, pointIds, difficults);
        logger.info("query total used time={}", System.currentTimeMillis() - stime1);

        int needQuestionCount = 0;

        for (SmartPaperModuleBean moduleBean : smartPaperBean.getModules()) {

            int moduleQcount = 0;

            for (SmartPaperModulePoint point : moduleBean.getPoints()) {
                int pointId = point.getId();
                int qcount = point.getQcount();

                needQuestionCount += qcount;

                //查询结果map, key:难度,value:{key:状态,value:qid}
                Map<Integer, Map<Integer, List<Integer>>> saveMap = new HashMap<>();

                for (Question question : allQuestions) {
                    if (question instanceof GenericQuestion) {
                        GenericQuestion genericQuestion = (GenericQuestion) question;

                        List<Integer> points = genericQuestion.getPoints();

                        if (points.contains(pointId)) {
                            Map<Integer, List<Integer>> statusQidMap = saveMap.get(question.getDifficult());
                            if (statusQidMap == null) {
                                statusQidMap = new HashMap();
                            }

                            List<Integer> qids = statusQidMap.get(question.getStatus());
                            if (qids == null) {
                                qids = new ArrayList<>();
                            }

                            qids.add(question.getId());

                            statusQidMap.put(question.getStatus(), qids);

                            saveMap.put(question.getDifficult(), statusQidMap);
                        }
                    }
                }

                //难度
                for (int i = 0; i < difficultRatio.length; i++) {

                    //该难度比例
                    Integer difficultCount = difficultRatio[i];

                    if (difficultCount == 0) {
                        continue;
                    }

                    //发布状态
                    for (int j = 0; j < publishRatio.length; j++) {

                        //发布比例
                        Integer publishCount = publishRatio[j];

                        if (publishCount == 0) {
                            continue;
                        }

                        //该难度和发布状态下，该知识点的题数
                        double tmpQcount = ((double)qcount) * difficultCount * publishCount / 10000;

                        //四舍五入
                        BigDecimal bigDecimal = new BigDecimal(tmpQcount).setScale(0, BigDecimal.ROUND_HALF_UP);

                        int pointQcount = bigDecimal.intValue();

                        int diffcultValue = getDifficultValue(i);
                        int publishValue = getPublishValue(j);

                        Map<Integer, List<Integer>> statusQidMap = saveMap.get(diffcultValue);
                        List<Integer> qids = statusQidMap != null ? statusQidMap.get(publishValue) : new ArrayList<>();
                        if (CollectionUtils.isEmpty(qids) || qids.size() < pointQcount) {
                            logger.info("question count not enough,pointId={},subject={},pointQcount={}",
                                    point.getId(), subject, pointQcount);
                            ErrorResult errorResult = ErrorResult.create(1001, "[" + point.getName() + "] [" +
                                    difficultMap.get(diffcultValue) + "难度] [" + publishMap.get(publishValue) + "状态] " +
                                    "题量不足\n实际题量为" + (CollectionUtils.isEmpty(qids)?0:qids.size()) + ",需要题量为" + pointQcount);
                            throw new BizException(errorResult);
                        }

                        Collections.shuffle(qids);

                        List<Integer> newQids = qids.subList(0, pointQcount);

                        paperQids.addAll(newQids);
                        moduleQcount += newQids.size();
                    }
                }


            }

            Module paperModule = Module.builder()
                    .category(moduleBean.getId())
                    .name(moduleBean.getName())
                    .qcount(moduleQcount)
                    .build();
            paperModules.add(paperModule);
        }


        paper.setModules(paperModules);
        paper.setQuestions(paperQids);
        paper.setQcount(paperQids.size());

        //组卷出来的题量大于所需的题量
        if (paperQids.size() > needQuestionCount) {
            //需要删除的，多余的qid
            List<Integer> removeQids = paperQids.subList(needQuestionCount, paperQids.size());

            List<ModuleBean> moduleBeanList = paperService.getModuleBeanList(paper);

            //题目id模块id map
            Map<Integer, Integer> qidModuleIdMap = new HashMap<>();
            for (ModuleBean moduleBean : moduleBeanList) {
                for (Integer qid : moduleBean.getQuestions().values()) {
                    qidModuleIdMap.put(qid, moduleBean.getId());
                }
            }

            //从模块中删除
            for (Integer removeQid : removeQids) {
                Integer removeModuleId = qidModuleIdMap.get(removeQid);

                for (Module paperModule : paperModules) {
                    if (paperModule.getCategory() == removeModuleId) {
                        //试题数-1
                        paperModule.setQcount(paperModule.getQcount() - 1);
                    }
                }
            }

            paperQids = paperQids.subList(0, needQuestionCount);
            paper.setModules(paperModules);
            paper.setQuestions(paperQids);
            paper.setQcount(paperQids.size());
        }

        logger.info("paper={}", JsonUtil.toJson(paper));

        logger.info("utime={}", System.currentTimeMillis() - stime);

        paperDao.update(paper);
    }


    private void check(SmartPaperBean smartPaperBean) throws BizException {
        //总题量
        int qcount = 0;
        for (SmartPaperModuleBean moduleBean : smartPaperBean.getModules()) {
            if (CollectionUtils.isEmpty(moduleBean.getPoints())) {
                throw new BizException(ErrorResult.create(1002, "无知识点"));
            }

            for (SmartPaperModulePoint modulePoint : moduleBean.getPoints()) {
                qcount += modulePoint.getQcount();
            }
        }

        if (qcount == 0) {
            throw new BizException(ErrorResult.create(1003, "总题量为0"));
        }

        int totalPublish = Arrays.stream(smartPaperBean.getPublishRatio()).sum();

        if (totalPublish != 100) {
            throw new BizException(ErrorResult.create(1004, "试题范围相加不等于100%"));
        }

        int totalDifficult = Arrays.stream(smartPaperBean.getDifficultRatio()).sum();

        if (totalDifficult != 100) {
            throw new BizException(ErrorResult.create(1005, "难度范围相加不等于100%"));
        }
    }

    private int getDifficultValue(int index) {
        return (index + 1) * 2;
    }

    private int getPublishValue(int index) {
        return index == 0 ? QuestionStatus.AUDIT_SUCCESS : QuestionStatus.AUDIT_SUCCESS_NOT_ISSUED;
    }
}
