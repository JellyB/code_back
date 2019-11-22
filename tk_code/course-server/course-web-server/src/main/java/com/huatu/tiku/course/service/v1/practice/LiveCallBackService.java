package com.huatu.tiku.course.service.v1.practice;

import com.huatu.tiku.course.bean.practice.LiveCallbackBo;
import com.huatu.tiku.essay.constant.course.CallBack;

import java.util.List;

/**
 * Created by lijun on 2019/3/7
 */
public interface LiveCallBackService {

	/**
	 * civil 消费
	 * @param callBack
	 */
	void liveCallBackAllInfo(CallBack callBack);

	/**
	 * 直播生成回放- 回调
	 *
	 * @param roomId             房间ID
	 * @param liveCallbackBoList 课件信息
	 */
	void liveCallBackAllInfo(Long roomId, List<LiveCallbackBo> liveCallbackBoList);

	/**
	 * 班级下班回调
	 * 
	 * @param roomId 房间id
	 * @param op     上课:start 下课:end
	 */
	void saveLiveInfo(Long roomId, String op, String op_time, String qid, Integer timestamp, String sign);

}
