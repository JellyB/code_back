package com.huatu.tiku.teacher.service.download.v1;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.paper.SelectActivityReq;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.common.QuestionMetaService;
import com.huatu.tiku.teacher.service.download.ExcelHandleService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.util.file.ExcelManageUtil;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/3/19.
 */
@Service
@Slf4j
public class ExcelHandleServiceImpl implements ExcelHandleService {
    @Autowired
    private PaperEntityService paperEntityService;
    @Autowired
    private PaperActivityService paperActivityService;
    @Autowired
    private PaperQuestionService paperQuestionService;
    @Autowired
    private NewQuestionDao questionDao;
    @Autowired
    private QuestionMetaService questionMetaService;

    public static final String EXCEL_TAIL_NAME = ".xls";
    private static final List<QuestionElementEnum.ElementEnum> elements = Lists.newArrayList(
            QuestionElementEnum.ElementEnum.SORT,
            QuestionElementEnum.ElementEnum.TYPE,
            QuestionElementEnum.ElementEnum.QUESTION_ID,
            QuestionElementEnum.ElementEnum.SOURCE,
            QuestionElementEnum.ElementEnum.KNOWLEDGE,
            QuestionElementEnum.ElementEnum.ACCURACY,
            QuestionElementEnum.ElementEnum.TRAIN_TIME
    );

    private static final String[] titleRow = new String[]{"题干", "题型", "试题ID", "题源", "知识点", "正确率", "作答次数"};

    @Override
    public String downloads(List<Long> paperIds, PaperInfoEnum.TypeInfo typeInfo) throws BizException {
        List<String> fileNames = Lists.newArrayList();
        LinkedList<String> linkedList = Lists.newLinkedList();
        paperIds.parallelStream().forEach(paperId -> {
            String fileName = null;
            try {
                fileName = download(paperId, typeInfo);
            } catch (BizException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (StringUtils.isNotBlank(fileName)) {
                linkedList.add(fileName.replace(EXCEL_TAIL_NAME, ""));
            }
        });
        if (CollectionUtils.isEmpty(linkedList)) {
            return "";
        }
        fileNames.addAll(linkedList);
        return ZipUtil.zipFile(fileNames, EXCEL_TAIL_NAME, FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH);

    }

    /**
     * 下载单个试卷的试题属性
     *
     * @param paperId
     * @param typeInfo
     * @return
     */
    private String download(Long paperId, PaperInfoEnum.TypeInfo typeInfo) throws BizException, IOException {
        List<List> result = Lists.newArrayList();

        String paperName = getPaperName(paperId, typeInfo);
        List<PaperQuestion> paperQuestions = getPaperQuestions(paperId, typeInfo);
        if (CollectionUtils.isEmpty(paperQuestions)) {
            throw new BizException(ErrorResult.create(10000011, "试卷" + paperName + "下没有绑定试题"));
        }
        List<Integer> questionIds = paperQuestions.stream().map(PaperQuestion::getQuestionId).map(Long::intValue).collect(Collectors.toList());
        List<Question> questions = questionDao.findByIds(questionIds);
        paperQuestions.sort(Comparator.comparing(PaperQuestion::getSort));
        for (PaperQuestion paperQuestion : paperQuestions) {
            Long questionId = paperQuestion.getQuestionId();
            Optional<Question> first = questions.stream().filter(i -> i.getId() == questionId.intValue())
                    .findFirst();
            if (!first.isPresent()) {
                continue;
            }
            Question question = first.get();
            QuestionMeta questionMeta = null;
            if (question instanceof GenericQuestion) {
                questionMeta = questionMetaService.findMeta((GenericQuestion) question);
            }
            ArrayList<Object> questionInfo = Lists.newArrayList();
            for (QuestionElementEnum.ElementEnum element : elements) {
                switch (element) {
                    case SORT:
                        questionInfo.add(paperQuestion.getSort());
                        break;
                    case TYPE:
                        questionInfo.add(QuestionInfoEnum.QuestionTypeEnum.create(question.getType()).getName());
                        break;
                    case QUESTION_ID:
                        questionInfo.add(questionId.toString());
                        break;
                    case SOURCE:
                        questionInfo.add(paperName);
                        break;
                    case KNOWLEDGE:
                        List<KnowledgeInfo> pointList = question.getPointList();
                        if (CollectionUtils.isNotEmpty(pointList)) {
                            List<String> names = pointList.stream().map(i -> StringUtils.join(i.getPointsName(), "-")).collect(Collectors.toList());
                            questionInfo.add(names);
                        } else if (question instanceof GenericQuestion) {
                            questionInfo.add(StringUtils.join(((GenericQuestion) question).getPointsName(), "-"));
                        } else {
                            questionInfo.add("未知知识点");
                        }
                        break;
                    case ACCURACY:
                        if (null != questionMeta) {
                            int[] percents = questionMeta.getPercents();
                            questionInfo.add(percents[questionMeta.getRindex()] + "%");
                        } else {
                            questionInfo.add("无正确率");
                        }
                        break;
                    case TRAIN_TIME:
                        if (null != questionMeta) {
                            questionInfo.add(questionMeta.getCount());
                        } else {
                            questionInfo.add("无做题次数");
                        }
                        break;
                }
            }
            result.add(questionInfo);
        }
        ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH, paperName, "xls", result, titleRow);
        return paperName + EXCEL_TAIL_NAME;
    }

    private List<PaperQuestion> getPaperQuestions(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        switch (typeInfo) {
            case ENTITY:
                return paperQuestionService.findByPaperIdAndType(paperId, typeInfo);
            case SIMULATION:
                List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(paperId, typeInfo);
                if (CollectionUtils.isEmpty(paperQuestions)) {
                    PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
                    if (null != paperActivity && null != paperActivity.getPaperId() && paperActivity.getPaperId() > 0) {
                        return paperQuestionService.findByPaperIdAndType(paperActivity.getPaperId(), PaperInfoEnum.TypeInfo.ENTITY);
                    }
                }
                return paperQuestions;
            default:
        }
        return Lists.newArrayList();
    }


    public String getPaperName(Long paperId, PaperInfoEnum.TypeInfo typeInfo) throws BizException {
        switch (typeInfo) {
            case ENTITY:
                PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
                if (null != paperEntity) {
                    return paperEntity.getName();
                }
                break;
            case SIMULATION:
                PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
                if (null != paperActivity) {
                    return paperActivity.getName();
                }
                break;
            default:
        }
        throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
    }
}
