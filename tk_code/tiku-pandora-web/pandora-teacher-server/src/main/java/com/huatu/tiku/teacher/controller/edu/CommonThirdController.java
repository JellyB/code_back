package com.huatu.tiku.teacher.controller.edu;

import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by huangyitian on 2019/4/24.
 */
@RestController
@Slf4j
@RequestMapping("eduapi/common")
public class CommonThirdController {

    @Autowired
    KnowledgeService knowledgeService;
    /**
     * 根据科目查询知识点树
     *
     * @param subject
     * @return
     */
    @LogPrint
    @GetMapping("knowledge/tree")
    public List<KnowledgeVO> getKnowledgeTree1BySubject(@RequestParam(defaultValue = "1") Long subject) {
        return knowledgeService.showKnowledgeTreeBySubject(subject, false);
    }
}
