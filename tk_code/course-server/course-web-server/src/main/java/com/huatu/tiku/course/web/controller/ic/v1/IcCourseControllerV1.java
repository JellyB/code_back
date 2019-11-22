package com.huatu.tiku.course.web.controller.ic.v1;

import com.huatu.common.exception.BizException;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.course.service.CourseBizService;
import com.huatu.tiku.course.service.ic.v1.IcCourseService;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.TokenType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

/**
 * 课程接口
 * Created by lijun on 2018/7/2
 */
@Slf4j
@RestController
@RequestMapping("/ic/courses")
@ApiVersion("v1")
public class IcCourseControllerV1 {


    @Autowired
    private CourseBizService courseBizService;

    @Autowired
    private IcCourseService icCourseService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询面库课程列表 - 首页展示的课程列表信息
     */
    @LocalMapParam(checkToken = false, tokenType = TokenType.IC)
    @GetMapping("icClassList")
    public Object icClassList(
            @RequestParam(defaultValue = "1") int isFree,
            @RequestParam(defaultValue = "0") int orderType,
            @RequestParam int categoryId,
            @RequestParam(defaultValue = "1000") int subjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int pageSize,
            @RequestHeader int terminal,
            @RequestHeader String cv
	) {
		HashMap<String, Object> map = LocalMapParamHandler.get();
		// 是否显示内购课程
		boolean audit = false;

		if (terminal == 2) {
			// 是否内购
			Object userId = map.get("userId");
			if (userId != null) {
				// 用户白名单
				audit = redisTemplate.getConnectionFactory().getConnection().sIsMember("ic.audit.uids".getBytes(),
						userId.toString().getBytes());
			}

			// 客户端版本
			if (!audit) {
				audit = redisTemplate.getConnectionFactory().getConnection().sIsMember("ic.ios.audit.versions".getBytes(),
						cv.getBytes());
			}
		}

		map.put("audit", audit ? 1 : 0);

		return icCourseService.icClassList(map);
	}

    /**
     * 获取课程详情
     */
    @LocalMapParam(checkToken = false, tokenType = TokenType.IC)
    @GetMapping("/{courseId}")
    public Object getCourseDetail() throws ExecutionException, InterruptedException {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        int courseId = Integer.valueOf(map.get("courseId").toString());
        Object userId = map.get("userId");
        return icCourseService.getCourseDetail(courseId, userId == null ? null : userId.toString());
    }

    /**
     * 课程详情页(h5)
     */
    @GetMapping(value = "/{courseId}/getClassExt")
    public Object getCourseHtml(@PathVariable int courseId) throws BizException {
        return courseBizService.getCourseHtml(courseId);
    }

    /**
     * 获取用户已购课程
     */
    @LocalMapParam(checkToken = true, tokenType = TokenType.IC)
    @GetMapping(value = "/userBuyCourse")
    public Object userBuyCourse() {
        HashMap<String, Object> map = LocalMapParamHandler.get();
        String userId = map.get("userId").toString();
        return icCourseService.userBuyCourse(userId);
    }

}
