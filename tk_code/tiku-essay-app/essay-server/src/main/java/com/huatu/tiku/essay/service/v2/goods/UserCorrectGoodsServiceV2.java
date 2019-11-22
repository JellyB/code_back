package com.huatu.tiku.essay.service.v2.goods;

import com.huatu.tiku.essay.vo.admin.correct.UserCorrectGoodsRewardV2VO;

import java.util.List;

public interface UserCorrectGoodsServiceV2 {

    List<String> reward(UserCorrectGoodsRewardV2VO vo);
}
