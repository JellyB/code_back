package com.huatu.tiku.schedule.biz.domain;

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherLevel;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;

import lombok.Getter;
import lombok.Setter;

/**
 * 教师
 * 
 * @author Geek-S
 *
 */
@Entity
@Getter
@Setter
public class Teacher extends BaseDomain {

	private static final long serialVersionUID = 5730966574014090038L;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 考试类型
	 */
	private ExamType examType;

	/**
	 * 科目
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subjectId", insertable = false, updatable = false)
	private Subject subject;

	/**
	 * 科目id
	 */
	private Long subjectId;

	/**
	 * 类型 0 组员 1 组长
	 */
	private Boolean leaderFlag;

	/**
	 * 教师类型
	 */
	private TeacherType teacherType;

	/**
	 * 教师职级
	 */
	private TeacherLevel teacherLevel;

	/**
	 * 手机
	 */
	@Column(unique = true)
	private String phone;

	/**
	 * 微信
	 */
	private String wechat;

	/**
	 * 密码
	 */
	private String password;

	/**
	 * 状态
	 */
	private TeacherStatus status;

	/**
	 * 录入人ID
	 */
	private Long createrId;

	/**
	 * 关联授课级别
	 */
	@OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
	private List<TeacherSubject> teacherSubjects;

	/**
	 * 教师角色
	 */
	@ManyToMany
	@JoinTable(joinColumns = @JoinColumn(name = "teacher_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;

	/**
	 * 数据权限
	 */
	@Column(name = "data_permission")
	@ElementCollection
	@JoinTable(joinColumns = @JoinColumn(name = "teacher_id"))
	private Set<ExamType> dataPermissions;

	/**
	 * php关联id
	 */
	private Long pid;

	//是否兼职
	private Boolean isPartTime;

	//是否是教师 true表示不是教师 排名时去除
	private Boolean isInvalid;
}
