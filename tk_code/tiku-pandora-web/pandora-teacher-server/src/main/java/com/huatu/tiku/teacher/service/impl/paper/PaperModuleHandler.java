package com.huatu.tiku.teacher.service.impl.paper;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.teacher.PaperModuleInfo;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.EnumCommon;
import com.huatu.tiku.enums.PaperInfoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 处理试卷 模块信息
 * Created by lijun on 2018/8/2
 */
public class PaperModuleHandler {

    /**
     * 需要默认添加模块信息的接口
     */
    private static List<Long> DEFAULT_ADD_INFO_SUBJECT = Lists.newArrayList();

    static {
        //公务员行测
        DEFAULT_ADD_INFO_SUBJECT.add(1l);
        //事业单位职测
        DEFAULT_ADD_INFO_SUBJECT.add(3l);
    }

    /**
     * 缓存节点信息
     */
    private static final Cache<String, List<PaperModuleInfo>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    /**
     * 获取ID
     *
     * @param paperId    试卷ID
     * @param moduleName 模块名称
     * @param supplier   数据补充
     * @return 不存在的返回 -1
     */
    public static int getModuleIdByName(Long paperId, PaperInfoEnum.TypeInfo typeInfo, String moduleName, Supplier<String> supplier) {
        List<PaperModuleInfo> moduleInfoList = CACHE.getIfPresent(paperId);
        //缓存为空
        if (CollectionUtils.isEmpty(moduleInfoList)) {
            //从mysql中查询
            String moduleInfo = supplier.get();
            //将字符串更改为集合
            moduleInfoList = analysisModuleStr(moduleInfo);
            //刷新到缓存中
            refreshCache(paperId, typeInfo, moduleInfoList);
        }

        //缓存不为空
        if (CollectionUtils.isNotEmpty(moduleInfoList)) {
            Optional<PaperModuleInfo> info = moduleInfoList.stream()
                    .filter(moduleInfo -> moduleInfo.getName().equals(moduleName))
                    .findFirst();

            if (info.isPresent()) {
                return info.get().getId();
            }
        }
        throw new BizException(ErrorResult.create(5000000, "模块信息不存在"));
    }

    /**
     * 刷新缓存
     */
    protected static void refreshCache(Long paperId, PaperInfoEnum.TypeInfo typeInfo, List<PaperModuleInfo> moduleInfoList) {
        if (CollectionUtils.isNotEmpty(moduleInfoList)) {
            String key = buildKey(paperId, typeInfo);
            CACHE.put(key, moduleInfoList);
        }
    }

    /**
     * 刷新缓存
     */
    protected static void refreshCache(Long paperId, PaperInfoEnum.TypeInfo typeInfo, String moduleInfo) {
        if (StringUtils.isNotBlank(moduleInfo)) {
            List<PaperModuleInfo> moduleInfoList = analysisModuleStr(moduleInfo);
            refreshCache(paperId, typeInfo, moduleInfoList);
        }
    }

    /**
     * 构建key
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @return
     */
    protected static String buildKey(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        return typeInfo.getCode() + ":" + paperId;
    }

    /**
     * 解析模块字段 转换成集合
     *
     * @param moduleStr
     * @return 模块集合
     */
    public static List<PaperModuleInfo> analysisModuleStr(String moduleStr) {
        if (StringUtils.isBlank(moduleStr)) {
            return Lists.newArrayList();
        }
        List<PaperModuleInfo> moduleInfoList = JSONObject.parseArray(moduleStr, PaperModuleInfo.class);
        return moduleInfoList;
    }

    /**
     * 集合对象装换成 字符串
     *
     * @param moduleInfoList 待转换数据
     */
    protected static String buildNewModuleStr(List<PaperModuleInfo> moduleInfoList) {
        return JSONObject.toJSONString(moduleInfoList);
    }

    /**
     * 构建 新增后的 模块信息
     *
     * @param baseModule     原始的字段信息
     * @param addModuleNames 需要新增的模块名称合集
     * @return
     */
    protected static List<PaperModuleInfo> buildAddNewModule(String baseModule, List<String> addModuleNames) {
        List<PaperModuleInfo> moduleInfoList = analysisModuleStr(baseModule);
        if (null == addModuleNames || addModuleNames.size() == 0) {
            return moduleInfoList;
        }
        //此处需要保证新的ID 必须是未被使用过的
        addModuleNames.stream()
//                .filter(//处理重复添加的问题
//                        moduleName -> !moduleInfoList.stream()
//                                .map(PaperModuleInfo::getName)
//                                .anyMatch(name -> name.equals(moduleName))
//                )
                .forEach(moduleName -> {
                    PaperModuleInfo build = PaperModuleInfo.builder()
                            .id(moduleInfoList.size() == 0 ? 0 : moduleInfoList.get(moduleInfoList.size() - 1).getId() + 1)
                            .name(moduleName)
                            .build();
                    moduleInfoList.add(build);
                });
        return moduleInfoList;
    }

    /**
     * 构建 修改后的模块信息
     *
     * @param baseModule        原始的字段信息
     * @param deleteModuleNames 需要新增的模块名称合集
     * @return 处理完成后的字符信息
     */
    protected static String buildDeleteNetModule(String baseModule, List<String> deleteModuleNames) {
        List<PaperModuleInfo> moduleInfoList = analysisModuleStr(baseModule);
        if (moduleInfoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        List<PaperModuleInfo> infoList = moduleInfoList.stream()
                .filter(moduleInfo -> !deleteModuleNames.stream().anyMatch(name -> name.equals(moduleInfo.getName())))
                .collect(Collectors.toList());
        if (infoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        return JSONObject.toJSONString(infoList);
    }

    /**
     * 构建 修改后的模块信息
     *
     * @param baseModule      原始的字段信息
     * @param deleteModuleIds 需要新增的模块名称合集
     * @return 处理完成后的字符信息
     */
    protected static String buildDeleteNetModuleByIds(String baseModule, List<Integer> deleteModuleIds) {
        List<PaperModuleInfo> moduleInfoList = analysisModuleStr(baseModule);
        if (moduleInfoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        List<PaperModuleInfo> infoList = moduleInfoList.stream()
                .filter(moduleInfo -> !deleteModuleIds.stream().anyMatch(id -> id.equals(moduleInfo.getId())))
                .collect(Collectors.toList());
        if (infoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        return JSONObject.toJSONString(infoList);
    }

    /**
     * 更新模块信息
     *
     * @param baseModule        原始模块信息
     * @param newModuleInfoList 待更新信息
     * @return 处理完成后的信息
     */
    protected static String buildUpdateModuleInfoByList(String baseModule, List<PaperModuleInfo> newModuleInfoList) {
        List<PaperModuleInfo> moduleInfoList = analysisModuleStr(baseModule);
        if (moduleInfoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        List<PaperModuleInfo> infoList = moduleInfoList.stream()
                .map(moduleInfo -> {
                            Optional<PaperModuleInfo> info = newModuleInfoList.stream()
                                    .filter(newModuleInfo -> newModuleInfo.getId().equals(moduleInfo.getId()))
                                    .findAny();
                            if (info.isPresent()) {
                                moduleInfo.setName(info.get().getName());
                            }
                            return moduleInfo;
                        }
                )
                .collect(Collectors.toList());
        if (infoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        return JSONObject.toJSONString(infoList);
    }

    /**
     * 验证ID 是否存在
     *
     * @param moduleInfo 基础信息
     * @param moduleId   待校验ID
     * @return 存在返回true
     */
    protected static boolean validateModuleIdExit(String moduleInfo, int moduleId) {
        List<PaperModuleInfo> moduleInfoList = analysisModuleStr(moduleInfo);
        Optional<PaperModuleInfo> info = moduleInfoList.stream()
                .filter(paperModuleInfo -> paperModuleInfo.getId().equals(moduleId))
                .findAny();
        return info.isPresent();
    }

    public static String buildChangeModuleInfoByList(String baseModule, List<PaperModuleInfo> moduleInfoList, List<PaperQuestion> paperQuestions, Consumer<PaperQuestion> savePaperQuestion) {
        List<PaperModuleInfo> paperModuleInfos = analysisModuleStr(baseModule);
        if (moduleInfoList.size() == 0) {
            return StringUtils.EMPTY;
        }
        long count = paperModuleInfos.stream().map(PaperModuleInfo::getName).distinct().count();
        if(count - paperModuleInfos.size() < 0 ){
            throw new BizException(ErrorResult.create(10000011,"试卷中不允许存在同名的模块"));
        }
        Map<Integer, List<PaperQuestion>> moduleQuestions = paperQuestions.stream().collect(Collectors.groupingBy(PaperQuestion::getModuleId));
        for (PaperModuleInfo paperModuleInfo : paperModuleInfos) {
            Integer id = paperModuleInfo.getId();
            String name = paperModuleInfo.getName();
            Optional<PaperModuleInfo> first = moduleInfoList.stream().filter(i -> i.getName().equals(name)).findFirst();
            if(!first.isPresent()){
                continue;
            }
            PaperModuleInfo newModuleInfo = first.get();
            if(newModuleInfo.getId().equals(id)){
                continue;
            }
            //修改模块ID信息
            paperModuleInfo.setId(newModuleInfo.getId());
            //从ID变为newModuleInfo.getId()
            List<PaperQuestion> tempList = moduleQuestions.getOrDefault(id, Lists.newArrayList());
            if(CollectionUtils.isEmpty(tempList)){
                continue;
            }
            //修改绑题模块ID信息
            for (PaperQuestion paperQuestion : tempList) {
                paperQuestion.setModuleId(newModuleInfo.getId());
                savePaperQuestion.accept(paperQuestion);
            }
        }
        //按照模块ID重新进行排序
        paperModuleInfos.sort(Comparator.comparing(PaperModuleInfo::getId));
        return JSONObject.toJSONString(paperModuleInfos);
    }


    /**
     * 默认的模块信息
     */
    @AllArgsConstructor
    @Getter
    public enum ModuleEnum implements EnumCommon {
        FIRST_PART(1, "第一部分 常识判断"),
        SECOND_PART(2, "第二部分 言语理解与表达"),
        THIRD_PART(3, "第三部分 数量关系"),
        FOURTH_PART(4, "第四部分 判断推理"),
        FIFTH_PART(5, "第五部分 资料分析");

        private int key;
        private String value;

        @Override
        public int getKey() {
            return this.key;
        }

        @Override
        public String getValue() {
            return this.value;
        }

        /**
         * 是否是默认值
         */
        private static boolean isDefaultSubject(Long subjectId) {
            return DEFAULT_ADD_INFO_SUBJECT.stream().anyMatch(id -> subjectId.equals(id));
        }

        /**
         * 构建默认的模块信息
         */
        public static String buildDefaultModuleInfo(Long subjectId) {
            if (isDefaultSubject(subjectId)) {
                List<PaperModuleInfo> collect = Arrays.stream(ModuleEnum.values())
                        .map(moduleEnum ->
                                PaperModuleInfo.builder()
                                        .id(moduleEnum.getKey())
                                        .name(moduleEnum.getValue())
                                        .build()
                        )
                        .collect(Collectors.toList());
                return buildNewModuleStr(collect);
            }
            return StringUtils.EMPTY;
        }
    }
}
