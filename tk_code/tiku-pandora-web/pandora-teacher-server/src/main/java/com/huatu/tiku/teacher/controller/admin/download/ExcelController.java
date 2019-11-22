package com.huatu.tiku.teacher.controller.admin.download;

import com.google.common.collect.Maps;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.service.download.ExcelHandleService;
import com.huatu.ztk.commons.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/3/19.
 */
@Slf4j
@RequestMapping("excel")
@RestController
public class ExcelController {

    @Autowired
    ExcelHandleService excelHandleService;
    /**
     * 批量下载试卷(试卷批量下载并获得zip链接)
     *
     * @param paperIds   试卷IDs
     * @param paperType  试卷类型 默认实体卷
     * @return
     */
    @PostMapping("papers/file")
    public Object downloadPaperForZip(@RequestParam String paperIds,
                                      @RequestParam(defaultValue = "1") int paperType) throws BizException {
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(paperType);
        //试卷ID
        List<Long> ids = Arrays.stream(paperIds.replaceAll("，", ",").split(",")).map(Long::new).collect(Collectors.toList());
        String fileUrl = excelHandleService.downloads(ids,typeInfo);
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("url", fileUrl);
        return mapData;
    }
}
