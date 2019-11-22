/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm;

import com.huatu.ztk.scm.dao.TicketLogDao;
import com.huatu.ztk.scm.dto.TicketLog;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * 工单历史接口
 * @author wenpingliu
 * @version v 0.1 15/9/17 16:31 wenpingliu Exp $$
 */
@Controller
@RequestMapping("/ticketLog")
public class TicketLogController {


    @Resource
    private TicketLogDao ticketLogDao;

    @RequestMapping("index.do")
    public String createTicket() {
        return "ticket-log";
    }

    /**
     * 列出所有工单日志
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping("listTicketLogByPage.do")
    @ResponseBody
    public String listTicketLogByPage(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {
        List<TicketLog> tickets = ticketLogDao.queryTicketLogsByPage(page, pageSize);
        JSONArray json = JSONArray.fromObject(tickets);
        JSONObject rt = new JSONObject();
        rt.put("logs",json);
        return rt.toString();
    }

    /**
     * 创建工单日志
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public String create(
      @RequestParam("note") String note,
      @RequestParam("create_by") String createBy,
      @RequestParam("ticket_id") int ticketId) {

        TicketLog ticketLog = new TicketLog();
        ticketLog.setNote(note);
        ticketLog.setCreateBy(createBy);
        ticketLog.setTicketId(ticketId);
        boolean result = ticketLogDao.insert(ticketLog);
        JSONObject rt = new JSONObject();
        rt.put("msg","success");
        return rt.toString();
    }
}
