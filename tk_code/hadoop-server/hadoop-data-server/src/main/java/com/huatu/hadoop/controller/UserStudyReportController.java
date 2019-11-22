package com.huatu.hadoop.controller;

import com.huatu.hadoop.service.UserStudyReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@Slf4j
@RestController
public class UserStudyReportController {


    @Autowired
    private UserStudyReportService studyService;

    /**
     * 昨日课程学习活跃用户
     */
    @GetMapping("/v1/study/active/user")
    public Object studyActiveUser(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType,
            @RequestParam String terminal
    ) {
        Object o = null;
        try {
            o = studyService.studyActiveUser(startRecordTime, endRecordTime, groupType, terminal);
        } catch (Exception e) {
            log.error("/v1/study/active/user 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 昨日课程学习时长用户分布
     */
    @GetMapping("/v1/study/report/length")
    public Object studyReportLength(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType,
            @RequestParam String terminal
    ) {

        Object o = null;
        try {
            o = studyService.studyReportLength(startRecordTime, endRecordTime, groupType, terminal);
        } catch (Exception e) {
            log.error("/v1/study/report/length 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 课程学习情况统计
     */
    @GetMapping("/v1/study/report")
    public Object studyReport(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType,
            @RequestParam String id,
            @RequestParam String idtype
    ) {
        Object o = null;
        try {
            o = studyService.netStudy(startRecordTime, endRecordTime, groupType, id, idtype);
        } catch (Exception e) {
            log.error("/v1/study/report 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 课程学习情况课程表格
     */
    @GetMapping("/v1/study/report/datagrid")
    public Object studyReportDatagrid(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType,
            @RequestParam String id,
            @RequestParam String idtype,
            @RequestParam int limitSize,
            @RequestParam long offset,
            @RequestParam int isend
    ) {
        Object o = null;
        try {
            o = studyService.netStudydatagrid(startRecordTime, endRecordTime, groupType, id, idtype, limitSize, offset, isend);
        } catch (Exception e) {
            log.error("/v1/study/report/datagrid 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 课程学习情况课时表格3
     */
    @GetMapping("/v1/study/report/datagrid/syllabus")
    public Object studyReportDatagridBySyllabus(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType,
            @RequestParam String syllabusid,
            @RequestParam(required = false) Integer parentId,
            @RequestParam int limitSize,
            @RequestParam long offset
    ) {
        Object o = null;
        try {
            o = studyService.datagridBySyllabus(startRecordTime, endRecordTime, groupType, syllabusid, parentId, limitSize, offset);
        } catch (Exception e) {
            log.error("/v1/study/report/datagrid/syllabus 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 根据课程查课时相关页面
     */
    @GetMapping("/v1/study/report/netclassid")
    public Object findByNetclassId(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam int isDatagrid,
            @RequestParam String netclassid,
            @RequestParam int limitSize,
            @RequestParam long offset,
            @RequestParam String querytype,
            @RequestParam String idtype
    ) {
        Object byNetclassId = null;
        try {
            byNetclassId = studyService.findByNetclassId(startRecordTime, endRecordTime, netclassid, isDatagrid, limitSize, offset, querytype, idtype);
        } catch (Exception e) {
            log.error("/v1/study/report/netclassid 异常\n" + e.getMessage());
        }
        return byNetclassId;
    }

    /**
     * 用户学习情况统计
     */
    @GetMapping("/v1/study/report/user")
    public Object uer(
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(required = false) Integer parentId,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam(required = false) String syllabusid,
            @RequestParam(required = false) String netclassid,
            @RequestParam(required = false) String classid,
            @RequestParam int limitSize,
            @RequestParam long offset,
            @RequestParam(value = "fcon") String filterCondition
    ) {
        Object o = null;
        try {
            o = studyService.userTable(startRecordTime, endRecordTime, parentId, netclassid, syllabusid, classid, limitSize, offset, filterCondition);
        } catch (Exception e) {
            log.error("/v1/study/report/user 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 到课率
     *
     * @return
     */
    @GetMapping("/v1/study/report/daoke")
    public Object daoke(
            @RequestParam(value = "netid", required = false) String netid,
            @RequestParam(value = "cid", required = false) String cid,
            @RequestParam(value = "rid") String rid,
            @RequestParam int isDatagrid,
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType
    ) {
        Object daoke = null;
        try {
            daoke = studyService.getDaoke(netid, cid, rid, isDatagrid, startRecordTime, endRecordTime, groupType);
        } catch (Exception e) {
            log.error("/v1/study/report/daoke 异常\n" + e.getMessage());
        }
        return daoke;
    }

    /**
     * 到课率
     *
     * @return
     */
    @GetMapping("/v1/study/report/daoke/cid")
    public Object daokecid(
            @RequestParam(value = "cid", required = false) String cid,
            @RequestParam int isDatagrid,
            @RequestParam(value = "start") String startRecordTime,
            @RequestParam(value = "end") String endRecordTime,
            @RequestParam String groupType
    ) {
        Object o = null;
        try {
            o = studyService.cidAttendance(cid, isDatagrid, startRecordTime, endRecordTime, groupType);
        } catch (Exception e) {
            log.error("/v1/study/report/daoke/cid 异常\n" + e.getMessage());
        }
        return o;
    }

    /**
     * 课件下拉列表
     *
     * @return
     */
    @GetMapping("/v1/study/report/ridlist")
    public Object ridlist(
            @RequestParam(value = "id") String id,
            @RequestParam String idtype
    ) {
        Object ridlist = null;
        try {
            ridlist = studyService.ridlist(idtype, id);
        } catch (Exception e) {
            log.error("/v1/study/report/ridlist 异常\n" + e.getMessage());
        }
        return ridlist;
    }


    @GetMapping("/v1/study/report/fixdata")
    public Object fixdata(
    ) {
        Object o = null;
        try {
            o = studyService.fixData();
        } catch (Exception e) {
            log.error("/v1/study/report/fixdata 异常\n" + e.getMessage());
        }
        return o;
    }


}
