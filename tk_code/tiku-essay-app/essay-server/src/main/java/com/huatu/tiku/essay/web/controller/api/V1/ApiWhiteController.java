package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 白名单相关接口
 */
@RestController
@RequestMapping("api/v1/white")
@Slf4j
public class ApiWhiteController {

    @Autowired
    EssayPaperService essayPaperService;

    /*
     *   将试卷加入白名单列表
     */
    @LogPrint
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayUpdateVO file(@RequestParam(name = "paperId", defaultValue = "0")Long paperId) {

        log.info("paperId ：{}", paperId);
        EssayUpdateVO vo = essayPaperService.addWhitePaper(paperId);

        return vo;

    }



}
