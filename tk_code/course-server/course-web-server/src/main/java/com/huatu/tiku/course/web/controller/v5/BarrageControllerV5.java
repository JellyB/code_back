package com.huatu.tiku.course.web.controller.v5;

import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.course.netschool.api.v5.BarrageServiceV5;
import com.huatu.tiku.course.service.v5.BarrageServiceV5Biz;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @author zhouwei
 * @Description: 弹幕接口
 * @create 2018-07-19 下午7:56
 **/
@RestController
@RequestMapping("barrages")
@ApiVersion("v5")
public class BarrageControllerV5 {
    @Autowired
    BarrageServiceV5 barrageServiceV5;

    @Autowired
    BarrageServiceV5Biz barrageServiceV5Biz;

    /**
     * 弹幕获取
     */
    @LocalMapParam(checkToken = true)
    @GetMapping
    public Object getBarrages(@RequestParam long classId,
                              @RequestParam long lessonId,
                              @RequestParam long videoNode,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int pageSize) throws BizException {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        return ResponseUtil.build(barrageServiceV5.barrageList(map));
    }

    /**
     * 弹幕提交
     */
    @LocalMapParam(checkToken = true)
    @PostMapping
    public Object addBarrage(
            @RequestParam long classId,
            @RequestParam String content,
            @RequestParam long lessonId,
            @RequestParam String videoNode,
            @RequestParam int background) {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        barrageServiceV5Biz.barrageAdd(map);
        return SuccessMessage.create();
    }
}
