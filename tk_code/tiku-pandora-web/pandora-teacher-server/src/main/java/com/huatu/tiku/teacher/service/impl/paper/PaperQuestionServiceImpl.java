package com.huatu.tiku.teacher.service.impl.paper;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.huatu.tiku.teacher.service.impl.paper.QuestionSortHandler.*;

/**
 * Created by lijun on 2018/8/3
 */
@Service
public class PaperQuestionServiceImpl extends BaseServiceImpl<PaperQuestion> implements PaperQuestionService {
    public PaperQuestionServiceImpl() {
        super(PaperQuestion.class);
    }

    @Autowired
    private CommonQuestionServiceV1 commonQuestionService;

    @Autowired
    private ImportService importService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private PaperEntityService paperEntityService;

    @Autowired
    private PaperActivityService paperActivityService;

    @Override
    public int savePaperQuestionWithSort(Long questionId, Long paperId, Integer moduleId, Integer sort, PaperInfoEnum.TypeInfo typeInfo) {
        return savePaperQuestionWithSort(questionId, paperId, moduleId, sort, 1d, null, typeInfo);
    }

    @Override
    public int savePaperQuestionWithSort(Long questionId, Long paperId, Integer moduleId, Integer sort, Double score, BiConsumer<Long, Integer> validatePaperInfo, PaperInfoEnum.TypeInfo typeInfo) {
        //通过paperid和paperType查询试题信息
        List<PaperQuestion> questionList = findByPaperIdAndType(paperId, typeInfo);
        //题号是否已经存在
        Optional<PaperQuestion> hasSort = questionList.stream()
                //避免修改时和自身的序号冲突
                .filter(paperQuestion -> paperQuestion.getSort().equals(sort) && !paperQuestion.getQuestionId().equals(questionId))
                .findAny();
        if (hasSort.isPresent()) {
            throwBizException("序号'" + sort + "'已经存在");
        }
        if (null != validatePaperInfo) {
            validatePaperInfo.accept(paperId, moduleId);
        }
        //查询试题是否已经存在
        Optional<PaperQuestion> hasQuestion = questionList.stream()
                .filter(paperQuestion -> paperQuestion.getQuestionId().equals(questionId))
                .findAny();
        if (hasQuestion.isPresent()) {
            //试题不为空，直接更新
            PaperQuestion paperQuestion = hasQuestion.get();
            //移除原始的sort 信息
            deleteSort(paperId, typeInfo, paperQuestion.getSort());
            //设置新属性
            paperQuestion.setSort(sort);
            paperQuestion.setScore(score);
            //添加sort信息
            addSort(paperId, typeInfo, sort);
            return updateByPrimaryKey(paperQuestion);
        } else {
            //试题不存在，插入试题
            PaperQuestion paperQuestion = PaperQuestion.builder()
                    .questionId(questionId)
                    .sort(sort)
                    .paperId(paperId)
                    .paperType(typeInfo.getCode())
                    .moduleId(moduleId)
                    .score(score)
                    .build();
            //添加sort信息
            addSort(paperId, typeInfo, sort);
            Integer save = save(paperQuestion);
            //如果当前是实体信息 - 需要更新试题信息
            if (PaperInfoEnum.TypeInfo.isNeedImportToMongoDB(typeInfo)) {
                importService.sendQuestion2Mongo(questionId.intValue());
            }
            return save;
        }
    }

    @Override
    public List<PaperQuestion> savePaperQuestion(List<PaperQuestion> list, BiConsumer<Long, Integer> validatePaperInfo, Boolean isContinue) {
        if (isContinue == false) {
            checkIsBindOtherPaper(list);
        }
        ArrayList<PaperQuestion> arrayList = Lists.newArrayList();
        list.stream()
                .filter(paperQuestion -> null != paperQuestion.getPaperId())
                .filter(paperQuestion -> null != paperQuestion.getPaperType())
                .filter(paperQuestion -> null != paperQuestion.getQuestionId())
                .filter(paperQuestion -> null != paperQuestion.getModuleId())
                .forEach(paperQuestion -> {
                    try {
                        savePaperQuestionWithSort(
                                paperQuestion.getQuestionId(),
                                paperQuestion.getPaperId(),
                                paperQuestion.getModuleId(),
                                paperQuestion.getSort(),
                                paperQuestion.getScore() == null ? 1d : paperQuestion.getScore(),
                                validatePaperInfo,
                                PaperInfoEnum.TypeInfo.create(paperQuestion.getPaperType())
                        );
                    } catch (Exception e) {
                        arrayList.add(paperQuestion);
                    }
                });
        List<PaperQuestion> collect = list.stream()
                .filter(paperQuestion ->
                        null == paperQuestion.getPaperId() ||
                                null == paperQuestion.getPaperType() ||
                                null == paperQuestion.getQuestionId() ||
                                null == paperQuestion.getModuleId()
                ).collect(Collectors.toList());
        arrayList.addAll(collect);
        return arrayList;
    }

    @Override
    public List<PaperQuestion> findByPaperIdAndType(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        WeekendSqls weekendSqls = buildByPaperIdAndType(paperId, typeInfo);
        Example example = Example.builder(PaperQuestion.class)
                .andWhere(weekendSqls)
                .orderBy(" sort ")
                .build();
        return selectByExample(example);
    }

    @Override
    public int deletePaperQuestionInfo(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Long questionId) {
        WeekendSqls<PaperQuestion> weekendSql = buildByPaperIdAndType(paperId, typeInfo);
        weekendSql.andEqualTo(PaperQuestion::getQuestionId, questionId);
        Example example = Example.builder(PaperQuestion.class)
                .andWhere(weekendSql)
                .build();
        //清除缓存的sort 信息
        List<PaperQuestion> paperQuestions = selectByExample(example);
        if (CollectionUtils.isNotEmpty(paperQuestions)) {
            deleteSort(paperId, typeInfo, paperQuestions.get(0).getSort());
        }
        int deleteByExample = deleteByExample(example);
        //信息同步
        if (deleteByExample > 0 && PaperInfoEnum.TypeInfo.isNeedImportToMongoDB(typeInfo)) {
            importService.sendQuestion2Mongo(questionId.intValue());
        }
        return deleteByExample;
    }

    @Override
    public int deletePaperQuestion(Long paperId, PaperInfoEnum.TypeInfo typeInfo, List<Long> questionIdList) {
        if (CollectionUtils.isEmpty(questionIdList)) {
            return 0;
        }
        //如果当前questionId有子节点，需要连带删除
        WeekendSqls<BaseQuestion> searchSql = WeekendSqls.custom();
        searchSql.andIn(BaseQuestion::getMultiId, questionIdList);
        Example searchExample = Example.builder(BaseQuestion.class)
                .andWhere(searchSql)
                .build();
        List<BaseQuestion> baseQuestionList = commonQuestionService.selectByExample(searchExample);
        List<Long> baseQuestionIdList = baseQuestionList.stream()
                .map(BaseQuestion::getId)
                .collect(Collectors.toList());
        //拼接出最终的ID集合
        questionIdList.addAll(baseQuestionIdList);
        //开始执行删除操作
        WeekendSqls<PaperQuestion> weekendSql = buildByPaperIdAndType(paperId, typeInfo);
        weekendSql.andIn(PaperQuestion::getQuestionId, questionIdList);
        Example example = Example.builder(PaperQuestion.class)
                .andWhere(weekendSql)
                .build();
        //清除缓存的sort 信息
        List<Integer> sortList = selectByExample(example)
                .stream()
                .map(PaperQuestion::getSort)
                .collect(Collectors.toList());
        deleteSort(paperId, typeInfo, sortList);
        int deleteByExample = deleteByExample(example);
        //信息同步
        if (deleteByExample > 0 && PaperInfoEnum.TypeInfo.isNeedImportToMongoDB(typeInfo)) {
            importService.sendQuestion2Mongo(questionIdList.stream().map(Long::intValue).collect(Collectors.toList()));
        }
        return deleteByExample;
    }

    @Override
    public int deletePaperQuestionInfo(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        WeekendSqls weekendSqls = buildByPaperIdAndType(paperId, typeInfo);
        Example example = Example.builder(PaperQuestion.class)
                .andWhere(weekendSqls)
                .build();
        //判断是否需要同步处理
        List<Integer> questionIdList = Lists.newArrayList();
        if (PaperInfoEnum.TypeInfo.isNeedImportToMongoDB(typeInfo)) {
            List<PaperQuestion> paperQuestionList = selectByExample(example);
            questionIdList.addAll(paperQuestionList.stream()
                    .map(PaperQuestion::getId)
                    .map(Long::intValue)
                    .collect(Collectors.toList()));
        }
        int delete = deleteByExample(example);
        deleteAllSort(paperId, typeInfo);
        if (delete > 0 && CollectionUtils.isNotEmpty(questionIdList) && PaperInfoEnum.TypeInfo.isNeedImportToMongoDB(typeInfo)) {
            importService.sendQuestion2Mongo(questionIdList);
        }
        return delete;
    }

    @Override
    public boolean validateSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer sort) {
        Supplier<Set<Integer>> supplier = () -> {
            WeekendSqls weekendSqls = buildByPaperIdAndType(paperId, typeInfo);
            Example example = Example.builder(PaperQuestion.class)
                    .andWhere(weekendSqls)
                    .build();
            List<PaperQuestion> paperQuestionList = selectByExample(example);
            Set<Integer> set = paperQuestionList.stream()
                    .map(PaperQuestion::getSort)
                    .collect(Collectors.toSet());
            return set;
        };
        return QuestionSortHandler.validateSort(paperId, typeInfo, sort, supplier);
    }

    @Override
    public int updateQuestionScoreByModuleId(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer moduleId, Double score) {
        WeekendSqls<PaperQuestion> weekendSql = buildByPaperIdAndType(paperId, typeInfo);
        weekendSql.andEqualTo(PaperQuestion::getModuleId, moduleId);
        Example example = Example.builder(PaperQuestion.class)
                .andWhere(weekendSql)
                .build();

        PaperQuestion update = PaperQuestion.builder()
                .score(score)
                .build();
        //TODO mongo暂时没有地方存储试卷下试题的特定分数，先不考虑同步问题
        return updateByExampleSelective(update, example);
    }

    @Override
    public List<PaperQuestion> findByQuestionId(long questionId) {
        Example example = new Example(PaperQuestion.class);
        example.and().andEqualTo("questionId", questionId);
        return selectByExample(example);
    }

    /**
     * 构建查询条件
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @return 查询条件
     */
    private static WeekendSqls<PaperQuestion> buildByPaperIdAndType(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        WeekendSqls<PaperQuestion> weekendSql = WeekendSqls.custom();
        weekendSql.andEqualTo(PaperQuestion::getPaperId, paperId)
                .andEqualTo(PaperQuestion::getPaperType, typeInfo.getCode());

        return weekendSql;
    }

    /**
     * 活动类型（小模考 && 阶段测试）,提示用户一个试题不能被多个试卷绑定
     *
     * @param list
     */
    private void checkIsBindOtherPaper(List<PaperQuestion> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //非活动卷不做处理
        if (list.get(0).getPaperType() != PaperInfoEnum.TypeInfo.SIMULATION.getKey()) {
            return;
        }
        Long paperId = list.get(0).getPaperId();
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
        if (null == paperActivity) {
            throwBizException("活动信息不存在");
        }
        //小模考与阶段测试做处理
        if (paperActivity.getType() == ActivityTypeAndStatus.ActivityTypeEnum.SMALL_ESTIMATE.getKey()
                || paperActivity.getType() == ActivityTypeAndStatus.ActivityTypeEnum.FORMATIVE_TEST_ESTIMATE.getKey()) {

            List<Long> collect = list.stream().map(PaperQuestion::getQuestionId)
                    .collect(Collectors.toList());
            //试题是否绑定多个试卷
            Example example = new Example(PaperQuestion.class);
            example.and().andIn("questionId", collect);
            example.and().andEqualTo("paperType", PaperInfoEnum.TypeInfo.SIMULATION.getKey());
            List<PaperQuestion> paperQuestionList = paperQuestionService.selectByExample(example);

            if (CollectionUtils.isNotEmpty(paperQuestionList)) {
                Map<Long, List<PaperQuestion>> mapList = paperQuestionList.stream()
                        .collect(Collectors.groupingBy(PaperQuestion::getQuestionId));
                List<String> strResult = new ArrayList<>();
                for (Map.Entry map : mapList.entrySet()) {
                    Object questionId = map.getKey();
                    List<PaperQuestion> questionList = (List<PaperQuestion>) map.getValue();
                    List<Long> paperIds = questionList.stream().map(PaperQuestion::getPaperId).collect(Collectors.toList());

                    //限制同一个考试类型不能重复绑定
                    Example paperExample = new Example(PaperActivity.class);
                    paperExample.and().andIn("id", paperIds);
                    paperExample.and().andEqualTo("type", paperActivity.getType());
                    List<PaperActivity> paperActivityList = paperActivityService.selectByExample(paperExample);
                    //如果此试题已经绑定了其他同类试卷，做此强制提示
                    if (CollectionUtils.isNotEmpty(paperActivityList)) {
                        String paperNames = paperActivityList.stream().map(PaperActivity::getName).collect(Collectors.joining(","));
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("试题ID:")
                                .append(questionId)
                                .append("已经绑定了:")
                                .append(paperNames)
                                .append(",请慎重使用");
                        strResult.add(stringBuffer.toString());
                    }
                    if (CollectionUtils.isNotEmpty(strResult)) {
                        String exceptionStr = strResult.stream().collect(Collectors.joining(","));
                        throwBizException(exceptionStr);
                    }
                }
            }
        }
    }


    /**
     * 单题算分,获取试卷总分
     *
     * @param paperId
     * @param typeInfo
     * @return
     */
    public Double getPaperQuestionScore(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        //返回增加计算单题总分字段
        Double questionTotalScore = 0D;
        List<PaperQuestion> paperQuestionList = paperQuestionService.findByPaperIdAndType(paperId, typeInfo);
        if (CollectionUtils.isNotEmpty(paperQuestionList)) {
            List<Double> scores = paperQuestionList.stream()
                    .map(PaperQuestion::getScore)
                    .collect(Collectors.toList());
            questionTotalScore = PaperQuestionScoreHandler.getQuestionTotalScore(scores);
        }
        return questionTotalScore;
    }

    @Override
    public void checkScore(double score) {
        if (score < 0 || score > 999.99) {
            throwBizException("单题分数不超过999分");
        }
    }
}
