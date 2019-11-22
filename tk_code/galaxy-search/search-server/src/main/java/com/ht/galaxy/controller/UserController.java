package com.ht.galaxy.controller;

import com.ht.galaxy.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author gaoyuchao
 * @create 2018-07-31 10:53
 */
@RestController
public class UserController {

    @Autowired
    private RegisterService registerService;


    @RequestMapping("/bj/register/sum")
    public List select(String startTime,String endTime,String mode) throws Exception{
        return registerService.select(startTime, endTime, mode);
    }
    @RequestMapping("/bj/register/type")
    public Map<String,List> select(String startTime,String endTime,String mode,String type) throws Exception{
        return registerService.select(startTime, endTime, mode, type);
    }
    @RequestMapping("/bj/register/mode")
    public Map selectMode(String mode) throws Exception{
        return registerService.selectMode(mode);
    }

    @RequestMapping("/bj/register/real/real")
    public List selectSumReal(String time,String mode) throws Exception{
        return registerService.selectSumReal(time, mode);
    }
    @RequestMapping("/bj/register/terminal")
    public Map<String,List> selectTerminal(@RequestParam("startTime") String startTime,
                               @RequestParam("endTime") String endTime,
                               @RequestParam("mode") String mode,
                               @RequestParam("terminal") String terminal)throws Exception {
        return registerService.selectTerminal(startTime, endTime, mode, terminal);
    }
}
