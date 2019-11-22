package com.huatu.tiku.schedule.biz.domain.convertor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.huatu.tiku.schedule.biz.enums.ExamType;

/**
 * 考试类型自定义值
 * 
 * @author geek-s
 *
 */
@Converter(autoApply = true)
public class ExamTypeConverter implements AttributeConverter<ExamType, Integer> {

	@Override
	public Integer convertToDatabaseColumn(ExamType examType) {
		if(examType==null) {
			return null;
		}
		return examType.getId();
	}

	@Override
	public ExamType convertToEntityAttribute(Integer id) {
		if(id==null) {
			return null;
		}
		return ExamType.findById(id);
	}
}
