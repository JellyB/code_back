package com.huatu.tiku.interview.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-04 下午12:12
 **/
public interface CoreService {
      String processRequest(Map<String, String> requestMap, HttpServletRequest request,HttpServletResponse response) ;
}
