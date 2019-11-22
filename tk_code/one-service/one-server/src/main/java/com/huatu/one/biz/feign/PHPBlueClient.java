package com.huatu.one.biz.feign;

import com.huatu.one.biz.vo.PHPBlueClassRankingResponse;
import com.huatu.one.biz.vo.PHPResponseWrapper;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PHP蓝色后台
 *
 * @author geek-s
 * @date 2019-09-12
 */
@FeignClient(name = "php-blue", url = "${php-blue.ribbon.listOfServers}")
public interface PHPBlueClient {

    /**
     * 获取排名数据
     *
     * @param examType      考试类型
     * @param orderBy       排序方式
     * @param rowcount      条数
     * @param showzeroPrice 是否显示0元课
     * @return 数据报表
     */
    @GetMapping("/class/classRanking")
    PHPResponseWrapper<PHPBlueClassRankingResponse> classRanking(@RequestParam("NetClassCategoryID") Long examType, @RequestParam("OrderBy") Integer orderBy,
                                                                 @RequestParam("time01") String date, @RequestParam("rowcount") Integer rowcount,
                                                                 @RequestParam("ShowzeroPrice") String showzeroPrice, @RequestHeader("token") String token);
}
