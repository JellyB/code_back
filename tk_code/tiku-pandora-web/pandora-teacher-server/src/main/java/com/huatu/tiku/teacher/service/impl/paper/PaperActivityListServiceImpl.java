package com.huatu.tiku.teacher.service.impl.paper;

import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.paper.PaperActivityListService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;
import java.util.List;
import java.util.function.Consumer;


/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/14
 * @描述
 */
@Service
public class PaperActivityListServiceImpl extends BaseServiceImpl<PaperActivity> implements PaperActivityListService {

    public PaperActivityListServiceImpl() {
        super(PaperActivity.class);
    }

    @Autowired
    PaperQuestionService paperQuestionService;

    /**
     * 保存模块信息
     *
     * @param activityId  试卷ID
     * @param moduleNames 模块名称
     * @return
     */
    public List<PaperModuleInfo> saveModuleInfo(Long activityId, List<String> moduleNames) {

        //查询试卷其模块信息
        String moduleInfo = getModuleInfo(activityId);
        //解析模块信息转化为集合
        List<PaperModuleInfo> moduleInfoList = PaperModuleHandler.buildAddNewModule(moduleInfo, moduleNames);
        // 集合对象装换成 字符串
        String newModule = PaperModuleHandler.buildNewModuleStr(moduleInfoList);
        //保存模块信息
        this.saveModule(activityId, newModule);
        //缓存刷新
        PaperModuleHandler.refreshCache(activityId, PaperInfoEnum.TypeInfo.SIMULATION, moduleInfoList);
        return moduleInfoList;
    }


    @Override
    public int getModuleIdByName(Long paperId, String moduleName) {
        return PaperModuleHandler.getModuleIdByName(paperId, PaperInfoEnum.TypeInfo.SIMULATION, moduleName, () -> getModuleInfo(paperId));
    }

    @Override
    public int changeModuleInfo(long activityId, List<PaperModuleInfo> moduleInfoList) {
        String moduleInfo = getModuleInfo(activityId);
        List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(activityId, PaperInfoEnum.TypeInfo.SIMULATION);
        Consumer<PaperQuestion> savePaperQuestion = ((paperQuestion)->paperQuestionService.save(paperQuestion));
        String newModule = PaperModuleHandler.buildChangeModuleInfoByList(moduleInfo, moduleInfoList,paperQuestions,savePaperQuestion);
        saveModule(activityId, newModule);
        PaperModuleHandler.refreshCache(activityId, PaperInfoEnum.TypeInfo.SIMULATION, newModule);
        return moduleInfoList.size();
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
        PaperModuleHandler.refreshCache(paperId, PaperInfoEnum.TypeInfo.SIMULATION, newModule);
        return moduleIds.size();
    }

    @Override
    public int updateModuleInfo(Long paperId, List<PaperModuleInfo> moduleInfoList) {
        String moduleInfo = getModuleInfo(paperId);
        String newModule = PaperModuleHandler.buildUpdateModuleInfoByList(moduleInfo, moduleInfoList);
        saveModule(paperId, newModule);
        PaperModuleHandler.refreshCache(paperId, PaperInfoEnum.TypeInfo.SIMULATION, newModule);
        return moduleInfoList.size();
    }


    private String getModuleInfo(Long acticvityId) {
        PaperActivity paperActivity = selectByPrimaryKey(acticvityId);
        if (paperActivity == null) {
            throwBizException("活动卷信息不存在");
        }
        return (null == paperActivity) ? StringUtils.EMPTY : paperActivity.getModule();
    }


    private int saveModule(Long activityId, String newModule) {
        PaperActivity paperActivity = PaperActivity.builder()
                .module(newModule)
                .build();
        paperActivity.setId(activityId);
        return save(paperActivity);
    }


}
