package com.huatu.tiku.course.service.v1;

/**
 * 
 * @author zhangchong
 *
 */
public interface ActivityService {

	/**
	 * 618活动签到送图币
	 * @param userName
	 * @param ucId
	 */
	int signGiveCoin(String userName, String ucId);

	/**
	 * 签到记录
	 * @param uname
	 * @return
	 */
	Object signList(String uname);

	/**
	 * 活动数据上报
	 * @param userName
	 * @param terminal
	 * @param cv
	 * @param day
	 * @return
	 */
	Object report(String userName, int terminal, String cv, String day);

	/**
	 * appstore评价送图币
	 * @param id
	 * @param uname
	 * @param terminal
	 * @param cv
	 * @return
	 */
	Object appStoreEvalution(int uid, String uname, Integer terminal, String cv);
}
