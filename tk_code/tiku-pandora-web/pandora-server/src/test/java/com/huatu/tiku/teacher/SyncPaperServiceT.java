package com.huatu.tiku.teacher;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionSearchMapper;
import com.huatu.tiku.teacher.service.SyncPaperService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.util.file.ExcelManageUtil;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\7\9 0009.
 */
public class SyncPaperServiceT extends TikuBaseTest {

    @Autowired
    SyncPaperService syncPaperService;

    @Autowired
    ImportService importService;


    @Autowired
    CommonQuestionServiceV1 teacherQuestionService;

    @Autowired
    NewQuestionDao newQuestionDao;

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Autowired
    BaseQuestionSearchMapper baseQuestionSearchMapper;

    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtil.class);

    @Test
    public void test() {

        syncPaperService.syncPaper(814);
    }

    /**
     * update by lzj
     * ES中刷试题
     */
    @Test
    public void testDuplicate() {
        Example example = new Example(BaseQuestion.class);
        example.and().andNotEqualTo("subjectId", 1);
        List<BaseQuestion> questions = teacherQuestionService.selectByExample(example);
        //questions = questions.subList(0, 2000);
        logger.info("试题总量是:{}", questions.size());
        List<Long> questionList = questions.stream().map(baseQuestion -> baseQuestion.getId()).collect(Collectors.toList());
        com.google.common.base.Stopwatch stopwatch = com.google.common.base.Stopwatch.createStarted();
        int count = 0;
        for (Long id : questionList) {
            importService.sendQuestion2SearchForDuplicate(id);
            logger.info("发送ES 试题ID是：{}", id);
            count++;
        }
        logger.info("发送试题数量：{},总耗时：{}", count, String.valueOf(stopwatch.stop()));
    }


    @Test
    public void AssembleQuestionInfo() {
        List<Long> list = new ArrayList<>();
        list.add(30732L);
        //teacherQuestionService.assembleQuestionInfo(list);
    }


    @Test
    public void checkFormula() {
        List<Question> questionList = newQuestionDao.findByIdGtAndLimit(1, 40000);
        List<Integer> questionIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(questionList)) {
            for (Question question : questionList) {
                if (question instanceof GenericQuestion) {
                    String analysis = ((GenericQuestion) question).getAnalysis();
                    if (null == analysis) {
                        continue;
                    }
                    String regex = "latex.codecogs.com";
                    Pattern pattern = Pattern.compile(regex);
                    final Matcher matcher = pattern.matcher(analysis);
                    while (matcher.find()) {
                        logger.info("匹配到的结果是：{}", matcher.group(0));
                        questionIdList.add(question.getId());
                    }
                }
            }
            logger.info("公式有问题的是：{}", questionIdList);
        }
    }

    /**
     * 对去重试题添加知识点,分批导出
     */
    @Test
    public void getDuplicateQuestionId() {

        List<String> duplicateQuestionIds = readTxtFile();
        logger.info("读取文件的行数是：{}", duplicateQuestionIds.size());
        duplicateQuestionIds = duplicateQuestionIds.stream().filter(id -> id != null).collect(Collectors.toList());
        logger.info("去空之后行数是：{}", duplicateQuestionIds.size());
        if (CollectionUtils.isEmpty(duplicateQuestionIds)) {
            logger.info("查重数据为空");
            return;
        }
        List<HashMap<String, Object>> result = new ArrayList<>();

        for (String questionIDs : duplicateQuestionIds) {
            if (StringUtils.isNotEmpty(questionIDs)) {
                List<Integer> questionIds = Arrays.stream(questionIDs.split(","))
                        .map(Integer::valueOf)
                        .collect(Collectors.toList());
                List<Question> questions = newQuestionDao.findByIds(questionIds);
                HashMap questionInfo = judgeKnowledge(questions);
                logger.info("questionInfo结果是:{}", questionInfo);
                if (null != questionInfo) {
                    HashMap<String, Object> map = new HashMap<>();
                    String newString = questionIDs.replaceAll(",", "—");
                    map.put("questionIds", newString);
                    map.put("pointName", questionInfo.get("firstPointName"));
                    map.put("firstPointId", questionInfo.get("firstPointId"));
                    logger.info("map信息是:{}", map);
                    result.add(map);
                }
            }
        }
        Map<Object, List<HashMap<String, Object>>> pointName = result.stream().collect(Collectors.groupingBy(u -> u.get("firstPointId")));
        String jsonResult = JsonUtil.toJson(pointName);
        String path = "/Users/lizhenjuan/tool/firstDeal.txt";
        writeFileWithPath(jsonResult, path);
    }


    /**
     * 写入本地文件
     *
     * @param jsonResult
     * @param path
     */
    public void writeFileWithPath(String jsonResult, String path) {
        File file = new File(path);
        Writer out = null;
        try {
            //测试可以吗
            out = new FileWriter(file);
            out.write(jsonResult);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断三级知识点是否相同，如果不同，记录questionId;如果相同，返回一级知识点名称
     *
     * @param questions
     */
    public HashMap judgeKnowledge(List<Question> questions) {

        Set<Integer> pointList = new HashSet<>();
        String module = "";
        HashMap<String, String> resultMap = new HashMap<>();

        //TODO
        if (CollectionUtils.isNotEmpty(questions)) {
            Set<Map<Integer, Integer>> list = new HashSet<>();
            List<Integer> firstPoints = new ArrayList<>();
            String firstPointName = "";
            int pointSize = 0;
            for (Question question : questions) {
                if (question instanceof GenericQuestion) {
                    //获取试题知识点
                    List<Integer> points = ((GenericQuestion) question).getPoints();
                    List<String> pointNames = ((GenericQuestion) question).getPointsName();
                    pointSize = points.size();
                    Integer thirdPointId = points.get(pointSize - 1);
                    Integer firstPoint = points.get(0);
                    //不包含，添加到集合中
                    if (!pointList.contains(thirdPointId)) {
                        pointList.add(thirdPointId);
                        HashMap map = new HashMap();
                        map.put(question.getId(), pointNames.toString());
                        list.add(map);
                        module = (pointNames.get(0));
                    }
                    //处理相同一级知识点
                    if (CollectionUtils.isNotEmpty(firstPoints)) {
                        if (firstPoints.contains(firstPoint)) {
                            //一级知识点一样,添加知识点ID
                            resultMap.put("firstPointId", firstPoints.toString());
                        } else {
                            //知识点不一样,设置标识
                            resultMap.put("firstPointId", "0000");
                        }
                    } else {
                        firstPoints.add(firstPoint);
                    }
                }
            }
            if (pointList.size() > 1) {
                firstPointName = list.toString();
            } else {
                firstPointName = module;
            }
            resultMap.put("firstPointName", firstPointName);
            return resultMap;
        }
        return null;
    }


    public List<String> readTxtFile() {
        String path = "/Users/lizhenjuan/tool/重复试题id.txt";
        List<String> newList = new ArrayList<>();
        //打开文件
        File file = new File(path);
        if (file.isDirectory()) {
            logger.info("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        newList.add(line);
                    }
                    instream.close();
                }
            } catch (IOException e) {
                logger.info("TestFile", e.getMessage());
            }
        }
        logger.info("行数是:{}", newList.size());
        return newList;
    }

    /**
     * 去除重题试题太多的ID
     */
    @Test
    public void getInfo() {
        List<String> list = readTxtFile();
        List<String> newList = new ArrayList<>();
        for (String questionIDs : list) {
            if (StringUtils.isNotEmpty(questionIDs)) {
                List<Integer> questionIds = Arrays.stream(questionIDs.split(","))
                        .map(Integer::valueOf)
                        .collect(Collectors.toList());
                if (questionIds.size() <= 4) {
                    newList.add(questionIDs);
                }
            }
        }
        String path = "";
        writeFileWithPath(newList.toString(), path);
    }

    /**
     *
     * 处理各学科组散题（适用常识和言语）
     */
    @Test
    public void readExcelData() {
        String path = "/Users/lizhenjuan/Desktop/清理散题/整理完毕/" + "常识处理excel.xls";
        try {
            List<List> listResult = ExcelManageUtil.readExcel(path);
            logger.info("result数量是:{}", listResult.size() - 1);
            List<HashMap<String, Object>> deleteMap = new ArrayList<>();
            for (List list : listResult) {
                String oldAndNewId = list.get(0).toString();
                Object newIdObject = list.get(2);
                if (null != newIdObject) {
                    int length = newIdObject.toString().length();
                    Long newId = Long.valueOf(list.get(2).toString().substring(0, length - 2));
                    if (StringUtils.isNotEmpty(oldAndNewId)) {

                        List<Long> oldAndNewIdList = Arrays.stream(oldAndNewId.split("—"))
                                .map(id -> Long.valueOf(id))
                                .collect(Collectors.toList());
                        oldAndNewIdList.remove(newId);
                        if (CollectionUtils.isNotEmpty(oldAndNewIdList)) {
                            Long needDeleteOldId = Long.valueOf(oldAndNewIdList.get(0).toString());
                            HashMap map = new HashMap();
                            map.put("oldId", needDeleteOldId);
                            map.put("newId", newId);
                            deleteMap.add(map);
                        }
                    }
                }
            }
            //cleanOldQuestion(deleteMap);
            logger.info("待删除的ID是:{}", deleteMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除散题（针推资料分析,数量关系学科给的表结构）
     */
    @Test
    public void dealOtherSubject() {
        String path = "/Users/lizhenjuan/Desktop/清理散题/" + "整理完毕/尚希桥.xls";
        try {
            List<List> listResult = ExcelManageUtil.readExcel(path);
            logger.info("result数量是:{}", listResult.size() - 1);
            List<HashMap<String, Object>> deleteMap = new ArrayList<>();
            if (listResult.isEmpty()) {
                return;
            }
            for (List list : listResult) {
                Long firstId = Long.valueOf(list.get(0).toString().substring(0, list.get(0).toString().length() - 2));
                Long secondId = Long.valueOf(list.get(1).toString().substring(0, list.get(1).toString().length() - 2));
                List<Long> oldAndNewIdList = new ArrayList<>();
                oldAndNewIdList.add(firstId);
                oldAndNewIdList.add(secondId);
                Object newIdObject = Long.valueOf(list.get(2).toString().substring(0, list.get(2).toString().length() - 2));
                if (null != newIdObject) {
                    oldAndNewIdList.remove(newIdObject);
                    if (CollectionUtils.isNotEmpty(oldAndNewIdList)) {
                        HashMap map = new HashMap();
                        map.put("oldId", oldAndNewIdList.get(0).toString());
                        map.put("newId", newIdObject);
                        deleteMap.add(map);
                    }
                }
            }
            //cleanOldQuestion(deleteMap);
            logger.info("待删除的ID是:{}", deleteMap);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    /*  public void cleanOldQuestion(List<HashMap<String, Object>> maps) {
     */
    @Test
    public void cleanOldQuestion() {
        //存放复合题的ID集合
        List<Long> multIds = new ArrayList<>();
        //存放还绑定试卷的ID
        List<Long> bingPaperQuestionIds = new ArrayList<>();
        List<HashMap<String, Integer>> maps = new ArrayList<>();
        HashMap paramMap = new HashMap();
        paramMap.put("oldId", 95018);
        paramMap.put("newId", 40032301);
       /* HashMap paramMap1 = new HashMap();
        paramMap1.put("oldId", 218557);
        paramMap1.put("newId", 40019735);*/
       /* HashMap paramMap2 = new HashMap();
        paramMap2.put("oldId", 99439);
        paramMap2.put("newId", 40032752);*/
        maps.add(paramMap);
        /* maps.add(paramMap1);*/
        /*maps.add(paramMap2);*/

        for (HashMap map : maps) {
            Long oldId = Long.valueOf(map.get("oldId").toString());
            Long newId = Long.valueOf(map.get("newId").toString());
            //单题
            HashMap baseQuestion = baseQuestionSearchMapper.findBaseQuestion(oldId);
            logger.info("baseQuestion内容是:{}", baseQuestion);
            if (baseQuestion != null) {
                if (baseQuestion.get("multi_id").toString().equals(0) && baseQuestion.get("multi_flag").equals(1)) {
                    multIds.add(oldId);
                } else {
                    //单题处理
                    //如果试题跟试卷还有绑定,将这些试题收集
                    Example example = new Example(PaperQuestion.class);
                    example.and().andEqualTo("questionId", oldId);
                    List<PaperQuestion> paperQuestions = paperQuestionService.selectByExample(example);
                    if (CollectionUtils.isNotEmpty(paperQuestions)) {
                        bingPaperQuestionIds.add(oldId);
                    } else {
                        //删除旧的试题ID,同步到mongo
                        if (baseQuestion.get("status").toString().equals("-1")) {
                            reflectQuestionDao.insertRelation(oldId.intValue(), newId);
                        } else {
                            teacherQuestionService.deleteQuestionByFlag(oldId, 0L, false);
                            importService.sendQuestion2Mongo(oldId.intValue());
                            reflectQuestionDao.insertRelation(oldId.intValue(), newId);
                        }

                    }
                }
            }
        }
        //将符合题ID写入到文本中
        writeFileWithPath(JsonUtil.toJson(multIds), "/Users/lizhenjuan/tool/复合题.txt");
        writeFileWithPath(JsonUtil.toJson(bingPaperQuestionIds), "/Users/lizhenjuan/tool/绑定试卷的试题ID.txt");
        logger.info("结果ID是:{}", multIds);
    }




    /**
     * 新 旧ID对照
     *
     * @param oldId 旧试题ID
     * @param newId 新试题ID
     */
    private void deleteOldId(Long oldId, Long newId) {
        //解绑old,校验newId是否绑定，尚未绑定的要绑定新题跟试卷
        //确认是否全都解绑了,如果没有解绑,如果确认解绑,此步骤去掉
        Example example = new Example(PaperQuestion.class);
        example.and().andEqualTo("questionId", oldId);
        List<PaperQuestion> paperQuestions = paperQuestionService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(paperQuestions)) {
            for (PaperQuestion paperQuestion : paperQuestions) {
                PaperQuestion buildPaperQuestion = PaperQuestion.builder().paperId(paperQuestion.getPaperId())
                        .questionId(newId)
                        .moduleId(paperQuestion.getModuleId())
                        .paperType(paperQuestion.getPaperType())
                        .score(paperQuestion.getScore())
                        .sort(paperQuestion.getSort())
                        .build();
                paperQuestionService.save(buildPaperQuestion);
            }
        }
        //删除旧的试题ID
        teacherQuestionService.deleteByPrimaryKey(oldId);
        importService.sendQuestion2Mongo(oldId.intValue());
    }

}



