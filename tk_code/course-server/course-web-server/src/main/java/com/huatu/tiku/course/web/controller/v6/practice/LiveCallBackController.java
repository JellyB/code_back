package com.huatu.tiku.course.web.controller.v6.practice;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.common.SuccessMessage;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.course.bean.practice.LiveCallbackBo;
import com.huatu.tiku.course.service.v1.practice.LiveCallBackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by lijun on 2019/3/5 直播生成回放时回调 使用 - 直播 roomId 会对应多个
 * courseId,用户在直播答题过程中能够获取到对应的 courseId, - 生成直播回放时 每个courseId 会对应一个 新测 courseId.
 * 1.持久化直播课的排名统计信息（json） 2.持久化直播课中每道题的统计信息(json) 3.直播生成回放，给对应的用户生成一个回放答题卡信息
 * 4.直播绑定的试题信息拷贝至录播回放（未实现）
 */
@RestController
@RequestMapping("practice/liveCallBack")
@RequiredArgsConstructor
@ApiVersion("v6")
@Slf4j
public class LiveCallBackController {

	private final LiveCallBackService liveCallBackService;

	@PostMapping(value = "/{roomId}/liveCallBack")
	public Object liveCallBack(@PathVariable Long roomId, @RequestBody List<LiveCallbackBo> liveCallbackBoList)
			throws ExecutionException, InterruptedException {
		log.info("php直播转录播回调接口参数:{}", roomId, liveCallbackBoList.toString());
		if (CollectionUtils.isNotEmpty(liveCallbackBoList)) {
			liveCallBackService.liveCallBackAllInfo(roomId, liveCallbackBoList);
		}
		return SuccessMessage.create();
	}

	/**
	 * 直播结束回调接口
	 * 
	 * @param roomId
	 * @param courseIdList
	 * @return
	 */
	@PostMapping(value = "/liveCallBack")
	public void liveOver(@RequestParam Long room_id, @RequestParam String op, @RequestParam String op_time,
			@RequestParam String qid, @RequestParam Integer timestamp, @RequestParam String sign,
			HttpServletResponse response) {
		// TODO 生成答题卡
		try {
			log.info("直播上下课回调接口调用------>roomId:{},回调类型:{},回调唯一标示:{},签名为:{},调用时间:{},时间戳为:{}", room_id, op, qid, sign,
					op_time, timestamp);
			liveCallBackService.saveLiveInfo(room_id, op,op_time,qid,timestamp,sign);
			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write("{\"code\": 0, \"msg\":\"success\"}");
		} catch (IOException e) {

			log.info("直播下课回调接口调用------>roomId:{}失败:{}", room_id, e);
		}
	}
}
