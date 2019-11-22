package com.huatu.tiku.schedule.biz.service.imple;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.TeacherScheduleReport;
import com.huatu.tiku.schedule.biz.repository.TeacherScheduleReportRepository;
import com.huatu.tiku.schedule.biz.service.TeacherScheduleReportService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.TimeUtil;
import com.huatu.tiku.schedule.biz.vo.TeacherScheduleReportVo;
import com.huatu.tiku.schedule.biz.vo.TeacherScheduleReportVo.TeacherScheduleReportDataVo;

@Service
public class TeacherScheduleReportServiceImpl extends BaseServiceImpl<TeacherScheduleReport, Long>
		implements TeacherScheduleReportService {

	private final TeacherScheduleReportRepository teacherScheduleReportRepository;

	@Autowired
	public TeacherScheduleReportServiceImpl(TeacherScheduleReportRepository teacherScheduleReportRepository) {
		this.teacherScheduleReportRepository = teacherScheduleReportRepository;
	}

//	@Override
//	public TeacherScheduleReportVo list(Integer year, Integer month, Long[] teacherIds) {
//		// 获取月份的最后一天
//		int dayEnd;
//
//		Calendar now = Calendar.getInstance();
//
//		int yearCurrent = now.get(Calendar.YEAR);
//
//		int monthCurrent = now.get(Calendar.MONTH) + 1;
//
//		if (yearCurrent == year && monthCurrent == month) {
//			dayEnd = now.get(Calendar.DAY_OF_MONTH) - 1;
//		} else {
//			now.set(Calendar.YEAR, year);
//			now.set(Calendar.MONTH, month - 1);
//
//			dayEnd = now.getActualMaximum(Calendar.DAY_OF_MONTH);
//		}
//
//		// 拼接表头
//		List<Map<String, Object>> headers = Lists.newArrayList();
//
//		headers.add(ImmutableMap.of("fixed", true, "width", "150px", "label", "姓名"));
//		headers.add(ImmutableMap.of("fixed", true, "width", "150px", "label",
//				DateformatUtil.format3(now.getTime()) + "合计"));
//
//		for (int i = 1; i <= dayEnd; i++) {
//			headers.add(ImmutableMap.of("minwidth", "150px", "label", month + "月" + i + "日"));
//		}
//
//		TeacherScheduleReportVo teacherScheduleReportVo = new TeacherScheduleReportVo();
//
//		teacherScheduleReportVo.setHeaders(headers);
//
//		// 拼接数据
//		List<Object[]> reports = null;
//
//		if (teacherIds == null || teacherIds.length == 0) {
//			reports = teacherScheduleReportRepository.getReports(year, month);
//		} else {
//			reports = teacherScheduleReportRepository.getReports(year, month, teacherIds);
//		}
//
//		// 所有教师
//		Set<String> teacherSet = Sets.newHashSet();
//
//		// 数据字典
//		Map<String, Object[]> dataDic = Maps.newHashMap();
//
//		reports.forEach(report -> {
//			teacherSet.add(report[0].toString().substring(report[0].toString().indexOf("-") + 1));
//			dataDic.put(report[0].toString(), report);
//		});
//
//		List<List<Object>> datas = Lists.newArrayList();
//
//		teacherSet.forEach(teacherId -> {
//			// 一行数据
//			List<Object> dataTemp = Lists.newArrayList();
//
//			// 姓名占位
//			dataTemp.add(null);
//
//			// 本月占位
//			dataTemp.add(null);
//
//			// 本月统计
//			int morningTimeTotal = 0;
//			int afternoonTimeTotal = 0;
//			int eveningTimeTotal = 0;
//
//			// 根据日期遍历数据
//			for (int i = 1; i <= dayEnd; i++) {
//				// 获取当前数据
//				Object[] data = dataDic.get(i + "-" + teacherId);
//				if (data == null) {
//					dataTemp.add(null);
//				} else {
//					if (dataTemp.get(0) == null) {
//						dataTemp.set(0, ImmutableMap.of("teacherName", data[1].toString()));
//					}
//
//					// 当前日期数据
//					TeacherScheduleReportDataVo teacherScheduleReportDataVo = new TeacherScheduleReportDataVo();
//
//					teacherScheduleReportDataVo.setMorningTime(data[2].toString().equals("0") ? "0"
//							: TimeUtil.minut2Hour(Integer.parseInt(data[2].toString())).toString());
//					teacherScheduleReportDataVo.setAfternoonTime(data[3].toString().equals("0") ? "0"
//							: TimeUtil.minut2Hour(Integer.parseInt(data[3].toString())).toString());
//					teacherScheduleReportDataVo.setEveningTime(data[4].toString().equals("0") ? "0"
//							: TimeUtil.minut2Hour(Integer.parseInt(data[4].toString())).toString());
//
//					dataTemp.add(teacherScheduleReportDataVo);
//
//					morningTimeTotal += Integer.parseInt(data[2].toString());
//					afternoonTimeTotal += Integer.parseInt(data[3].toString());
//					eveningTimeTotal += Integer.parseInt(data[4].toString());
//				}
//			}
//
//			// 本月统计
//			TeacherScheduleReportDataVo teacherScheduleReportDataVo = new TeacherScheduleReportDataVo();
//
//			teacherScheduleReportDataVo
//					.setMorningTime(morningTimeTotal == 0 ? "0" : TimeUtil.minut2Hour(morningTimeTotal).toString());
//			teacherScheduleReportDataVo.setAfternoonTime(
//					afternoonTimeTotal == 0 ? "0" : TimeUtil.minut2Hour(afternoonTimeTotal).toString());
//			teacherScheduleReportDataVo
//					.setEveningTime(eveningTimeTotal == 0 ? "0" : TimeUtil.minut2Hour(eveningTimeTotal).toString());
//
//			dataTemp.set(1, teacherScheduleReportDataVo);
//
//			datas.add(dataTemp);
//		});
//
//		teacherScheduleReportVo.setDatas(datas);
//
//		return teacherScheduleReportVo;
//	}

	public TeacherScheduleReportVo list2(Integer year, Integer month, Long[] teacherIds) {//TODO 待完成
		TeacherScheduleReportVo teacherScheduleReportVo=new TeacherScheduleReportVo();
		// 获取月份的最后一天
		int dayEnd;

		Calendar now = Calendar.getInstance();

		int yearCurrent = now.get(Calendar.YEAR);

		int monthCurrent = now.get(Calendar.MONTH) + 1;

		if (yearCurrent == year && monthCurrent == month) {
			dayEnd = now.get(Calendar.DAY_OF_MONTH) - 1;
		} else {
			now.set(Calendar.YEAR, year);
			now.set(Calendar.MONTH, month - 1);

			dayEnd = now.getActualMaximum(Calendar.DAY_OF_MONTH);
		}

		// 拼接表头
		List<Map<String, Object>> headers = Lists.newArrayList();

		headers.add(ImmutableMap.of("fixed", true, "width", "150px", "label", "姓名"));
		headers.add(ImmutableMap.of("fixed", true, "width", "150px", "label",
				DateformatUtil.format3(now.getTime()) + "合计"));

		for (int i = 1; i <= dayEnd; i++) {
			headers.add(ImmutableMap.of("minwidth", "150px", "label", month + "月" + i + "日"));
		}
		// 拼接数据
		List<Object[]> reports = null;
		String begin=year+"-"+month+"-01";
		String end=year+"-"+month+"-"+dayEnd;
		if (teacherIds == null || teacherIds.length == 0) {
			reports = teacherScheduleReportRepository.getReports2(begin, end);
		} else {
			reports = teacherScheduleReportRepository.getReports2(begin, end, teacherIds);
		}
		List<List<Object>> datas = Lists.newArrayList();
		Map<Long,List> map=new HashMap();
		for(Object[] objectArray:reports){//循环数据
			Long teacherid=Long.valueOf(objectArray[4].toString());
			List<Object> teacherDatas= map.get(teacherid);
			if(teacherDatas==null){//未创建数据,进行初始化
				teacherDatas=new ArrayList<>();
				teacherDatas.add(ImmutableMap.of("teacherName", objectArray[5].toString()));//教师姓名
				teacherDatas.add(new TeacherScheduleReportDataVo());//创建本月合计
				for (int i = 1; i <= dayEnd; i++) {
					teacherDatas.add(null);//填补空数据
				}
			}
			Integer date=Integer.valueOf(objectArray[1].toString().substring(8));//日期转数字
			Integer timeBegin=Integer.valueOf(objectArray[2].toString());//开始时间
			Integer timeEnd=Integer.valueOf(objectArray[3].toString());//结束时间
			TeacherScheduleReportDataVo teacherScheduleReportDataVo=(TeacherScheduleReportDataVo)teacherDatas.get(date+1);//第一天为第三条数据 下标2
			if(null==teacherScheduleReportDataVo){
				teacherScheduleReportDataVo=new TeacherScheduleReportDataVo();
			}
			Integer sumMorning=teacherScheduleReportDataVo.getMorningTime()==null?0:TimeUtil.hour2Minut((teacherScheduleReportDataVo.getMorningTime()));
			Integer sumAfternoon=teacherScheduleReportDataVo.getAfternoonTime()==null?0:TimeUtil.hour2Minut((teacherScheduleReportDataVo.getAfternoonTime()));
			Integer sumEvening=teacherScheduleReportDataVo.getEveningTime()==null?0:TimeUtil.hour2Minut((teacherScheduleReportDataVo.getEveningTime()));

			TeacherScheduleReportDataVo sumVo = (TeacherScheduleReportDataVo)teacherDatas.get(1);//取出本月合计
			Integer sumMorningMonth=sumVo.getMorningTime()==null?0:TimeUtil.hour2Minut((sumVo.getMorningTime()));
			Integer sumAfternoonMonth=sumVo.getAfternoonTime()==null?0:TimeUtil.hour2Minut((sumVo.getAfternoonTime()));
			Integer sumEveningMonth=sumVo.getEveningTime()==null?0:TimeUtil.hour2Minut((sumVo.getEveningTime()));

			if (timeBegin < 1200) {
				// 上午结束
				if (timeEnd <= 1200) {
					sumMorning += TimeUtil.interval(timeBegin, timeEnd);
					sumMorningMonth+=TimeUtil.interval(timeBegin, timeEnd);;
					// 下午结束
				} else {
					sumMorning += TimeUtil.interval(timeBegin, 1200);
					sumMorningMonth += TimeUtil.interval(timeBegin, 1200);
					sumAfternoon += TimeUtil.interval(1200, timeEnd);
					sumAfternoonMonth += TimeUtil.interval(1200, timeEnd);
				}
				// 下午开始
			} else if (timeBegin < 1800) {
				// 下午结束
				if (timeEnd <= 1800) {
					sumAfternoon +=  TimeUtil.interval(timeBegin, timeEnd);
					sumAfternoonMonth +=  TimeUtil.interval(timeBegin, timeEnd);
					// 晚上结束
				} else {
					sumAfternoon += TimeUtil.interval(timeBegin, 1800);
					sumAfternoonMonth += TimeUtil.interval(timeBegin, 1800);
					sumEvening += TimeUtil.interval(1800, timeEnd);
					sumEveningMonth += TimeUtil.interval(1800, timeEnd);
				}
				// 晚上
			} else {
				sumEvening += TimeUtil.interval(timeBegin, timeEnd);
				sumEveningMonth += TimeUtil.interval(timeBegin, timeEnd);
			}
			teacherScheduleReportDataVo.setMorningTime(TimeUtil.minut2Hour(sumMorning).toString());
			teacherScheduleReportDataVo.setAfternoonTime(TimeUtil.minut2Hour(sumAfternoon).toString());
			teacherScheduleReportDataVo.setEveningTime(TimeUtil.minut2Hour(sumEvening).toString());
			teacherDatas.set(date+1,teacherScheduleReportDataVo);

			sumVo.setMorningTime(TimeUtil.minut2Hour(sumMorningMonth).toString());
			sumVo.setAfternoonTime(TimeUtil.minut2Hour(sumAfternoonMonth).toString());
			sumVo.setEveningTime(TimeUtil.minut2Hour(sumEveningMonth).toString());
			teacherDatas.set(1,sumVo);

			map.put(teacherid,teacherDatas);
		}
		for(Map.Entry<Long,List> entry:map.entrySet()){
			datas.add(entry.getValue());
		}

		teacherScheduleReportVo.setHeaders(headers);
		teacherScheduleReportVo.setDatas(datas);
		return teacherScheduleReportVo;
	}

	@Override
	public void generateReport() {
		// 获取技术时间
		ZonedDateTime now = LocalDate.now().atStartOfDay(ZoneId.systemDefault());

		// 结束时间
		Date end = Date.from(now.toInstant());

		// 统计时间
		ZonedDateTime yesterday = now.minusDays(1);

		// 开始时间
		Date start = Date.from(yesterday.toInstant());

		// 获取上课记录
		List<Object[]> datas = teacherScheduleReportRepository.findSchedules(start, end);

		Map<Long, TeacherScheduleReport> reports = Maps.newHashMap();

		datas.forEach(data -> {
			Long teacherId = (Long) data[1];
			TeacherScheduleReport teacherScheduleReport = reports.get(teacherId);
			if (teacherScheduleReport == null) {
				teacherScheduleReport = new TeacherScheduleReport();
				teacherScheduleReport.setTeacherId(teacherId);
				teacherScheduleReport.setDate(start);
				teacherScheduleReport.setYear(yesterday.getYear());
				teacherScheduleReport.setMonth(yesterday.getMonthValue());
				teacherScheduleReport.setDay(yesterday.getDayOfMonth());
				teacherScheduleReport.setMorningTime(0);
				teacherScheduleReport.setAfternoonTime(0);
				teacherScheduleReport.setEveningTime(0);

				reports.put(teacherId, teacherScheduleReport);
			}
			// 开始时间
			int timeBegin = (int) data[2];

			// 结束时间
			int timeEnd = (int) data[3];

			// 上午开始（考虑实际情况，凌晨0-6点不加入逻辑）
			if (timeBegin < 1200) {
				// 上午结束
				if (timeEnd <= 1200) {
					teacherScheduleReport.setMorningTime(
							teacherScheduleReport.getMorningTime() + TimeUtil.interval(timeBegin, timeEnd));
					// 下午结束
				} else {
					teacherScheduleReport.setMorningTime(
							teacherScheduleReport.getMorningTime() + TimeUtil.interval(timeBegin, 1200));

					teacherScheduleReport.setAfternoonTime(
							teacherScheduleReport.getAfternoonTime() + TimeUtil.interval(1200, timeEnd));
				}
				// 下午开始
			} else if (timeBegin < 1800) {
				// 下午结束
				if (timeEnd <= 1800) {
					teacherScheduleReport.setAfternoonTime(
							teacherScheduleReport.getAfternoonTime() + TimeUtil.interval(timeBegin, timeEnd));
					// 晚上结束
				} else {
					teacherScheduleReport.setAfternoonTime(
							teacherScheduleReport.getAfternoonTime() + TimeUtil.interval(timeBegin, 1800));

					teacherScheduleReport
							.setEveningTime(teacherScheduleReport.getEveningTime() + TimeUtil.interval(1800, timeEnd));
				}
				// 晚上
			} else {
				teacherScheduleReport
						.setEveningTime(teacherScheduleReport.getEveningTime() + TimeUtil.interval(timeBegin, timeEnd));
			}
		});

		teacherScheduleReportRepository.save(reports.values());
	}

}
