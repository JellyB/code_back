package com.huatu.tiku.essay.web.controller.api.v4;

import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.dto.ApiPHPCourseGoodsOrderDto;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.OrderCreateVO;
import com.huatu.tiku.essay.vo.resp.correct.UserCorrectTimesVO;
import com.huatu.tiku.essay.vo.resp.goods.GoodsOrderDetailVO;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author huangqingpeng
 * @title: ApiUserCorrectGoodsControllerV4
 * @description: 用户批改次数商品数据维护
 * @date 2019-07-0823:26
 */
@RestController
@Slf4j
@RequestMapping("api/v4/user")
public class ApiUserCorrectGoodsControllerV4 {

    @Autowired
    private UserCorrectGoodsServiceV4 userCorrectGoodsService;
    @Autowired
    private UserCorrectGoodsService userCorrectGoodsServiceV1;

    /**
     * 查询用户剩余批改次数(7.0加了新字段)
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctTimes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserCorrectTimesVO getCorrectTimes(@Token(check = false) UserSession userSession) {
		if (userSession == null) {
			UserCorrectTimesVO ret = new UserCorrectTimesVO();
			ret.setTotalNum(0);
			return ret;
		}
        int userId = userSession.getId();
        return userCorrectGoodsService.findByUserIdAndBizStatusAndStatus(userId);
    }


    /**
     * 检查某种批改类型的批改次数
     *
     * @param userSession
     * @param type
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping(value = "check/correctTimes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map countCorrectTimes(@Token UserSession userSession,
                                 @RequestParam int type,
                                 @RequestParam(defaultValue = "-1") int id) {
        EssayCorrectGoodsConstant.GoodsTypeEnum intelligenceGoodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(type, CorrectModeEnum.INTELLIGENCE.getMode());
        EssayCorrectGoodsConstant.GoodsTypeEnum manualGoodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(type, CorrectModeEnum.MANUAL.getMode());
        int userId = userSession.getId();
        Map map = Maps.newHashMap();
        map.put("intelligence", userCorrectGoodsService.findCorrectTimes(userId, intelligenceGoodsTypeEnum, id));
        map.put("manual", userCorrectGoodsService.findCorrectTimes(userId, manualGoodsTypeEnum, id));
        return map;
    }

    @LogPrint
    @GetMapping(value = "goods/detailList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object detail(@Token UserSession userSession,
                         @RequestParam(name = "goodsType") int goodsType,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum = EssayCorrectGoodsConstant.GoodsTypeEnum.create(goodsType);
        if (null == goodsTypeEnum) {
            throw new BizException(ErrorResult.create(1000591, "无效的商品类型"));
        }
        /**
         * 用户详情数据查询
         */
        List<GoodsOrderDetailVO> detail = userCorrectGoodsService.detail(page, pageSize, userSession.getId(), goodsTypeEnum);
        long count = userCorrectGoodsService.countDetail(userSession.getId(), goodsTypeEnum);

        PageUtil p = PageUtil.builder().result(detail).next(count > page * pageSize ? 1 : 0).build();
        return p;
    }


    /**
     * 创建订单(切换新的支付宝)
     *
     * @param orderCreateVO
     * @return
     */
    @LogPrint
    @PostMapping(value = "goodsOrder", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createOrder(@Token UserSession userSession,
                              @RequestHeader int terminal,
                              @RequestHeader String cv,
                              @RequestBody OrderCreateVO orderCreateVO) {
    	orderCreateVO.setNewPay(1);
        return userCorrectGoodsService.createOrder(userSession.getId(), orderCreateVO, terminal, userSession.getUname());
    }

    /**
     * 剩余批改次数校验
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "goods/status/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object check(@Token UserSession userSession,
                        @PathVariable(value = "type") int type,
                        @RequestParam(defaultValue = "-1") int id) {
        int userId = userSession.getId();
        return userCorrectGoodsService.check(userId, type, id);
    }

    /**
     * PHP课程赠送申论
     *
     * @param goodsOrderDto 赠送批改信息
     * @return true/false
     */
    @LogPrint
    @PostMapping(value = "course", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object buyCourse(@RequestBody @Validated ApiPHPCourseGoodsOrderDto goodsOrderDto) {
        int userId = userCorrectGoodsServiceV1.findIdByName(goodsOrderDto.getUserName());
        if (userId <= 0) {
            //1. 模拟登录接口，同步用户信息
            userCorrectGoodsServiceV1.fakeLogin(goodsOrderDto.getUserName());
            //2. 再次查询用户信息（查询成功 or  查询失败（信息同步失败））
            userId = userCorrectGoodsServiceV1.findIdByName(goodsOrderDto.getUserName());

            //用户id还是错误。再抛出异常
            if (userId <= 0) {
                log.warn("用户id错误,userName:{}，userId:{}", goodsOrderDto.getUserName(), userId);
                throw new BizException(EssayErrors.USER_ID_ERROR);
            }
        }

        userCorrectGoodsService.buyCourse(userId, goodsOrderDto);

        return true;
    }
}
