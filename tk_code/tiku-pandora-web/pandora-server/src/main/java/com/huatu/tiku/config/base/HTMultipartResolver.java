package com.huatu.tiku.config.base;

import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

/**
 * Created by duanxiangchao on 2018/5/7
 */
public class HTMultipartResolver extends CommonsMultipartResolver {

    private String[] excludeUrlArray;


    public void setExcludeUrls(String excludeUrls) {
        this.excludeUrlArray = excludeUrls.split(",");
    }

    @Override
    public boolean isMultipart(HttpServletRequest request) {
        if (null != excludeUrlArray) {
            boolean anyMatch = Stream.of(this.excludeUrlArray).anyMatch(url ->
                    request.getRequestURI().contains(url)
            );
            if (anyMatch) {
                return false;
            }
        }
        return super.isMultipart(request);
    }
}
