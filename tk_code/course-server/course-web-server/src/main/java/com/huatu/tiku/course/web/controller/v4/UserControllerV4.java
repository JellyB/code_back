package com.huatu.tiku.course.web.controller.v4;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.netschool.api.v4.UserGoldServiceV4;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/userGold")
@ApiVersion("v4")
public class UserControllerV4 {

    @Autowired
    UserGoldServiceV4 userGoldServiceV4;

    @GetMapping(value = "info")
    public Object getUserGoldInfo(
            @Token UserSession userSession
    ) {
        return ResponseUtil.build(userGoldServiceV4.getUserGoldInfo(userSession.getUname()));
    }
}
