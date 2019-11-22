package com.huatu.tiku.essay.util.admin;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.EssayTeacherOrderType;
import com.huatu.tiku.essay.essayEnum.TeacherLevelEnum;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.essayEnum.TeacherStatusEnum;
import com.huatu.tiku.essay.essayEnum.YesNoEnum;
import com.huatu.tiku.essay.vo.resp.TeacherDetailVo;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/13
 */
public class TeacherWrapper {

    public static TeacherDetailVo wrapperToTeacherDetailVo(EssayTeacher teacher, List<EssayTeacherOrderType> teacherOrderTypes){
        TeacherDetailVo teacherDetail = new TeacherDetailVo();
        teacherDetail.setTeacherId(teacher.getId());
        teacherDetail.setUCenterId(teacher.getUCenterId());
        teacherDetail.setUCenterName(teacher.getUCenterName());
        teacherDetail.setRealName(teacher.getRealName());
        teacherDetail.setNickName(teacher.getNickName());
        teacherDetail.setPhoneNum(teacher.getPhoneNum());
        teacherDetail.setEmail(teacher.getEmail());
        teacherDetail.setTeacherLevel(teacher.getTeacherLevel());
        teacherDetail.setTeacherLevelText(TeacherLevelEnum.create(teacher.getTeacherLevel()).getTitle());
        teacherDetail.setTeacherStatus(teacher.getTeacherStatus());
        teacherDetail.setTeacherStatusText(TeacherStatusEnum.create(teacher.getTeacherStatus()).getTitle());
        teacherDetail.setDepartment(teacher.getDepartment());
        teacherDetail.setDepartmentText(TeacherStatusEnum.create(teacher.getDepartment()).getTitle());
        //TODO duanxiangchao
        teacherDetail.setTeacherScore(new BigDecimal("5.0"));
        if(teacher.getEntryDate() != null) {
        	teacherDetail.setEntryDate(DateFormatUtils.format(teacher.getEntryDate(), "yyyy-MM-dd"));
        }
        teacherDetail.setBankName(teacher.getBankName());
        teacherDetail.setBankBranch(teacher.getBankBranch());
        teacherDetail.setBankAddress(teacher.getBankAddress());
        teacherDetail.setIdCard(teacher.getIdCard());
        teacherDetail.setBankUserName(teacher.getBankUserName());
        teacherDetail.setBankNum(teacher.getBankNum());

        List<Integer> correctTypes = Lists.newArrayList();
        List<String> correctTypeTexts = Lists.newArrayList();
        List<Integer> orderTypes = Lists.newArrayList();
        List<String> orderTypesTexts = Lists.newArrayList();
        StringBuffer maxLimit = new StringBuffer();
        teacherOrderTypes.forEach(essayTeacherOrderType -> {
            TeacherOrderTypeEnum orderTypeEnum = TeacherOrderTypeEnum.create(essayTeacherOrderType.getOrderType());
            correctTypes.add(orderTypeEnum.getValue());
            correctTypeTexts.add(orderTypeEnum.getTitle());
            if(essayTeacherOrderType.getReceiptStatus() == YesNoEnum.YES.getValue()){
                orderTypes.add(orderTypeEnum.getValue());
                orderTypesTexts.add(orderTypeEnum.getTitle());
            }
            switch (orderTypeEnum){
                case QUESTION:
                    teacherDetail.setQuestionLimit(essayTeacherOrderType.getMaxOrderLimit());
                    break;
                case SET_QUESTION:
                    teacherDetail.setSetQuestionLimit(essayTeacherOrderType.getMaxOrderLimit());
                    break;
                case ARGUMENT:
                    teacherDetail.setArgumentLimit(essayTeacherOrderType.getMaxOrderLimit());
                    break;
                case PRACTICAL:
                    teacherDetail.setPracticalLimit(essayTeacherOrderType.getMaxOrderLimit());
                    break;
            }
            if(orderTypeEnum == TeacherOrderTypeEnum.QUESTION){

            }
            maxLimit.append(orderTypeEnum.getTitle() + "每日" + essayTeacherOrderType.getMaxOrderLimit() + orderTypeEnum.getUnit() + "、");
        });
        teacherDetail.setCorrectType(correctTypes);
        teacherDetail.setCorrectTypeTexts(correctTypeTexts);
        teacherDetail.setOrderType(orderTypes);
        teacherDetail.setOrderTypeTexts(orderTypesTexts);
        if(maxLimit.length() > 0) {
        	teacherDetail.setMaxLimit(maxLimit.substring(0, maxLimit.length() - 1));
        }
        return teacherDetail;

    }

}
