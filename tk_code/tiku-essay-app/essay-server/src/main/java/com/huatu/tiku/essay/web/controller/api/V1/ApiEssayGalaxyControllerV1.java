package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.tiku.essay.service.EssayGalaxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("api/v1/galaxy")
/**
 * 订单统计（银河数据统计平台 调用）
 */

public class ApiEssayGalaxyControllerV1 {


    @Autowired
    private EssayGalaxyService essayGalaxyService;
//    /**
//     * 订单统计
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    @LogPrint
//    @GetMapping(value="order",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object correctGoods(@RequestParam(name = "page", defaultValue = "1") int page,
//                                                  @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
//                                                  @RequestParam(name = "start", defaultValue = "0") Long start,
//                                                  @RequestParam(name = "end", defaultValue = "0") Long end) {
//        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id");
//        return  essayGalaxyService.order(start,end);
//    }


}
