package com.huatu.ztk.user.util;

import org.springframework.util.StringUtils;

import java.util.function.Function;

public class UserTokenUtil {

    /**
     * 获取subject
     *
     * @param subjectIds 请求header中的subject值
     * @param token      请求header中的token值
     * @param transToken token获取subject的实现函数
     * @return
     */
    public static int getHeaderSubject(String token, Function<String, Integer> transToken, Integer... subjectIds) {
        for (Integer subjectId : subjectIds) {
            if (subjectId > 0) {
                return subjectId;
            }
        }
        Integer apply = transToken.apply(token);
        apply = apply < 0 ? 1 : apply;
        return apply;
    }
}
