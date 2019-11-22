package com.huatu.tiku.essay.web.controller.api.V2;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.EssayFileService;
import com.huatu.tiku.essay.service.v2.fileDownload.EssayFileServiceV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.vo.resp.FileResultVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/1
 * @描述
 */
@RestController
@RequestMapping("api/v2/file")
@Slf4j
public class ApiFileControllerV2 {

    @Autowired
    private EssayFileServiceV2 essayFileService;
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



}
