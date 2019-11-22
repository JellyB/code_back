package com.huatu.tiku.teacher.service.impl.paper;

import com.huatu.tiku.entity.teacher.*;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.paper.PaperEntityMapper;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.paper.PaperAreaService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/2
 */
@Service
public class PaperEntityServiceImpl extends BaseServiceImpl<PaperEntity> implements PaperEntityService {

    public PaperEntityServiceImpl() {
        super(PaperEntity.class);
    }

    @Autowired
    private PaperEntityMapper mapper;

    @Autowired
    private PaperAreaService paperAreaService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private CommonQuestionServiceV1 questionService;

    @Autowired
    private PaperActivityService activityService;

    @Autowired
    private CommonQuestionServiceV1 commonQuestionService;

    @Override
    public int getModuleIdByName(Long paperId, String moduleName) {
        return PaperModuleHandler.getModuleIdByName(paperId, PaperInfoEnum.TypeInfo.ENTITY, moduleName, () -> getModuleInfo(paperId));
    }

    @Override
    public List<PaperModuleInfo> saveModuleInfo(Long paperId, List<String> moduleNames) {
        //查询试卷其模块信息
        String moduleInfo = getModuleInfo(paperId);
        //解析模块信息转化为集合
        List<PaperModuleInfo> moduleInfoList = PaperModuleHandler.buildAddNewModule(moduleInfo, moduleNames);
        //重新构建模块信息
        String newModule = PaperModuleHandler.buildNewModuleStr(moduleInfoList);
        saveModule(paperId, newModule);
        //缓存刷新
        PaperModuleHandler.refreshCache(paperId, PaperInfoEnum.TypeInfo.ENTITY, moduleInfoList);
        return moduleInfoList;
    }

    @Override
    public int deleteModuleInfo(Long paperId, List<String> moduleName) {
        //1.校验是否存在试题信息
        List<Integer> moduleIds = moduleName.stream().map(name -> getModuleIdByName(paperId, name)).collect(Collectors.toList());
        return deleteModuleInfoByIdList(paperId, moduleIds);
    }

    @Override
    public int deleteModuleInfoByIdList(Long paperId, List<Integer> moduleIds) {
        WeekendSqls<PaperQuestion> weekendSql = WeekendSqls.<PaperQuestion>custom()
                .andIn(PaperQuestion::getModuleId, moduleIds)
                .andEqualTo(PaperQuestion::getPaperId, paperId)
                .andEqualTo(PaperQuestion::getPaperType, PaperInfoEnum.TypeInfo.ENTITY.getCode());
        Example paperQuestionExample = Example.builder(PaperQuestion.class)
                .andWhere(weekendSql)
                .build();
        List<PaperQuestion> paperQuestionList = paperQuestionService.selectByExample(paperQuestionExample);
        if (paperQuestionList.size() > 0) {
            throwBizException("模块下存在试题");
        }
        //删除模块信息
        String moduleInfo = getModuleInfo(paperId);
        String newModule = PaperModuleHandler.buildDeleteNetModuleByIds(moduleInfo, moduleIds);
        saveModule(paperId, newModule);
        PaperModuleHandler.refreshCache(paperId, PaperInfoEnum.TypeInfo.ENTITY, newModule);
        return moduleIds.size();
    }

    @Override
    public int updateModuleInfo(Long paperId, List<PaperModuleInfo> moduleInfoList) {
        String moduleInfo = getModuleInfo(paperId);
        String newModule = PaperModuleHandler.buildUpdateModuleInfoByList(moduleInfo, moduleInfoList);
        saveModule(paperId, newModule);
        PaperModuleHandler.refreshCache(paperId, PaperInfoEnum.TypeInfo.ENTITY, newModule);
        return moduleInfoList.size();
    }

    @Override
    public List<HashMap<String, Object>> list(int mode, int year, int specialFlag, int missFlag, long subjectId, int bizStatus, String areaIds, String name) {
        return mapper.list(mode, year, specialFlag, missFlag, subjectId, bizStatus, areaIds, name);
    }

    @Transactional
    @Override
    public int savePaper(PaperEntity paperEntity) {
        //设置试卷默认状态
        if (null == paperEntity.getId() && null == paperEntity.getBizStatus()) {
            paperEntity.setBizStatus(PaperInfoEnum.BizStatus.NO_PUBLISH.getCode());
        }
        boolean isInsert = null == paperEntity.getId() || paperEntity.getId() < 0;
        String moduleInfo = "";
        //只有在新增的时候处理 默认模块信息
        if (isInsert) {
            moduleInfo = PaperModuleHandler.ModuleEnum.buildDefaultModuleInfo(paperEntity.getSubjectId());
            paperEntity.setModule(moduleInfo);
        }
        Integer save = save(paperEntity);
        handlePaperArea(paperEntity);
        //只有在新增的时候处理 默认模块信息
        if (isInsert && save > 0 && StringUtils.isNotBlank(moduleInfo)) {
            PaperModuleHandler.refreshCache(paperEntity.getId(), PaperInfoEnum.TypeInfo.ENTITY, moduleInfo);
        }
        return save;
    }

    /**
     * 处理地区信息
     */
    private void handlePaperArea(PaperEntity paperEntity) {
        final long paperId = paperEntity.getId();
        if (StringUtils.isNotBlank(paperEntity.getAreaIds())) {
            List<Long> collect = Arrays.stream(paperEntity.getAreaIds().split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            paperAreaService.savePaperAreaInfo(paperId, collect, PaperInfoEnum.TypeInfo.ENTITY);
        }
    }

    @Transactional
    @Override
    public int deletePaper(Long paperId) {
        checkActivityInfo(paperId);
        //删除试卷信息
        int delete = deleteByPrimaryKey(paperId);
        //删除试卷-区域信息
        paperAreaService.deletePaperAreaInfo(paperId, PaperInfoEnum.TypeInfo.ENTITY);
        //删除试卷-试题信息
        paperQuestionService.deletePaperQuestionInfo(paperId, PaperInfoEnum.TypeInfo.ENTITY);
        return delete;
    }

    @Override
    public PaperEntity detail(long paperId) {
        PaperEntity paperEntity = selectByPrimaryKey(paperId);
        if (null != paperEntity) {
            List<PaperArea> list = paperAreaService.list(paperId, PaperInfoEnum.TypeInfo.ENTITY);
            String areaIds = list.stream()
                    .map(PaperArea::getAreaId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            paperEntity.setAreaIds(areaIds);
        }
        //返回增加计算单题总分字段
        List<PaperQuestion> paperQuestionList = paperQuestionService.findByPaperIdAndType(paperId, PaperInfoEnum.TypeInfo.ENTITY);
        if (CollectionUtils.isNotEmpty(paperQuestionList)) {
            List<Double> scores = paperQuestionList.stream()
                    .map(PaperQuestion::getScore)
                    .collect(Collectors.toList());
            paperEntity.setQuestionTotalScore(PaperQuestionScoreHandler.getQuestionTotalScore(scores));
        }
        return paperEntity;
    }

    @Override
    @Transactional
    public int insertPaper(PaperEntity paperEntity) {
        if (null == paperEntity.getBizStatus()) {
            paperEntity.setBizStatus(PaperInfoEnum.BizStatus.NO_PUBLISH.getCode());
        }
        Integer insert = insert(paperEntity);
        final long paperId = paperEntity.getId();
        if (StringUtils.isNotBlank(paperEntity.getAreaIds())) {
            List<Long> collect = Arrays.stream(paperEntity.getAreaIds().split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            paperAreaService.savePaperAreaInfo(paperId, collect, PaperInfoEnum.TypeInfo.ENTITY);
        }
        return insert;
    }

    @Transactional
    @Override
    public int updateReleaseStatus(Long paperId) {
        PaperEntity paperEntity = selectByPrimaryKey(paperId);
        /**
         * 原有逻辑：将未发布改为发布，同时发布试卷下的试题；将发布改为未发布
         * 现有逻辑：无论什么状态，修改试卷状态为发布状态，并发布试卷下的试题
         */
        if (null != paperEntity) {
            paperEntity.setBizStatus(PaperInfoEnum.BizStatus.PUBLISH.getCode());
            //根据试卷ID查询试卷下的所有试题
            List<PaperQuestion> baseQuestionList = paperQuestionService.findByPaperIdAndType(paperId, PaperInfoEnum.TypeInfo.ENTITY);
            if (CollectionUtils.isNotEmpty(baseQuestionList)) {
                List<Long> questionIds = baseQuestionList.stream()
                        .map(PaperQuestion::getQuestionId)
                        .collect(Collectors.toList());
                commonQuestionService.updateQuestionBizStatusBatch(questionIds, BizStatusEnum.PUBLISH.getValue());
            }
            return save(paperEntity);
        }
        return 0;
    }

    @Override
    public List<String> getEntityPaperTime() {
        return mapper.getEntityPaperTime();
    }

    /**
     * 获取模块信息
     */
    private String getModuleInfo(Long paperId) {
        PaperEntity entity = selectByPrimaryKey(paperId);
        if (null == entity) {
            throwBizException("试卷信息不存在");
        }
        return entity.getModule();
    }

    /**
     * 保存模块信息
     */
    private int saveModule(Long paperId, String module) {
        PaperEntity paperEntity = PaperEntity.builder()
                .id(paperId)
                .module(module)
                .build();
        return save(paperEntity);
    }

    /**
     * 校验 活动信息
     *
     * @param paperId
     */
    private void checkActivityInfo(Long paperId) {
        //校验 是否有活动卷关联
        List<Long> activityByPaperId = activityService.findByPaperId(paperId);
        if (CollectionUtils.isNotEmpty(activityByPaperId)) {
            Example example = new Example(PaperActivity.class);
            example.and().andIn("id", activityByPaperId);
            List<PaperActivity> paperActivities = activityService.selectByExample(example);
            String activityNames = paperActivities.stream().map(activity -> activity.getName()).collect(Collectors.joining(","));
            throwBizException("存在关联的活动试卷,名称为：" + activityNames);
        }
    }


    /**
     * 信息验证规则
     */
    public BiConsumer<Long, Integer> createPaperQuestionValidate() {
        BiConsumer<Long, Integer> validate = (paperId, moduleId) -> {
            //验证 模块信息是否存在
            PaperEntity paperEntity = selectByPrimaryKey(paperId);
            if (null == paperEntity) {
                throwBizException("试卷信息不存在");
            }
            boolean idExit = PaperModuleHandler.validateModuleIdExit(paperEntity.getModule(), moduleId);
            if (!idExit) {
                throwBizException("模块信息不存在");
            }
        };
        return validate;
    }

    @Override
    public int changeModuleInfo(long paperId, List<PaperModuleInfo> moduleInfoList) {
        String moduleInfo = getModuleInfo(paperId);
        List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(paperId, PaperInfoEnum.TypeInfo.ENTITY);
        Consumer<PaperQuestion> savePaperQuestion = ((paperQuestion)->paperQuestionService.save(paperQuestion));
        String newModule = PaperModuleHandler.buildChangeModuleInfoByList(moduleInfo, moduleInfoList,paperQuestions,savePaperQuestion);
        saveModule(paperId, newModule);
        PaperModuleHandler.refreshCache(paperId, PaperInfoEnum.TypeInfo.ENTITY, newModule);
        return moduleInfoList.size();
    }

    @Override
    public int updateSourceFlag(long paperId) {
        PaperEntity paperEntity = selectByPrimaryKey(paperId);
        if(null != paperEntity){
            paperEntity.setSourceFlag(paperEntity.getSourceFlag()== BaseInfo.YESANDNO.NO.getCode()?
                    BaseInfo.YESANDNO.YES.getCode():
                    BaseInfo.YESANDNO.NO.getCode());
            return save(paperEntity);
        }
        return 0;
    }

}
