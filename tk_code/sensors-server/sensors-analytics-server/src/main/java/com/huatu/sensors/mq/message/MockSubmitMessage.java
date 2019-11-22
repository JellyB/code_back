package com.huatu.sensors.mq.message;

import java.io.Serializable;

import lombok.Data;

/**
 * 交卷msg
 * 
 * @author zhangchong
 *
 */
@Data
public class MockSubmitMessage implements Serializable {
	private static final long serialVersionUID = -4731326195678504565L;

	private String token;
	private long practiceId;
	private long userId;
	private int area;
	private String uname;
	private int cardType;
	private int terminal;
}
