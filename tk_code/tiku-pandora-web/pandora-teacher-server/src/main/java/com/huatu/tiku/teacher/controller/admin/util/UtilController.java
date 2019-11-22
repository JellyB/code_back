package com.huatu.tiku.teacher.controller.admin.util;

import com.google.common.collect.Maps;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.common.Area;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.service.SyncPaperService;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.knowledge.SyncKnowledgeService;
import com.huatu.tiku.teacher.service.question.SyncQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.SyncSubjectService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.util.log.LogPrint;
import com.huatu.ztk.paper.bean.Paper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by huangqp on 2018\6\25 0025.
 */
@Slf4j
@RestController
@RequestMapping(value = "util")
public class UtilController {

    @Autowired
    SyncQuestionService syncQuestionService;
    @Autowired
    SyncPaperService syncPaperService;
    @Autowired
    TeacherSubjectService subjectService;
    @Autowired
    AreaService areaService;
    @Autowired
    ImportService importService;
    @Autowired
    SyncSubjectService syncSubjectService;
    @Autowired
    SyncKnowledgeService syncKnowledgeService;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    /**
     * 同步mongo(ztk_question数据到mysql)
     *
     * @param id
     * @return
     */
    @LogPrint
    @PostMapping("question")
    public Object syncQuestion(@RequestParam Integer id) {
        //先删除原有的数据，在重新迁移
        commonQuestionServiceV1.deleteQuestionPhysical(id);
        //同步数据
        syncQuestionService.syncQuestion(id);
        return SuccessMessage.create("同步成功");
    }

    /**
     * 同步mongo(ztk_paper和ztk_match数据到mysql)
     *
     * @param id
     * @return
     */
    @LogPrint
    @PostMapping("paper")
    public Object syncPaper(@RequestParam Integer id) {
        syncPaperService.syncPaper(id);
        return SuccessMessage.create("同步试卷成功");
    }

    /**
     * 查询mongo库，试卷信息
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping("paper/{id}")
    public Object findMongoPaper(@PathVariable Integer id) throws BizException {
        Paper paper = syncPaperService.findPaperById(id);
        if (null == paper) {
            return paper;
        }
        Map mapData = Maps.newHashMap();
        mapData.put("id", paper.getId());
        mapData.put("name", paper.getName());
        Subject subject = subjectService.selectById(new Long(paper.getCatgory()));
        if (subject != null) {
            mapData.put("subject", subject.getName());
        } else {
            mapData.put("subject", paper.getCatgory());
        }
        mapData.put("year", paper.getYear());
        Area area = areaService.selectAll().stream().filter(i -> i.getId().intValue() == paper.getArea()).findAny().orElse(null);
        mapData.put("area", area == null ? paper.getArea() : area.getName());
        mapData.put("sorce", paper.getScore());
        mapData.put("limitTime", paper.getTime() / 60);
        return mapData;
    }

    /**
     * 同步mongo(ztk_paper和ztk_match数据到mysql),不迁移试卷试题绑定关系，只迁移实体试卷和活动卷数据过来
     *
     * @param id
     * @return
     */
    @LogPrint
    @PostMapping("paper/{id}")
    public Object syncPaperSingle(@PathVariable Integer id) {
        syncPaperService.syncPaperSingle(id);
        return SuccessMessage.create("同步试卷成功");
    }

    /**
     * 同步试题信息，试题跟试卷模块的关系(试题正常迁移逻辑)
     *
     * @param questionId
     * @param paperId
     * @param sort
     * @param module
     * @return
     */
    @LogPrint
    @PostMapping("question/{questionId}")
    public Object syncQuestionWithPaper(@PathVariable Integer questionId,
                                        @RequestParam Long paperId,
                                        @RequestParam(defaultValue = "-1") Integer sort,
                                        @RequestParam String module) {
        //mongo->mysql
        syncQuestionService.syncQuestion(questionId, paperId, sort, module);
        //mysql->mongo
        importService.importQuestion(questionId);
        return SuccessMessage.create("同步成功");
    }

    /**
     * 同步试题信息，试题跟试卷模块的关系(试题正常迁移逻辑)
     *
     * @param questionId
     * @param paperId
     * @param sort
     * @param module
     * @param id         重题id
     * @return
     */
    @LogPrint
    @PostMapping("duplicate/{questionId}")
    public Object duplicateQuestionWithPaper(@PathVariable Integer questionId,
                                             @RequestParam Long paperId,
                                             @RequestParam(defaultValue = "-1") Integer sort,
                                             @RequestParam String module,
                                             @RequestParam Long id) {
        syncQuestionService.duplicateQuestion(questionId, paperId, sort, module, id);
        //mysql->mongo
        importService.importQuestion(questionId);
        return SuccessMessage.create("同步成功");
    }

    /**
     * 查询试卷详细信息（模块信息+试题信息）
     *
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping("paper/detail")
    public Object findPaperDetail(@RequestParam Integer paperId) {
        try {
            Object paperDetail = syncPaperService.findPaperDetail(paperId);
            return paperDetail;
        } catch (Exception e) {
            log.error("pand_util_paper_detail error，paperId={}", paperId);
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 根据mongo试题id,查询mysql库中的数据是否有重复数据
     *
     * @param questionId
     * @param subjectFlag 0全部科目1只确定同一科目的试题
     * @param yearFlag    0全部年份1只确定同一年份的试题
     * @return
     */
    @LogPrint
    @GetMapping("duplicate/list")
    public Object findDuplicateQuestion(@RequestParam Integer questionId,
                                        @RequestParam(defaultValue = "1") Integer subjectFlag,
                                        @RequestParam(defaultValue = "1") Integer yearFlag) {
        return syncQuestionService.findDuplicateQuestion(questionId, subjectFlag, yearFlag);
    }

    /**
     * 将原题库考试类型和科目表的备份数据整理插入新的科目表中
     *
     * @param name
     * @return
     */
    @LogPrint
    @PostMapping("sync/subject")
    public Object syncSubject(@RequestParam String name) {
        return syncSubjectService.syncQuestionByCatGory(name);
    }

    /**
     * 将原题库考试类型和科目表的备份数据整理插入新的科目表中
     *
     * @param id
     * @return
     */
    @LogPrint
    @PostMapping("sync/subject/{id}")
    public Object syncSubject(@PathVariable Integer id) {
        return syncSubjectService.syncQuestionByCatGoryId(id);
    }

    /**
     * 将已有的科目下的知识点复制过来
     *
     * @return
     */
    @LogPrint
    @PostMapping("sync/knowledge")
    public Object syncKnowledge() {
        return syncKnowledgeService.syncKnowledge();
    }


    /**
     * 将一张试题卷同步为活动卷
     */
    @LogPrint
    @GetMapping("syncPaperEntityToPaperActivity")
    public void syncPaperEntityToPaperActivity() {
        syncPaperService.syncPaperEntityToPaperActivity();
    }
}

