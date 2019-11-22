package com.huatu.tiku.schedule.biz.controller;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.config.ResponseVo;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.OffRecord;
import com.huatu.tiku.schedule.biz.dto.CreateOffRecordDto;
import com.huatu.tiku.schedule.biz.service.OffRecordService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeformatUtil;
import com.huatu.tiku.schedule.biz.vo.OffRecordVo;

/**
 * 教师请假Controller
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("offRecord")
public class OffRecordController {

	private final OffRecordService offRecordService;

	@Autowired
	public OffRecordController(OffRecordService offRecordService) {
		this.offRecordService = offRecordService;
	}

	/**
	 * 新增请假记录
	 * 
	 * @param offRecordDto
	 *            请假记录
	 * @return 请假记录
	 */
	@PostMapping
	public ResponseVo createCourse(@Valid @RequestBody CreateOffRecordDto offRecordDto, BindingResult bindingResult) {
		// 校验参数是否合法
		if (bindingResult.hasErrors()) {
			throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
		}

		// 校验请假时间
		boolean flag = offRecordService.validateTime(offRecordDto.getTeacherId(), offRecordDto.getDate(),
				offRecordDto.getTimeBegin(), offRecordDto.getTimeEnd());

		if (flag) {
			// 转成domain并入库
			OffRecord offRecord = new OffRecord();

			BeanUtils.copyProperties(offRecordDto, offRecord);

			offRecordService.save(offRecord);

			return ResponseVo.success(offRecord);
		} else {
			return ResponseVo.fail("请假时间有误");
		}
	}

	/**
	 * 根据教师ID获取请假记录
	 * 
	 * @param teacherId
	 *            教师ID
	 * @param page
	 *            分页信息
	 * @return 请假记录
	 */
	@GetMapping("/teacher/{teacherId}")
	public Page<OffRecordVo> findByTeacherId(@PathVariable Long teacherId,
			@DateTimeFormat(pattern = "yyyy-MM-dd") Date begin, @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
			Pageable page) {

		Page<OffRecord> offRecords = offRecordService.findByTeacherIdAndDateBetween(teacherId, begin, end, page);

		List<OffRecordVo> offRecordVos = Lists.newArrayList();

		offRecords.forEach(offRecord -> {
			OffRecordVo offRecordVo = new OffRecordVo();
			offRecordVo.setId(offRecord.getId());
			offRecordVo.setTeacherId(offRecord.getTeacherId());
			offRecordVo.setDate(DateformatUtil.format0(offRecord.getDate()));
			offRecordVo.setTimeBegin(TimeformatUtil.format(offRecord.getTimeBegin()));
			offRecordVo.setTimeEnd(TimeformatUtil.format(offRecord.getTimeEnd()));

			offRecordVos.add(offRecordVo);
		});

		return new PageImpl<>(offRecordVos, page, offRecords.getTotalElements());
	}
}
