package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.service.EssayImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author zhaoxi
 * @Description 试题算法自动录入
 */
@RestController
@RequestMapping("/end/import")
@Slf4j
public class EssayImportController {

    @Autowired
    private EssayImportService essayImportService;

    /**
     * 自动导入试题算法
     *
     * @param file
     * @param questionDetailId
     * @return
     */
    @PostMapping("rule")
    public Object importRule(@RequestParam("file") MultipartFile file,
                             @RequestParam(defaultValue = "-1") Long questionDetailId) {
        return essayImportService.readQuestionRule(file, questionDetailId);
    }
}
