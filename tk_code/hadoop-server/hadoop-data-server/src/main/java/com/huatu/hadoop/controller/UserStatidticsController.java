package com.huatu.hadoop.controller;

import com.huatu.hadoop.service.UserStatidticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@Slf4j
@RestController
public class UserStatidticsController {


    @Autowired
    private UserStatidticsService userAnswerService;


    @GetMapping("/v1/hadoop/user/stat/active")
    public Object getActive(@RequestParam String quert_start_time,
                            @RequestParam String quert_end_time,
                            @RequestParam String group_type_s,
                            @RequestParam String phone,
                            @RequestParam Integer project
    ) {

//        String str = getAnswerGroupType(group_type);
//        String pro = getProject(group_type);
        Integer group_type = abc(group_type_s);
        String kylin_project = "";
        String group_type_str = "";

        switch (project) {
            case 1:
                kylin_project = "v_user_answer";
                group_type_str = getAnswerGroupType(group_type);
                break;
            case 2:
                kylin_project = "v_user_video";
                group_type_str = getVideoGroupType(group_type);
                break;
            default:
                break;
        }
        Object active = null;
        try {
            active = userAnswerService.getActive(quert_start_time, quert_end_time, group_type, group_type_str, kylin_project, project, phone);
        } catch (Exception e) {
            log.error("/v1/hadoop/user/stat/active 异常\n" + e.getMessage());
        }
        return active;
    }

    @GetMapping("/v1/hadoop/user/stat/num")
    public Object getNum(@RequestParam String quert_start_time,
                         @RequestParam String quert_end_time,
                         @RequestParam String group_type_s,
                         @RequestParam String phone,
                         @RequestParam Integer project
    ) {

        Integer group_type = abc(group_type_s);
//        String str = getAnswerGroupType(group_type);
//        String pro = getProject(group_type);
        String kylin_project = "";
        String group_type_str = "";

        switch (project) {
            case 1:
                kylin_project = "v_user_answer";
                group_type_str = getAnswerGroupType(group_type);
                break;
            case 2:
                kylin_project = "v_user_video";
                group_type_str = getVideoGroupType(group_type);
                break;
            default:
                break;
        }
        Object num = null;
        try {
            num = userAnswerService.getNum(quert_start_time, quert_end_time, group_type, group_type_str, kylin_project, project, phone);
        } catch (Exception e) {
            log.error("/v1/hadoop/user/stat/num 异常\n" + e.getMessage());
        }
        return num;
    }


    public static String getAnswerGroupType(Integer i) {

        String str = "";

        switch (i) {
            case 1:
                str = "V_USER_ANSWER.CREATE_TIME_M";
                break;
            case 2:
                str = "V_USER_ANSWER.CREATE_TIME_W";
                break;
            case 3:
                str = "V_USER_ANSWER.CREATE_TIME_D";
                break;
            case 4:
                str = "V_USER_ANSWER.CREATE_TIME_H";
                break;
            default:
                break;
        }
        return str;
    }

    public static String getVideoGroupType(Integer i) {

        String str = "";

        switch (i) {
            case 1:
                str = "V_USER_VIDEO.CREATE_TIME_M";
                break;
            case 2:
                str = "V_USER_VIDEO.CREATE_TIME_W";
                break;
            case 3:
                str = "V_USER_VIDEO.CREATE_TIME_D";
                break;
            case 4:
                str = "V_USER_VIDEO.CREATE_TIME_H";
                break;
            default:
                break;
        }
        return str;
    }

    private static int abc(String str) {

        Integer i = 0;

        if (str.equals("hour")) {
            i = 4;
        } else if (str.equals("day")) {
            i = 3;
        } else if (str.equals("week")) {
            i = 2;
        } else if (str.equals("month")) {
            i = 1;
        }
        return i;
    }

    public static void main(String[] args) {

        System.out.println(abc("hour"));
    }
}
