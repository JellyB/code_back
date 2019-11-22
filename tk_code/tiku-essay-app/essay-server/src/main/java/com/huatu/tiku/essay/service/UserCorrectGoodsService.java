package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.PayReturn;
import com.huatu.tiku.essay.entity.UpdateCorrectTimesOrderVO;
import com.huatu.tiku.essay.entity.WeChatPay;
import com.huatu.tiku.essay.vo.admin.UserAccountDetailVO;
import com.huatu.tiku.essay.vo.admin.UserCorrectGoodsRewardVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.util.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface UserCorrectGoodsService {

    ResponseVO findByUserIdAndBizStatusAndStatus(int userId);

    OrderResponseVO createOrder(int userId, OrderCreateVO orderCreateVO, int terminal, String  userName);

    List<EssayGoodsOrderVO> detail(Pageable pageable, int userId, Date date);

    ResponseVO check(int userId, int type);

    long savePayReturn(PayReturn payReturn);



    int updateUserCorrectGoods(long orderId);

    void shutReturn(long orderId);

    long countDetail(int id,Date date);

    long saveWeChatPay(WeChatPay weChatPay);

    long countByIdAndPayTypeAndBizStatus(long out_trade_no, int payType, int bizStatus);

    //查询用户剩余金币数量
    UserAccountDetailVO coin(String userName);

    Object updateCorrectTimesByUser(UpdateCorrectTimesOrderVO updateVO,int userId,long orderId);

    boolean addFreeUser(UpdateCorrectTimesOrderVO vo,int userId,long orderId);

    boolean delFreeUser(UpdateCorrectTimesOrderVO vo,int userId,long orderId);

    Object delCorrectTimesByUser(UpdateCorrectTimesOrderVO updateVO,int userId,long orderId);


    int findIdByName(String userName);


    int fakeLogin(String userName);


    boolean importCourse();

    boolean addFreeUserV2(UpdateCorrectTimesOrderVO updateVO, int userId, Long orderId);

    PageUtil<EssayOrderVO> orderList(int userId, int type, Pageable pageable);

    Map delOrder(long id);

    void closeOrder();

    Map cancelOrder(long id);


    List<String> reward(UserCorrectGoodsRewardVO vo);

    List<String> preHandleFile(String file);

    Object getRewardList(int page, int pageSize);

//    Object mockReward();
}
