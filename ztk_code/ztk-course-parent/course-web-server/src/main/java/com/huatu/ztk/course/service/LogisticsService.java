package com.huatu.ztk.course.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.course.bean.ExpressListResponse;
import com.huatu.ztk.course.bean.ExpressResult;
import com.huatu.ztk.course.bean.NetSchoolResponse;
import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.common.NetSchoolUrl;
import com.huatu.ztk.course.utils.Crypt3Des;
import com.huatu.ztk.course.utils.ParamsUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 物流
 * Created by linkang on 11/30/16.
 */

@Service
public class LogisticsService {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsService.class);


    @Autowired
    private CourseClient courseClient;

    /**
     *物流列表
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public Object getList(Map<String, Object> parameterMap) throws Exception{
        String params = ParamsUtils.makeParams(parameterMap);

        String httpData = courseClient.getHttpData(NetSchoolUrl.LOGISTICS + "?" + params, null);

        if (StringUtils.isBlank(httpData)) {
            return null;
        }

        NetSchoolResponse response = JsonUtil.toObject(httpData, NetSchoolResponse.class);
        ExpressListResponse expressListResponse = null;
        if (response.getCode() == NetSchoolConfig.SUCCESS_CODE) {
            expressListResponse = JsonUtil.toObject(httpData, ExpressListResponse.class);

            //将运单号解密
            expressListResponse.getData().stream().forEach(item->item.setExpressNo(Crypt3Des.decryptMode(item.getExpressNo())));
            return expressListResponse.getData();
        } else {
            throw new BizException(ErrorResult.create(response.getCode(), response.getMsg()));
        }
    }


    /**
     * 物流详情
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public ExpressResult getDetail(Map<String, Object> parameterMap) throws Exception{
        String params = ParamsUtils.makeParams(parameterMap);
        //加密
        String encryptparams = Crypt3Des.encryptMode(params);
        String url = NetSchoolUrl.LOGISTICS_QUERY + "?p=" + encryptparams;
        logger.info("url={}",url);
        String httpData = courseClient.getHttpData(url, null);
        logger.info("httpDate={}", httpData);

        ExpressResult result = JsonUtil.toObject(httpData, ExpressResult.class);

        if (result.getCode() == NetSchoolConfig.SUCCESS_CODE) {

            //com字段或route为空,说明没有物流信息
            if (result.getData() != null &&
                    (StringUtils.isBlank(result.getData().getCom())
                            || CollectionUtils.isEmpty(result.getData().getRoute()))) {

                result = ExpressResult.builder()
                        .code(-1)
                        .msg("")
                        .build();
            }
        }
        logger.info("express result={}",JsonUtil.toJson(result));
        return result;
    }

}
