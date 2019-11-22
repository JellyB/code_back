package com.huatu.tiku.essay.service.correct;

import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.entity.EssayGoodsOrderDetail;
import com.huatu.tiku.essay.dto.ApiPHPCourseGoodsOrderDto;
import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import com.huatu.tiku.essay.vo.resp.OrderCreateVO;
import com.huatu.tiku.essay.vo.resp.OrderResponseVO;
import com.huatu.tiku.essay.vo.resp.correct.CorrectTimesSimpleVO;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import com.huatu.tiku.essay.vo.resp.correct.UserCorrectTimesVO;
import com.huatu.tiku.essay.vo.resp.goods.GoodsOrderDetailVO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author huangqingpeng
 * @title: UserCorrectGoodsServiceV4
 * @description: 用户批改次数维护
 * @date 2019-07-0823:32
 */
public interface UserCorrectGoodsServiceV4 {
    /**
     * 用户批改次数查询
     * @param userId
     * @return
     */
    UserCorrectTimesVO findByUserIdAndBizStatusAndStatus(int userId);

    /**
     * 分页查询用户批改次数订单详情
     * @param pageRequest
     * @param userId
     * @param goodsTypeEnum
     * @return
     */
    List<GoodsOrderDetailVO> detail(Pageable pageRequest, int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum);

    /**
     * 查询用户批改次数订单详情总数
     * @param id
     * @param goodsTypeEnum
     * @return
     */
    long countDetail(int id, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum);

    /**
     * 查询用户的某种类型商品，针对某个特定ID的批改次数（检查是否有批改次数）
     *
     * @param userId
     * @param goodsTypeEnum
     * @param id
     * @return
     */
    CorrectTimesSimpleVO findCorrectTimes(int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum, int id);

    /**
     * 创建订单
     * @param userId
     * @param orderCreateVO
     * @param terminal
     * @param userName
     * @return
     */
    OrderResponseVO createOrder(int userId, OrderCreateVO orderCreateVO, int terminal, String userName);
    
    /**
     * 扣减用户订单详情中批改次数
     * @param userId
     * @param goodsTypeEnum
     * @param specialId     专用批改次数对应的专用试题ID和试卷ID
     * @param reduceMap
     */
    List<EssayGoodsOrderDetail> updateCorrectOrderDetailTimes(int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum, long specialId, Map<Long, Long> reduceMap);


    /**
     * 批改次数校验
     * @param userId
     * @param type
     * @param id
     * @return
     */
    ResponseExtendVO check(int userId, int type, int id);

    /**
     * PHP课程赠送申论
     *
     * @param userId        用户ID
     * @param goodsOrderDto 商品信息
     */
    void buyCourse(Integer userId, ApiPHPCourseGoodsOrderDto goodsOrderDto);

    /**
     * 重新生成用户批改次数总表数据
     * @param userId
     * @param goodsTypeEnum
     */
    EssayUserCorrectGoods resetUserCorrectTime(int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum);

    /**
     * 分页查询用户批改次数订单详情
     * @param page
     * @param pageSize
     * @param userId
     * @param goodsTypeEnum
     * @return
     */
    List<GoodsOrderDetailVO> detail(int page, int pageSize, int userId, EssayCorrectGoodsConstant.GoodsTypeEnum goodsTypeEnum);
}
