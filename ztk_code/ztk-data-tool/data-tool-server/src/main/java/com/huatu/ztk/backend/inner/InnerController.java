package com.huatu.ztk.backend.inner;

import com.huatu.ztk.commons.exception.SuccessMessage;
import org.aspectj.ajde.Ajde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by huangqp on 2018\6\19 0019.
 */

@RestController
@RequestMapping("inner")
public class InnerController {
    private static final Logger logger = LoggerFactory.getLogger(InnerController.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @RequestMapping(value = "/question/sync")
    public Object syncQuestion(){
        //查询是否试卷跟试题绑定关系不同步的
        String sql1 = "select count(1) from v_pastpaper_info i,v_pastpaper_question_r r where r.bb102 =1 and i.BB102 =-1 and r.pastpaper_id = i.PUKEY;\n";
        Integer count = jdbcTemplate.queryForObject(sql1,Integer.class);
        if(count>0){
            //删除不合法的绑定关系
            String sql2 = "";
            jdbcTemplate.update(sql2);
        }
        return SuccessMessage.create("同步试题成功");

    }
 }

