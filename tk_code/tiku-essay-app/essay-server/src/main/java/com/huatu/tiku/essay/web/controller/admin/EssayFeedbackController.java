package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayFeedBackConstant;
import com.huatu.tiku.essay.service.EssayFeedbackService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.util.file.HtmlFileUtil;
import com.huatu.tiku.essay.vo.excel.ExcelView;
import com.huatu.tiku.essay.vo.excel.FeedBackExcelView;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.push.constant.SuggestFeedbackInfo;
import com.huatu.ztk.commons.JsonUtil;
import com.itextpdf.text.BadElementException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 意见反馈管理
 *
 * @Author ZhenYang
 * @Date Created in 2018/2/6 13:26
 * @Description
 */
@RestController
@Slf4j
@RequestMapping("/end/feedback")
public class EssayFeedbackController {

    @Autowired
    RestTemplate restTemplate;

    @Value("${feedback}")
    private String feedbackUrl;
    @Value("${reply}")
    private String replyUrl;
    @Value("${delFeedback}")
    private String delFeedback;
    @Value("${get_reply}")
    private String getReplyUrl;
    @Value("${solveFeedback}")
    private String solveFeedback;
    @Value("${feedBackReplyContentUrl}")
    private String feedBackReplyContentUrl;

    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    EssayFeedbackService essayFeedbackService;

    /**
     * 获取反馈列表
     *
     * @param type(1其他,4程序bug,5功能建议,6内容意见 7申论)
     * @param size
     * @param page
     * @return
     */
    @LogPrint
    @GetMapping
    public FeedbackDto getFeedback(@RequestParam(defaultValue = "7") Integer type,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "0") Integer status,
                                   @RequestParam(defaultValue = "-1") Long id,
                                   @RequestParam(defaultValue = "") String content,
                                   @RequestParam(defaultValue = "0") long start,
                                   @RequestParam(defaultValue = "0") long end) {
        //将前端查询状态 -》实际库中的状态
        int isSolve = EssayFeedBackConstant.EssayFeedBackSolveStatusEnum.ALL.getStatus();
        if (EssayFeedBackConstant.EssayFeedBackQueryStatusEnum.CLOSED.getStatus() == status) {
            status = EssayFeedBackConstant.EssayFeedBackQueryStatusEnum.ALL.getStatus();
            isSolve = EssayFeedBackConstant.EssayFeedBackSolveStatusEnum.CLOSED.getStatus();
        }
        String url = feedbackUrl + "?type=" + type
                + "&processed=" + status
                + "&size=" + size
                + "&page=" + page
                + "&isSolve=" + isSolve
                + "&content=" + content
                + "&id=" + id
                + "&start=" + start
                + "&end=" + end;
        log.info("url={}", url);
        ResponseEntity<FeedbackResult> o = restTemplate.getForEntity(url, FeedbackResult.class);
        FeedbackDto data = o.getBody().getData();
        if (null != data) {
            List<Feedback> feedbackList = data.getFeedbacks();
            if (CollectionUtils.isNotEmpty(feedbackList)) {
                feedbackList.forEach(feedback -> {
                    if (feedback.getReplyNum() > 0) {
                        feedback.setStatus(EssayFeedBackConstant.EssayFeedBackQueryStatusEnum.REPLYEFD.getStatus());
                    } else {
                        feedback.setStatus(EssayFeedBackConstant.EssayFeedBackQueryStatusEnum.INIT.getStatus());
                    }
                    //将库中的状态 -》查询状态
                    if (feedback.getIsSolve() == EssayFeedBackConstant.EssayFeedBackSolveStatusEnum.CLOSED.getStatus()) {
                        feedback.setStatus(EssayFeedBackConstant.EssayFeedBackQueryStatusEnum.CLOSED.getStatus());
                    }
                });
            }
        }
        return data;
    }


    /**
     * 回复用户反馈
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "reply", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object reply(@RequestBody UserReplyVO vo,
                        @RequestHeader String admin) throws IOException, BadElementException {
        int id = vo.getId();
        String content = vo.getContent();
        String title = vo.getTitle();

        content = htmlFileUtil.imgManage(content, id + "", 0);
        /**
         * 旧版本消息回复更新旧表
         */
        ResponseEntity<ResponseMsg> forEntity = restTemplate.postForEntity(replyUrl + "id=" + id + "&content=" + content + "&title=" + title + "&modifier=" + admin, null,
                ResponseMsg.class);
        ResponseMsg body = forEntity.getBody();
        if (null != body && null != body.getData()) {
            if (!"1".equals(body.getData().toString())) {
                log.warn("意见反馈回复失败。id：{}", id);
                throw new BizException(EssayErrors.REPLY_ERROR);
            }
        } else {
            log.warn("意见反馈回复失败。id：{}", id);
            throw new BizException(EssayErrors.REPLY_ERROR);
        }

        /**
         *新版本，消息回复实时推送
         */
        SuggestFeedbackInfo suggestFeedbackInfo = new SuggestFeedbackInfo();
        suggestFeedbackInfo.setBizId(vo.getId());
        suggestFeedbackInfo.setUserId(vo.getUserId());
        suggestFeedbackInfo.setCreateTime(System.currentTimeMillis());
        suggestFeedbackInfo.setSuggestContent(vo.getSuggestContent());
        suggestFeedbackInfo.setReplyTitle(title);
        suggestFeedbackInfo.setReplyContent(content);
        return essayFeedbackService.pushFeedbackReply(suggestFeedbackInfo);
    }


    /**
     * 查询用户反馈
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping(value = "reply", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getReply(int id) {
        ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(getReplyUrl + "id=" + id,
                ResponseMsg.class);

        ResponseMsg body = forEntity.getBody();
        List<UserMessageVO> data = new LinkedList<>();
        log.info(body + "");
        if (null != body && null != body.getData()) {
            data = (List<UserMessageVO>) body.getData();
            return data;
        } else {
            log.warn("查询意见反馈回复失败。id：{}", id);
            throw new BizException(EssayErrors.GET_REPLY_ERROR);
        }
    }


    /**
     * 删除意见反馈
     *
     * @param id
     */
    @LogPrint
    @DeleteMapping
    public void delFeedBack(Integer id) {
        log.info("进入删除意见反馈接口,意见反馈ID为:{}", id);
        restTemplate.delete(delFeedback + "id=" + id);
    }


    /**
     * 意见反馈置为已解决
     *
     * @param id
     * @param solve（1已处理 2未处理）
     */
    @LogPrint
    @GetMapping("solve")
    public void solveFeedback(Integer id,
                              Integer solve,
                              @RequestHeader String admin) {
        restTemplate.getForObject(solveFeedback + "?solve=" + solve + "&id=" + id + "&modifier=" + admin, String.class);
    }


    @LogPrint
    @GetMapping("push")
    public Object pushFeedbackReply(@RequestBody SuggestFeedbackInfo info) {
        return essayFeedbackService.pushFeedbackReply(info);
    }


    /**
     * 获取反馈列表
     *
     * @param type(1其他,4程序bug,5功能建议,6内容意见 7申论)
     * @param size
     * @param page
     * @return
     */
    @LogPrint
    @GetMapping("export")
    public ModelAndView exportFeedback(@RequestParam(defaultValue = "7") Integer type,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "0") Integer status,
                                       @RequestParam(defaultValue = "-1") Long id,
                                       @RequestParam(defaultValue = "") String content,
                                       @RequestParam(defaultValue = "0") long start,
                                       @RequestParam(defaultValue = "0") long end) {

        FeedbackDto data = getFeedback(type, size, page, status, id, content, start, end);
        Map<String, Object> map = new HashMap<String, Object>();
        //获取反馈内容
        List<HashMap> mapList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(data.getFeedbacks())) {
            List<Integer> feedBackIds = data.getFeedbacks().stream().map(Feedback::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(feedBackIds)) {
                log.info("参数是:{}", feedBackIds);
                ResponseMsg responseMsg = restTemplate.postForObject(feedBackReplyContentUrl, feedBackIds, ResponseMsg.class);
                mapList = (List<HashMap>) responseMsg.getData();
                //log.info("结果是:{}", JsonUtil.toJson(mapList));
            }
        }
        map.put("replyContents", mapList);
        map.put("members", data.getFeedbacks());
        map.put("name", "意见反馈数据");
        ExcelView excelView = new FeedBackExcelView();
        return new ModelAndView(excelView, map);
    }
}