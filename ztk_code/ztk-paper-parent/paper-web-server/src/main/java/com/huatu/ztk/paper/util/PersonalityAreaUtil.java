package com.huatu.ztk.paper.util;

import com.huatu.ztk.paper.bean.PaperSummary;

import java.util.Optional;

/**
 * @author huangqingpeng
 * @title: PersonalityAreaUtil
 * @description: TODO
 * @date 2019-11-2022:34
 */
public class PersonalityAreaUtil {

    public static void changeName(int subject, PaperSummary paperSummary) {
        if (subject == 1 && null != paperSummary) {
            if (paperSummary.getArea() == -9) {
                paperSummary.setAreaName("国家");
            }
        }
    }
}
