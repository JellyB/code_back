package com.huatu.tiku.teacher.service.impl.paper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import com.huatu.tiku.entity.teacher.*;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.paper.PaperAreaService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperSearchService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/8/8
 */
@Service
public class PaperSearchServiceImpl implements PaperSearchService {

    @Autowired
    private PaperEntityService paperEntityService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private QuestionSearchService questionSearchService;

    @Autowired
    private PaperActivityService paperActivityService;

    @Autowired
    private PaperAreaService paperAreaService;

    @Autowired
    private ReflectQuestionDao reflectQuestionDao;


    public PaperSearchInfo entityDetail(long paperId) {
        Supplier<PaperSearchInfo> supplier = () -> {
            PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
            if (null == paperEntity) {
                return null;
            }
            PaperSearchInfo paperSearchInfo = PaperSearchInfo.builder()
                    .id(paperEntity.getId())
                    .name(paperEntity.getName())
                    .moduleInfoStr(paperEntity.getModule())
                    .build();
            return paperSearchInfo;
        };
        return detail(supplier, PaperInfoEnum.TypeInfo.ENTITY);
    }


    private PaperSearchInfo activityDetail(Long paperId) {
        Supplier<PaperSearchInfo> supplier = () -> {
            PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
            if (paperActivity == null) {
                return null;
            }
            PaperSearchInfo paperSearchInfo = PaperSearchInfo.builder()
                    .id(paperActivity.getId())
                    .name(paperActivity.getName())
                    .moduleInfoStr(paperActivity.getModule())
                    .build();
            return paperSearchInfo;
        };
        return detail(supplier, PaperInfoEnum.TypeInfo.SIMULATION);
    }

    /**
     * 查询一张试卷信息
     *
     * @param activityId 试卷ID
     * @return
     */
    public PaperSearchInfo entityActivityDetail(long activityId) {
        //实体卷
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(activityId);
        if (paperActivity == null) {
            return null;
        }
        if (paperActivity.getPaperId() != null && paperActivity.getPaperId() > 0) {
            return entityDetail(paperActivity.getPaperId());
        }
        //活动卷
        return activityDetail(paperActivity.getId());
    }

    @Override
    public PageInfo entityList(int mode, int year, String areaIds, String paperTime, String name, int page, int pageSize) {
        WeekendSqls<PaperEntity> sql = WeekendSqls.custom();
        if (BaseInfo.isNotDefaultSearchValue(mode)) {
            sql.andEqualTo(PaperEntity::getModule, mode);
        }
        if (BaseInfo.isNotDefaultSearchValue(year)) {
            sql.andEqualTo(PaperEntity::getYear, year);
        }
        if (BaseInfo.isNotDefaultSearchValue(paperTime)) {
            sql.andEqualTo(PaperEntity::getPaperTime, paperTime);
        }
        if (BaseInfo.isNotDefaultSearchValue(name)) {
            sql.andLike(PaperEntity::getName, name);
        }
        if (BaseInfo.isNotDefaultSearchValue(areaIds)) {
            String[] split = areaIds.split(",");
            List<Long> areaIdList = Arrays.stream(split)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            Example example = new Example(PaperArea.class);
            example.and().andIn("areaId", areaIdList);
            List<PaperArea> list = paperAreaService.selectByExample(example);
            sql.andIn(PaperEntity::getId, list.stream().map(PaperArea::getPaperId).collect(Collectors.toList()));
        }
        Example example = Example.builder(PaperEntity.class)
                .andWhere(sql)
                .build();
        PageInfo<PaperEntity> pageInfo = PageHelper.startPage(page, pageSize)
                .doSelectPageInfo(
                        () -> paperEntityService.selectByExample(example)
                );
        List<PaperEntity> collect = pageInfo.getList().stream()
                .map(paperEntity -> {
                    List<PaperModuleInfo> paperModuleInfoList = PaperModuleHandler.analysisModuleStr(paperEntity.getModule());
                    paperEntity.setModuleInfo(paperModuleInfoList);
                    return paperEntity;
                })
                .collect(Collectors.toList());
        pageInfo.setList(collect);
        return pageInfo;
    }

    /**
     * 试卷详情查询
     *
     * @param supplier 基础试卷实体
     * @param typeInfo 试卷类型
     * @return
     */
    private PaperSearchInfo detail(Supplier<PaperSearchInfo> supplier, PaperInfoEnum.TypeInfo typeInfo) {
        PaperSearchInfo paperSearchInfo = supplier.get();
        if (null == paperSearchInfo) {
            return null;
        }
        //获取试卷下关联的所有试题信息，此处不包含 复合题的ID 信息
        List<PaperQuestion> questionList = paperQuestionService.findByPaperIdAndType(paperSearchInfo.getId(), typeInfo);
        reflectQuestionDao.transQuestionId(questionList);
        //获取所有的模块信息
        List<PaperModuleInfo> paperModuleInfoList = PaperModuleHandler.analysisModuleStr(paperSearchInfo.getModuleInfoStr());
        if (CollectionUtils.isNotEmpty(paperModuleInfoList)) {
            //存在模块信息
            List<Long> questionIdList = questionList.stream()
                    .map(PaperQuestion::getQuestionId)
                    .collect(Collectors.toList());
            //通过questionId 处理查询所有的试题信息
            List<QuestionSimpleInfo> questionSimpleInfoList = questionSearchService.listAllByQuestionId(questionIdList);
            //转换成 PaperQuestionSimpleInfo
            //属性复制
            BiConsumer<PaperQuestionSimpleInfo, QuestionSimpleInfo> copy = (paperQuestionSimpleInfoData, questionSimpleInfoData) -> {
                BeanUtils.copyProperties(questionSimpleInfoData, paperQuestionSimpleInfoData);
                List<PaperQuestionSimpleInfo> collect = questionSimpleInfoData.getChildren().stream()
                        .map(data -> {
                            PaperQuestionSimpleInfo result = new PaperQuestionSimpleInfo();
                            BeanUtils.copyProperties(data, result);
                            return result;
                        })
                        .collect(Collectors.toList());
                paperQuestionSimpleInfoData.setChildren(collect);
            };
            //添加额外的属性
            //update by lizhenjuan 添加知识点属性
            BiConsumer<PaperQuestionSimpleInfo, PaperQuestion> setInfo =
                    (paperQuestionSimpleInfoData, paperQuestionData) -> {
                        paperQuestionSimpleInfoData.setModuleId(paperQuestionData.getModuleId());
                        paperQuestionSimpleInfoData.setScore(paperQuestionData.getScore());
                        paperQuestionSimpleInfoData.setSort(paperQuestionData.getSort());
                    };
            List<PaperQuestionSimpleInfo> paperQuestionSimpleInfoList = questionSimpleInfoList.parallelStream()
                    .map(questionSimpleInfo -> {
                        PaperQuestionSimpleInfo paperQuestionSimpleInfo = new PaperQuestionSimpleInfo();
                        //QuestionSimpleInfo 属性转换成 PaperQuestionSimpleInfo
                        copy.accept(paperQuestionSimpleInfo, questionSimpleInfo);
                        //处理复合题的情况，复合题只有两层结构
                        List<PaperQuestion> paperQuestionList = questionList.parallelStream()
                                .filter(paperQuestion -> {
                                    //由于复合题是在绑定关系中是不存在的，此处需要重新处理
                                    if (CollectionUtils.isNotEmpty(questionSimpleInfo.getChildren())) {
                                        return questionSimpleInfo.getChildren().stream()
                                                .anyMatch(children -> paperQuestion.getQuestionId().equals(children.getId()));
                                    } else {
                                        return paperQuestion.getQuestionId().equals(questionSimpleInfo.getId());
                                    }
                                })
                                .collect(Collectors.toList());
                        if (CollectionUtils.isNotEmpty(paperQuestionList)) {
                            if (CollectionUtils.isNotEmpty(paperQuestionSimpleInfo.getChildren())) {
                                List<PaperQuestionSimpleInfo> newChildrenList = paperQuestionSimpleInfo.getChildren().stream()
                                        .map((data) -> {
                                            Optional<PaperQuestion> any = paperQuestionList.stream()
                                                    .filter(paperQuestion -> paperQuestion.getQuestionId().equals(data.getId()))
                                                    .findAny();
                                            if (any.isPresent()) {
                                                setInfo.accept(data, any.get());
                                            }
                                            return data;
                                        })
                                        .collect(Collectors.toList());
                                newChildrenList.sort(Comparator.comparingInt(PaperQuestionSimpleInfo::getSort));
                                //绑定父类试题信息
                                paperQuestionSimpleInfo.setSort(newChildrenList.get(0).getSort());
                                paperQuestionSimpleInfo.setModuleId(newChildrenList.get(0).getModuleId());
                                paperQuestionSimpleInfo.setChildren(newChildrenList);
                            } else {
                                setInfo.accept(paperQuestionSimpleInfo, paperQuestionList.get(0));
                            }
                        }
                        return paperQuestionSimpleInfo;
                    })
                    .collect(Collectors.toList());
            //组装到信息到module 中
            List<PaperSearchInfo.ModuleInfo> moduleInfoList = paperModuleInfoList.parallelStream()
                    .map(paperModuleInfo -> {
                        PaperSearchInfo.ModuleInfo moduleInfo = new PaperSearchInfo.ModuleInfo();
                        moduleInfo.setId(paperModuleInfo.getId());
                        moduleInfo.setName(paperModuleInfo.getName());
                        List<PaperQuestionSimpleInfo> collect = paperQuestionSimpleInfoList.stream()
                                .filter(paperQuestionSimpleInfo -> paperModuleInfo.getId().equals(paperQuestionSimpleInfo.getModuleId()))
                                .collect(Collectors.toList());
                        collect.sort(Comparator.comparingInt(PaperQuestionSimpleInfo::getSort));
                        moduleInfo.setList(collect);
                        return moduleInfo;
                    })
                    .collect(Collectors.toList());
            moduleInfoList.sort(Comparator.comparingInt(PaperSearchInfo.ModuleInfo::getId));
            paperSearchInfo.setModuleInfo(moduleInfoList);
        }
        //减少字段传递
        paperSearchInfo.setModuleInfoStr(StringUtils.EMPTY);
        paperSearchInfo.initQuestionCount();
        //向上取整,转化为整数
        Long score = Math.round(paperSearchInfo.iniTotalScore());
        System.out.println("分数数：{}"+ score);
        paperSearchInfo.setTotalScore(Double.parseDouble(score.toString()));
        return paperSearchInfo;
    }
}
