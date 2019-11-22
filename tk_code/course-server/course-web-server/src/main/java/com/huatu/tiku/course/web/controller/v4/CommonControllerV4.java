package com.huatu.tiku.course.web.controller.v4;

import com.google.common.collect.Maps;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.netschool.api.v4.CommonServiceV4;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by junli on 2018/3/29.
 */
@RestController
@RequestMapping(value = "/common")
@ApiVersion("v4")
public class CommonControllerV4 {

    @Autowired
    private CommonServiceV4 commonServiceV4;

    /**
     * 面试状元0元赠送课程
     */
    @GetMapping("/champion/sendnetclasslist")
    public Object sendnetclasslist(
            @Token UserSession userSession
    ) {
        String userName = userSession.getUname();
        return ResponseUtil.build(commonServiceV4.sendnetclasslist(userName));
    }

    /**
     * 面试状元封闭集训营
     *
     * @return
     */
    @GetMapping("/champion")
    public Object champion(
            @Token UserSession userSession,
            @RequestParam(value = "admissionTicket", required = true) String admissionTicket,
            @RequestParam(value = "areaId", required = true) String areaId,
            @RequestParam(value = "examNum", required = true) String examNum,
            @RequestParam(value = "examSort", required = false, defaultValue = "0") Integer examSort,
            @RequestParam(value = "name", required = true) String name
    ) {
        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("userName", userSession.getUname());
        params.put("admissionticket", admissionTicket);
        params.put("area_id", areaId);
        params.put("exam_num", examNum);
        params.put("exam_sort", examSort);
        params.put("name", name);
        return ResponseUtil.build(commonServiceV4.champion(params));
    }

    /**
     * 面试状元封闭集训营信息完善
     *
     * @return
     */
    @GetMapping("/championupdate")
    public Object championUpdate(
            @RequestParam(value = "hid", required = true) Integer hid,
            @RequestParam(value = "phone", required = true) String phone,
            @RequestParam(value = "sid", required = true) String sid
    ) {
        final HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("hid", hid);
        paramMap.put("phone", phone);
        paramMap.put("sid", sid);
        return ResponseUtil.build(commonServiceV4.championUpdate(paramMap));
    }

    /**
     * 面试状元赠送课程列表接口
     */
    @GetMapping("/championclasslist")
    public Object championClassList() {
        return ResponseUtil.build(commonServiceV4.championClassList());
    }


    /**
     * 修改或添加协议的用户信息
     */
    @PostMapping("/protocol/userProtocolInfo")
    public Object getUserProtocolInfo(
            @Token UserSession userSession,
            @RequestParam(value = "examCertifacteNo",required = false) String examCertifacteNo,
            @RequestParam(value = "feeAccountName",required = false) String feeAccountName,
            @RequestParam(value = "feeAccountNo",required = false) String feeAccountNo,
            @RequestParam(value = "feeBank",required = false) String feeBank,
            @RequestParam("idCard") String idCard,
            @RequestParam("studentName") String studentName,
            @RequestParam("telNo") String telNo,
            @RequestParam(value = "forExam",required = false) String forExam,
            @RequestParam(value = "nation",required = false) String nation,
            @RequestParam("protocolId") Long protocolId,
            @RequestParam(value = "rid",required = false) Long rid,
            @RequestParam("sex") Integer sex
    ) {
        final HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("ExamCertifacteNo", examCertifacteNo);
            put("FeeAccountName", feeAccountName);
            put("FeeAccountNo", feeAccountNo);
            put("FeeBank", feeBank);
            put("IdCard", idCard);
            put("StudentName", studentName);
            put("TelNo", telNo);
            put("forExam", forExam);
            put("nation", nation);
            put("protocolId", protocolId);
            put("rid", rid);
            put("sex", sex);
            put("userName", userSession.getUname());
        }};
        return ResponseUtil.build(commonServiceV4.userProtocolInfo(params));
    }

    /**
     * 获取协议内容h5地址
     */
    @GetMapping("/protocol/protocolInfo")
    public Object protocolInfo(
            @Token UserSession userSession,
            @RequestParam("protocolId") String protocolId
    ) {
        final HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("protocolId", protocolId);
            put("userName", userSession.getUname());
        }};
        return ResponseUtil.build(commonServiceV4.protocolInfo(params));
    }

    /**
     * 获取用户签订协议填写的信息
     */
    @GetMapping("/protocol/userProtocolInfo")
    public Object userProtocolInfo(
            @Token UserSession userSession,
            @RequestParam("protocolId") String protocolId) {
        final HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("protocolId", protocolId);
            put("userName", userSession.getUname());
        }};
        return ResponseUtil.build(commonServiceV4.getUserProtocolInfo(params));
    }

    /**
     * 上课token
     * @param terminal
     * @param userSession
     * @param coursewareId
     * @param netClassId
     * @param videoType
     * @return
     */
    @GetMapping(value = "/class/token")
    public Object classToken(
            @RequestHeader(value = "terminal") int terminal,
            @Token UserSession userSession,
            @RequestParam(value = "coursewareId") long coursewareId,
            @RequestParam(value = "netClassId") long netClassId,
            @RequestParam(value = "videoType") int videoType){
        final HashMap<String, Object> params = new HashMap<String, Object>() {{
            put("terminal", terminal);
            put("coursewareId", coursewareId);
            put("netClassId", netClassId);
            put("videoType", videoType);
            put("userName", userSession.getUname());
        }};
        return ResponseUtil.build(commonServiceV4.classToken(params));
    }
}
