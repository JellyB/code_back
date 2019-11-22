package com.ht.galaxy.service;


import com.ht.galaxy.common.Event;
import com.ht.galaxy.repository.HiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author gaoyuchao
 * @create 2018-07-03 18:14
 */
@Service
public class HiveService {

    @Autowired
    private HiveRepository hiveRepository;

    public Map<String,List> selectType(String startTime, String endTime,String mode,String type) throws Exception{
        List list = hiveRepository.selectType(startTime,endTime,mode,type);
        return JsonUtils.getResult(list,type,mode);
    }
    public List selectMode(String startTime, String endTime,String mode) throws Exception{
        return hiveRepository.selectMode(startTime,endTime,mode);
    }
    public Map<String,List> selectEvent(String startTime, String endTime, String mode, String sign, Event event) throws Exception{
        List list = hiveRepository.selectEvent(startTime,endTime,mode,sign,event);
        return JsonUtils.getEvent(list,mode);
    }
    public int selectSum(String mode) throws Exception{
        return hiveRepository.selectSum(mode);
    }
    public List selectSumReal(String time,String mode) throws Exception{
        return hiveRepository.selectSumReal(time, mode);
    }
    public List selectAll(String startTime,String endTime,String mode,String type) throws Exception{
        return hiveRepository.selectAll(startTime, endTime, mode, type);
    }

    public Map<String,List> selectTerminal(String startTime,String endTime,String mode,String terminal) throws Exception{
        List list = hiveRepository.selectTerminal(startTime, endTime, mode, terminal);
        return JsonUtils.getTerminal(list,mode);
    }

}
