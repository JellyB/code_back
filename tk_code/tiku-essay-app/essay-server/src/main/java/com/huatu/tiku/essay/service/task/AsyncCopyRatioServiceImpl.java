package com.huatu.tiku.essay.service.task;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayLabelErrors;
import com.huatu.tiku.essay.service.EssayLabelService;
import com.huatu.tiku.essay.service.EssayMatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author zhouwei
 * @Description: 异步任务保存答题卡的抄袭度
 * @create 2017-12-27 下午1:22
 **/
@Slf4j
@Component
public class AsyncCopyRatioServiceImpl {


    @Autowired
    private EssayLabelService essayLabelService;


    /**
     * 异步任务保存答题卡的抄袭度
     * @param answerId
     * @param copyRatio
     */
    @Async
    public void saveCopyRatioToMysql(long answerId, double copyRatio) {
        int update = essayLabelService.saveCopyRatioToMysql(answerId, copyRatio);
        if(update != 1){
            throw new BizException(EssayLabelErrors.LABEL_UPDATE_ERROR);
        }

    }
}
