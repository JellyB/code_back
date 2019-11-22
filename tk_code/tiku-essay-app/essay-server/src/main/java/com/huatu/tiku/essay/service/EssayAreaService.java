package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.manager.AreaManager;
import com.huatu.tiku.essay.vo.resp.AreaResp;

import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/17
 */
public interface EssayAreaService {

    List<AreaResp> fetchAreaTree();

}
