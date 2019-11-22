/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm;

import com.google.common.collect.Maps;
import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.dao.TicketDao;
import com.huatu.ztk.scm.dao.UserDao;
import com.huatu.ztk.scm.dto.Ticket;
import com.huatu.ztk.scm.util.FreemarkerUtils;
import freemarker.template.TemplateException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 工单接口
 * @author wenpingliu
 * @version v 0.1 15/9/16 22:55 wenpingliu Exp $$
 */
@Controller
@RequestMapping("/ticket")
public class TicketController extends BaseController {

    @RequestMapping("index.do")
    public String createTicket() {
        return "ticket-manager";
    }

    @Resource
    private UserDao scmUserDao;

    @Resource
    private TicketDao ticketDao;

    /**
     * 列出所有工单
     * @return
     */
    @RequestMapping("listTicketByServerName.do")
    @ResponseBody
    public String listTicketByServerName(@RequestParam("serverName") String serverName) {
        List<Ticket> tickets = ticketDao.queryTicketsByServerName(serverName);
        JSONArray json = JSONArray.fromObject(tickets);
        JSONObject rt = new JSONObject();
        rt.put("tickets", json);
        return rt.toString();
    }

    /**
     * 列出所有工单
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping("listTicketByPage.do")
    @ResponseBody
    public String listTicketByPage(@RequestParam("page") int page, @RequestParam("pageSize") int pageSize, @RequestParam(value = "status", required = false, defaultValue = "-1") int status) {
        List<Ticket> tickets;
        if (status != -1) {
            tickets = ticketDao.queryTicketsByPageAndStatus(page, pageSize, status);
        } else {
            tickets = ticketDao.queryTicketsByPage(page, pageSize);
        }
        JSONArray json = JSONArray.fromObject(tickets);
        JSONObject rt = new JSONObject();

        rt.put("tickets", json);
        return rt.toString();
    }

    /**
     * 创建工单
     * http://127.0.0.1:8080/ticket/create.do?
     * projectName=push-platform&module=push-spns-server&serverName=android-spns&branch=master&
     * releaseLog=&deployer=zhiyongmu%40sohu.com&type=1
     * @return
     */
    @RequestMapping("create.do")
    @ResponseBody
    public String create(
      @RequestParam("type") int type,
      @RequestParam("projectName") String projectName,
      @RequestParam("module") String module,
      @RequestParam("serverName") String serverName,
      @RequestParam("releaseLog") String releaseLog,
      @RequestParam("branch") String branch,
      @RequestParam(value = "tester",required = false) String tester,
      @RequestParam("deployer") String deployer) throws IOException, TemplateException {

        Ticket ticket = new Ticket();
        ticket.setProjectName(projectName);
        ticket.setServerName(serverName);
        ticket.setTester(tester);
        ticket.setDeployer(deployer);
        ticket.setModule(module);
        ticket.setBranch(branch);
        ticket.setReleaseLog(releaseLog);
        ticket.setStatus(0);
        ticket.setType(type);
        ticket.setCreateBy(getCurrentPassport());
        boolean result = ticketDao.insert(ticket);
        JSONObject rt = new JSONObject();
        rt.put("msg","工单创建成功");
        rt.put("result",result);

        //发送系统变更邮件
        Map<String,Object> data = Maps.newLinkedHashMap();
        data.put("ticket", ticket);
        data.put("username", getUserName());

        String content = FreemarkerUtils.getRenderResutl("email/ticket_create.ftl",data);
        String title = getModle(ticket.getType()) + serverName + "系统发布申请";
        //所有部署通知相关同学
        String to = getPassportInc(deployer) + "," + getPassportInc(getCurrentPassport())
                    + ",qidongqin@sohu-inc.com,"
                    + "sarowliu@sohu-inc.com,"
                    + "weizhang208953@sohu-inc.com,"
                    + "shuyiguo@sohu-inc.com,"
                    + "zhiyongmu@sohu-inc.com";
        boolean emailStatus;
        if(type == 3) {
            //紧急发布，抄启东
            title = "![需要审批]" +  title;
           // emailStatus = MailUtil.sendEmail("1001",  to , title, content);
        }else{
          //  emailStatus = MailUtil.sendEmail("1001", to , title, content);
        }

        return rt.toString();
    }

    @RequestMapping("updateStatus.do")
    @ResponseBody
    public String create(
      @RequestParam("ticketId") int ticketId,
      @RequestParam("status") int status){

        JSONObject rt = new JSONObject();
        if(getUser().getRole() != 3){
           rt.put("msg","仅运维同学有权限关闭工单");
           return rt.toString();
        }

        boolean result = ticketDao.updateStatus(ticketId, status);
        if(result){
            //发送处理完毕邮件
            Ticket ticket = ticketDao.getTicketById(ticketId);
            Map<String,Object> data = Maps.newLinkedHashMap();
            data.put("ticket", ticket);
            data.put("username", getUserName());

            String content = FreemarkerUtils.getRenderResutl("email/ticket_create.ftl",data);
            String title = getModle(ticket.getType()) + ticket.getServerName() + "系统发布处理完成";
         //   MailUtil.sendEmail("1001", getPassportInc(ticket.getCreateBy()),title , content);
        }

        rt.put("msg","操作成功");
        rt.put("result",result);
        return rt.toString();
    }

    private String getModle(int type){
        String model = "";
        switch (type){
            case 1:
                model = "[周二日常部署]";
                break;
            case 2:
                model = "[周四日常部署]";
                break;
            case 3:
                model = "[紧急部署]";
                break;
        }
        return model;
    }


}
