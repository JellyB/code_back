package com.huatu.tiku.teacher.controller.admin.activity;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.activity.EstimateService;
import com.huatu.tiku.teacher.util.dingTalkNotice.DingTalkNoticeUtil;
import com.huatu.tiku.teacher.util.dingTalkNotice.DingTextVo;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.common.PaperType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.huatu.tiku.service.impl.BaseServiceImpl.throwBizException;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/19
 * @描述 处理精准估分礼包信息
 */
@Slf4j
@RestController
@RequestMapping("estimate")
public class EstimateController {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtil.class);

    @Autowired
    EstimateService estimateService;

    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    DingTalkNoticeUtil dingTalkNotice;

    /**
     * 新增/修改 活动礼包配置
     *
     * @param estimate
     * @return
     */
    @PostMapping
    public Object saveEstimateInfo(@RequestBody Estimate estimate) {
        //TODO 传入文件，生成图片链接
        if (null == estimate) {
            throwBizException("参数不能为空");
        }
        Long id = estimate.getActivityId();
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(id);
        if (null == paperActivity) {
            throwBizException("活动不存在");
        }
        Estimate estimateInfo = estimateService.getEstimateInfo(id);
        if (null != estimateInfo) {
            estimate.setId(estimateInfo.getId());
        }

        logger.info("礼包信息是:{},科目信息：{}", JsonUtil.toJson(estimate), estimate.getType());
        switch (paperActivity.getType()) {
            case PaperType.MATCH:
                estimate.setType(AnswerCardType.MATCH);
                break;
            case PaperType.ESTIMATE_PAPER:
                estimate.setType(AnswerCardType.ESTIMATE);
                break;
            default:
                estimate.setType(paperActivity.getType());
                break;
        }
        String estimateKey = "estimate_" + id.toString();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        Long result = hashOperations.delete(estimateKey, id.toString());
        logger.info("result is :{}", result);

        if (null == estimate.getId()) {
            //配置礼包,发送钉钉提醒,修改
            StringBuffer content = new StringBuffer();
            String message = content.append("试卷:").append(id).append(",名称:").append(paperActivity.getName())
                    .append(",配置了礼包活动,请注意开启礼包开关～")
                    .toString();

            DingTextVo dingTextVo = DingTextVo.builder().content(message).atAll(false).mobiles("17611401891").build();
            dingTalkNotice.textNotice(dingTextVo);
        }
        estimateService.save(estimate);

        return SuccessMessage.create("操作成功!");
    }

    /**
     * 根据活动ID查询活动礼包信息
     *
     * @param activityId 活动ID
     * @return
     */
    @GetMapping
    public Estimate getEstimateInfo(@RequestParam("activityId") Long activityId) {
        if (null == activityId) {
            throwBizException("参数不能为空");
        }
        return estimateService.getEstimateInfo(activityId);
    }


}
