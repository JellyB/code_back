package com.huatu.sensors.mq.message;

import java.io.Serializable;

import lombok.Data;

/**
 * 确认报名消息
 * 
 * @author zhangchong
 *
 */
@Data
public class EnrollMatchMessage implements Serializable {
	private static final long serialVersionUID = -4731326195678504565L;

	private String token;
	private int matchId;
	private long userId;
	private int positionId;
	private int subject;
	private int terminal;

}
