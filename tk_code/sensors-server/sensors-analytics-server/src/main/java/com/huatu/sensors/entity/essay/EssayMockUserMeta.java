package com.huatu.sensors.entity.essay;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  申论模考用户报名信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "v_essay_mock_user_meta")
public class EssayMockUserMeta {

	private Integer userId; // 用户id
	private Long paperId; // 试卷id
	private Integer positionCount; // 职位报名人数
	// 答题卡id
	private Long practiceId; // 练习id

	@Id
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.IDENTITY)

	protected Long id;
	protected Integer bizStatus;
	protected Integer status;
	protected String creator;
	protected String modifier;
	protected Date gmtCreate;
	protected Date gmtModify;

}
