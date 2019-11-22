package com.huatu.hadoop.controller;

import com.huatu.hadoop.bean.ConditionDTO;
import com.huatu.hadoop.service.UserPictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController()
@Slf4j
public class UserPictureController {

    @Autowired
    private UserPictureService userPictureService;

    //http://123.103.86.52/hadoop/v1/user/picture/active?startTime=20190703&endTime=20190703
    @PostMapping("/v1/user/picture/active")
    public Object getActive(@RequestParam String startTime,
                            @RequestParam String endTime,
                            @RequestBody(required = false) List<ConditionDTO> cons
    ) {
        log.info("cons : {}", cons);

        Object answerActive = null;
        try {
            answerActive = userPictureService.getAnswerActive(startTime, endTime, setScid(cons));
        } catch (Exception e) {
            log.error("/v1/user/picture/active 异常\n" + e.getMessage());
        }

        return answerActive;
    }

    //http://123.103.86.52/hadoop/v1/user/picture/question?startTime=20190703&endTime=20190703
    @PostMapping("/v1/user/picture/question")
    public Object getQuestion(@RequestParam String startTime,
                              @RequestParam String endTime,
                              @RequestBody(required = false) List<ConditionDTO> cons
    ) {
        log.info("cons : {}", cons);
        Object num = null;
        try {
            num = userPictureService.getNum(startTime, endTime, setScid(cons));
        } catch (Exception e) {
            log.error("/v1/user/picture/question 异常\n" + e.getMessage());
        }
        return num;
    }

    //http://localhost:11149/hadoop/v1/user/picture/video?startTime=20190703&endTime=20190703
    @PostMapping("/v1/user/picture/video")
    public Object getAnswerActive(@RequestParam String startTime,
                                  @RequestParam String endTime,
                                  @RequestBody(required = false) List<ConditionDTO> cons
    ) {
        log.info("cons : {}", cons);
        Object videoActive = null;
        try {
            videoActive = userPictureService.getVideoActive(startTime, endTime, cons);
        } catch (Exception e) {
            log.error("/v1/user/picture/video 异常\n" + e.getMessage());
        }
        return videoActive;
    }

    /**
     * 模考
     */
    @PostMapping("/v1/user/picture/match/active")
    public Object getMatchActive(@RequestParam String startTime,
                                 @RequestParam String endTime,
                                 @RequestBody(required = false) List<ConditionDTO> cons
    ) {

        log.info("cons : {}", cons);
        Object matchActive = null;
        try {
            matchActive = userPictureService.getMatchActive(startTime, endTime, cons);
        } catch (Exception e) {
            log.error("/v1/user/picture/match/active 异常\n" + e.getMessage());
        }
        return matchActive;
    }

    @PostMapping("/v1/user/picture/match/top")
    public Object getMatchTop(@RequestParam String startTime,
                              @RequestParam String endTime,
                              @RequestBody(required = false) List<ConditionDTO> cons
    ) {
        log.info("cons : {}", cons);
        Object matchTop = null;
        try {
            matchTop = userPictureService.getMatchTop(startTime, endTime, cons);
        } catch (Exception e) {
            log.error("/v1/user/picture/match/top 异常\n" + e.getMessage());
        }
        return matchTop;
    }

    //http://localhost:11149/hadoop/v1/user/picture/percentage?startTime=20190703&endTime=20190703
    @PostMapping("/v1/user/picture/match/percentage")
    public Object getMatchPer(@RequestParam String startTime,
                              @RequestParam String endTime,
                              @RequestBody(required = false) List<ConditionDTO> cons
    ) {
        log.info("cons : {}", cons);
        Object matchPer = null;
        try {
            matchPer = userPictureService.getMatchPer(startTime, endTime, cons);
        } catch (Exception e) {
            log.error("/v1/user/picture/match/percentage 异常\n" + e.getMessage());
        }
        return matchPer;
    }

    //http://123.103.86.52/hadoop/v1/user/picture/match/count?startTime=20190703&endTime=20190708
    @PostMapping("/v1/user/picture/match/count")
    public Object getMatchCount(@RequestParam String startTime,
                                @RequestParam String endTime,
                                @RequestBody(required = false) List<ConditionDTO> cons
    ) {
        log.info("cons : {}", cons);
        Object matchCount = null;
        try {
            matchCount = userPictureService.getMatchCount(startTime, endTime, cons);
        } catch (Exception e) {
            log.error("/v1/user/picture/match/count 异常\n" + e.getMessage());
        }
        return matchCount;
    }


    private static List<ConditionDTO> setScid(List<ConditionDTO> cons) {
        List<ConditionDTO> newcons = new ArrayList<>();
        if (cons != null && cons.size() > 0) {
            for (ConditionDTO c : cons) {

                if (c.getType().equals("subject_id")) {
                    Integer[] values = c.getValues();
                    List<Integer> list = new ArrayList<>();
                    for (Integer i : values) {
                        Integer[] irr = ConditionDTO.getSecId(i);
                        list.addAll(Arrays.asList(irr));
                    }
                    Integer[] t = new Integer[1];
                    list.toArray(t);
                    c.setValues(list.toArray(t));
                }
            }
        }
        newcons = cons;
        return newcons;
    }
}
