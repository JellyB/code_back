package com.huatu.tiku.teacher.controller.edu;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.match.MatchMetaService;
import com.huatu.tiku.teacher.service.paper.PaperSearchService;
import com.huatu.tiku.util.log.LogPrint;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.PaperType;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangyitian on 2019/4/24.
 */
@RestController
@Slf4j
@RequestMapping("eduapi")
public class PaperThirdController {

    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    private PaperSearchService paperSearchService;

    @Autowired
    private MatchMetaService matchMetaService;

    /**
     * 查询试题卷接口
     */
    @GetMapping("/paper/{paperId}/entity")
    public Object entityDetail(@PathVariable long paperId) {
        return paperSearchService.entityDetail(paperId);
    }

    /**
     * 试卷列表查询
     *
     * @param paperType
     * @param name
     * @return
     */
    @LogPrint
    @GetMapping("paper/list")
    public Object paperList(@RequestParam(defaultValue = "19") int paperType,
                            @RequestParam(defaultValue = "") String name) {
        Example example = new Example(PaperActivity.class);
        example.and().andEqualTo("type", paperType);
        if (StringUtils.isNotBlank(name)) {
            example.and().andLike("name", "%" + name.trim() + "%");
        }
        List<PaperActivity> paperActivities = paperActivityService.selectByExample(example);
        if (CollectionUtils.isEmpty(paperActivities)) {
            return Lists.newArrayList();
        }
        return paperActivities.stream().map(i -> {
            HashMap<Object, Object> result = Maps.newHashMap();
            result.put("id", i.getId());
            result.put("name", i.getName());
            result.put("score", i.getTotalScore());
            result.put("questionTotalScore", i.getQuestionTotalScore());
            result.put("time", i.getLimitTime());
            result.put("status", i.getBizStatus());
            result.put("modifyTime", i.getGmtModify() == null ? i.getGmtCreate().getTime() : i.getGmtModify().getTime());
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * 试卷列表查询
     *
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping("paper/{paperId}")
    public Object paperInfo(@PathVariable long paperId) {
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);

        HashMap<Object, Object> result = Maps.newHashMap();
        result.put("id", paperActivity.getId());
        result.put("name", paperActivity.getName());
        result.put("score", paperActivity.getTotalScore());
        result.put("questionTotalScore", paperActivity.getQuestionTotalScore());
        result.put("time", paperActivity.getLimitTime());
        result.put("status", paperActivity.getBizStatus());
        result.put("modifyTime", paperActivity.getGmtModify() == null ? paperActivity.getGmtCreate().getTime() : paperActivity.getGmtModify().getTime());
        return result;

    }

    @LogPrint
    @GetMapping("mock/list")
    public Object mockList(@RequestParam(defaultValue = "-1") long startTime,
                           @RequestParam(defaultValue = "-1") long endTime,
                           @RequestParam(defaultValue = "1") int subjectId,
                           @RequestParam(defaultValue = "-1") int tagId,
                           @RequestParam(defaultValue = "") String paperId,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "-1") int status,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) {
        PageInfo<List<HashMap<String, Object>>> mapPageInfo = PageHelper.startPage(page, size)
                .doSelectPageInfo(() -> paperActivityService.getActivityListForEdu(ActivityTypeAndStatus.ActivityTypeEnum.MATCH,
                        status, name.trim(), subjectId, startTime, endTime,tagId,paperId));
        return mapPageInfo;

    }

    @LogPrint
    @GetMapping("mock/scores")
    public Object mockList(@RequestParam int paperId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) {
        PageInfo mapPageInfo = PageHelper.startPage(page, size)
                .doSelectPageInfo(() -> matchMetaService.getMetaForEdu(paperId));
        List<MatchUserMeta> list = (List<MatchUserMeta>)mapPageInfo.getList();
        mapPageInfo.setList(matchMetaService.assemblingEduMeta(list));
        return mapPageInfo;

    }
}
