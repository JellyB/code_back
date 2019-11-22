package com.huatu.ztk.backend.paper.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.backend.arena.dao.ArenaDao;
import com.huatu.ztk.backend.paper.bean.*;
import com.huatu.ztk.backend.paper.dao.AnswerCardDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.util.PhoneAddressUtils;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.LookParseStatus;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCorrectType;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.common.RegexConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.huatu.ztk.question.common.QuestionType.*;

/**
 * Created by linkang on 3/6/17.
 */

@Service
public class EstimateService {

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private ArenaDao arenaDao;

    @Autowired
    private PaperQuestionDao paperQuestionDao;

    @Autowired
    private PaperService paperService;

    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Autowired
    private CreatePaperWordService createPaperWordService;

    @Autowired
    private CreatePaperPdfService createPaperPdfService;

    @Autowired
    private PracticeService practiceService;

    //错题统计的缓存
    Cache<Integer, List<ErrorCountBean>> ERROR_COUNT_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(40)
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .build();

    /**
     * 结果的缓存
     */
    Cache<Integer, Map> RESULT_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(12, TimeUnit.HOURS)
                    .build();


    public static final String ESTIMATE_RESULT_FILE_HEAD = "用户ID,用户名,昵称,注册邮箱,手机号,分数,开始时间,答题用时";


    public static final String USER_PHONE_FILE_HEAD = "用户名,手机号,省份,城市";

    //CSV文件基本保存路径
    private static final String ESTIMATE_RESULT_FILE_BASE_BATH = "/var/www/cdn/csv/";


    //url前缀
    private static final String ESTIMATE_RESULT_FILE_BASE_URL = "http://tiku.huatu.com/cdn/csv/";

    private static final Map<Integer, String> questionTypeMap = new HashMap();

    static {
        questionTypeMap.put(SINGLE_CHOICE, "单选题");
        questionTypeMap.put(SINGLE_OR_MULTIPLE_CHOICE, "不定项选择");
        questionTypeMap.put(MULTIPLE_CHOICE, "多选题");
        questionTypeMap.put(WRONG_RIGHT, "对错题");
        questionTypeMap.put(COMPOSITED, "复合题");
        questionTypeMap.put(SINGLE_SUBJECTIVE, "单一主观题");
        questionTypeMap.put(MULTI_SUBJECTIVE, "复合主观题");
    }


    /**
     * 真题卷添加到估分列表
     *
     * @param id
     */
    public void addEstimate(int id) throws BizException {
        Paper old = paperDao.findById(id);
        if (old == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        if (old.getStatus() != BackendPaperStatus.ONLINE && old.getStatus() != BackendPaperStatus.AUDIT_SUCCESS) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        Paper estimate = paperDao.findByNameAndType(old.getName(), PaperType.ESTIMATE_PAPER);

        //如果已经存在
        if (estimate != null) {
            throw new BizException(PaperErrors.EXISTS_ESTIMATE_PAPER);
        }


        EstimatePaper paper = new EstimatePaper();

        paper.setName(old.getName());
        paper.setYear(old.getYear());
        paper.setArea(old.getArea());
        paper.setTime(old.getTime());
        paper.setScore(old.getScore());
        paper.setPassScore(old.getPassScore());
        paper.setQcount(old.getQcount());
        paper.setDifficulty(old.getDifficulty());
        paper.setType(PaperType.ESTIMATE_PAPER);
        paper.setCatgory(old.getCatgory());
        paper.setModules(old.getModules());
        paper.setStatus(old.getScore());
        paper.setScore(old.getScore());
        paper.setScore(old.getScore());
        paper.setStatus(old.getStatus());
        paper.setQuestions(old.getQuestions());
        paper.setCreatedBy(old.getCreatedBy());
        paper.setCreateTime(new Date());

        paper.setDescrp("");
        paper.setUrl("");
        paper.setLookParseTime(LookParseStatus.IMMEDIATELY);


        setEstimatePaperOnline(paper);
        paper.setId(paperService.generatePaperId());
        paperDao.createPaper(paper);
    }


    /**
     * 设置估分试卷上线
     *
     * @param paper
     */
    public void setEstimatePaperOnline(EstimatePaper paper) {

        long current = System.currentTimeMillis();

        //结束时间/下线时间设置为一年后
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
        long endTime = calendar.getTimeInMillis();

        paper.setOnlineTime(current);
        paper.setStartTime(current);
        paper.setOfflineTime(endTime);
        paper.setEndTime(endTime);

        paper.setStatus(BackendPaperStatus.ONLINE);
    }


    /**
     * 估分列表
     *
     * @param catgory
     * @param area
     * @param year
     * @param name
     * @param status
     * @return
     */
    public List<PaperBean> findEstimateList(String catgory, String area, int year, String name, int status, int uid) throws BizException {

        if (StringUtils.isEmpty(catgory)) {
            return null;
        }

        List<Integer> catgorys = Arrays.stream(catgory.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());

        List<Integer> areas = area.equals("0") ? Lists.newArrayList()
                : Arrays.stream(area.split(","))
                .map(Integer::new)
                .collect(Collectors.toList());


        //去掉已删除,审核未通过，未审核，待审核的试卷
        List<Paper> paperList = paperDao.list(catgorys, areas, year, name,
                Arrays.asList(PaperType.ESTIMATE_PAPER),
                Arrays.asList(BackendPaperStatus.DELETED, BackendPaperStatus.AUDIT_REJECT, BackendPaperStatus.CREATED, BackendPaperStatus.AUDIT_PENDING),
                null, paperService.findCreator(catgorys, uid));


        List<EstimatePaper> estimatePapers = paperList.stream().map(i -> (EstimatePaper) i).collect(Collectors.toList());

        for (EstimatePaper paper : estimatePapers) {
            long startTime = paper.getStartTime();
            long endTime = paper.getEndTime();
            long offlineTime = paper.getOfflineTime();
            long currentTime = System.currentTimeMillis();

            if (paper.getStatus() == BackendPaperStatus.ONLINE && (currentTime > startTime && currentTime < endTime)) {
                paper.setStatus(BackendPaperStatus.ING);
            } else {
                paper.setStatus(BackendPaperStatus.OFFLINE);
            }
        }

        if (status > 0) {
            estimatePapers.removeIf(i -> i.getStatus() != status);
        }

        List<PaperBean> result = estimatePapers.stream().map(i -> paperService.shortCastPaper(i)).collect(Collectors.toList());

        return result;
    }


    /**
     * 考试结果
     *
     * @param paperId
     * @return
     */
    public Map findResult(int paperId) throws BizException {
//        Map cache = RESULT_CACHE.getIfPresent(paperId);
//
//        if (cache != null) {
//            return cache;
//        }


        Paper paper = paperDao.findById(paperId);

        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        Set<String> resultSet = findResultSet(paperId);

        if (CollectionUtils.isEmpty(resultSet)) {
            throw new BizException(ErrorResult.create(1000106, "用户尚未答题记录"));
        }

        List<UserPaperResult> userPaperResults = new ArrayList<>();
        List<Double> scores = new ArrayList<>();

        //参加考试的人按省分类  map的key是省 value是UserPaperResult
        List<Map<String, UserPaperResult>> userByProvince = Lists.newLinkedList();

        for (String pidStr : resultSet) {
            long practiceId = Long.parseLong(pidStr);
            AnswerCard answerCard = answerCardDao.findById(practiceId);

            long userId = answerCard.getUserId();
            UserDto userDto = arenaDao.findUserById(userId);


            if (userDto == null) {
                continue;
            }


            int expendTime = answerCard.getExpendTime();
            int sec = expendTime % 60;

            int min = expendTime / 60 % 60;

            int hour = expendTime / 60 / 60 % 60;

            String utime = "";

            if (hour > 0) {
                utime += hour + "小时";
            }

            if (min > 0) {
                utime += min + "分";
            }

            if (sec > 0) {
                utime += sec + "秒";
            }

            //  getAreaByPhone(userDto.getMobile());

            Map<String, String> areaMap = PhoneAddressUtils.getAreaMap(userDto.getMobile());
            String province = areaMap.getOrDefault("province", " ");
            String city = areaMap.getOrDefault("city", " ");

            UserPaperResult uResult = UserPaperResult
                    .builder()
                    .uid(userDto.getId())
                    .username(userDto.getName())
                    .phone(StringUtils.trimToEmpty(userDto.getMobile()))
                    .email(StringUtils.trimToEmpty(userDto.getEmail()))
                    .utime(utime)
                    .createTime(new Date(answerCard.getCreateTime()))
                    .score(answerCard.getScore())
                    .rcount(answerCard.getRcount())
                    .province(province)
                    .city(city)
                    .nick(userDto.getNick())
                    .cardId(answerCard.getId() + "")
                    .build();

            userPaperResults.add(uResult);
            scores.add(answerCard.getScore());
        }

        //  double max = DoubleStream.of(scoreArray).max().getAsDouble();
        double max = Collections.max(scores);
        //  double min = DoubleStream.of(scoreArray).min().getAsDouble();
        double min = Collections.min(scores);
        //获取最高分所在省份  根据id去获取？ 待定
        List<Long> userIds = Lists.newLinkedList();

        for (UserPaperResult userPaperResult : userPaperResults) {
            if (userPaperResult.getScore() == max) {
                userIds.add(userPaperResult.getUid());
            }
        }


        double[] scoreArray = scores.stream().mapToDouble(Double::new).toArray();

        double avg = DoubleStream.of(scoreArray).average().getAsDouble();
        avg = new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();


        PaperResult paperResult = PaperResult
                .builder()
                .avgScore(avg)
                .maxScore(max)
                .minScore(min)
                .eightyHighCount((int) scores.stream().filter(i -> i >= 80).count())
                .sixtyToEightyCount((int) scores.stream().filter(i -> i >= 60 && i < 80).count())
                .sixtyLowCount((int) scores.stream().filter(i -> i < 60).count())
                .zeroCount((int) scores.stream().filter(i -> i == 0).count())
                .totalCount(scores.size())
                .qcount(paper.getQcount())
                .build();


        Map map = new HashMap();
        map.put("userPaperResults", userPaperResults);
        map.put("paperResult", paperResult);


        //RESULT_CACHE.put(paperId, map);

        return map;
    }


    public Set<String> findResultSet(int paperId) {
        final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);

        Set<String> resultSet = zSetOperations.reverseRange(paperPracticeIdSore, 0, -1);

        return resultSet;
    }


    public String getAreaByPhone(String phone) {
        Map<String, String> area = PhoneAddressUtils.getAreaMap(phone);
        return area.getOrDefault("province", " ") +
                area.getOrDefault("city", " ");
    }


    /**
     * 获取用户答题信息
     *
     * @param id
     * @return
     * @throws BizException
     */
    public Object findCard(long id) throws BizException {
        StandardCard card = (StandardCard) answerCardDao.findById(id);

        if (card == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        String[] answers = card.getAnswers();
        int[] times = card.getTimes();
        int[] corrects = card.getCorrects();
        int[] questions = card.getPaper().getQuestions().stream().mapToInt(Integer::new).toArray();

        List<Answer> resultList = new ArrayList<>();

        for (int i = 0; i < answers.length; i++) {
            Answer answer = new Answer();
            answer.setAnswer(answers[i]);
            answer.setCorrect(corrects[i]);
            answer.setQuestionId(questions[i]);
            answer.setTime(times[i]);

            resultList.add(answer);
        }

        return resultList;
    }


    /**
     * 错题统计map
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    public Object findErrorCount(int paperId) throws BizException {
        List<ErrorCountBean> countBeans = ERROR_COUNT_CACHE.getIfPresent(paperId);
        if (CollectionUtils.isNotEmpty(countBeans)) {
            return countBeans;
        }

        Paper paper = paperDao.findById(paperId);

        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        Set<String> resultSet = findResultSet(paperId);

        if (CollectionUtils.isEmpty(resultSet)) {
            throw new BizException(PaperErrors.NO_USER_RESULT);
        }


        List<Integer> questions = paper.getQuestions();

        int[] errorArray = new int[questions.size()];

        for (String pidStr : resultSet) {
            long practiceId = Long.parseLong(pidStr);
            AnswerCard answerCard = answerCardDao.findById(practiceId);

            int[] corrects = answerCard.getCorrects();

            for (int i = 0; i < corrects.length; i++) {
                if (corrects[i] == QuestionCorrectType.WRONG) {
                    errorArray[i]++;
                }
            }
        }

        List<ModuleBean> moduleBeanList = paperService.getModuleBeanList(paper);

        //试题id，模块名称map
        Map<Integer, String> map = new HashMap<>();
        for (ModuleBean bean : moduleBeanList) {
            Collection<Integer> qids = bean.getQuestions().values();
            qids.forEach(q -> map.put(q, bean.getName()));
        }

        List<ErrorCountBean> result = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Question question = paperQuestionDao.findQuestionById(questions.get(i));

            if (question instanceof GenericQuestion) {

                GenericQuestion genericQuestion = (GenericQuestion) question;

                String newStem = practiceService.repairStem(genericQuestion.getStem(), 30, "...");

                genericQuestion.setStem(newStem);
            }


            ErrorCountBean countBean = ErrorCountBean.builder()
                    .question(question)
                    .errorCount(errorArray[i])
                    .typeName(questionTypeMap.get(question.getType()))
                    .moduleName(map.get(question.getId()))
                    .build();
            result.add(countBean);
        }

        result.sort((a, b) -> b.getErrorCount() - a.getErrorCount());

        result.removeIf(i -> i.getErrorCount() == 0);


        //放入缓存
        ERROR_COUNT_CACHE.put(paperId, result);

        return result;
    }

    public String findEstimateResultFileUrl(int paperId) throws BizException {
        try {

            Paper paper = paperDao.findById(paperId);

            Map result = findResult(paperId);

            List<UserPaperResult> userPaperResults = (List<UserPaperResult>) result.get("userPaperResults");

            File file = new File("/tmp/" + paper.getId() + "_"
                    + DateFormatUtils.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".csv");

            Writer fileWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file), "GBK"));

            fileWriter.write(ESTIMATE_RESULT_FILE_HEAD + "\n");

            for (UserPaperResult userPaperResult : userPaperResults) {
                long uid = userPaperResult.getUid();
                String phone = userPaperResult.getPhone();
                String email = userPaperResult.getEmail();
                String score = userPaperResult.getScore() + "";
                String username = userPaperResult.getUsername();
                String nick = userPaperResult.getNick();
                String createTime = DateFormatUtils.format(userPaperResult.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                String utime = userPaperResult.getUtime();
                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s\n", uid, username, nick, email, phone, score, createTime, utime);

                fileWriter.write(line);
            }

            fileWriter.flush();
            fileWriter.close();


            uploadFileUtil.ftpUpload(file, file.getName(), ESTIMATE_RESULT_FILE_BASE_BATH);


            String url = ESTIMATE_RESULT_FILE_BASE_URL + file.getName();
            return url;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (BizException be) {
            throw be;
        }
        return null;
    }


    /**
     * 用户手机号搜集
     *
     * @param id
     * @param area
     * @return
     * @throws BizException
     */
    public String findUserPhoneCollect(int id, String area) throws BizException {

        Integer areaId = Integer.valueOf(area);
        String fullAreaName = AreaConstants.getFullAreaNmae(areaId);

        if (StringUtils.isBlank(area)) {
            return null;
        }

        Map result = findResult(id);

        if (MapUtils.isEmpty(result)) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        try {
            List<UserPaperResult> userPaperResults = (List<UserPaperResult>) result.get("userPaperResults");

            File file = new File("/tmp/" + id + "_user_phone_" + area + "_"
                    + DateFormatUtils.format(new Date(), "yyyy_MM_dd_HH_mm_ss") + ".csv");


            Writer fileWriter = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file), "GBK"));

            fileWriter.write(USER_PHONE_FILE_HEAD + "\n");

            for (UserPaperResult userPaperResult : userPaperResults) {

                String province = userPaperResult.getProvince();
                String city = userPaperResult.getCity();
                String userAreaName = StringUtils.trimToEmpty(province + city);

                //-9表示全国，打印所有答题用户
                if (userPaperResult.getPhone().matches(RegexConfig.MOBILE_PHONE_REGEX)
                        && (userAreaName.contains(fullAreaName) || areaId == -9)) {
                    String username = userPaperResult.getUsername();
                    String phone = userPaperResult.getPhone();
                    String line = String.format("%s,%s,%s,%s\n", username, phone, province, city);

                    fileWriter.write(line);
                }
            }

            fileWriter.flush();
            fileWriter.close();

            uploadFileUtil.ftpUpload(file, file.getName(), ESTIMATE_RESULT_FILE_BASE_BATH);

            String url = ESTIMATE_RESULT_FILE_BASE_URL + file.getName();

            return url;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 更新估分卷的状态
     *
     * @param paperId
     * @return
     */
    public String updateEstimateStatus(int paperId) {
        EstimatePaper paper = (EstimatePaper) paperDao.findById(paperId);

        String msg = "";

        long startTime = paper.getStartTime();
        long endTime = paper.getEndTime();
        long currentTime = System.currentTimeMillis();

        if (paper.getStatus() == BackendPaperStatus.ONLINE && (currentTime > startTime && currentTime < endTime)) {
            paper.setStatus(BackendPaperStatus.ING);
        } else {
            paper.setStatus(BackendPaperStatus.OFFLINE);
        }

        if (paper.getStatus() == BackendPaperStatus.OFFLINE) {
            //已下线的更新为上线
            setEstimatePaperOnline(paper);

            paperDao.update(paper);

            msg = "已上线";
        } else if (paper.getStatus() == BackendPaperStatus.ING) {
            //上线的更新为下线
            paperDao.updatePaperStatus(paperId, BackendPaperStatus.OFFLINE);

            msg = "已下线";
        }

        return msg;
    }

    /**
     * 试题导出
     *
     * @param id   试卷id
     * @param type 1:'导出试题PDF',
     *             2:'导出试题和答案PDF',
     *             3:'导出试题WORD',
     *             4:'导出试题和答案WORD'
     * @return
     * @throws BizException
     */
    public String exportQuestion(int id, int type) throws Exception {
        Paper paper = paperDao.findById(id);
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        String fileUrl = "";
        if (type == 1) {
            fileUrl = createPaperPdfService.downFileUrl(paper, ExportType.PAPER_PDF_TYPE_STEM);
        } else if (type == 2) {
            fileUrl = createPaperPdfService.downFileUrl(paper, ExportType.PAPER_PDF_TYPE_All);
        } else if (type == 3) {
            fileUrl = createPaperWordService.getDownFileUrl(paper, ExportType.PAPER_WORD_TYPE_SIDE_STEM);
        } else if (type == 4) {
            fileUrl = createPaperWordService.getDownFileUrl(paper, ExportType.PAPER_WORD_TYPE_ALL);
        }
        return fileUrl;
    }
}
