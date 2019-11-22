package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.constant.error.EssayLabelErrors;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.service.EssayLabelService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayLabelCheckVO;
import com.huatu.tiku.essay.vo.resp.EssayLabelInfoVO;
import com.huatu.tiku.essay.vo.resp.LabelSmallVO;
import com.huatu.tiku.essay.vo.resp.QuestionAnswerLabelListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标注相关管理
 *
 * @Author zhaoxi
 */
@RestController
@Slf4j
@RequestMapping("/end/label")
public class EssayLabelController {


    @Autowired
    private EssayLabelService essayLabelService;

    /**
     * 查询批注列表
     */
    @LogPrint
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<List<QuestionAnswerLabelListVO>> findList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                              @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                                              @RequestParam(name = "areaId", defaultValue = "-1") long areaId,
                                                              @RequestParam(name = "year", defaultValue = "") String year,
                                                              @RequestParam(name = "examScoreMin", defaultValue = "-1") double examScoreMin,
                                                              @RequestParam(name = "wordNumMin", defaultValue = "-1") int wordNumMin,
                                                              @RequestParam(name = "subScoreRatioMin", defaultValue = "-1") double subScoreRatioMin,
                                                              @RequestParam(name = "examScoreMax", defaultValue = "-1") double examScoreMax,
                                                              @RequestParam(name = "wordNumMax", defaultValue = "-1") int wordNumMax,
                                                              @RequestParam(name = "subScoreRatioMax", defaultValue = "-1") double subScoreRatioMax,
                                                              @RequestParam(name = "labelStatus", defaultValue = "-1") int labelStatus,
                                                              @RequestParam(name = "questionId", defaultValue = "-1") long questionId,
                                                              @RequestParam(name = "stem", defaultValue = "") String stem,
                                                              @RequestParam(name = "answerId", defaultValue = "-1") long answerId,
                                                              @RequestHeader String admin) {
        PageUtil<List<QuestionAnswerLabelListVO>> pageUtil = essayLabelService.findByConditions(areaId, year, examScoreMin, wordNumMin, subScoreRatioMin, examScoreMax, wordNumMax, subScoreRatioMax, labelStatus, questionId, stem, answerId, page, pageSize, admin);
        List<QuestionAnswerLabelListVO> result = pageUtil.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(questionAnswerLabelListVO -> {
                if (3 == questionAnswerLabelListVO.getLabelStatus() &&
                        CollectionUtils.isNotEmpty(questionAnswerLabelListVO.getLabelList()) &&
                        questionAnswerLabelListVO.getLabelList().size() == 2) {
                    questionAnswerLabelListVO.getLabelList().add(1, LabelSmallVO.builder().build());
                }
            });
        }
        return pageUtil;
    }


    /**
     * 开始批注
     */
    @LogPrint
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map startLabel(@RequestParam(name = "answerId", defaultValue = "0") long answerId,
                          @RequestParam(name = "isFinal", defaultValue = "0") int isFinal,
                          @RequestHeader(name = "admin", defaultValue = "") String admin) {
        return essayLabelService.startLabel(answerId, admin, isFinal);

    }


    /**
     * 校验是否有标题批注&&结构批注
     */
    @LogPrint
    @GetMapping(value = "check/{totalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayLabelCheckVO checkFlag(@PathVariable long totalId) {

        return essayLabelService.checkFlag(totalId);
    }


    /**
     * 保存总体批注（代表批注完成）
     */
    @LogPrint
    @PostMapping(value = "total", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayLabelTotal saveTotalLabel(@RequestBody EssayLabelTotal total,
                                          @RequestHeader(name = "admin", defaultValue = "") String admin) {

        //校验参数
        if (null == total) {
            log.error("请求参数异常");
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        } else if (total.getId() <= 0) {
            log.error("请求参数异常，总体批注id错误，totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.LABEL_TOTAL_ID_ERROR);
        } else if (null == total.getTotalScore()) {
            log.error("综合批注分数不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_TOTAL_SCORE);
        } else if (StringUtils.isEmpty(total.getWordNumScore())) {
            log.error("综合批注字数得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_WORD_NUM_SCORE);
        } else if (StringUtils.isEmpty(total.getParagraphScore())) {
            log.error("综合批注分段得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_PARAGRAPH_SCORE);
        } else if (StringUtils.isEmpty(total.getTitleScore())) {
            log.error("综合批注标题得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_TITLE_SCORE);
        } else if (StringUtils.isEmpty(total.getThesisScore())) {
            log.error("综合批注论点得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_THESIS_SCORE);
        } else if (StringUtils.isEmpty(total.getStructScore())) {
            log.error("综合结构得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_STRUCTURE_SCORE);
        } else if (StringUtils.isEmpty(total.getSentenceScore())) {
            log.error("综合语言得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_SENTENCE_SCORE);
        } else if (StringUtils.isEmpty(total.getEvidenceScore())) {
            log.error("综合批注论据得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_EVIDENCE_SCORE);
        } else if (StringUtils.isEmpty(total.getLiteraryScore())) {
            log.error("综合批注文采得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_LITERARY_SCORE);
        } else if (StringUtils.isEmpty(total.getThoughtScore())) {
            log.error("综合批注思想性得分不能为空。totalId：{}", total.getId());
            throw new BizException(EssayLabelErrors.ERROR_THOUGHTFUL_SCORE);
        }
        total.setCreator(admin);

        return essayLabelService.saveTotalLabel(total, admin);
    }


    /**
     * 保存详细批注
     */
    @LogPrint
    @PostMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public long saveDetailLabel(@RequestBody EssayLabelDetail detail,
                                @RequestHeader(name = "admin", defaultValue = "") String admin) {
        //校验参数
        if (null == detail) {
            log.error("请求参数异常");
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        if (StringUtils.isEmpty(detail.getTitleScore()) && StringUtils.isEmpty(detail.getEvidenceScore()) && StringUtils.isEmpty(detail.getThesisScore())
                && StringUtils.isEmpty(detail.getStructScore()) && StringUtils.isEmpty(detail.getSentenceScore()) && StringUtils.isEmpty(detail.getLiteraryScore())
                && StringUtils.isEmpty(detail.getThoughtScore())) {
            log.error("请求参数异常");
            throw new BizException(EssayLabelErrors.NOTHING_LABEL_CONTENT);
        }

        if (StringUtils.isNotEmpty(detail.getThesisScore())) {
            String[] split = detail.getThesisScore().split("-");
            if (split.length != 2 && !"4".equals(split[0])) {
                log.error("论点必须关联标答论点");
                throw new BizException(EssayLabelErrors.THESIS_LABEL_CONNECT_ERROR);
            }

        }
        detail.setCreator(admin);
        return essayLabelService.saveDetailLabel(detail);
    }


    /**
     * 获取下一篇
     */
    @LogPrint
    @GetMapping(value = "next", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public QuestionAnswerLabelListVO getNext(@RequestHeader(name = "admin", defaultValue = "") String admin,
                                             @RequestParam(name = "areaId", defaultValue = "-1") long areaId,
                                             @RequestParam(name = "year", defaultValue = "") String year,
                                             @RequestParam(name = "examScoreMin", defaultValue = "-1") double examScoreMin,
                                             @RequestParam(name = "wordNumMin", defaultValue = "-1") int wordNumMin,
                                             @RequestParam(name = "subScoreRatioMin", defaultValue = "-1") double subScoreRatioMin,
                                             @RequestParam(name = "examScoreMax", defaultValue = "-1") double examScoreMax,
                                             @RequestParam(name = "wordNumMax", defaultValue = "-1") int wordNumMax,
                                             @RequestParam(name = "subScoreRatioMax", defaultValue = "-1") double subScoreRatioMax,
                                             @RequestParam(name = "labelStatus", defaultValue = "-1") int labelStatus,
                                             @RequestParam(name = "questionId", defaultValue = "-1") long questionId,
                                             @RequestParam(name = "stem", defaultValue = "") String stem,
                                             @RequestParam(name = "answerId", defaultValue = "-1") long answerId) {
        return essayLabelService.getNext(admin, areaId, year, examScoreMin, wordNumMin, subScoreRatioMin, examScoreMax, wordNumMax, subScoreRatioMax, labelStatus, questionId, stem, answerId);
    }


    /**
     * 查询批注详情
     */
    @LogPrint
    @GetMapping(value = "info/{totalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayLabelInfoVO getInfo(@PathVariable long totalId) {
        return essayLabelService.getInfo(totalId);
    }


    /**
     * 根据批注id查询所有的论点批注
     */
    @LogPrint
    @GetMapping(value = "thesis/{totalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayLabelDetail> getThesisList(@PathVariable long totalId) {

        return essayLabelService.getThesisList(totalId);
    }


    /**
     * 使用现有批注作为终审批注
     */
    @LogPrint
    @GetMapping(value = "copy/{totalId}/{finalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map copy(@PathVariable long totalId,
                    @PathVariable long finalId,
                    @RequestHeader String admin) {
        essayLabelService.copy(totalId, finalId, admin);
        HashMap<String, Long> map = new HashMap<>();
        map.put("totalId", finalId);
        return map;
    }


    /**
     * 查询批注详情
     */
    @LogPrint
    @GetMapping(value = "detail/{detailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayLabelDetail getDetailInfo(@PathVariable long detailId) {
        return essayLabelService.getDetailInfo(detailId);
    }

    /**
     * 删除批注
     */
    @LogPrint
    @DeleteMapping(value = "{totalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map delLabel(@PathVariable long totalId) {
        int delLabel = essayLabelService.delLabel(totalId);
        HashMap<String, Boolean> map = new HashMap<>();
        boolean flag = (delLabel == 1);
        map.put("flag", flag);
        return map;
    }

    /**
     * 放弃批注
     */
    @LogPrint
    @DeleteMapping(value = "giveup/{totalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map giveUpLabel(@PathVariable long totalId,
                           @RequestHeader String admin,
                           @RequestParam Integer type) {
        int giveUpLabel = essayLabelService.giveUpLabel(totalId, admin, type);
        HashMap<String, Boolean> map = new HashMap<>();
        boolean flag = (giveUpLabel == 1);
        map.put("flag", flag);
        return map;
    }


    /**
     * 删除详细批注
     */
    @LogPrint
    @DeleteMapping(value = "detail/{detailId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map delDetailLabel(@PathVariable long detailId) {
        int delLabel = essayLabelService.delDetailLabel(detailId);
        HashMap<String, Boolean> map = new HashMap<>();
        boolean flag = (delLabel == 1);
        map.put("flag", flag);
        return map;
    }


    /**
     * 重新批注
     */
    @LogPrint
    @PostMapping(value = "restart/{finalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map restart(@PathVariable long finalId,
                       @RequestHeader String admin) {
        int delLabel = essayLabelService.restart(finalId, admin);
        HashMap<String, Boolean> map = new HashMap<>();
        boolean flag = (delLabel == 1);
        map.put("flag", flag);
        return map;
    }


    /**
     * 终审人员列表
     */
    @LogPrint
    @GetMapping(value = "final/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object finalList() {
        return essayLabelService.getFinalLabelTeacher();
    }


    /**
     * 添加终审人员
     */
    @LogPrint
    @PutMapping(value = "final/teacher", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map finalList(@RequestParam(name = "teacher", defaultValue = "0") String teacher) {
        essayLabelService.addFinalLabelTeacher(teacher);
        HashMap<String, String> map = new HashMap<>();
        map.put("teacher", teacher);
        return map;

    }


    /**
     * 添加VIP批注教师
     */
    @LogPrint
    @PutMapping(value = "vip/teacher", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map addVIP(@RequestParam(name = "teacher", defaultValue = "0") String teacher) {
        essayLabelService.addVIPlLabelTeacher(teacher);
        HashMap<String, String> map = new HashMap<>();
        map.put("teacher", teacher);
        return map;
    }


    /**
     * 移除VIP批注教师
     */
    @LogPrint
    @DeleteMapping(value = "vip/teacher", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map remVIP(@RequestParam(name = "teacher", defaultValue = "0") String teacher) {
        long remove = essayLabelService.delVIPlLabelTeacher(teacher);
        HashMap<String, Object> map = new HashMap<>();
        map.put("remove", remove);
        return map;
    }

    /**
     * VIP人员列表
     */
    @LogPrint
    @GetMapping(value = "vip/list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object vipList() {
        return essayLabelService.getVIPLabelTeacher();
    }

    /**
     * 导出终审批注数据分布情况
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "/final/excel")

    public ModelAndView getFinalExcel(@RequestParam long questionId,
                                      @RequestParam long start,
                                      @RequestParam long end,
                                      @RequestHeader(name = "admin", defaultValue = "") String admin) {
        return essayLabelService.getFinalExcel(questionId, start, end);
    }

    /**
     * 查询终审批注数据分布情况
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "/final/chart")
    public Object getFinalChart(@RequestParam long questionId,
                                @RequestParam long start,
                                @RequestParam long end,
                                @RequestHeader(name = "admin", defaultValue = "") String admin) {
        return essayLabelService.getFinalChart(questionId, start, end);
    }

    /**
     * 导出老师工作量
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "/teacher/excel")
    public ModelAndView getTeacherExcel(@RequestParam long start,
                                        @RequestParam long end,
                                        @RequestHeader(name = "admin", defaultValue = "") String admin) {
        return essayLabelService.getTeacherExcel(start, end);
    }


    /**
     * 导出终审批注的数据
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "/final/xml")
    public ModelAndView getFinalXmlExcel(@RequestParam Long labelId,
                                         @RequestHeader(name = "admin", defaultValue = "") String admin) {

        return essayLabelService.getFinalXmlExcel(labelId);
    }

    /**
     * 修复前端没有传标题的旧数据
     */
    @LogPrint
    @GetMapping(value = "/fix/title")
    public String fixTitle() {
        essayLabelService.fixTitle();
        return "处理成功";
    }


    /**
     * 校验是否可编辑
     */
    @LogPrint
    @GetMapping(value = "/check/edit/{totalId}")
    public Boolean checkEdit(@PathVariable long totalId) {
        return essayLabelService.checkEdit(totalId);
    }
}
