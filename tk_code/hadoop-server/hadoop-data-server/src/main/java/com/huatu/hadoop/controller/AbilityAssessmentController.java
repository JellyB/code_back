package com.huatu.hadoop.controller;

import com.huatu.hadoop.bean.AbilityAssessment;
import com.huatu.hadoop.service.AbilityAssessmentService;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController()
@Slf4j
public class AbilityAssessmentController {

    @Autowired
    private AbilityAssessmentService assessmentService;
    @Autowired
    private UserSessionService userSessionService;

    @GetMapping("/v1/ability/assessment/report")
    public Object userFunAnalysis(
            @Token(check = false) UserSession userSession) {

        AbilityAssessment userReport = null;
        log.info("report : {}", userSession);
        try {
            userReport = assessmentService.getUserReport(
                    Long.parseLong(Integer.valueOf(userSession.getId()).toString()), userSession.getSubject());
        } catch (Exception e) {
            log.error("/v1/ability/assessment/report 异常\n" + e.getMessage());
        }


        return userReport;
    }

    @GetMapping("/v1/ability/assessment/accuracy")
    public Object userAccuracy(
            @Token(check = false) UserSession userSession) {
        List<Map<String, Object>> accuracy = null;
        try {
            accuracy = assessmentService.getUserAccuracy(
                    Long.parseLong(Integer.valueOf(userSession.getId()).toString()), userSession.getSubject());
        } catch (Exception e) {
            log.error("/v1/ability/assessment/accuracy 异常\n" + e.getMessage());
        }

        return accuracy;
    }

    @GetMapping("/v1/ability/assessment/report/top")
    public Object userFunAnalysisTop(
            @RequestParam Long userid,
            @RequestParam Integer subject) throws Exception {
        AbilityAssessment userReport = null;
        try {
            userReport = assessmentService.getUserReport(
                    userid, subject);
        } catch (Exception e) {
            log.error("/v1/ability/assessment/report/top 异常\n" + e.getMessage());
        }


        return userReport;
    }


    @GetMapping("/v1/ability/assessment/test/report")
    public Object testUserFunAnalysis(
            @RequestParam Long userid,
            @RequestParam Integer subject) {
        AbilityAssessment userReport = null;
        try {
            userReport = assessmentService.getUserReport(
                    userid, subject);
        } catch (Exception e) {
            log.error("/v1/ability/assessment/test/report 异常\n" + e.getMessage());
        }
        return userReport;
    }

    @GetMapping("/v1/ability/assessment/test/accuracy")
    public Object testUserAccuracy(
            @RequestParam Long userid,
            @RequestParam Integer subject) {

        List<Map<String, Object>> accuracy = null;
        try {
            accuracy = assessmentService.getUserAccuracy(
                    userid, subject);
        } catch (Exception e) {
            log.error("/v1/ability/assessment/test/accuracy 异常\n" + e.getMessage());
        }

        return accuracy;
    }


    public static void main(String[] args) {

    }

}
