package com.ht.galaxy.service;

import com.ht.galaxy.repository.RegisterResopitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author gaoyuchao
 * @create 2018-07-27 10:59
 */
@Service
public class RegisterService {

    @Autowired
    private RegisterResopitory registerResopitory;

    public List select(String startTime,String endTime,String mode) throws Exception{
        return registerResopitory.select(startTime, endTime, mode);
    }
    public Map<String,List> select(String startTime,String endTime,String mode,String type) throws Exception{
        List list = registerResopitory.select(startTime, endTime, mode, type);
        return JsonUtils2.getResult(list,mode);
    }
    public Map selectMode(String mode) throws Exception {
        return registerResopitory.selectMode(mode);
    }
    public List selectSumReal(String time,String mode) throws Exception{
        return registerResopitory.selectSumReal(time, mode);
    }
    public Map<String,List> selectTerminal(String startTime,String endTime,String mode,String terminal) throws Exception{
        List list = registerResopitory.selectTerminal(startTime, endTime, mode, terminal);
        return JsonUtils2.getTerminal(list,mode);
    }
}
