package com.huatu.tiku.teacher.controller.util;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.teacher.service.match.MatchMetaService;
import com.huatu.tiku.util.file.FunFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 对原有的模考大赛数据做持久化操作
 * Created by huangqingpeng on 2018/10/16.
 */
@Slf4j
@RestController
@RequestMapping("match")
public class MatchMetaController {

    @Autowired
    MatchMetaService matchMetaService;

    private final static String EXCEL_TAIL_NAME = ".xls";

    /**
     * 持久化某一个模考大赛的数据
     *
     * @param matchId
     * @return
     */
    @PostMapping("single")
    public Object persistenceMatch(@RequestParam int matchId) {
        return matchMetaService.persistence(matchId);
    }


    @PostMapping("meta/enroll/{paperId}")
    public Object metaEnrollQuestion(@PathVariable int paperId) {

        return matchMetaService.metaEnroll(paperId);
    }

    @GetMapping("meta/enroll/batch")
    public void metaEnrollBatch(@RequestParam String ids, HttpServletResponse response) {
        if (StringUtils.isBlank(ids)) {
            throw new BizException(ErrorResult.create(10000123, "参数非法"));
        }
        List<Map> collect = Arrays.stream(ids.replaceAll("，", ",").split(","))
                .filter(NumberUtils::isDigits)
                .map(Integer::parseInt).map(matchMetaService::metaAllTime).collect(Collectors.toList());

        try {
            List<String> fileNames = Lists.newArrayList();
            for (Map fileMap : collect) {
                String fileName = MapUtils.getString(fileMap, "filePath");
                String name = new File(fileName).getParent() + File.separator + MapUtils.getString(fileMap, "name") + EXCEL_TAIL_NAME;
                FileUtils.copyFile(new File(fileName), new File(name));
                fileNames.add(MapUtils.getString(fileMap, "name"));
            }
            //生成压缩包
            String zipName = DateFormatUtil.NUMBER_FORMAT.format(new Date());
            boolean bln = FunFileUtils.zipFile(zipName, fileNames, EXCEL_TAIL_NAME, FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH);
            log.info("获取压缩包：{}", FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH + zipName + ".zip");
            File file = new File(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH + zipName + ".zip");
            FileInputStream fileInputStream = new FileInputStream(file);
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + zipName + ".zip");
            response.setHeader("Content-Type", "application/octet-stream");
//			FileCopyUtils.copy(is,new FileOutputStream(file));
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("meta/report")
    public void metaReport(@RequestParam int paperId, HttpServletResponse response) {
        File file = matchMetaService.metaResult(paperId);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + file.getName());
            response.setHeader("Content-Type", "application/octet-stream");
//			FileCopyUtils.copy(is,new FileOutputStream(file));
            FileCopyUtils.copy(fileInputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            file.deleteOnExit();
        }
    }
}
