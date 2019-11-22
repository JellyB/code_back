package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.service.task.AsyncFileSaveServiceImpl;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.admin.UserCorrectGoodsRewardVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 赠送申论批改次数
 *
 */
@RestController
@RequestMapping("api/v1/correct/reward")
@Slf4j
public class EssayRewardCorrectTimesController {

    @Autowired
    UserCorrectGoodsService userCorrectGoodsService;
    @Autowired
    private AsyncFileSaveServiceImpl asyncFileSaveService;

    /**
     * 赠送批改次数
     */
    @LogPrint
    @PostMapping(value = "batch")
    public List<String> reward(@RequestBody UserCorrectGoodsRewardVO vo,
                               @RequestHeader String admin) {

        vo.setCreator(admin);
        if(StringUtils.isNotEmpty(vo.getUrl())){
            List<String> accountList = userCorrectGoodsService.preHandleFile(vo.getUrl());
            vo.setAccountList(accountList);
        }
        List<String> errorList = userCorrectGoodsService.reward(vo);
        System.out.println("赠送批改异常，账号信息：" + errorList.toString());
        return errorList;
    }


    /**
     * 操作记录
     */
    @LogPrint
    @GetMapping(value = "list")
    public Object rewardList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int pageSize) {
        return userCorrectGoodsService.getRewardList(page, pageSize);

    }

//    /**
//     *临时代码:0323模考异常赠送批改次数
//     */
//    @LogPrint
//    @GetMapping(value = "mock")
//    public Object rewardMock() {
//        return userCorrectGoodsService.mockReward();
//
//    }

}
