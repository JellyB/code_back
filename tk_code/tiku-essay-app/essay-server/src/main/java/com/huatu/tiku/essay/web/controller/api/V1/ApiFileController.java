package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.util.file.FunFileUtils;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.vo.resp.EssayHtmlPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.vo.resp.FileResultVO;
import com.huatu.tiku.essay.service.EssayFileService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.huatu.tiku.essay.util.file.FunFileUtils.PICTURE_SAVE_PATH;
import static com.huatu.tiku.essay.util.file.FunFileUtils.PICTURE_SAVE_URL;

/**
 * Created by x6 on 2017/12/1.
 */
@RestController
@RequestMapping("api/v1/file")
@Slf4j
public class ApiFileController {

    @Autowired
    private EssayFileService essayFileService;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    /**
     * 下载pdf
     * @param userSession
     * @param terminal
     * @param cv
     * @param questionBaseId
     * @param paperId
     * @param questionAnswerId
     * @param paperAnswerId
     * @return
     */
    @LogPrint
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public FileResultVO file(@Token UserSession userSession,
                             @RequestHeader int terminal,
                             @RequestHeader String cv,
                             @RequestParam(name = "questionBaseId", defaultValue = "0") Long questionBaseId,
                             @RequestParam(name = "paperId", defaultValue = "0") Long paperId,
                             @RequestParam(name = "questionAnswerId", defaultValue = "0") Long questionAnswerId,
                             @RequestParam(name = "paperAnswerId", defaultValue = "0") Long paperAnswerId) {

        int userId = userSession.getId();
        log.info("questionBaseId: {},paperId ：{}，questionAnswerId ：{}，paperAnswerId ：{}", questionBaseId, paperId, questionAnswerId, paperAnswerId);
        FileResultVO fileResultVO = essayFileService.saveFile(questionBaseId, paperId, questionAnswerId, paperAnswerId);

        return fileResultVO;

    }


    /**
     * 根据id获取估分试卷信息
     */
    @LogPrint
    @GetMapping(value = "paper", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayHtmlPaperVO paper(@RequestParam(name = "paperId", defaultValue = "0") Long paperId) {
        EssayHtmlPaperVO vo = essayFileService.createHtml(paperId);
        return vo;

    }


    /**
     * 估分试卷列表添加试题
     */
    @LogPrint
    @PostMapping(value = "paper", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object addPaper(@RequestParam(name = "paperId", defaultValue = "0") Long paperId) {
        return essayFileService.addPaper(paperId);
    }

    /**
     * 获取估分试卷html的试卷列表
     */
    @LogPrint
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list() {
        return essayFileService.getPaperList();
    }


    /**
     * 获取估分试卷html的title
     */
    @LogPrint
    @GetMapping(value = "pageTitle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayUpdateVO pageTitle() {
        return essayFileService.getPageTitle();
    }


    /**
     * 设置估分试卷html的title
     */
    @LogPrint
    @PostMapping(value = "pageTitle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayUpdateVO setPageTitle(@RequestParam(name = "value", defaultValue = "0") String value) {
        return essayFileService.setPageTitle(value);
    }


    /**
     * 系统当前时间
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "time", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getCurrentTime() {
        Map map = new HashMap<String, Long>();
        map.put("currentTime", System.currentTimeMillis());
        return map;
    }


    /**
     * 上传文件
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "upload", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        //读取文件后缀
        int indexOf = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(indexOf, originalFilename.length());
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + suffix;

        try {
            InputStream inputStream = file.getInputStream();
            uploadFileUtil.ftpUploadFileInputStream(inputStream, fileName, PICTURE_SAVE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return PICTURE_SAVE_URL + fileName;
    }


}
