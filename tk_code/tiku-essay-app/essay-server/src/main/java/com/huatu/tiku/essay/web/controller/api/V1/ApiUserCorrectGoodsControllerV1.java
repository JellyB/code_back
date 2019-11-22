package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.entity.UpdateCorrectTimesOrderVO;
import com.huatu.tiku.essay.vo.admin.UserAccountDetailVO;
import com.huatu.tiku.essay.vo.resp.EssayGoodsOrderVO;
import com.huatu.tiku.essay.vo.resp.OrderCreateVO;
import com.huatu.tiku.essay.vo.resp.ResponseVO;
import com.huatu.tiku.essay.vo.resp.UpdateCorrectTimesByUserVO;
import com.huatu.tiku.essay.service.UserCorrectGoodsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 用户&&批改商品关系
 * @author  zhaoxi
 */

@RestController
@Slf4j
@RequestMapping("api/v1/user")
public class ApiUserCorrectGoodsControllerV1 {
    @Autowired
    UserCorrectGoodsService userCorrectGoodsService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 查询用户剩余批改次数(7.0加了新字段)
     *（无限次批改剩余时间，剩余批改总数，每道题目最大可批改次数，单题批改次数，套题批改次数，议论文批改次数）
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctTimes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO left(@Token(check = false, defaultValue = "") UserSession userSession) {
        return userCorrectGoodsService.findByUserIdAndBizStatusAndStatus(userSession == null ? 0 : userSession.getId());
    }

    /**
     * 创建订单
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
    	orderCreateVO.setNewPay(0);
        return userCorrectGoodsService.createOrder(userSession.getId(), orderCreateVO, terminal, userSession.getUname());

    }

    /**
     * 查询申论相关消费明细
     *
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "goods/detailList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object detail(@Token UserSession userSession,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtModify");
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String s = LocalDateTime.now().minusMonths(3).format(f);
        LocalDateTime d = LocalDateTime.parse(s, f);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = d.atZone(zone).toInstant();
        Date date = Date.from(instant);

        List<EssayGoodsOrderVO> detail = userCorrectGoodsService.detail(pageRequest, userSession.getId(), date);
        long count = userCorrectGoodsService.countDetail(userSession.getId(), date);

        PageUtil p = PageUtil.builder().result(detail).next(count > page * pageSize ? 1 : 0).build();
        return p;
    }

    /**
     * 剩余批改次数校验
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "goods/status/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object check(@Token UserSession userSession,
                        @PathVariable(value = "type", required = true) int type) {

        return userCorrectGoodsService.check(userSession.getId(), type);
    }

    /**
     * 查询用户剩余金币
     */
    @LogPrint
    @GetMapping(value = "coin", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public UserAccountDetailVO coin(@Token UserSession userSession) {
        return userCorrectGoodsService.coin(userSession.getUname());

    }

    /**
     * PHP 购买课程赠送批改次数 或 批改免费（加入白名单）
     */
    @LogPrint
    @PostMapping(value = "course", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object buyCourse(@RequestBody UpdateCorrectTimesByUserVO vo) {

        //userId错误
        String userName = vo.getUserName();
        Long orderId = vo.getOrderId();
        if(null == orderId || orderId <= 0){
            //订单ID错误
            log.warn("订单ID错误,orderId:{}",orderId);
            throw new BizException(EssayErrors.ORDER_ID_ERROR);
        }
        if(StringUtils.isEmpty(userName)){
            //用户名称错误
            log.warn("用户名称错误,userName:{}",userName);
            throw new BizException(EssayErrors.USER_NAME_ERROR);
        }
        int userId = userCorrectGoodsService.findIdByName( userName);
        if(userId <= 0){
            //1. 模拟登录接口，同步用户信息
            userCorrectGoodsService.fakeLogin(userName);
            //2. 再次查询用户信息（查询成功 or  查询失败（信息同步失败））
            userId = userCorrectGoodsService.findIdByName( userName);

            //用户id还是错误。再抛出异常
            if(userId <= 0){
                log.warn("用户id错误,userName:{}，userId:{}",userName,userId);
                throw new BizException(EssayErrors.USER_ID_ERROR);
            }

        }
        List<UpdateCorrectTimesOrderVO> list = vo.getList();
        if(CollectionUtils.isEmpty(list)){
            log.warn("课程列表为空,updateVO:{}",vo);
            throw new BizException(EssayErrors.COURSE_LIST_NOT_EXIST);
        }

        for(UpdateCorrectTimesOrderVO updateVO:list){
            /** 操作类型   1加批改次数  2 批改免费**/
            int saveType = updateVO.getSaveType();
            if(saveType == 1){
                return userCorrectGoodsService.updateCorrectTimesByUser(updateVO, userId, orderId);
            }else if(saveType == 2){
                return userCorrectGoodsService.addFreeUserV2(updateVO, userId, orderId);
            }else{
                log.warn("操作类型异常,saveType:{}",saveType);
                throw new BizException(EssayErrors.SAVE_TYPE_ERROR);

            }
        }
        return true;
    }

    /**
     * PHP 课程退款 扣除批改次数或 移除白名单
     */
    @LogPrint
    @DeleteMapping(value = "course", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object cancelCourse(@RequestBody UpdateCorrectTimesByUserVO vo) {
        //userId错误
        String userName = vo.getUserName();
        Long orderId = vo.getOrderId();
        if(null == orderId || orderId <= 0){
            //用户id错误
            log.warn("订单ID错误,userId:{}",userName);
            throw new BizException(EssayErrors.ORDER_ID_ERROR);
        }

        if(StringUtils.isEmpty(userName)){
            //用户名称错误
            log.warn("用户名称错误,userName:{}",userName);
            throw new BizException(EssayErrors.USER_NAME_ERROR);
        }
        int userId = userCorrectGoodsService.findIdByName( userName);
        if(userId <= 0){
            //用户id错误
            log.warn("用户id错误,userName:{}，userId:{}",userName,userId);
            throw new BizException(EssayErrors.USER_ID_ERROR);
        }


        List<UpdateCorrectTimesOrderVO> list = vo.getList();
        if(CollectionUtils.isEmpty(list)){
            log.warn("课程列表为空,updateVO:{}",vo);
            throw new BizException(EssayErrors.COURSE_LIST_NOT_EXIST);
        }

        for(UpdateCorrectTimesOrderVO updateVO:list){
            /** 操作类型   1加批改次数  2 批改免费**/
            int saveType = updateVO.getSaveType();
            if(saveType == 1){
                userCorrectGoodsService.delCorrectTimesByUser(updateVO, userId, orderId);
            }else if(saveType == 2){
                userCorrectGoodsService.delFreeUser(updateVO, userId, orderId);
            }else{
                log.warn("操作类型异常,saveType:{}",saveType);
                throw new BizException(EssayErrors.SAVE_TYPE_ERROR);
            }
        }
        return true;
    }


//    /**
//     * 测试post接口
//     */
//    @LogPrint
//    @GetMapping(value = "test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object test() {
//        userCorrectGoodsService.fakeLogin("htwx_7723964");
//
//        return "测试login接口";
//    }
//
//
//        /**
//         * 批量导入  批改免费的白名单中加入用户
//         * @param vo
//         * @return
//         */
//    @LogPrint
//    @PostMapping(value = "batch", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public boolean addFreeUserBatch(@RequestBody EssayCorrectFreeBatchVO vo) {
//
//        log.info("EssayCorrectFreeBatchVO: {}", vo);
//        List<UpdateCorrectTimesByUserVO> list = vo.getList();
//        if(CollectionUtils.isEmpty(list)){
//            log.warn("操作列表为空");
//            throw new BizException(EssayErrors.SAVE_LIST_NOT_EXIST);
//        }
//        for(UpdateCorrectTimesByUserVO updateVO:list){
//            userCorrectGoodsService.addFreeUser(null, updateVO.getUserId(), updateVO.getOrderId());
//        }
//        return true;
//    }




//    /**
//     * 批改免费的白名单中加入用户
//     * @param vo
//     * @return
//     */
//    @LogPrint
//    @PostMapping(value = "free", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public boolean addFreeUser(@RequestBody UpdateCorrectTimesByUserVO vo) {
//
//        log.info("EssayCorrectFreeUser: {}", vo);
//        return userCorrectGoodsService.addFreeUser(vo);
//
//    }
//
//
//    /**
//     * 从批改免费的白名单中移除用户
//     * @param vo
//     * @return
//     */
//    @LogPrint
//    @DeleteMapping(value = "free", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public boolean delFreeUser(@RequestBody UpdateCorrectTimesByUserVO vo) {
//
//        log.info("EssayCorrectFreeUser: {}", vo);
//        return userCorrectGoodsService.delFreeUser(vo);
//
//    }

}
