package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.manager.AreaManager;
import com.huatu.tiku.essay.service.EssayAreaService;
import com.huatu.tiku.essay.vo.resp.AreaResp;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/17
 */
@Service
public class EssayAreaServiceImpl implements EssayAreaService {

    @Resource
    private AreaManager areaManager;

    @Override
    public List<AreaResp> fetchAreaTree() {
        return areaManager.fetchAreaTree();
    }
}
