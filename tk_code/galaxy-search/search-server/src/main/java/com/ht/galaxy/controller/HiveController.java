package com.ht.galaxy.controller;

import com.ht.galaxy.common.Event;
import com.ht.galaxy.service.HiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author gaoyuchao
 * @create 2018-07-03 18:12
 */
@RestController
public class HiveController {

    @Autowired
    private HiveService hiveService;

    @RequestMapping("/bj/active/type")
    public Map<String,List> select(@RequestParam("startTime") String startTime,
                                   @RequestParam("endTime") String endTime,
                                   @RequestParam("mode") String mode,
                                   @RequestParam("type") String type) throws Exception{
        return hiveService.selectType(startTime,endTime,mode,type);
    }
    @RequestMapping("/bj/active/sum")
    public List select(@RequestParam("startTime") String startTime,
                       @RequestParam("endTime") String endTime,
                       @RequestParam("mode") String mode) throws Exception{
        return hiveService.selectMode(startTime,endTime,mode);
    }
    @PostMapping("/bj/active/event")
    public Map<String,List> select(@RequestParam("startTime") String startTime,
                       @RequestParam("endTime") String endTime,
                       @RequestParam("mode") String mode,
                       @RequestParam("sign") String sign,
                       @RequestBody Event event) throws Exception{
        return hiveService.selectEvent(startTime,endTime,mode,sign,event);
    }
    @RequestMapping("/bj/active/real/sum")
    public int selectSum(@RequestParam("mode") String mode) throws Exception{
        return hiveService.selectSum(mode);
    }
    @RequestMapping("/bj/active/real/real")
    public List selectSumReal(@RequestParam("time") String time,
                              @RequestParam("mode") String mode) throws Exception{
        return hiveService.selectSumReal(time, mode);
    }

    @RequestMapping("/bj/active/selectAll")
    public List selectAll(@RequestParam("startTime") String startTime,
                          @RequestParam("endTime") String endTime,
                          @RequestParam("mode") String mode,
                          @RequestParam("type") String type) throws Exception{
        return hiveService.selectAll(startTime, endTime, mode, type);
    }

    @RequestMapping("/bj/active/terminal")
    public Map<String,List> selectTerminal(@RequestParam("startTime") String startTime,
                               @RequestParam("endTime") String endTime,
                               @RequestParam("mode") String mode,
                               @RequestParam("terminal") String terminal) throws Exception {
        return hiveService.selectTerminal(startTime, endTime, mode, terminal);
    }

}
