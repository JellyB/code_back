package com.huatu.tiku.essay.web.controller.api.V2;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.entity.UpdateCorrectTimesOrderVO;
import com.huatu.tiku.essay.vo.resp.EssayOrderVO;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api/v2/user")
/**
 * 用户&&批改商品关系
 */
public class ApiUserCorrectGoodsControllerV2 {
    @Autowired
    UserCorrectGoodsService userCorrectGoodsService;


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
     * 订单列表
     * 全部：-1, 待付款0,已付款1, 已取消2
     * @param userSession
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value="order/list",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<EssayOrderVO> correctGoods(@Token UserSession userSession,
                                               @RequestParam(name = "type", defaultValue = "-1") int type,
                                               @RequestParam(name = "page", defaultValue = "1") int page,
                                               @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id");
        return  userCorrectGoodsService.orderList(userSession.getId(),type,pageRequest);
    }


    /**
     * 删除订单
     * @param userSession
     * @return
     */
    @LogPrint
    @DeleteMapping(value="order/{id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map order(@Token UserSession userSession,
                     @PathVariable long id) {
        return  userCorrectGoodsService.delOrder(id);
    }


    /**
     * 删除订单
     * @param userSession
     * @return
     */
    @LogPrint
    @PutMapping(value="cancel/{id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map cancelOrder(@Token UserSession userSession,
                     @PathVariable long id) {
        return  userCorrectGoodsService.cancelOrder(id);
    }

}
