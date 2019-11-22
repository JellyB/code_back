package com.huatu.tiku.teacher.controller.admin.download;

import com.google.common.collect.Maps;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperAssembly;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.download.v1.PdfWriteServiceV1;
import com.huatu.tiku.teacher.service.paper.PaperAssemblyService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqingpeng on 2018/11/8.
 */
@Slf4j
@RequestMapping("pdf")
@RestController
public class PdfController {

    @Autowired
    PdfWriteServiceV1 pdfWriteServiceV1;
    @Autowired
    PaperActivityService paperActivityService;
    @Autowired
    PaperEntityService paperEntityService;
    @Autowired
    PaperAssemblyService paperAssemblyService;

    /**
     * 下载试卷
     *
     * @param paperId    试卷ID
     * @param paperType  试卷类型 默认2活动卷
     * @param exportType 下载类型 默认2题目内容    1全部属性   3解析内容
     * @return
     */
    @PostMapping("paper")
    public Object downloadPaper(@RequestParam Long paperId,
                                @RequestParam(defaultValue = "2") int paperType,
                                @RequestParam(defaultValue = "2") int exportType,
                                @RequestParam(defaultValue = "true") boolean cacheFlag) throws BizException {
        QuestionElementEnum.QuestionFieldEnum questionFieldEnum = QuestionElementEnum.QuestionFieldEnum.create(exportType);
        Map mapData = Maps.newHashMap();
        mapData.put("paperId", paperId);
        PaperInfoEnum.TypeInfo typeInfo = PaperInfoEnum.TypeInfo.create(paperType);
        Long updateTime = 0L;

        Map cacheDown = pdfWriteServiceV1.getCacheDown(paperId, paperType, exportType);
        if (cacheFlag
                && null != cacheDown
                && null != MapUtils.getLong(cacheDown, "time")) {
            log.info("直接读取缓存：{}", JsonUtil.toJson(cacheDown));
            return cacheDown;
        }
        String fileUrl = null;
        try {
            fileUrl = pdfWriteServiceV1.download(paperId, typeInfo, questionFieldEnum, mapData);
            /**
             * 异常情况，不更新缓存,所以移到try内
             */
            updateTime = getUpdateTime(paperId, typeInfo);
            mapData.put("url", fileUrl);
            mapData.put("time", updateTime);
            mapData.put("updateTime", new Date(updateTime));
            pdfWriteServiceV1.saveDownCache(paperId, paperType, exportType, mapData);
            if (null != cacheDown && null != cacheDown.get("url")) {
                UploadFileUtil.getInstance().ftpDeleteFile(MapUtils.getString(cacheDown, "url"));
            }
        } catch (IOException e) {
            mapData.put("error", e.getMessage());
            e.printStackTrace();
            throw new BizException(ErrorResult.create(1000011, "试卷下载失败"));
        } catch (DocumentException e) {
            mapData.put("error", e.getMessage());
            e.printStackTrace();
            throw new BizException(ErrorResult.create(1000011, "试卷生成失败"));
        }
        return mapData;
    }


    /**
     * 获取更新时间
     */

    public Long getUpdateTime(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        Long updateTime = 0L;
        switch (typeInfo) {
            case SIMULATION:
                PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
                if (null != paperActivity) {
                    if (null == paperActivity.getGmtModify()) {
                        updateTime = paperActivity.getGmtCreate().getTime();
                    } else {
                        updateTime = paperActivity.getGmtModify().getTime();
                    }
                }
                break;
            case ENTITY:
                PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(paperId);
                if (null != paperEntity) {
                    if (null == paperEntity.getGmtModify()) {
                        updateTime = paperEntity.getGmtCreate().getTime();
                    } else {
                        updateTime = paperEntity.getGmtModify().getTime();
                    }
                }
            case ASSEMBLY:
                PaperAssembly paperAssembly = paperAssemblyService.selectByPrimaryKey(paperId);
                if (null != paperAssembly) {
                    if (null == paperAssembly.getModifierId()) {
                        updateTime = paperAssembly.getGmtCreate().getTime();
                    } else {
                        updateTime = paperAssembly.getGmtModify().getTime();
                    }
                }
                break;
        }
        return updateTime;
    }

    @DeleteMapping("clear")
    public Object clearCache(@RequestParam(defaultValue = "-1") Long paperId) {
        if (paperId > 0) {
            pdfWriteServiceV1.delDownCache(paperId,PaperInfoEnum.TypeInfo.SIMULATION.getCode());
        }else {
            List<PaperActivity> paperActivities = paperActivityService.selectAll();
            for (PaperActivity paperActivity : paperActivities) {
                pdfWriteServiceV1.delDownCache(paperActivity.getId(),PaperInfoEnum.TypeInfo.SIMULATION.getCode());
            }
        }
        return SuccessMessage.create("清空操作完成");
    }
}
