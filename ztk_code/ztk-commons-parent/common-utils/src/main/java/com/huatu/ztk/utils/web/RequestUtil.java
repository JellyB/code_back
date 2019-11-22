package com.huatu.ztk.utils.web;

import com.google.common.net.HttpHeaders;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hanchao
 * @date 2017/2/12 17:57
 */
public class RequestUtil {
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String[] ips = ip.split(",");
        return ips[0].trim();
    }


    /**
     * 防刷控制
     * @param request
     * @param hosts
     */
    public static void refererCheck(HttpServletRequest request,String ...hosts){
        if(hosts == null){
            return;
        }
        String referer = request.getHeader(HttpHeaders.REFERER);
        if(StringUtils.isBlank(referer)){
            throw new IllegalStateException("abnormal request...");
        }
        for (String host : hosts) {
            if(host == null){
                continue;
            }
            if(referer.contains(host)){
                return;
            }
        }
        throw new IllegalStateException("abnormal request...");
    }
}
