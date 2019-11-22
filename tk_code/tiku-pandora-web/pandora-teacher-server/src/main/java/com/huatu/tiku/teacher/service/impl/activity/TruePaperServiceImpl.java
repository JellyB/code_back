package com.huatu.tiku.teacher.service.impl.activity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.activity.TruePaperService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.util.file.ExcelManageUtil;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.util.QuestionPointUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.huatu.tiku.util.file.UploadFileUtil.getInstance;

/**
 * Created by huangqingpeng on 2019/1/28.
 */
@Slf4j
@Service
public class TruePaperServiceImpl implements TruePaperService {


    @Autowired
    PaperActivityService paperActivityService;
    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    PaperQuestionService paperQuestionService;
    @Autowired
    NewQuestionDao questionDao;

    @Override
    public String handlerKnowledgeExcelById(long activityId) {
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(activityId);
        String name = paperActivity.getName();
        if (name.indexOf(File.separator) > -1) {
            name = name.replaceAll(File.separator, "、");
        }
        if (name.indexOf("/") > -1) {
            name = name.replace("/", "、");
        }
        String excelPath = FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH + name + "（知识点统计）.xls";
        try {
            List<Integer> questions = findQuestionByActivityId(activityId);
            List<QuestionPointTree> questionPointTrees = questionPointSummary(questions);
            List<List> result = Lists.newArrayList();
            List<QuestionPointTree> tempList = Lists.newArrayList();
            handlerPointTree2List(questionPointTrees, tempList, result);
            String[] temp = new String[]{
                    "一级知识点", "数量", "二级知识点", "数量", "三级知识点", "数量"
            };

            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH, name + "（知识点统计）", "xls", result, temp);
            String tempName = name + ".xls";
            getInstance().ftpUploadFile(new File(excelPath), tempName, FunFileUtils.EXCEL_FILE_SAVE_PATH);
            return FunFileUtils.EXCEL_FILE_SAVE_URL + tempName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return excelPath;
    }

    @Override
    public List<String> handlerKnowledgeExcelsByRange(int year, int subject, int activityType) {
        ArrayList<Integer> subjectList = Lists.newArrayList(subject);
        subjectList.add(subject);
        List<HashMap<String, Object>> activityList = paperActivityService.getActivityList(activityType, -1, year, "", "", subjectList, "", "", -1);
        System.out.println("activityList = " + activityList);
        List<String> result = activityList.stream().map(i -> MapUtils.getLong(i, "id")).map(this::handlerKnowledgeExcelById).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(result)) {
            return Lists.newArrayList();
        }
        return result;
    }

    private void handlerPointTree2List(List<QuestionPointTree> questionPointTrees, List<QuestionPointTree> tempList, List<List> result) {
        for (QuestionPointTree questionPointTree : questionPointTrees) {
            List<QuestionPointTree> children = questionPointTree.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                List<QuestionPointTree> temps = Lists.newArrayList();
                temps.addAll(tempList);
                temps.add(questionPointTree);
                handlerPointTree2List(children, temps, result);
            } else {
                ArrayList<Object> list = Lists.newArrayList();
                for (QuestionPointTree pointTree : tempList) {
                    String name = pointTree.getName();
                    int qnum = pointTree.getQnum();
                    list.add(name);
                    list.add(qnum + "");
                }
                list.add(questionPointTree.getName());
                list.add(questionPointTree.getQnum());
                result.add(list);
            }
        }
    }

    private List<Integer> findQuestionByActivityId(long activityId) {
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(activityId);
        List<PaperQuestion> paperQuestionList = Lists.newArrayList();
        if (null != paperActivity) {
            if (null == paperActivity.getPaperId() || paperActivity.getPaperId().intValue() <= 0) {
                paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(activityId, PaperInfoEnum.TypeInfo.SIMULATION));
            } else {
                Long paperEntityId = paperActivity.getPaperId();
                PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperEntityId);
                if (null != paperEntity) {
                    paperQuestionList.addAll(paperQuestionService.findByPaperIdAndType(paperEntityId, PaperInfoEnum.TypeInfo.ENTITY));
                }
            }
        }
        if (CollectionUtils.isEmpty(paperQuestionList)) {
            return Lists.newArrayList();
        }
        return paperQuestionList.stream().map(PaperQuestion::getQuestionId).map(Long::intValue).collect(Collectors.toList());

    }

    public List<QuestionPointTree> questionPointSummary(List<Integer> questions) {
        if (null == questions) {
            return new ArrayList<>();
        }
        final List<Question> bath = questionDao.findByIds(questions);
        Map<Integer, QuestionPointTree> data = new HashMap<>();
        Map<Integer, QuestionPoint> temp = Maps.newHashMap();
        if (bath != null) {
            for (int i = 0; i < bath.size(); i++) {
                Question question = bath.get(i);
                if (question == null || !(question instanceof GenericQuestion)) {//理论上是不存在的
                    continue;
                }

                GenericQuestion genericQuestion = (GenericQuestion) question;
                if (null == genericQuestion || CollectionUtils.isEmpty(genericQuestion.getPoints())) {
                    log.info("试题缺少知识点>>>>>,试题ID = {}", genericQuestion.getId());
                    continue;
                }
                final List<Integer> points = genericQuestion.getPoints();
                final List<String> pointsName = genericQuestion.getPointsName();
                putPoints2Map(points, pointsName, temp);
                for (Integer point : points) {
                    QuestionPointTree questionPointTree = data.get(point);
                    if (questionPointTree == null) {
                        final QuestionPoint questionPoint = temp.get(point);
                        if (questionPoint == null) {//知识点没有查询到
                            continue;//不进行处理
                        }
                        questionPointTree = QuestionPointUtil.conver2Tree(questionPoint);
                        questionPointTree.setQnum(0);//初始化题数，防止conver2Tree里面设置qnum
                        //写入map
                        data.put(questionPointTree.getId(), questionPointTree);
                    }

                    if (questionPointTree == null) {//找不到对应的知识点，则处理,理论上不存在此情况
                        log.error("can`t find parent knowledge point. pointId={}", point);
                        continue;
                    }
                    questionPointTree.setQnum(questionPointTree.getQnum() + 1);
                }
            }
        }
        return QuestionPointUtil.wapper2Trees(data.values());
    }

    private void putPoints2Map(List<Integer> points, List<String> pointsName, Map<Integer, QuestionPoint> temp) {
        for (int i = points.size() - 1; i >= 0; i--) {
            Integer id = points.get(i);
            if (null != temp.get(id)) {
                continue;
            }
            QuestionPoint questionPoint = new QuestionPoint();
            questionPoint.setId(id);
            questionPoint.setName(pointsName.get(i));
            if (i - 1 >= 0) {
                Integer parent = points.get(i - 1);
                questionPoint.setParent(parent);
            } else {
                questionPoint.setParent(0);
            }
            questionPoint.setLevel(i);
            questionPoint.setStatus(1);
            temp.put(id, questionPoint);
        }
    }
}
