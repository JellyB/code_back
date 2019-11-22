package com.huatu.tiku.essay.web.controller.admin.v2.goods;

import com.ht.base.start.security.service.UserOption;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.service.v2.goods.UserCorrectGoodsServiceV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.admin.correct.UserCorrectGoodsRewardV2VO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 赠送申论批改次数
 */
@RestController
@RequestMapping("end/v2/correct/reward")
@Slf4j
public class EssayRewardCorrectTimesControllerV2 {

    @Autowired
    private UserCorrectGoodsServiceV2 userCorrectGoodsServiceV2;

    @Autowired
    private UserCorrectGoodsService userCorrectGoodsService;

    @Autowired
    private UserOption userOption;

    /**
     * 赠送批改次数
     */
    @LogPrint
    @PostMapping(value = "batch")
    public List<String> reward(@RequestBody UserCorrectGoodsRewardV2VO vo) {
        if (StringUtils.isNotEmpty(vo.getUrl())) {
            List<String> accountList = userCorrectGoodsService.preHandleFile(vo.getUrl());

            vo.setAccountList(accountList);
        }

        vo.setCreator(userOption.getUserInfo().getUsername());

        List<String> errorList = userCorrectGoodsServiceV2.reward(vo);

        return errorList;
    }
}
