package com.huatu.ztk.course.service;

import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.course.common.NetSchoolSydwUrl;
import com.huatu.ztk.course.common.NetSchoolUrl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by linkang on 11/30/16.
 */

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private CourseClient courseClient;


    private static final String RECEIPT_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
//    private static final String RECEIPT_URL = "https://buy.itunes.apple.com/verifyReceipt";

    /**
     * 创建订单
     * @param parameterMap
     * @param basicUrl
     * @param needDecrypt data是否需要解密
     * @return
     * @throws Exception
     */
    public Object createOrder(Map<String, Object> parameterMap, String basicUrl, boolean needDecrypt) throws Exception {
        logger.info("create order,params={}",parameterMap);
        return courseClient.getJsonByEncryptJsonParams(parameterMap, basicUrl, needDecrypt);
    }


    /**
     * 赠送课程
     * @param parameterMap
     * @throws Exception
     */
    public void sendFreeCourse(Map<String, Object> parameterMap,int catgory) throws Exception{
        Object result = null;
        try {

            String url = catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolUrl.LOGIN_COMMENT_COURSE : NetSchoolSydwUrl.SYDW_LOGIN_COURSE;
            result = courseClient.getJsonByEncryptJsonParams(parameterMap, url, false);

        } catch (Exception e) {
            logger.info("send free course fail,msg={},params={}",e.getMessage(),JsonUtil.toJson(parameterMap));
        }
        logger.info("send free course,result={}", JsonUtil.toJson(result));
    }

    /**
     * 苹果内购验证
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public void receipt(Map<String, Object> parameterMap) throws Exception{
        logger.info("parameterMap={}",JsonUtil.toJson(parameterMap));

        String data = courseClient.postHttpDataByStringEntity(RECEIPT_URL, parameterMap);

        if (StringUtils.isNoneBlank(data)) {
            Map map = JsonUtil.toMap(data);

            Object status = map.get("status");

            int statusCode = Integer.valueOf(status.toString());

            if (statusCode != 0) {
                throw new BizException(ErrorResult.create(statusCode, "验证失败"));
            }
        }
    }

    /**
     * 内购回调
     * @param parameterMap
     * @return
     * @throws Exception
     */
    public Object iosPayVerify(Map<String, Object> parameterMap) throws Exception{
        logger.info("parameterMap={}",JsonUtil.toJson(parameterMap));
        String result = courseClient.postHttpDataByFormEntity(NetSchoolUrl.IOS_PAY_VERIFY, parameterMap);

        if (result.equals("success")) {
            return SuccessMessage.create("订单状态更新成功");
        } else if (result.startsWith("fail")) {
            logger.info("iosPayVerify fail,result=" + result);
            throw new BizException(ErrorResult.create(-1, "更新订单状态失败"));
        }

        return null;
    }
}
