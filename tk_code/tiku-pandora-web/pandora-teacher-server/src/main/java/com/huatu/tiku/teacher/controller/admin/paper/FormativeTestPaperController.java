package com.huatu.tiku.teacher.controller.admin.paper;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.log.LogPrint;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.common.PaperType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/20
 * @描述 阶段性测试试卷查询列表, php课程大纲绑定阶段测试试卷，需要提供列表数据
 */

@Slf4j
@RestController
@RequestMapping("formativeTestPaper")
public class FormativeTestPaperController {


    @Autowired
    private PaperActivityService paperActivityService;

    @Autowired
    private TeacherSubjectService subjectService;

    /**
     * 阶段测试列表查询（php使用）
     *
     * @param subjectId  默认是行测
     * @param category   默认是考试类别是公务员
     * @param bizStatus
     * @param year
     * @param name
     * @param searchType 搜索的类型 1 名称搜索;2 ID搜索
     * @return
     */
    @LogPrint
    @GetMapping("list")
    public Object list(
            @RequestParam(defaultValue = "1") Long category,
            @RequestParam(defaultValue = "-1") Integer subjectId,
            @RequestParam(defaultValue = "-1") Integer bizStatus,
            @RequestParam(defaultValue = "-1") Integer year,
            @RequestParam(defaultValue = "") String areaIds,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String startTime,
            @RequestParam(defaultValue = "") String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "1") int searchType) {

        final List<Integer> subjectIds = new ArrayList<>();
        if (BaseInfo.isNotDefaultSearchValue(subjectId)) {
            subjectIds.add(subjectId);
        } else {
            final List<Integer> childrenId = subjectService.findChildrenId(Long.valueOf(category), 1);
            if (CollectionUtils.isNotEmpty(childrenId)) {
                subjectIds.addAll(childrenId);
            }
        }

        Integer type = PaperType.FORMATIVE_TEST_ESTIMATE;
        PageInfo<List<HashMap<String, Object>>> mapPageInfo = PageHelper.startPage(page, size)
                .doSelectPageInfo(() ->
                        paperActivityService.getActivityList(type, bizStatus, year,
                                areaIds, name, subjectIds, startTime, endTime, searchType)
                );
        return mapPageInfo;
    }

    /**
     * @param courseId   课程ID
     * @param syllabusId 课程大纲ID
     * @param paperIds   活动ID，开始时间是否有效
     * @return
     */
    @PostMapping("saveRelation")
    public Object saveRelation(@RequestParam int courseId,
                               @RequestParam int syllabusId,
                               @RequestParam List<Long> paperIds) {


        paperActivityService.saveFormativePaper(courseId, syllabusId, paperIds);
        return SuccessMessage.create("操作成功!");
    }


    /**
     * 阶段测试查询活动状态（php使用）
     *
     * @param
     * @return
     */
    @LogPrint
    @GetMapping("activityState")
    public Object getFormativeState(@RequestParam Integer activityType) {

        Map<Integer, String> mapList = Arrays.stream(ActivityTypeAndStatus.ActivityStatusEnum.values())
                .collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue()));

        List<HashMap<Integer, String>> resultList = new ArrayList<>();
        for (Map.Entry<Integer, String> state : mapList.entrySet()) {
            HashMap resultMap = new HashMap();
            resultMap.put("key", state.getKey());
            resultMap.put("value", state.getValue());
            resultList.add(resultMap);
        }
        HashMap allMap = new HashMap();
        allMap.put("key", "-1");
        allMap.put("value", "全部");
        resultList.add(allMap);
        return resultList;
    }

    /**
     * 根据考试类别查询科目
     *
     * @param categoryId
     * @return
     */
    @GetMapping("subjectInfo")
    public Object getSubjectInfoByCategoryId(@RequestParam Integer categoryId) {
        List<Subject> children = subjectService.findChildren(Long.valueOf(categoryId), 1);
        List<HashMap> mapInfo = getMapInfo(children);
        return mapInfo;
    }


    @GetMapping("categoryInfo")
    public Object getCategory(@RequestParam(defaultValue = "1") Integer level,
                              @RequestParam(defaultValue = "0") Integer parent) {
        List<Subject> allCategory = subjectService.findAllCategory(level, parent);
        allCategory = allCategory.stream().sorted(Comparator.comparing(Subject::getId)).collect(Collectors.toList());
        List<HashMap> mapInfo = getMapInfo(allCategory);
        return mapInfo;

    }


    public List<HashMap> getMapInfo(List<Subject> subjects) {
        List<HashMap> mapList = new ArrayList<>();
        HashMap map = new HashMap();
        map.put("key", -1);
        map.put("value", "全部");
        mapList.add(map);

        if (CollectionUtils.isNotEmpty(subjects)) {
            subjects.stream().forEach(subject -> {
                HashMap subjectMap = new HashMap();
                subjectMap.put("key", subject.getId());
                subjectMap.put("value", subject.getName());
                mapList.add(subjectMap);
            });
        }
        return mapList;


    }
}
