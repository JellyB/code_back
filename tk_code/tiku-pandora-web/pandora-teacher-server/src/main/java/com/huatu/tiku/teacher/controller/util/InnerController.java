package com.huatu.tiku.teacher.controller.util;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.download.QuestionErrorDownloadTask;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.tiku.teacher.dao.question.QuestionErrorDownloadTaskMapper;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import com.huatu.tiku.teacher.listener.QuestionErrorDownloadListener;
import com.huatu.tiku.teacher.service.InnerServiceImpl;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.huatu.tiku.constants.RabbitKeyConstant.SyncPaperQuestion;
import static com.huatu.tiku.constants.RabbitKeyConstant.SyncQuestionByPaper;

/**
 * 试卷迁移相关接口
 * Created by huangqingpeng on 2018/8/29.
 */
@Slf4j
@RestController
public class InnerController {

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Autowired
    BaseQuestionMapper baseQuestionMapper;
    @Autowired
    ImportService importService;
    @Autowired
    InnerServiceImpl innerService;

    @Autowired
    PaperEntityService paperEntityService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    QuestionErrorDownloadTaskMapper questionErrorDownloadTaskMapper;

    @Autowired
    QuestionErrorDownloadListener questionErrorDownloadListener;
    /**
     * 将所有的试题都刷新到mongo中(该接口内部使用，只使用特定逻辑，请谨慎使用)
     *
     * @param subject   需要同步的科目ID
     * @param bizStatus 统一pandora项目中的试题发布状态
     * @param user      统一pandora项目中的试题发布状态
     */
    @RequestMapping("sync/question")
    public void syncQuesitonBySubject(@RequestParam long subject,
                                      @RequestParam(defaultValue = "-1") int bizStatus,
                                      @RequestParam(defaultValue = "") String user) {
        if (StringUtils.isBlank(user)) {
            throw new BizException(ErrorResult.create(10000101, "该接口内部使用，只使用特定逻辑，请谨慎使用,务必输入特定的代号user:"));
        }
        long userId = System.currentTimeMillis() / 1000000;     //时间临时生成的用户编号
        log.info("sync/question by userId:{},user:{}", userId, user);
        BizStatusEnum bizStatusEnum = BizStatusEnum.create(bizStatus);
        Example example = new Example(BaseQuestion.class);
        example.and().andEqualTo("subjectId", subject);
        int page = 1;
        int size = 200;
        int total = 0;
        while (true) {
            try {
                PageInfo<BaseQuestion> pageInfo = PageHelper.startPage(page, size)
                        .doSelectPageInfo(
                                () -> baseQuestionMapper.selectByExample(example)
                        );
                List<BaseQuestion> baseQuestions = pageInfo.getList();
                if (CollectionUtils.isNotEmpty(baseQuestions)) {
                    List<Long> ids = baseQuestions.stream().map(BaseQuestion::getId).collect(Collectors.toList());
                    for (Long id : ids) {
                        BaseQuestion baseQuestion = baseQuestions.stream().filter(i -> i.getId().equals(id)).findFirst().get();
                        if (null != bizStatusEnum) {
                            baseQuestion.setBizStatus(bizStatusEnum.getValue());
                            baseQuestion.setModifierId(userId);
                        }
                        commonQuestionServiceV1.save(baseQuestion);
                        importService.sendQuestion2Mongo(id.intValue());
                        total++;
                    }
                }
                log.info("page={},total={}", page, pageInfo.getTotal());
                if (!pageInfo.isHasNextPage()) {
                    break;
                }
            } catch (Exception e) {
                log.error("分页查询处理失败：页数={},大小={}", page, size);
                e.printStackTrace();
            }
            page++;
        }
        log.info("sync/question，count={}", total);
    }

    @DeleteMapping("composite/{paperId}")
    public void deleteCompositeByPaperId(@PathVariable long paperId) {
        innerService.deleteByPaperId(paperId);
    }

    @RequestMapping("sync/question2")
    public void syncQuesitonByPaper() {
        List<PaperEntity> paperEntities = paperEntityService.selectAll();
        List<Integer> paperIds = paperEntities.stream().map(PaperEntity::getId).map(Long::intValue).collect(Collectors.toList());
        for (Integer paperId : paperIds) {
            Map map = Maps.newHashMap();
            map.put("id", paperId);
            rabbitTemplate.convertAndSend("", SyncQuestionByPaper, map);
        }
    }

    @RequestMapping("sync/bind")
    public void syncPaperQuesiton() {
        List<PaperEntity> paperEntities = paperEntityService.selectAll();
        List<Integer> paperIds = paperEntities.stream().map(PaperEntity::getId).map(Long::intValue).collect(Collectors.toList());
        for (Integer paperId : paperIds) {
            Map map = Maps.newHashMap();
            map.put("id", paperId);
            rabbitTemplate.convertAndSend("", SyncPaperQuestion, map);
        }
    }

    /**
     * 查询真实知识点题量
     *
     * @param subject
     * @return
     */
    @GetMapping("point/tree/{subject}")
    public Object getValidTree(@PathVariable int subject,
                               @RequestParam(defaultValue = "1") int flag) {
        return innerService.getValidKnowledgeTree(subject, flag, false);
    }


    /**
     * 同步ztk_question_new 到缓存---知识点题量
     *
     * @param subject
     * @return
     */
    @PostMapping("point/tree/{subject}")
    public Object overValidTree(@PathVariable int subject,
                                @RequestParam(defaultValue = "1") int flag) {
        return innerService.getValidKnowledgeTree(subject, flag, true);
    }

    /**
     * 根据科目或者知识点来锁定要处理的试题,修改之前选项中被删除掉的空格（言语理解中选项空格比较多，统一处理）
     *
     * @param subject
     * @param knowledgeId
     * @return
     */
    @PostMapping("sync/question/choices")
    public Object syncChoices(@RequestParam(defaultValue = "-1") long subject,
                              @RequestParam(defaultValue = "-1") long knowledgeId) {
        return innerService.SyncQuestionChoice(subject, knowledgeId);
    }

    /**
     * 单个试题执行同步逻辑
     *
     * @param questionId
     * @return
     */
    @PostMapping("sync/choice/{questionId}")
    public Object syncSingeChoice(@PathVariable long questionId) {
        int i = innerService.syncSingleChoice(questionId);
        if (i > 0) {
            return SuccessMessage.create();
        }
        return ErrorResult.create(1212121, "失败");
    }

    @PostMapping("download/{taskId}")
    public Object reDownload(@PathVariable long taskId){
        QuestionErrorDownloadTask questionErrorDownloadTask = questionErrorDownloadListener.selectByPrimaryKey(taskId);
        questionErrorDownloadListener.onMessage(questionErrorDownloadTask);
        return questionErrorDownloadTask;
    }
}
