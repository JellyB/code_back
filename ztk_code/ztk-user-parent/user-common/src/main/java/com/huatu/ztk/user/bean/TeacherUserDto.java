package com.huatu.ztk.user.bean;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教师网用户dto
 * 
 * @author zhangchong
 *
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class TeacherUserDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private String mobile;// 手机号
	private String email;// 邮箱
	private String name;// 用户名,该字段不要更改,跟网校有联系
	private String nick;// 昵称,显示用户姓名用这个字段
	private Integer subject;// 用户练习的科目
	private String avatar;// 头像地址 例如：http://xxx/header.png
	private Integer regFrom;// 注册来源
	private Long createTime;// 创建时间
	private String password;
	private Long reChargeCion;// 充值金币
	private Long taskCion;// 任务金币
	private String openId;//三方登录id
	private Integer source; // 三方登录来源 0 无 1qq 2微信

}
