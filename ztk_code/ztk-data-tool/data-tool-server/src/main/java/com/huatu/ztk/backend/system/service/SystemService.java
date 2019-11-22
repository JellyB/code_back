package com.huatu.ztk.backend.system.service;

import com.huatu.ztk.backend.system.bean.NsTextMsg;
import com.huatu.ztk.backend.system.dao.SystemDao;
import com.huatu.ztk.backend.util.DateFormat;
import com.huatu.ztk.backend.util.FuncStr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by ht on 2016/11/21.
 */
@Service
public class SystemService {
    private static final Logger logger = LoggerFactory.getLogger(SystemService.class);

    @Autowired
    private SystemDao systemDao;

    /**
     * 消息列表
     * @param msg
     * @return
     */
    public List<NsTextMsg> query(NsTextMsg msg){
       return  systemDao.query(msg);
    }

    /**
     * 删除系统消息
     * @param id
     * @return
     */
    public boolean delete(int id){
        return  systemDao.delete(id);
    }

    /**
     * 新增系统消息
     * @param msg
     * @return
     */
    public int insert(NsTextMsg msg){
        NsTextMsg nsTextMsg=NsTextMsg.builder()
                .id(Integer.parseInt(FuncStr.GetGUID()))
                .msgType("text")
                .useType(2)
                .title(msg.getTitle())
                .content(msg.getContent())
                .createTime(DateFormat.getCurrentDate())
                .lasteditTime(DateFormat.getCurrentDate())
                .deadLine(DateFormat.strTOYMD(msg.getDeadLine()))
                .catgory(msg.getCatgory())
                .build();
       return systemDao.insert(nsTextMsg);
    }

    /**
     * 修改系统消息
     * @param msg
     * @return
     */
    public int update(NsTextMsg msg){
        return systemDao.update(msg);
    }

    /**
     * 获取系统消息
     * @param id
     * @return
     */
    public NsTextMsg findById(int id){
        return systemDao.findById(id);
    }
}
