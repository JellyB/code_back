package com.huatu.tiku.teacher.service.impl.paper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.baseEnum.BaseStatusEnum;
import com.huatu.tiku.constants.teacher.EssayConstant;
import com.huatu.tiku.constants.teacher.TeacherErrors;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.entity.teacher.*;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.EnumUtil;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.request.paper.ActivityPaperReq;
import com.huatu.tiku.request.paper.InsertActivityPaperReq;
import com.huatu.tiku.request.paper.SelectActivityReq;
import com.huatu.tiku.request.paper.UpdateActivityPaperReq;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.paper.PaperActivityMapper;
import com.huatu.tiku.teacher.enums.ActivityLookParseType;
import com.huatu.tiku.teacher.enums.ActivityTagEnum;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.PaperActivitySubjectService;
import com.huatu.tiku.teacher.service.SyncPaperService;
import com.huatu.tiku.teacher.service.WeChatService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.tiku.teacher.service.paper.PaperAreaService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperSearchService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.teacher.util.personality.PersonalityAreaUtil;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PaperType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * Created by huangqp on 2018\6\23 0023.
 */
@Slf4j
@Service
public class PaperActivityServiceImpl extends BaseServiceImpl<PaperActivity> implements PaperActivityService {
    public PaperActivityServiceImpl() {
        super(PaperActivity.class);
    }

    public static final String essayUrl = "ns.huatu.com";


    @Autowired
    PaperActivitySubjectService paperActivitySubjectService;

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    PaperAreaService paperAreaService;

    @Autowired
    PaperSearchService paperSearchService;

    @Autowired
    PaperActivityMapper paperActivityMapper;

    @Autowired
    ImportService importService;

    @Autowired
    SyncPaperService syncPaperService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    TeacherSubjectService teacherSubjectService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Autowired
    private WeChatService weChatService;


    @Override
    public List<PaperActivity> selectByIds(List<Long> activityIds) {
        if (CollectionUtils.isEmpty(activityIds)) {
            return Lists.newArrayList();
        }
        Example example = new Example(PaperActivity.class);
        example.and().andIn("id", activityIds);
        List<PaperActivity> paperActivities = selectByExample(example);
        if (CollectionUtils.isEmpty(paperActivities)) {
            return Lists.newArrayList();
        }
        return paperActivities;
    }

    /**
     * @param insertActivityPaperReq
     * @param paperId
     */
    @Override
    @Transactional
    public Map insertPaper(InsertActivityPaperReq insertActivityPaperReq, Long paperId) {


        Integer paperType = insertActivityPaperReq.getType();
        PaperActivity paperActivity = new PaperActivity();
        paperActivity.setType(paperType);

        //通用属性（真题演练所有）
        checkCommonTruePaper(paperActivity, insertActivityPaperReq);
        if (paperType > ActivityTypeAndStatus.ActivityTypeEnum.TRUE_PAPER.getKey()) {
            //除真题演练外，活动时间不能为空
            checkCommonEstimatePaper(paperActivity, insertActivityPaperReq);
        }
        if (paperType == ActivityTypeAndStatus.ActivityTypeEnum.MATCH.getKey()) {
            checkMatchPaper(paperActivity, insertActivityPaperReq);
        }
        //如果paperId参数大于0,则表示试卷id使用确认值
        if (paperId > 0) {
            //设置paperID
            PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
            if (paperEntity == null) {
                throwBizException("试卷信息不存在");
            }
            paperActivity.setPaperId(paperId);
        }
        org.springframework.beans.BeanUtils.copyProperties(insertActivityPaperReq, paperActivity);
        //小模考需拼接课程名称
        if (paperActivity.getType() == PaperType.SMALL_ESTIMATE) {
            paperActivity.setCourseName(getCourseName(paperActivity.getName()));
        }

        /**
         *单题算分,paperActivity存放总分=试卷所有试题总分
         */
        Double paperQuestionScore = 0D;
        if (insertActivityPaperReq.getScoreFlag() == BaseStatusEnum.NORMAL.getCode()) {
            if (paperId != 0) {
                paperQuestionScore = paperQuestionService.getPaperQuestionScore(paperId, PaperInfoEnum.TypeInfo.ENTITY);
            }
        } else {
            paperQuestionScore = insertActivityPaperReq.getTotalScore();
        }
        paperActivity.setTotalScore(paperQuestionScore);
        //插入
        save(paperActivity);
        //试卷科目校验并插入
        checkPaperSubject(paperActivity, insertActivityPaperReq);
        //试卷地区校验并插入
        checkPaperArea(paperActivity, insertActivityPaperReq);

        Map mapData = Maps.newHashMap();
        mapData.put("activityId", paperActivity.getId());
        mapData.put("paperId", paperActivity.getPaperId());
        return mapData;
    }


    /**
     * 添加活动-绑定活动卷跟实体卷
     *
     * @param paperActivity
     */
    public void savePaper(PaperActivity paperActivity) {
        //题目题型的校验
        //paperId不能为空
        if (paperActivity.getPaperId() != null) {
            throwBizException("绑定的试卷信息不存在");
        }
        //如果当前是修改 - 同步修改数据
        if (paperActivity.getId() > 0) {
            importService.importPaper(paperActivity.getPaperId());
        }
        save(paperActivity);
    }


    /**
     * 删除试卷及试卷相关属性相关
     *
     * @param paperId
     */
    @Override
    @Transactional
    public void deletePaper(Long paperId) {
        PaperActivity paperActivity = selectByPrimaryKey(paperId);
        if (paperActivity == null) {
            throwBizException("已不存在的活动");
        } else {
            if (paperActivity.getBizStatus() != ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_NO_PUBLISH.getKey()) {
                throwBizException("活动已发布，不可删除！");
            }
            //活动卷表
            deleteByPrimaryKey(paperId);
            //活动卷和地区关联表
            paperAreaService.deletePaperAreaInfo(paperId, PaperInfoEnum.TypeInfo.SIMULATION);
            //活动卷和科目关联表
            paperActivitySubjectService.deleteByPaperId(paperId);
            //活动卷和试题关联表
            paperQuestionService.deletePaperQuestionInfo(paperId, PaperInfoEnum.TypeInfo.SIMULATION);
            //同步mongo的match和paper状态
            importService.importPaper(paperId);
        }
    }


    @Override
    public void updatePaper(UpdateActivityPaperReq updateActivityPaperReq) {

        Integer paperType = updateActivityPaperReq.getType();
        PaperActivity paperActivity = new PaperActivity();
        //通用属性（真题演练所有）
        checkCommonTruePaper(paperActivity, updateActivityPaperReq);
        if (paperType > PaperType.TRUE_PAPER) {
            //模考相关通用数据（万人模考，专项模考，精准估分）
            checkCommonEstimatePaper(paperActivity, updateActivityPaperReq);
        }
        if (paperType == PaperType.MATCH) {
            checkMatchPaper(paperActivity, updateActivityPaperReq);
        }
        paperActivity.setId(updateActivityPaperReq.getId());
        BeanUtils.copyProperties(updateActivityPaperReq, paperActivity);
        /**
         * 原有逻辑：创建活动时才会将实体卷中的年份赋值给活动卷，编辑时不做处理
         * 现有逻辑：编辑时也要更新这个字段（暂时还是使用实体卷的年份）
         */
        Long paperId = paperActivity.getPaperId();
        if (null != paperId && paperId > 0) {
            PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
            if (null != paperEntity) {
                paperActivity.setYear(paperEntity.getYear());
            }
        }
        //小模考需拼接课程名称
        if (paperActivity.getType() == PaperType.SMALL_ESTIMATE) {
            paperActivity.setCourseName(getCourseName(paperActivity.getName()));
        }
        /**
         *单题算分,paperActivity存放总分=试卷所有试题总分
         */
        Double paperQuestionScore = 0D;
        //单题算分
        if (updateActivityPaperReq.getScoreFlag() == BaseStatusEnum.NORMAL.getCode()) {
            if (null != paperId && paperId != 0) {
                //活动卷绑定实体卷
                paperQuestionScore = paperQuestionService.getPaperQuestionScore(paperId, PaperInfoEnum.TypeInfo.ENTITY);
            } else {
                //直接活动卷创建
                paperQuestionScore = paperQuestionService.getPaperQuestionScore(updateActivityPaperReq.getId(), PaperInfoEnum.TypeInfo.ENTITY);
            }
        } else {
            //按照比率算分
            paperQuestionScore = updateActivityPaperReq.getTotalScore();
        }

        if (updateActivityPaperReq.getScoreFlag() != BaseStatusEnum.NORMAL.getCode()) {
            paperQuestionScore = updateActivityPaperReq.getTotalScore();
            paperActivity.setTotalScore(paperQuestionScore);
        }
        log.info("更新分数是:{}", paperQuestionScore);

        //保存试卷(纠正模考大赛修改为估分活动导致的时间问题)
        if (paperActivity.getType() == PaperType.ESTIMATE_PAPER || paperActivity.getType() == PaperType.CUSTOM_PAPER) {
            paperActivity.setStartTime(updateActivityPaperReq.getOnlineTime());
            paperActivity.setEndTime(updateActivityPaperReq.getOfflineTime());
        }
        this.save(paperActivity);
        //清空小程序二维码
        clearQRcode(paperActivity.getId(),paperActivity.getType());
        //试卷科目校验并插入
        checkPaperSubject(paperActivity, updateActivityPaperReq);
        //试卷地区校验并插入
        checkPaperArea(paperActivity, updateActivityPaperReq);
        //同步mongo 数据
        importService.importPaper(paperActivity.getId());

    }

    /**
     * 清空小程序推广二维码
     * @param id
     */
	private int clearQRcode(Long id, int type) {
		if (type == PaperType.ESTIMATE_PAPER) {
			PaperActivity selectByPrimaryKey = paperActivityMapper.selectByPrimaryKey(id);
			selectByPrimaryKey.setQrcode(null);
			return paperActivityMapper.updateByPrimaryKey(selectByPrimaryKey);
		}
		return 0;
	}

	@Override
    public SelectActivityReq findById(Long id) {
        SelectActivityReq selectActivityReq = new SelectActivityReq();
        PaperActivity paperActivity = paperActivityMapper.selectByPrimaryKey(id);
        if (paperActivity != null) {
            if (paperActivity.getPaperId() != null && paperActivity.getPaperId() > 0) {
                //实体卷
                PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperActivity.getPaperId());
                if (paperEntity == null) {
                    throwBizException("该试卷信息不存在!");
                }
                selectActivityReq.setPaperEntityName(paperEntity.getName());
                selectActivityReq.setEntityScore(paperEntity.getTotalScore());
            }


            BeanUtils.copyProperties(paperActivity, selectActivityReq);
            //单题算分计算分数
            selectActivityReq.setTotalScore(paperActivity.getTotalScore());
            selectActivityReq.setQuestionTotalScore(getActivitySelectScore(selectActivityReq));

            selectActivityReq.setLimitTime(paperActivity.getLimitTime());

            //地区
            List<Long> areaIds = getAreaIds(id, PaperInfoEnum.TypeInfo.SIMULATION);
            selectActivityReq.setAreaIds(areaIds);
            //获取活动类型名称
            selectActivityReq.setTypeName(EnumUtil.valueOf(paperActivity.getType(), ActivityTypeAndStatus.ActivityTypeEnum.class));
            //获取答案解析名称
            if (null != paperActivity.getLookParseTime()) {
                selectActivityReq.setLooKParseName(EnumUtil.valueOf(paperActivity.getLookParseTime(), ActivityLookParseType.class));
            }
            if (paperActivity.getTag() != null && paperActivity.getType().equals(ActivityTypeAndStatus.ActivityTypeEnum.MATCH.getKey())) {
                selectActivityReq.setTagName(ActivityTagEnum.TagEnum.create(selectActivityReq.getTag()).getTagName());
            } else {
                selectActivityReq.setTagName("未知");
            }
            //科目
            List<Long> subjectIds = paperActivitySubjectService.findSubjectByPaperId(id);
            selectActivityReq.setSubjectIds(subjectIds);
            return selectActivityReq;
        }
        return null;
    }


    private List<Long> getAreaIds(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        List<PaperArea> paperAreaList = paperAreaService.list(paperId, typeInfo);
        return paperAreaList.stream()
                .map(PaperArea::getAreaId)
                .collect(Collectors.toList());
    }

    @Override
    public void unBindWithQuestion(Long paperId, Long questionId) {
        if (isFromEntity(paperId)) {
            throwBizException("试卷试题绑定关系来自实体卷，不能在此被修改");
        }
        //删除 试卷 - 试题绑定信息
        int deleteByExample = paperQuestionService.deletePaperQuestionInfo(paperId, PaperInfoEnum.TypeInfo.SIMULATION, questionId);
        //同步 试卷 信息
        if (deleteByExample > 0) {
            importService.importPaper(paperId);
        }
    }

    @Override
    public void unBindWithSort(Long paperId, Integer sort) {
        if (isFromEntity(paperId)) {
            throwBizException("试卷试题绑定关系来自实体卷，不能在此被修改");
        }
        delHandlerBinding(paperId, sort);
    }

    /**
     * 删除绑定关系 -- 试卷+题序
     */
    private void delHandlerBinding(Long paperId, Integer sort) {
        Example example = new Example(PaperQuestion.class);
        example.and()
                .andEqualTo("paperId", paperId)
                .andEqualTo("paperType", PaperInfoEnum.TypeInfo.SIMULATION.getCode())
                .andEqualTo("sort", sort);
        int deleteByExample = paperQuestionService.deleteByExample(example);
        //同步 试卷信息
        if (deleteByExample > 0) {
            importService.importPaper(paperId);
        }
    }

    @Override
    public void updatePaperQuestion(Long paperId, List<Long> questionIds) {
        if (isFromEntity(paperId)) {
            throwBizException("试卷试题绑定关系来自实体卷，不能在此被修改");
        }
        if (paperId == null || paperId <= 0 || CollectionUtils.isEmpty(questionIds)) {
            throw new BizException(TeacherErrors.ILLEGAL_PARAM);
        }
        List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(paperId, PaperInfoEnum.TypeInfo.SIMULATION);
        if (CollectionUtils.isNotEmpty(paperQuestions)) {
            int deletePaperQuestionInfo = paperQuestionService.deletePaperQuestionInfo(paperId, PaperInfoEnum.TypeInfo.SIMULATION);
            if (deletePaperQuestionInfo > 0) {
                importService.importPaper(paperId);
            }
        }
        //TODO 批量添加试题试卷关系表
//        paperQuestionService.insertPaperQuestionInfo(paperId,questionIds, PaperInfoEnum.TypeInfo.SIMULATION);
    }

    @Override
    public void saveBindWithQuestion(Long paperId, Long questionId, Integer sort, String moduleName) {
        if (isFromEntity(paperId)) {
            throwBizException("试卷试题绑定关系来自实体卷，不能在此被修改");
        }
        int savePaperQuestionWithSort = paperQuestionService.savePaperQuestionWithSort(questionId, paperId, getModuleIdByName(paperId, moduleName), sort, PaperInfoEnum.TypeInfo.SIMULATION);
        if (savePaperQuestionWithSort > 0) {
            importService.importPaper(paperId);
        }
    }

    /**
     * 查询活动卷的模块信息
     *
     * @param paperId
     * @param moduleName
     * @return
     */
    private int getModuleIdByName(Long paperId, String moduleName) {
        return PaperModuleHandler.getModuleIdByName(paperId, PaperInfoEnum.TypeInfo.ENTITY, moduleName, () -> getModuleInfo(paperId));
    }

    private String getModuleInfo(Long paperId) {
        PaperActivity paperActivity = selectByPrimaryKey(paperId);
        if (null == paperActivity) {
            throwBizException("试卷信息不存在");
        }
        //试卷的信息处理
        return null == paperActivity ? StringUtils.EMPTY : paperActivity.getModule();
    }

    @Override
    @Transactional
    public void bindEntityPaperId(Long id, Long paperId) {
        Example example = new Example(PaperActivity.class);
        example.and().andEqualTo("id", id);
        PaperActivity paperActivity = PaperActivity.builder().paperId(paperId).build();
        updateByExampleSelective(paperActivity, example);
    }

    /**
     * 统计活动的总体考试数据
     * TODO 测试数据（难度统计问题）
     *
     * @param paperId
     * @return
     */
    @Override
    public Map countExamInfo(Long paperId) {
        Map mapData = Maps.newHashMap();
        if (Math.random() < 0.4D) {
            return mapData;
        }
        mapData.put("count", (int) (Math.random() * 10000));
        mapData.put("average", (Math.random() * 100));
        mapData.put("difficult", 6D);
        return mapData;
    }

    /**
     * 根据实体卷id查询关联的活动卷id
     *
     * @param paperId
     * @return
     */
    @Override
    public List<Long> findByPaperId(Long paperId) {
        Example example = new Example(PaperActivity.class);
        example.and().andEqualTo("paperId", paperId);
        List<PaperActivity> paperActivities = selectByExample(example);
        if (CollectionUtils.isEmpty(paperActivities)) {
            return Lists.newArrayList();
        }
        return paperActivities.stream().map(i -> i.getId()).collect(Collectors.toList());
    }

    /**
     * 判断试卷的试题关联信息来自实体卷还是自身的
     *
     * @param paperId
     * @return true 来自实体卷  false 来自活动卷
     */
    private boolean isFromEntity(Long paperId) {
        PaperActivity paperActivity = selectByPrimaryKey(paperId);
        if (paperActivity.getPaperId() != null && paperActivity.getPaperId() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 关联申论模考大赛并修改申论时间 TODO lzj添加/更新活动卷，涉及到申论
     *
     * @param paperActivity
     */
    private void connectEssayMatch(PaperActivity paperActivity) {
        if (paperActivity.getEssayEndTime() != null) {
            log.info("试卷已经关联成功申论模考大赛");
            return;
        }
        Map<String, String> mapData = null;
        // try {
        //1已关联  2已上线  4解除绑定  5下线
        String url = essayUrl + "/e/api/v1/mock/status?id=" + paperActivity.getEssayId() + "&practiceId=" + paperActivity.getPaperId() + "&type=" + EssayConstant.EssayPracticeType.CONNECTED.getType();
        RestTemplate restTemplate = new RestTemplate();
        ResponseMsg<EssayMockExam> responseMsgResponseEntity = restTemplate.postForObject(url, null, ResponseMsg.class);
        if (responseMsgResponseEntity.getCode() != 1000000) {
            throwBizException(responseMsgResponseEntity.getMsg());
        }
        if (responseMsgResponseEntity != null) {
            mapData = (Map) responseMsgResponseEntity.getData();
            if (mapData != null && mapData.get("endTime") != null && mapData.get("startTime") != null) {
                String endTimeEssay = mapData.get("endTime");
                String startTimeEssay = mapData.get("startTime");
                Date dateStart = null;
                Date dateEnd = null;
                try {
                    dateStart = DateUtils.parseDateStrictly(startTimeEssay, "yy-MM-dd HH:mm:ss");
                    dateEnd = DateUtils.parseDateStrictly(endTimeEssay, "yy-MM-dd HH:mm:ss");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                paperActivity.setEssayStartTime(new Timestamp(dateStart.getTime()));
                paperActivity.setEssayEndTime(new Timestamp(dateEnd.getTime()));
                save(paperActivity);
            }
        } else {
            throwBizException("关联申论无返回值");
        }
    }

    /**
     * 检查试卷科目属性并存储
     *
     * @param paperActivity
     * @param activityPaperReq
     */
    private void checkPaperSubject(PaperActivity paperActivity, ActivityPaperReq activityPaperReq) {
        List<Long> subjectIds = activityPaperReq.getSubjectIds();
        if (CollectionUtils.isEmpty(subjectIds)) {
            throwBizException("试卷科目不能为空");
        }

        if (activityPaperReq instanceof UpdateActivityPaperReq) {
            paperActivitySubjectService.deleteByPaperId(paperActivity.getId());
        }
        paperActivitySubjectService.insertPaperSubject(paperActivity.getId(), subjectIds);
        //log.info("添加活动科目校验:{}", paperActivity.getId());
        // importService.importPaper(paperActivity.getId());
    }

    /**
     * 校验模考大赛特有属性
     *
     * @param paperActivity
     * @param activityPaperReq
     */
    private void checkMatchPaper(PaperActivity paperActivity, ActivityPaperReq activityPaperReq) {
        //所有模考大赛都具有的属性
        if (activityPaperReq.getOnlineTime() == null) {
            throwBizException("活动开始时间不能为空");
        }
        paperActivity.setOnlineTime(activityPaperReq.getOnlineTime());
        if (activityPaperReq.getOfflineTime() == null) {
            throwBizException("活动结束时间不能为空");
        }
        paperActivity.setOfflineTime(activityPaperReq.getOfflineTime());
        if (activityPaperReq.getStartTime() == null) {
            throwBizException("试卷开始时间不能为空");
        }
        paperActivity.setStartTime(activityPaperReq.getStartTime());
        if (activityPaperReq.getEndTime() == null) {
            throwBizException("试卷结束时间不能为空");
        }
        paperActivity.setEndTime(activityPaperReq.getEndTime());

        if (null == activityPaperReq.getTag() || activityPaperReq.equals("")) {
            throwBizException("模考大赛标签不能为空");
        }
        if (StringUtils.isBlank(activityPaperReq.getInstruction())) {
            throwBizException("模考大赛需要有考试说明");
        }
        paperActivity.setInstruction(activityPaperReq.getInstruction());
        paperActivity.setInstructionPC(activityPaperReq.getInstruction());
        if (activityPaperReq.getCourseId() == null) {
            throwBizException("模考大赛需要有课程ID");
        }
        paperActivity.setCourseId(activityPaperReq.getCourseId());
        if (StringUtils.isBlank(activityPaperReq.getCourseInfo())) {
            throwBizException("模考大赛需要课程信息");
        }
        paperActivity.setCourseInfo(activityPaperReq.getCourseInfo());
    }


    /**
     * 检查所有模考估分试卷共用属性
     *
     * @param paperActivity
     * @param activityPaperReq
     */
    private void checkCommonEstimatePaper(PaperActivity paperActivity, ActivityPaperReq activityPaperReq) {
        if (activityPaperReq.getType() == PaperType.FORMATIVE_TEST_ESTIMATE && activityPaperReq.getStartTimeIsEffective() == BaseStatusEnum.NORMAL.getCode()) {
            if (activityPaperReq.getOnlineTime() == null) {
                throwBizException("活动开始时间不能为空");
            }
            paperActivity.setOnlineTime(activityPaperReq.getOnlineTime());
            if (activityPaperReq.getOfflineTime() == null) {
                throwBizException("活动结束时间不能为空");
            }
        }
        paperActivity.setOfflineTime(activityPaperReq.getOfflineTime());
        if (activityPaperReq.getLookParseTime() == null) {
            paperActivity.setLookParseTime(0);
        } else {
            paperActivity.setLookParseTime(activityPaperReq.getLookParseTime());
        }
        if (activityPaperReq.getHideFlag() == null) {
            paperActivity.setHideFlag(0);
        } else {
            paperActivity.setHideFlag(activityPaperReq.getHideFlag());
        }

    }

    /**
     * 检查试卷地区属性，并添加绑定关系
     *
     * @param paperActivity
     * @param activityPaperReq
     */
    private void checkPaperArea(PaperActivity paperActivity, ActivityPaperReq activityPaperReq) {
        List<Long> areaIds = activityPaperReq.getAreaIds();
        if (CollectionUtils.isEmpty(areaIds)) {
            throwBizException("试卷地区不能为空");
        }
        int savePaperAreaInfo = paperAreaService.savePaperAreaInfo(paperActivity.getId(), areaIds, PaperInfoEnum.TypeInfo.SIMULATION);
        if (savePaperAreaInfo > 0) {
            //log.info("更新地区不能为空:{}", paperActivity.getId());
            //importService.importPaper(paperActivity.getId());
        }
    }

    /**
     * 检查所有试卷共用属性（包括真题演练和各种模考试卷）
     *
     * @param paperActivity
     * @param activityPaperReq
     */
    private void checkCommonTruePaper(PaperActivity paperActivity, ActivityPaperReq activityPaperReq) {
        if (StringUtils.isBlank(activityPaperReq.getName())) {
            throwBizException("试卷名称不能为空");
        }
        paperActivity.setName(activityPaperReq.getName());
       /* if (activityPaperReq.getYear() == null || activityPaperReq.getYear() <= 0) {
            throwBizException("试卷年份不能为空");
        }
        paperActivity.setYear(activityPaperReq.getYear());*/
        if (activityPaperReq.getTotalScore() == null || activityPaperReq.getTotalScore() < 0) {
            paperActivity.setTotalScore(0D);
        } else {
            paperActivity.setTotalScore(activityPaperReq.getTotalScore());
        }
        if (activityPaperReq.getLimitTime() == null || activityPaperReq.getLimitTime() < 0) {
            paperActivity.setLimitTime(120 * 60);
        } else {
            paperActivity.setLimitTime(activityPaperReq.getLimitTime());
        }
    }


    public List<HashMap<String, Object>> getActivityList(Integer activityType, Integer activityState, Integer year,
                                                         String areaIds, String activityName, List<Integer> subjectId, String startTime,
                                                         String endTime, int searchType) {
        List<HashMap<String, Object>> list = paperActivityMapper.getActivityPaperList(activityType, activityState, year, areaIds,
                activityName, subjectId, startTime, endTime, searchType);

        //已经结束的转化为结束状态
        if (CollectionUtils.isNotEmpty(list)) {
            List<HashMap<String, Object>> resultList = list.stream()
                    .map(this::assemblingEduInfo)
                    .peek(map->map.computeIfPresent("areaNames",(a,b)-> {
                        Integer subject = Optional.ofNullable(subjectId).filter(CollectionUtils::isNotEmpty)
                                .map(i -> i.get(0))
                                .orElse(-1);
                        return PersonalityAreaUtil.getAreaName(subject,-1,String.valueOf(b));
                    }))
                    .collect(Collectors.toList());
            return resultList;
        }
        return null;
    }


    /**
     * 处理活动状态
     */
    private HashMap<String, Object> activityStatus(String activityEndTime, Integer statusId) throws ParseException {

        Date endTime = DateUtils.parseDate(activityEndTime, "yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String statusName = null;
        //当前时间<结束时间
        if (now.compareTo(endTime) == 1) {
            statusId = ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_END.getKey();
        }
        statusName = EnumUtil.valueOf(statusId, ActivityTypeAndStatus.ActivityStatusEnum.class);
        HashMap statusMap = new HashMap();
        statusMap.put("key", statusId);
        statusMap.put("value", statusName);
        return statusMap;
    }


    /**
     * 活动列表-查询活动报名人数（现在只统计模考大赛）
     *
     * @param id
     */
    public Map<String, Integer> activityData(Long id, Long subjectId) {
        int applicantCount = 0;
        int participantCount = 0;

        PaperActivity paperActivity = selectByPrimaryKey(id);
        if (null == paperActivity) {
            throwBizException("活动不存在！");
        }
        if (paperActivity.getType() == PaperType.TRUE_PAPER) {
            throwBizException("真题演练，暂未统计");
        }
        //模考大赛
        if (paperActivity.getType() == PaperType.MATCH) {
            // 模考大赛有报名人数
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId", id);
            List<MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
            if (CollectionUtils.isNotEmpty(matchUserMetas)) {
                applicantCount = matchUserMetas.size();
            }

            // 模考大赛参加人数
            List<MatchUserMeta> participantUserInfo = matchUserMetas.stream().filter(matchUserMeta -> matchUserMeta.getPracticeId() > 0L)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(participantUserInfo)) {
                participantCount = participantUserInfo.size();
            }

        } else {
            String paperPracticeIdSoreKey = PaperRedisKeys.getPaperPracticeIdSore(id.intValue());
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Set<byte[]> practiceIds = connection.zRange(paperPracticeIdSoreKey.getBytes(), 0, -1);
            try {
                participantCount = practiceIds.size();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.close();
            }
        }


        Map<String, Integer> mapData = Maps.newHashMap();
        mapData.put("applicants", applicantCount);
        mapData.put("participants", participantCount);
        return mapData;
    }


    /**
     * 修改活动发布状态
     *
     * @param activityId
     * @return
     */
    public int updatePaperStatus(Long activityId) {
        PaperActivity paperActivity = this.selectByPrimaryKey(activityId);
        //更新:设置了按照单题算分,只按照最后试题总分计算,不考虑总分
       /* if (paperActivity.getScoreFlag() == 1) {
            checkAllQuestionScoreIsEqualPaperTotalScore(paperActivity);
        }*/
        if (paperActivity != null) {
            Integer activityStatus = paperActivity.getBizStatus();
            //未发布->发布
            if (activityStatus.equals(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_NO_PUBLISH.getKey())) {
                paperActivity.setBizStatus(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_PUBLISH.getKey());
                //发布——>已下线
            } else if (activityStatus.equals(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_PUBLISH.getKey())) {
                paperActivity.setBizStatus(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_OFFLINE.getKey());
                //已下线——>上线(已发布)
            } else if (activityStatus.equals(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_OFFLINE.getKey())) {
                paperActivity.setBizStatus(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_PUBLISH.getKey());
            } else {
                //未知状态的变为初始状态
                paperActivity.setBizStatus(ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_NO_PUBLISH.getKey());
            }
            Integer save = this.save(paperActivity);
            if (save > 0) {
                importService.importPaper(activityId);
            }
            return save;
        }
        return 0;
    }


    /**
     * 查询一张活动卷
     *
     * @param activityId
     * @return
     */
    public PaperSearchInfo paperDetail(Long activityId) {
        return paperSearchService.entityActivityDetail(activityId);
    }


    public List<HashMap<Integer, String>> getTags(Long subjectId, Integer level) {
        ArrayList<Integer> subjectIds = Lists.newArrayList();
        if (level.intValue() == 1) {
            List<Subject> children = teacherSubjectService.findChildren(subjectId, level);
            if (CollectionUtils.isNotEmpty(children)) {
                subjectIds.addAll(children.stream().map(Subject::getId).map(Long::intValue).collect(Collectors.toList()));
            }
        } else {
            subjectIds.add(subjectId.intValue());
        }
        List<ActivityTagEnum.TagEnum> tagList = Arrays.stream(ActivityTagEnum.Subject.values()).
                filter(subject -> subjectIds.contains(subject.getKey()))
                .flatMap(subject -> {
                    List<ActivityTagEnum.TagEnum> values = subject.getValues();
                    return values.stream();
                })
                .collect(Collectors.toList());
        // 显示只是有用状态的标签
        tagList = tagList.stream().filter(tagEnum -> tagEnum.isWork() == true).collect(Collectors.toList());

        List<HashMap<Integer, String>> newTagList = new ArrayList<>();
        if (CollectionUtils.isEmpty(tagList)) {
            HashMap defaultMap = new HashMap();
            defaultMap.put("key", BaseInfo.YESANDNO.NO);
            defaultMap.put("value", "暂无数据");
            newTagList.add(defaultMap);
        } else {
            for (ActivityTagEnum.TagEnum enumCommon : tagList) {
                HashMap map = new HashMap();
                //申论标签
                if (subjectId == 14L) {
                    map.put("key", enumCommon.getCode());
                } else {
                    map.put("key", enumCommon.getTagId());
                }
                map.put("value", enumCommon.getTagName());
                newTagList.add(map);
            }
        }
        return newTagList;
    }

    /**
     * 信息验证规则
     */
    public BiConsumer<Long, Integer> createPaperQuestionValidate() {
        BiConsumer<Long, Integer> validate = (paperId, moduleId) -> {
            PaperActivity paperActivity = selectByPrimaryKey(paperId);
            if (null == paperActivity) {
                throwBizException("试卷信息不存在");
            }
            boolean idExit = PaperModuleHandler.validateModuleIdExit(paperActivity.getModule(), moduleId);
            if (!idExit) {
                throwBizException("模块信息不存在");
            }
        };
        return validate;
    }

    /**
     * 更新活动卷名称
     */
    public int saveActivityInfo(PaperActivity paperActivity) {
        if (paperActivity != null) {
            Integer save = save(paperActivity);
            if (save > 0) {
                importService.importPaper(paperActivity.getId());
            }
        }
        return 0;
    }


    /**
     * @return
     */
    public String dealDateFormat(Object startTime, Object endTime) {
        StringBuffer buffer = new StringBuffer();
        if (startTime != null && endTime != null) {
            buffer.append(startTime.toString().substring(0, startTime.toString().indexOf(".")))
                    .append(" 至 ")
                    .append(endTime.toString().substring(0, endTime.toString().indexOf(".")));
        } else {
            buffer.append("----");
        }
        return buffer.toString();
    }

    @Override
    @Transactional
    public void createActivityByPaperId() {
        //查询需要处理的实体卷信息
        List<Long> paperIdList = paperEntityService.selectAll().stream().map(PaperEntity::getId).collect(Collectors.toList());
        log.info("开始替换活动ID，需要处理数量：{},paperIdList 有：{}", paperIdList.size(), paperIdList);
        log.info("开始处理时间：{}", System.currentTimeMillis());
        paperIdList.stream().forEach(paperId -> {
                    //迁移实体卷的方法
                    syncPaperService.createActivityByPaperId(paperId);
                }
        );
        log.info("处理完毕：{}", System.currentTimeMillis());
    }


    /**
     * 保存课程-课程大纲-阶段测试绑定关系
     *
     * @param courseId
     * @param syllabusId
     * @param paperIds
     */
    public void saveFormativePaper(int courseId, int syllabusId,
                                   List<Long> paperIds) {

        Example example = new Example(PaperActivity.class);
        example.and().andIn("id", paperIds);
        List<PaperActivity> paperActivityList = this.selectByExample(example);

        paperActivityList.stream().forEach(paperInfo -> {
            EstimatePaper estimatePaper = new EstimatePaper();
            estimatePaper.setSyllabusId(syllabusId);
            estimatePaper.setFormativeCourseId(courseId);
            estimatePaper.setId(paperInfo.getId().intValue());
            estimatePaper.setStartTimeIsEffective(paperInfo.getStartTimeIsEffective());
            mongoTemplate.save(estimatePaper);

        });
    }

    /**
     * 校验试卷所有试题的总分是否等于试卷设置的总分
     */
    public void checkAllQuestionScoreIsEqualPaperTotalScore(PaperActivity paperActivity) {
        if (null == paperActivity) {
            return;
        }
        Integer paperType = 0;
        Long paperId = 0L;
        if (paperActivity.getPaperId() != 0L) {
            paperType = PaperInfoEnum.TypeInfo.ENTITY.getKey();
            paperId = paperActivity.getPaperId();

        } else {
            paperType = PaperInfoEnum.TypeInfo.SIMULATION.getKey();
            paperId = paperActivity.getId();
        }

        Example example = new Example(PaperQuestion.class);
        example.and().andEqualTo("paperType", paperType);
        example.and().andEqualTo("paperId", paperId);
        List<PaperQuestion> paperQuestionList = paperQuestionService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(paperQuestionList)) {
            BigDecimal totalScore = paperQuestionList.stream().map(paperQuestion -> {
                BigDecimal bigDecimalScore = new BigDecimal(paperQuestion.getScore());
                return bigDecimalScore;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (paperActivity.getTotalScore() != totalScore.doubleValue()) {
                StringBuffer exception = new StringBuffer();
                String resultException = exception.append("试卷总分是:")
                        .append(paperActivity.getTotalScore())
                        .append(";试题总分相加是:").append(totalScore.doubleValue())
                        .append(",二者分数不一致").toString();
                throwBizException(resultException);
            }


        }

    }

    /**
     * 拼接课程字符串
     *
     * @param name
     * @return
     */
    private String getCourseName(String name) {
        String courseName = " 直播解析";
        StringBuffer course = new StringBuffer();
        return course.append(name).append(courseName).toString();
    }

    /**
     * 回显单题算分
     *
     * @param paperActivity
     * @return
     */
    private Double getActivitySelectScore(SelectActivityReq paperActivity) {
        Double score = 0D;
        if (paperActivity.getPaperId() != 0) {
            score = paperQuestionService.getPaperQuestionScore(paperActivity.getPaperId(), PaperInfoEnum.TypeInfo.ENTITY);
        } else {
            if (null != paperActivity.getId()) {
                score = paperQuestionService.getPaperQuestionScore(paperActivity.getId(), PaperInfoEnum.TypeInfo.SIMULATION);
            }
        }
        return score;
    }


    public Double getScore(PaperActivity paperActivity) {
        Double paperQuestionScore = 0D;
        if (paperActivity.getScoreFlag() == BaseStatusEnum.NORMAL.getCode()) {
            if (paperActivity.getPaperId() != 0) {
                paperQuestionScore = paperQuestionService.getPaperQuestionScore(paperActivity.getPaperId(), PaperInfoEnum.TypeInfo.ENTITY);
            } else {
                paperQuestionScore = paperQuestionService.getPaperQuestionScore(paperActivity.getId(), PaperInfoEnum.TypeInfo.ENTITY);
            }
        } else {
            paperQuestionScore = paperActivity.getTotalScore();
        }
        return paperQuestionScore;
    }

    @Override
    public int updateSourceFlag(Long id) {
        PaperActivity paperActivity = selectByPrimaryKey(id);
        if (null != paperActivity) {
            paperActivity.setSourceFlag(paperActivity.getSourceFlag() == BaseInfo.YESANDNO.NO.getCode() ?
                    BaseInfo.YESANDNO.YES.getCode() :
                    BaseInfo.YESANDNO.NO.getCode());
            return save(paperActivity);
        }
        return 0;
    }

    @Override
    public List<HashMap<String, Object>> getActivityListForEdu(ActivityTypeAndStatus.ActivityTypeEnum activityTypeEnum, int status, String name, int subjectId, long startTime, long endTime, int tagId, String paperId) {
        List<HashMap<String, Object>> results = paperActivityMapper.getActivityListForEdu(activityTypeEnum, status, name, subjectId, startTime, endTime, tagId, paperId);
        for (HashMap<String, Object> result : results) {
            assemblingEduInfo(result);
            Map<String, Integer> countMap = activityData(MapUtils.getLong(result, "id"), new Long(subjectId));
            result.putAll(countMap);
        }
        return results;
    }

    private HashMap<String, Object> assemblingEduInfo(HashMap<String, Object> activity) {
        if (MapUtils.isEmpty(activity)) {
            return activity;
        }

        Object examStartTime = activity.get("startTime");
        Object examEndTime = activity.get("endTime");
        Object activityStartTime = activity.get("onlineTime");
        Object activityEndTime = activity.get("offlineTime");

        Integer statusId = Integer.valueOf(activity.get("bizStatus").toString());
        if (activityEndTime != null) {
            try {
                activity.put("activityStatus", this.activityStatus(activityEndTime.toString().substring(0, activityEndTime.toString().indexOf(".")), statusId));
                Integer tag = MapUtils.getInteger(activity, "tag", 0);
                if (tag != 0) {
                    ActivityTagEnum.TagEnum tagEnum = ActivityTagEnum.TagEnum.create(tag);
                    activity.put("tagName", tagEnum.getTagName());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            HashMap map = new HashMap();
            map.put("key", statusId);
            map.put("value", EnumUtil.valueOf(statusId, ActivityTypeAndStatus.ActivityStatusEnum.class));
            activity.put("activityStatus", map);
        }
        activity.put("activityTime", dealDateFormat(activityStartTime, activityEndTime));
        activity.put("examTime", dealDateFormat(examStartTime, examEndTime));
        activity.put("onlineTime", activityStartTime == null ? "" : activityStartTime);
        activity.put("offlineTime", activityEndTime == null ? "" : activityEndTime);
        if (activity.get("areaNames") == null) {
            activity.put("areaNames", "未知");
        }
        return activity;
    }

    @Override
    public Object getQRCode(Long id, Long subjectId) {
        String qrcodeUrl = "";
        PaperActivity paperActivity = selectByPrimaryKey(id);
        if (paperActivity != null) {
            if (StringUtils.isNoneBlank(paperActivity.getQrcode())) {
                qrcodeUrl = paperActivity.getQrcode();
            } else {
                List<Long> areaIds = getAreaIds(id, PaperInfoEnum.TypeInfo.SIMULATION);
                // 科目
                //List<Long> subjectIds = paperActivitySubjectService.findSubjectByPaperId(id);
                Long category = teacherSubjectService.findParent(subjectId, 2);
                String areaIdsStr = areaIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                String scene = category + "#" + subjectId + "#" + areaIdsStr;
                // 获取二维码
                qrcodeUrl = weChatService.getQrCode(scene);
                Example example = new Example(PaperActivity.class);
                example.or().andEqualTo("id", id);
                int updateByExampleSelective = updateByExampleSelective(
                        PaperActivity.builder().qrcode(qrcodeUrl).build(), example);
                log.info("update paperActivity qrcode ret:{}", updateByExampleSelective);
            }
        }

        return qrcodeUrl;
    }
}
