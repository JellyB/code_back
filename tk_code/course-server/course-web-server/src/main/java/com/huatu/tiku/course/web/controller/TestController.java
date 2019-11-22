package com.huatu.tiku.course.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.course.bean.vo.CoursePracticeReportSensorsVo;
import com.huatu.tiku.course.netschool.api.TestService;
import com.huatu.tiku.course.netschool.api.v5.RedPackageServiceV6;
import com.huatu.tiku.course.service.v6.SensorsService;

/**
 * 
 * @author zhangchong
 *
 */
@RestController
@RequestMapping(value = "test")
public class TestController {

	@Autowired
	private TestService testService;
	
	@Autowired
	private RedPackageServiceV6 redPackageServiceV6;
	
	@Autowired
	private SensorsService sensorsService;

	/**
	 * 
	 * @param rid
	 * @return
	 */
	@RequestMapping(value = "/hystrix", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
	public Object saleDetail(@RequestParam int rid) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "success";
	}

	@RequestMapping(value = "/local", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
	public Object local(@RequestParam int rid) {
		Object ret = testService.courseDetail(rid);
		System.out.println("-------------->"+ret);
		return ret;
	}
	
	@RequestMapping(value = "/php", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
	public Object php(@RequestParam int rid) {
		Object ret = redPackageServiceV6.showRedEvn();
		System.out.println("-------------->"+ret);
		return ret;
	}
	
	@RequestMapping(value = "/report", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
	public Object report() {
		CoursePracticeReportSensorsVo vo = CoursePracticeReportSensorsVo.builder().userId(233982024).coursewareId(1L).qcount(1).rcount(1).build();
		sensorsService.reportCoursePracticeData(vo);
		return "ok";
	}
	
}
