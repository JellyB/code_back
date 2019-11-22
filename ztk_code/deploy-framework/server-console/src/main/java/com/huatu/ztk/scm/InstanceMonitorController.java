package com.huatu.ztk.scm;

import com.huatu.ztk.scm.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 实例的各种信息的监听查看，例如进程、JVM、log等信息的查看
 * @author shaojieyue
 * @date 2013-08-02 11:47:36
 */
@RequestMapping("/monitor/")
@Controller
public class InstanceMonitorController extends BaseController {
	
	@RequestMapping("dashboard.do")
	public String initMonitor(String monitorIp,String monitorServerName){
		return "dashboard";
	}
	
	@RequestMapping("execCommand.do")
	@ResponseBody
	public String execCommand(String monitorIp,String monitorServerName,String command){
		
		return "测试数据";
	}
}
