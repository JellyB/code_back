package com.huatu.tiku.essay.vo.video;

import lombok.Getter;
import lombok.Setter;

/**
 * 百家云返回值
 */
@Getter
@Setter
public class PlayerToken {

	/**
	 * 视频ID
	 */
	private String video_id;

	/**
	 * Token
	 */
	private String token;
}
