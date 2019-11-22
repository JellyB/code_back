package com.huatu.tiku.teacher.controller.admin.knowledge;

import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.request.knowledge.InsertKnowledgeReq;
import com.huatu.tiku.request.knowledge.UpdateKnowledgeReq;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * 知识点管理
 *
 * @author zhouwei
 * @create 2018-04-23 下午4:40
 **/
@RestController
@RequestMapping("knowledge")
@Slf4j
public class KnowledgeController {
    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    TeacherSubjectService subjectService;
    /**
     * 根据科目查询知识点树
     *
     * @param subject 科目id
     * @return 知识点树（无试题数量）
     */
    @LogPrint
    @GetMapping("/tree")
    public Object getKnowledgeTreeBySubject(@RequestParam(defaultValue = "1") Long subject) {
        return knowledgeService.treeBySubject(subject);
    }

    /**
     * 根据知识点id获取知识点详细信息
     *
     * @param ids
     * @return
     */
    @LogPrint
    @GetMapping("detail")
    public Object getKnowledgeNameByIds(@RequestParam(defaultValue = "-1") List<Long> ids) {
        return knowledgeService.getKnowledgeInfoByIds(ids);
    }

    /**
     * 科目+学段展示
     * @return
     */
    @LogPrint
    @GetMapping("subject")
    public Object getSubjectList(@RequestParam(defaultValue = "-1") Long id) {
        return subjectService.treeForKnowledge(id);
    }

    /**
     * 新增知识点
     *
     * @param insertKnowledgeReq
     * @return
     */
    @LogPrint
    @PostMapping("")
    public Object createKnowledge(@Valid @RequestBody InsertKnowledgeReq insertKnowledgeReq, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BizException(ErrorResult.create(1000001, bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        long id = knowledgeService.insertKnowledge(insertKnowledgeReq);
        HashMap<Object, Object> result = Maps.newHashMap();
        result.put("knowledgeId",id);
        return result;
    }

    /**
     * 修改知识点
     *
     * @param updateKnowledgeReq
     * @return
     */
    @LogPrint
    @PutMapping("")
    public Object updateKnowledge(@Valid @RequestBody UpdateKnowledgeReq updateKnowledgeReq,BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new BizException(ErrorResult.create(1000001, bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        long id = knowledgeService.updateKnowledge(updateKnowledgeReq);
        HashMap<Object, Object> result = Maps.newHashMap();
        result.put("knowledgeId",id);
        return result;
    }

    /**
     * 删除知识点
     *
     * @param id
     * @return
     */
    @LogPrint
    @DeleteMapping("")
    public Object deleteKnowledge(@RequestParam Long id) {
        int i = knowledgeService.deleteKnowledge(id);
        HashMap<Object, Object> result = Maps.newHashMap();
        result.put("knowledgeId",id);
        return result;
    }
}
