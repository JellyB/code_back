package com.huatu.tiku.match.ztk.api.fallback;

import com.huatu.tiku.match.util.ZTKResponseUtil;
import com.huatu.tiku.match.ztk.api.TestApiService;
import org.springframework.stereotype.Component;

/**
 * 基础测试类
 * Created by lijun on 2018/10/12
 */
@Component
public class TestApiServiceFallback implements TestApiService {

    @Override
    public Object testGet() {
        return ZTKResponseUtil.SUCCESS_RESULT;
    }
}
