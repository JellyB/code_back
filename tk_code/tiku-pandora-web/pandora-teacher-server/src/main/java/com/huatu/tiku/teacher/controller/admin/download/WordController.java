package com.huatu.tiku.teacher.controller.admin.download;

import com.google.common.collect.Maps;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.teacher.service.download.v1.WordWriteServiceV1;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * word 数据导出
 * Created by huangqingpeng on 2018/8/15.
 */
@RestController
@RequestMapping("word")
@Slf4j
public class WordController {

    @Autowired
    WordWriteServiceV1 wordWriteServiceV1;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 下载试卷
     *
     * @param paperId    试卷ID
     * @param paperType  试卷类型 默认实体卷
     * @param exportType 下载类型 默认1全部属性  2题目内容 3解析内容
     * @return
     */
    @PostMapping("paper")
    public Object downloadPaper(@RequestParam Long paperId,
                                @RequestParam(defaultValue = "1") int paperType,
                                @RequestParam(defaultValue = "1") int exportType) {
        QuestionElementEnum.QuestionFieldEnum questionFieldEnum = QuestionElementEnum.QuestionFieldEnum.create(exportType);
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(paperType);
        String fileUrl = wordWriteServiceV1.download(paperId, typeInfo, questionFieldEnum);
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("url", fileUrl);
        return mapData;
    }

    /**
     * 批量下载试卷
     *
     * @param paperIds   试卷IDs
     * @param paperType  试卷类型 默认实体卷
     * @param exportType 下载类型 默认1全部属性  2题目内容 3解析内容
     * @return
     */
    @PostMapping("papers")
    public Object downloadPaper(@RequestParam String paperIds,
                                @RequestParam(defaultValue = "1") int paperType,
                                @RequestParam(defaultValue = "1") int exportType,
                                @RequestParam(defaultValue = "") String moduleName,
                                @RequestParam(defaultValue = "false") boolean duplicateFlag) throws BizException {
        QuestionElementEnum.QuestionFieldEnum questionFieldEnum = QuestionElementEnum.QuestionFieldEnum.create(exportType);
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(paperType);
        //试卷ID
        List<Long> ids = Arrays.stream(paperIds.replaceAll("，", ",").split(",")).map(Long::new).collect(Collectors.toList());
        if (duplicateFlag) {
            String key = "duplicate_set";       //批量下载，去重逻辑兼容
            redisTemplate.delete(key);
        }
        String fileUrl = wordWriteServiceV1.downLoadList(ids, typeInfo, questionFieldEnum, moduleName, duplicateFlag);
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("url", fileUrl);
        return mapData;
    }


    /**
     * 联考试卷按照模块分组导出
     *
     * @param paperIds   试卷IDs
     * @param paperType  试卷类型 默认实体卷
     * @param exportType 下载类型 默认1全部属性  2题目内容 3解析内容
     * @return
     */
    @PostMapping("activity")
    public Object downloadActivity(@RequestParam String paperIds,
                                   @RequestParam(defaultValue = "1") int paperType,
                                   @RequestParam(defaultValue = "1") int exportType) throws BizException {
        QuestionElementEnum.QuestionFieldEnum questionFieldEnum = QuestionElementEnum.QuestionFieldEnum.create(exportType);
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(paperType);
        //试卷ID
        List<Long> ids = Arrays.stream(paperIds.replaceAll("，", ",").split(",")).map(Long::new).collect(Collectors.toList());
        String fileUrl = wordWriteServiceV1.downloadGroupByModule(ids, typeInfo, questionFieldEnum);
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("url", fileUrl);
        return mapData;
    }

    @PostMapping("knowledge")
    public Object downloadByPoint(@RequestParam Long pointId,
                                  @RequestParam(defaultValue = "1") int exportType,
                                  @RequestParam(defaultValue = "") String ids,
                                  @RequestParam(defaultValue = "50") int size) throws BizException {
        QuestionElementEnum.QuestionFieldEnum questionFieldEnum = QuestionElementEnum.QuestionFieldEnum.create(exportType);
        String fileUrl = "";
        if(StringUtils.isNotBlank(ids)){
            fileUrl = wordWriteServiceV1.downloadByQuestionWithKnowledge(pointId,ids,questionFieldEnum);
        }else{
            fileUrl = wordWriteServiceV1.downloadTopPoint(pointId, size, questionFieldEnum);
        }
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("url", fileUrl);
        return mapData;
    }
}
