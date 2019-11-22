package com.arj.monitor.advice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhouwei
 * @Description: 忽略一些url
 * @create 2018-10-15 上午11:04
 **/
@Slf4j
public class AdviceExcluder {
    private Set<String> ignoreClasses = new HashSet();
    private List<String> ignoreUrls = new ArrayList();

    public AdviceExcluder(){
        this(null,null);
    }

    public AdviceExcluder(Set<String> ignoreClasses, List<String> ignoreUrls){
        /* swagger 内容 */
        this.ignoreUrls.add("swagger-resources");
        this.ignoreUrls.add("api-docs");
        this.ignoreUrls.add("druid");
          /* spring 内容 */
        this.ignoreClasses.add("org.springframework.hateoas.ResourceSupport");
        this.ignoreClasses.add("org.springframework.http.ResponseEntity");
        this.ignoreClasses.add("byte[]");
        if(CollectionUtils.isNotEmpty(ignoreClasses)){
            this.ignoreClasses.addAll(ignoreClasses);
        }
        if(CollectionUtils.isNotEmpty(ignoreUrls)){
            this.ignoreUrls.addAll(ignoreUrls);
        }
    }



    public boolean ignore(Object o,ServerHttpRequest serverHttpRequest){
        if (o != null && ignoreClasses.contains(o.getClass().getCanonicalName())) {
            return true;
        }
        if(serverHttpRequest instanceof ServletServerHttpRequest){
            String path = getRequestPath(((ServletServerHttpRequest) serverHttpRequest).getServletRequest());
            for (String ignoreUrl : ignoreUrls) {
                log.info("---------------"+path+"---------");
                if(path.contains(ignoreUrl)){
                    return true;
                }
            }
        }
        return false;
    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();

        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }

        return url;
    }

    private String getRequestUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            url += ("?"+request.getQueryString());
        }
        return url;
    }
}
