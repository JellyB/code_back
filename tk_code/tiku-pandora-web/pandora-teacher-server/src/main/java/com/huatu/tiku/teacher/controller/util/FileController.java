package com.huatu.tiku.teacher.controller.util;

import com.huatu.tiku.request.FileRequest;
import com.huatu.tiku.teacher.service.common.FileService;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 文件管理
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    FileService fileService;
    /**
     * 图片上传接口
     *
     * @param file
     * @return
     */
    @LogPrint
    @PostMapping(value = "/upload")
    public Object upload(@RequestParam("file") MultipartFile file,
                         HttpServletResponse response
    ) {
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        return fileService.upload(file);
    }


    /**
     * 富文本编辑器内容保存
     *
     * @param file
     * @return
     */
    @LogPrint
    @PostMapping(value = "")
    public Object upload(@RequestBody FileRequest file) {
        return fileService.save(file);
    }


}
