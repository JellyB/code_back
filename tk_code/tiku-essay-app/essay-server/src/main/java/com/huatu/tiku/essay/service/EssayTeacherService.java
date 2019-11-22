package com.huatu.tiku.essay.service;

import com.ht.base.user.module.security.UserInfo;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.EssayTeacherOrderType;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.req.CreateOrUpdateTeacherReq;
import com.huatu.tiku.essay.vo.req.FetchTeacherReq;
import com.huatu.tiku.essay.vo.req.TeacherOrderTypeReq;
import com.huatu.tiku.essay.vo.resp.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2019/7/10
 */
public interface EssayTeacherService {


	List<UCenterTeacherVo> listTeacher(String name);

	Long addOrUpdateTeacher(CreateOrUpdateTeacherReq teacherVO);

	PageUtil<List<TeacherVo>> fetchTeacher(FetchTeacherReq fetchTeacherReq);

	TeacherDetailVo teacherDetail(Long teacherId);

	PageUtil<List<CorrectOrderVo>> fetchCorrectOrder(Long teacherId, Integer page, Integer pageSize);

	UserInfo getUserInfo();

	/**
	 * 获取老师个人配置
	 * @param uCentername
	 * @return
	 */
	Object getSettings(String uCentername);

	EssayTeacher findById(long teacherId);

	/**
	 * 修改个人配置
	 * @param teacherOrderTypeReq
	 * @return
	 */
	Integer updateTeacherSettings(List<TeacherOrderTypeReq> teacherOrderTypeReq);

	/**
	 * 薪资列表
	 *
	 * @param teacherId 用户名
	 * @param startDate 开始时间
	 * @param endDate   结束时间
	 * @return 薪资列表
	 */
	List<TeacherSalaryVo> getSalaryList(Long teacherId, Date startDate, Date endDate);

	/**
	 * 查看老师工作量是否饱和
	 * @param orderType
	 * @return
	 */
	boolean checkCanCorrect(Integer orderType);


	void validTeacherIsMe(long teacherId,String message);

	List<EssayTeacherOrderType> findCanCorrectTeachers(int orderType);

	EssayTeacherOrderType findTeacherOrderType(int orderType,long teacherId);

	/**
	 * 任务分配时的老师列表
	 * @param fetchTeacherReq
	 * @return
	 */
	PageUtil fetchDistributionTeacher(FetchTeacherReq fetchTeacherReq);

	/**
	 * 初始化当天的数量
	 */
    void initTodayAmount();

	/**
	 * 根据权限中心ID查询老师
	 *
	 * @param uCenterId 权限中心ID
	 * @return 老师信息
	 */
	EssayTeacher getTeacherByUCenterId(Long uCenterId);
}


