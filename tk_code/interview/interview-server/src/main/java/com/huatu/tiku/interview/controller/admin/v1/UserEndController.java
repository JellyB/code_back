package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.UserService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author jbzm
 * @Date Create on 2018/1/19 10:55
 */
@Slf4j
@RestController
@RequestMapping(value = "end/user", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserEndController {
    @Autowired
    private UserService userService;

    @LogPrint
    @GetMapping
    public Result findUser( @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                            @RequestParam(name = "content",defaultValue = "") String content,
                           @RequestParam(name = "classId",defaultValue = "-1") long classId,
                           @RequestParam(name = "areaId",defaultValue = "-1") long areaId
                ) {
        return Result.ok(userService.findUserByConditions(page,pageSize,content,classId,areaId));
    }


//    /**
//     * 返回试卷所在的所有地区列表
//     *
//     * @return
//     */
//    @LogPrint
//    @GetMapping(value = "areaList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Result areaList() {
//
//        return Result.ok(userService.findAreaList());
//    }




}
