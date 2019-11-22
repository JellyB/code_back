package com.huatu.tiku.match.service.impl.v1.tag;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.match.common.MatchConfig;
import com.huatu.tiku.match.common.Tag;
import com.huatu.tiku.match.dao.manual.pandora.SubjectMapper;
import com.huatu.tiku.match.service.v1.tag.TagService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述：tag service
 *
 * @author biguodong
 * Create time 2018-10-24 上午10:07
 **/

@Service
@Slf4j
public class TagServiceImpl implements TagService, Observer {

    @Autowired
    SubjectMapper subjectMapper;

    public static final Cache<Integer, String> CATEGORY_NAME_CACHE = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();

    private static final List<Tag> tagList = Lists.newArrayList();
    private static final List<HashMap> subjectList = Lists.newArrayList();      //一个科目保存一个相关的hash对象
    /**
     * 科目名称简化
     */
    private static final Map<String, String> reduceSubjectNameMap = Maps.newHashMap();

    static {
        reduceSubjectNameMap.put("综合素质（小学）", "综素");
        reduceSubjectNameMap.put("综合素质（中学）", "综素");
        reduceSubjectNameMap.put("教育教学知识与能力", "教知");
        reduceSubjectNameMap.put("教育知识与能力", "教知");
        reduceSubjectNameMap.put("职测-联考A类", "职测A类");
        reduceSubjectNameMap.put("职测-联考B类", "职测B类");
        reduceSubjectNameMap.put("职测-联考C类", "职测C类");
        reduceSubjectNameMap.put("职测-非联考", "职测非联考");
        //标签数据通过pandora接口生成(/pand/match/tag)
        String tagString = "[{\"subject\":1,\"name\":\"2020省考行测\",\"channel\":0,\"id\":25,\"category\":1,\"subjectName\":\"行测\"},{\"subject\":1,\"name\":\"2020国考行测\",\"channel\":0,\"id\":24,\"category\":1,\"subjectName\":\"行测\"},{\"subject\":1,\"name\":\"2019国考行测\",\"channel\":0,\"id\":1,\"category\":1,\"subjectName\":\"行测\"},{\"subject\":1,\"name\":\"2019省考行测\",\"channel\":0,\"id\":2,\"category\":1,\"subjectName\":\"行测\"},{\"subject\":14,\"name\":\"2020申论模考\",\"channel\":1,\"id\":26,\"category\":1,\"subjectName\":\"申论\"},{\"subject\":14,\"name\":\"2019申论模考\",\"channel\":1,\"id\":3,\"category\":1,\"subjectName\":\"申论\"},{\"subject\":2,\"name\":\"2019年公基\",\"channel\":0,\"id\":12,\"category\":3,\"subjectName\":\"公基\"},{\"subject\":2,\"name\":\"2018年公基\",\"channel\":0,\"id\":1,\"category\":3,\"subjectName\":\"公基\"},{\"subject\":100100262,\"name\":\"2019教招教综\",\"channel\":0,\"id\":2,\"category\":200100045,\"subjectName\":\"教育综合知识\"},{\"subject\":100100262,\"name\":\"2019特岗教综\",\"channel\":0,\"id\":3,\"category\":200100045,\"subjectName\":\"教育综合知识\"},{\"subject\":100100173,\"name\":\"2020年招警机考\",\"channel\":0,\"id\":28,\"category\":200100047,\"subjectName\":\"行测\"},{\"subject\":100100173,\"name\":\"2018年招警机考\",\"channel\":0,\"id\":1,\"category\":200100047,\"subjectName\":\"行测\"},{\"subject\":100100175,\"name\":\"全真模考\",\"channel\":0,\"id\":27,\"category\":200100047,\"subjectName\":\"公安专业科目\"},{\"subject\":200100049,\"name\":\"2019综素-小学\",\"channel\":0,\"id\":15,\"category\":200100048,\"subjectName\":\"综合素质（小学）\"},{\"subject\":200100051,\"name\":\"2019教知-小学\",\"channel\":0,\"id\":16,\"category\":200100048,\"subjectName\":\"教育教学知识与能力\"},{\"subject\":200100050,\"name\":\"2019综素-中学\",\"channel\":0,\"id\":17,\"category\":200100053,\"subjectName\":\"综合素质（中学）\"},{\"subject\":200100052,\"name\":\"2019教知-中学\",\"channel\":0,\"id\":18,\"category\":200100053,\"subjectName\":\"教育知识与能力\"},{\"subject\":200100054,\"name\":\"2019年职测A\",\"channel\":0,\"id\":19,\"category\":3,\"subjectName\":\"联考职测-A类\"},{\"subject\":200100055,\"name\":\"2019年职测B\",\"channel\":0,\"id\":20,\"category\":3,\"subjectName\":\"联考职测-B类\"},{\"subject\":200100056,\"name\":\"2019年职测C\",\"channel\":0,\"id\":21,\"category\":3,\"subjectName\":\"联考职测-C类\"},{\"subject\":200100057,\"name\":\"2019年职测非联考\",\"channel\":0,\"id\":22,\"category\":3,\"subjectName\":\"非联考职测\"},{\"subject\":200100063,\"name\":\"2019年军队文职\",\"channel\":0,\"id\":23,\"category\":200100060,\"subjectName\":\"军队文职\"},{\"subject\":420,\"name\":\"2019金融\",\"channel\":0,\"id\":30,\"category\":200100002,\"subjectName\":\"金融\"}]";

        List<HashMap> hashMaps = JsonUtil.toList(tagString, HashMap.class);
        for (HashMap hashMap : hashMaps) {
            tagList.add(Tag.builder().id(MapUtils.getInteger(hashMap, "id"))
                    .name(MapUtils.getString(hashMap, "name"))
                    .flag(MapUtils.getInteger(hashMap, "channel"))
                    .subject(MapUtils.getInteger(hashMap, "subject"))
                    .category(MapUtils.getInteger(hashMap, "category"))
                    .build());
            Optional<HashMap> any = subjectList.stream().filter(i -> MapUtils.getInteger(i, "subject").equals(MapUtils.getInteger(hashMap, "subject")))
                    .findAny();
            if (any.isPresent()) {
                continue;
            }
            subjectList.add(hashMap);
        }
    }

    @Autowired
    private MatchConfig matchConfig;


    @PostConstruct
    public void AfterConstructed() {
        firstBuild();
        matchConfig.addObserver(this);
    }


    /**
     * 获取tag信息
     *
     * @param subject
     * @return
     */
    @Override
    public List<Tag> getTags(int subject) {
        //非申论科目使用科目ID做筛选
        if (subject != 14) {
            List<Tag> list = tagList.stream()
                    .filter(tag -> tag.getSubject() == subject)
                    .collect(Collectors.toList());
            //找到兄弟科目的标签，排在后面
            if (CollectionUtils.isNotEmpty(list)) {
                Tag tag = list.get(0);
                int category = tag.getCategory();
                List<Tag> collect = tagList.stream().filter(i -> i.getSubject() != subject)
                        .filter(i -> i.getCategory() == category)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    list.addAll(collect);
                    filterJIKAO(list, subject);
                }
            }
            return list;

        }
        //申论科目使用flag  = 1 做筛选，意味着只保留调用申论接口的标签
        List<Tag> list = tagList.stream()
                .filter(tag -> tag.getFlag() == 1)
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 包含招警机考的标签筛选
     *
     * @param list
     * @param subject
     */
    private void filterJIKAO(List<Tag> list, int subject) {
        final int jiKao = 100100173;
        boolean hasJiKao = list.stream().filter(i -> i.getSubject() == jiKao).findAny().isPresent();
        if (hasJiKao) {
            if (subject == jiKao) {
                list.removeIf(i -> i.getSubject() != subject);
            } else {
                list.removeIf(i -> i.getSubject() == jiKao);
            }
        }
    }

    @Override
    public Object getSubjectList(int subject) {
        Function<HashMap, HashMap> trans = (map -> {
            HashMap<Object, Object> temp = Maps.newHashMap();
            temp.put("id", MapUtils.getInteger(map, "subject"));
            String subjectName = MapUtils.getString(map, "subjectName");
            if (null != reduceSubjectNameMap.get(subjectName)) {
                subjectName = reduceSubjectNameMap.get(subjectName);
            }
            temp.put("name", subjectName);
            return temp;
        });
        ArrayList<HashMap> list = Lists.newArrayList();
        Optional<HashMap> any = subjectList.stream().filter(i -> MapUtils.getInteger(i, "subject").intValue() == subject)
                .findAny();
        if (any.isPresent()) {
            if (subject == 14) {      //申论逻辑
                HashMap apply = trans.apply(any.get());
                apply.put("flag", 1);
                list.add(apply);
            } else {              //非申论逻辑
                HashMap hashMap = any.get();
                log.info("subjectList 科目list是:{}", subjectList);
                List<HashMap> collect = subjectList.stream().filter(i -> MapUtils.getInteger(i, "category").equals(MapUtils.getInteger(hashMap, "category")))
                        .collect(Collectors.toList());
                for (HashMap map : collect) {
                    HashMap apply = trans.apply(map);
                    if (MapUtils.getInteger(map, "subject").equals(MapUtils.getInteger(hashMap, "subject"))) {
                        apply.put("flag", 1);            //flag = 1 标识当前科目，flag = 0 标识其他科目
                    } else {
                        apply.put("flag", 0);
                    }
                    list.add(apply);
                }
            }
        }
        return list;
    }

    @Override
    public Object getMatchCategory(int terminal) {
        if (CollectionUtils.isEmpty(subjectList)) {
            return Lists.newArrayList();
        }
        Map<Integer, String> categoryInfo = getCategoryInfo();
        if (terminal != TerminalType.PC) {
            subjectList.removeIf(i -> 100100173 == MapUtils.getInteger(i, "subject").intValue());
        }
        Map<Integer, List<HashMap>> tagTree = subjectList.stream().collect(Collectors.groupingBy(i -> MapUtils.getInteger(i, "category")));
        return filtrateCategoryTree(tagTree, categoryInfo);
    }

    /**
     * 组装数据
     *
     * @param tagTree
     * @param categoryInfo
     * @return
     */
    private Object filtrateCategoryTree(Map<Integer, List<HashMap>> tagTree, Map<Integer, String> categoryInfo) {
        //获取科目列表信息
        Function<List<HashMap>, List<HashMap>> getSubject = (tags -> {
            Set<Integer> subjectIds = Sets.newHashSet();
            List<HashMap> list = Lists.newArrayList();
            for (HashMap tag : tags) {
                Integer subject = MapUtils.getInteger(tag, "subject");
                if (subjectIds.contains(subject)) {
                    continue;
                }
                String subjectName = MapUtils.getString(tag, "subjectName");
                if (null != reduceSubjectNameMap.get(subjectName)) {
                    subjectName = reduceSubjectNameMap.get(subjectName);
                }
                HashMap<Object, Object> map = Maps.newHashMap();
                map.put("id", subject);
                map.put("name", subjectName);
                list.add(map);
                subjectIds.add(subject);
            }
            return list;
        });
        return tagTree.entrySet().stream()
                .map(entry -> {
                    Integer key = entry.getKey();
                    String categoryName = categoryInfo.getOrDefault(key, "未知考试类型");
                    List<HashMap> value = entry.getValue();
                    HashMap<Object, Object> map = Maps.newHashMap();
                    map.put("id", key);
                    map.put("name", categoryName);
                    map.put("children", getSubject.apply(value));
                    return map;
                }).sorted(Comparator.comparing(map -> MapUtils.getInteger(map, "id"))).collect(Collectors.toList());
    }

    /**
     * 考试类型信息获取
     *
     * @return
     */
    private Map<Integer, String> getCategoryInfo() {
        ConcurrentMap<Integer, String> map = CATEGORY_NAME_CACHE.asMap();
        if (null != map && map.size() > 0) {
            return map;
        }
        List<Subject> subjectList = subjectMapper.selectAll();
        final ConcurrentMap<Integer, String> temp = Maps.newConcurrentMap();
        if (CollectionUtils.isEmpty(subjectList)) {
            return temp;
        }
        subjectList.stream().filter(i -> i.getStatus() == 1)
                .filter(i -> i.getLevel() == 1)
                .forEach(i -> temp.put(i.getId().intValue(), i.getName()));
        CATEGORY_NAME_CACHE.putAll(temp);
        return temp;
    }


    /**
     * 观察到配置更新
     *
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        log.info("update new tags:{}", arg);
        tagList.clear();
        String tagsStr = String.valueOf(arg);
        tagList.addAll(JSONObject.parseArray(tagsStr, Tag.class));
    }

    /**
     * 首次加载初始化
     */
    private void firstBuild() {
        log.info("firstBuild tags:{}", matchConfig.getTags());
        String tagsStr = matchConfig.getTags();
        if (StringUtils.isBlank(tagsStr)) {
            return;
        }
        tagList.addAll(JSONObject.parseArray(tagsStr, Tag.class));
    }
}


