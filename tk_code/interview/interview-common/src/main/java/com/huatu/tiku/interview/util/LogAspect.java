package com.huatu.tiku.interview.util;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


/*
 * @desp 日志切面实现类
 * */
@Slf4j
@Aspect
@Component
public class LogAspect {


	
	@Pointcut(value="@annotation(com.huatu.tiku.interview.util.LogPrint)")
   private void myPointCut(){
   }
	@Before("myPointCut()")
	public void beforeExecutePrintLogs(JoinPoint joinPoint){
		Signature sg = joinPoint.getSignature();
		StringBuffer sb = new StringBuffer();
		sb.append(sg.getDeclaringTypeName());
		sb.append(" method:");
		sb.append(sg.getName());
		sb.append("  参数列表：");
		 Object[]  obj = joinPoint.getArgs();
		 for(int i = 0;i<obj.length;i++){
			 sb.append(obj[i]);
			 sb.append(",");
		 }
		log.info(sb.toString());
	}
//	@AfterReturning(returning="rvt")
//	public void AfterExec(Object rvt){
//
//		StringBuffer sb = new StringBuffer();
//		sb.append("响应数据：");
//		sb.append(JSON.toJSON(rvt));
//		log.info(sb.toString());
//	}
}
