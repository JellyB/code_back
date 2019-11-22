package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.biz.enums.Nature;
import com.huatu.tiku.position.biz.enums.PositionType;

import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
public interface ImportInterface {

    void importExcelPostition(List<List<List<String>>> list, Nature nature, Integer year, Date beginDate, Date endDate, PositionType type,Date enrolmentEndDate);

    void updateEnrolmentEndDateString(List<List<List<String>>> list,String date);

    void updatePostition(List<List<List<String>>> list);

    void importExcelScoreLine(List<List<List<String>>> list, Nature nature, Integer year);

    void importExcelSpecialty(List<List<List<String>>> list);

    void updatePostitionArea(List<List<List<String>>> list, Date enrolmentEndDate);

}
