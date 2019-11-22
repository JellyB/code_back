package com.huatu.tiku.interview.controller.api.v1;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.MorningReadingService;
import com.huatu.tiku.interview.util.LogPrint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/13 18:58
 * @Description
 */
@RestController
@RequestMapping("/api/mr")
public class MorningReadingApiController {
    // df
    @Autowired
    private MorningReadingService readingService;

    @LogPrint
    @GetMapping
    public Result get(Long id){

        return Result.ok(readingService.get(id));
    }


}
