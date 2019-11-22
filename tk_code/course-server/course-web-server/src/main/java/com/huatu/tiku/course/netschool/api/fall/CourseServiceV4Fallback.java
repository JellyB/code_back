package com.huatu.tiku.course.netschool.api.fall;

import com.huatu.common.utils.web.RequestUtil;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v4.CourseServiceV4;
import com.huatu.tiku.course.util.ResponseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.huatu.tiku.course.bean.NetSchoolResponse.DEFAULT_ERROR;

/**
 * @author hanchao
 * @date 2017/10/2 14:53
 */
@Component
public class CourseServiceV4Fallback implements CourseServiceV4 {

    @Override
    public NetSchoolResponse findRecordingList(Map<String, Object> params) {
        params.remove("username");
        String key = "_mock_recoding_listV4$"+ RequestUtil.getParamSign(params);
        NetSchoolResponse response = FallbackCacheHolder.get(key);
        if(response == null){
            return DEFAULT_ERROR;
        }else{
            return response;
        }
    }

    public void setRecordingList(Map<String,Object> params,NetSchoolResponse response){
        params.remove("username");
        String key = "_mock_recoding_listV4$"+ RequestUtil.getParamSign(params);
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key,response);
        }
    }
}
