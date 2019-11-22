package com.huatu.hadoop.controller;

import com.huatu.hadoop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/v1/hadoop/user/base")
    public Object userBase(
            @RequestParam String phone) {

        return userService.getUserBase(phone);
    }


    @GetMapping("/v1/hadoop/user/answer")
    public Object userAnswer(
            @RequestParam String phone) {
        Object userAnswer = null;
        try {
            userAnswer = userService.getUserAnswer(phone);
        } catch (Exception e) {
            log.error("/v1/hadoop/user/answer 异常\n" + e.getMessage());
        }
        return userAnswer;

    }
    @GetMapping("/v1/hadoop/user/video")
    public Object userVideo(
            @RequestParam String phone) {
        Object userVideo = null;
        try {
            userVideo = userService.getUserVideo(phone);
        } catch (Exception e) {
            log.error("/v1/hadoop/user/video 异常\n" + e.getMessage());
        }
        return userVideo;
    }
    @GetMapping("/v1/hadoop/user/match")
    public Object userMatch(
            @RequestParam String phone) {

        Object userMatch = null;
        try {
            userMatch = userService.getUserMatch(phone);
        } catch (Exception e) {
            log.error("/v1/hadoop/user/match 异常\n" + e.getMessage());
        }
        return userMatch;
    }
}
