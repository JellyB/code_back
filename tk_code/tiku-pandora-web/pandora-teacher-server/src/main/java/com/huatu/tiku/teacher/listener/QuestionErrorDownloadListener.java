package com.huatu.tiku.teacher.listener;

import com.huatu.tiku.entity.download.QuestionErrorDownloadTask;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.mongo.AnswerCardDao;
import com.huatu.tiku.teacher.dao.mongo.OldQuestionDao;
import com.huatu.tiku.teacher.dao.question.QuestionErrorDownloadTaskMapper;
import com.huatu.tiku.teacher.service.download.v1.DownloadWriteServiceV1;
import com.huatu.tiku.teacher.service.download.v1.PdfWriteServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.PracticeCard;
import com.huatu.ztk.paper.bean.PracticePaper;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by huangqp on 2018\7\2 0002.
 */
@Slf4j
@Component
public class QuestionErrorDownloadListener extends BaseServiceImpl<QuestionErrorDownloadTask> {

    private static final Logger logger = LoggerFactory.getLogger(QuestionErrorDownloadListener.class);

    @Autowired
    OldQuestionDao oldQuestionDao;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    QuestionErrorDownloadTaskMapper questionErrorDownloadTaskMapper;

    @Autowired
    TeacherSubjectService subjectService;

    @Autowired
    PdfWriteServiceV1 pdfWriteServiceV1;

    @Autowired
    DownloadWriteServiceV1 downloadWriteServiceV1;

    public QuestionErrorDownloadListener() {
        super(QuestionErrorDownloadTask.class);
    }

    /**
     * 同步试题时，同时同步它的子题和相关复用数据的试题
     *
     * @param message
     */
    @RabbitHandler
    public void onMessage(QuestionErrorDownloadTask message) {
        try {
            log.info("message={}", JsonUtil.toJson(message));
            String questionIds = message.getQuestionIds();
            //初始化任务记录
            message.setBizStatus(0);
            logger.info("save 0 ,{}", JsonUtil.toJson(message));
            Integer save = save(message);
            List<Integer> ids = Arrays.stream(questionIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            List<Question> questions = oldQuestionDao.findByIds(ids);
            if (CollectionUtils.isEmpty(questions)) {
                return;
            }
            Long answerId = message.getAnswerId();
            PracticeCard practiceCard = null;
            //已完成做题的下载任务，不能被取代答题卡，否则可以取代
            if (null != answerId && answerId > 0) {
                AnswerCard answerCard = answerCardDao.findById(answerId);
                if (null != answerCard) {
                    if(answerCard.getStatus() == AnswerCardStatus.FINISH){
                        practiceCard = (PracticeCard) answerCard;       //不再重新生成答题卡
                    }else{
                        answerCardDao.delete(answerId,answerCard.getUserId());
                    }
                }
            }
            if(null == practiceCard){
                PracticePaper practicePaper = toPracticePaper(message, questions);
                practiceCard = create(practicePaper, 1, AnswerCardType.WRONG_PAPER_DOWNLOAD, message.getUserId(), 0);
            }
            message.setAnswerId(practiceCard.getId());
            message.setBizStatus(1);
            logger.info("save 1,{}", JsonUtil.toJson(message));
            save(message);
            String fileUrl = pdfWriteServiceV1.downloadByPracticePaper(practiceCard);
            System.out.println("fileUrl = " + FunFileUtils.TMP_PDF_SOURCE_FILEPATH + fileUrl);
            //cdn上传下载文件后缀
            File file = new File(FunFileUtils.TMP_PDF_SOURCE_FILEPATH + fileUrl);
            if (file.exists() && file.isFile()) {
                BigDecimal divide = new BigDecimal(file.length()).divide(new BigDecimal(1024));
                message.setSize(divide.setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "KB");
                message.setFileSize(file.length());
            }
            String tempName = UUID.randomUUID() + ".pdf";
            try {
                //cdn上传需要的文件名
                UploadFileUtil.getInstance().ftpUploadFile(file, tempName, FunFileUtils.PDF_FILE_SAVE_PATH);
                FunFileUtils.deleteFile(file);
            } finally {
                if (file.exists()) {
                    FunFileUtils.deleteFile(file);
                }
            }
            //cdn文件下载路径
            String url = FunFileUtils.PDF_FILE_SAVE_URL + tempName;
            System.out.println("var/cdn/url = " + url);
            message.setFileUrl(url);
            message.setBizStatus(2);

        } catch (Exception e) {
            log.error("question_error_download_task fail ,message = {}", JsonUtil.toJson(message));
            e.printStackTrace();
            message.setBizStatus(3);
        } finally {
            logger.info("save 2,{} ", JsonUtil.toJson(message));
            save(message);
        }

    }

    /**
     * 创建练习试卷
     *
     * @param practicePaper
     * @param terminal      终端
     * @param type          试卷类型
     * @param userId        用户id
     * @param remainingTime 试卷总计时间
     * @return
     * @throws BizException
     */
    public PracticeCard create(PracticePaper practicePaper, int terminal, int type, long userId, int remainingTime) throws BizException {
        PracticeCard practiceCard = PracticeCard.builder().build();
        long stime = System.currentTimeMillis();
        final int qcount = practicePaper.getQcount();//题量
        long id = 0;

        id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));

        if (id < 1) {//获取id失败
            throw new BizException(CommonErrors.SERVICE_INTERNAL_ERROR);
        }
        logger.info("get PracticeCard id time={},uid={}", System.currentTimeMillis() - stime, userId);

        long stime1 = System.currentTimeMillis();
        practiceCard.setId(id);
        practiceCard.setUserId(userId);
        practiceCard.setPaper(practicePaper);
        practiceCard.setDifficulty(practicePaper.getDifficulty());
        int[] intAnswers = new int[qcount];
        practiceCard.setAnswers(Arrays.stream(intAnswers).mapToObj(String::valueOf).toArray(String[]::new));
        practiceCard.setCreateTime(System.currentTimeMillis());
        practiceCard.setExpendTime(0);
        practiceCard.setName(practicePaper.getName());
        practiceCard.setRcount(0);//正确数量
        practiceCard.setWcount(0);//错误数量
        practiceCard.setUcount(qcount);//未做数量
        practiceCard.setCorrects(new int[qcount]);
        practiceCard.setStatus(AnswerCardStatus.CREATE);
        practiceCard.setCatgory(practiceCard.getSubject());
        practiceCard.setSubject(practicePaper.getSubject());
        practiceCard.setTerminal(terminal);
        practiceCard.setTimes(new int[qcount]);
        practiceCard.setType(type);
        practiceCard.setRemainingTime(remainingTime);//答题剩余时间
        practiceCard.setDoubts(new int[qcount]);
        answerCardDao.saveWithReflectQuestion(practiceCard);

        logger.info("insert time={},uid={}", System.currentTimeMillis() - stime1, userId);
        return practiceCard;
    }

    public PracticePaper toPracticePaper(QuestionErrorDownloadTask message, List<Question> questions) {
        final PracticePaper practicePaper = PracticePaper.builder().build();
        practicePaper.setQuestions(questions.stream().map(i -> i.getId()).collect(Collectors.toList()));
        practicePaper.setDifficulty(4);
        practicePaper.setQcount(questions.size());
        Map<Integer, Module> collect = questions.stream()
                .filter(i -> i instanceof GenericQuestion)
                .map(i -> (GenericQuestion) i)
                .collect(() -> new HashMap<Integer, Module>(),
                        (Map<Integer, Module> map, GenericQuestion question) -> {
                            Integer pointId = question.getPoints().get(0);
                            Module module = map.get(pointId);
                            if (null == module) {
                                map.put(pointId, Module.builder().category(pointId).qcount(1).name(question.getPointsName().get(0)).build());
                            } else {
                                module.setQcount(module.getQcount() + 1);
                            }
                        },
                        (a, b) -> {
                            for (Map.Entry<Integer, Module> entry : b.entrySet()) {
                                Integer key = entry.getKey();
                                Module value = entry.getValue();
                                if (a.containsKey(key)) {
                                    Module module = a.get(key);
                                    module.setQcount(module.getQcount() + value.getQcount());
                                } else {
                                    a.put(key, value);
                                }
                            }
                        });
        List<Module> modules = collect.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).collect(Collectors.toList());
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            String name = String.format("第%s部分 ", convert(i + 1)) + module.getName();
            module.setName(name);
        }
        practicePaper.setModules(modules);
        practicePaper.setSubject(message.getSubject());
        practicePaper.setName(message.getName());
        return practicePaper;
    }

    public static String convert(int num) {
        String[] nums = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String[] unit = {"", "十", "百", "千", "万", "十", "百", "千", "亿", "十", "百", "千", "万亿"};
        String str = String.valueOf(num);
        char[] charNum = str.toCharArray();
        String result = "";
        int length = str.length();
        for (int i = 0; i < length; i++) {
            int c = charNum[i] - '0';
            if (c != 0) {
                result += nums[c] + unit[length - i - 1];
            } else {
                result += nums[c];
            }
        }
        return result;
    }

}

