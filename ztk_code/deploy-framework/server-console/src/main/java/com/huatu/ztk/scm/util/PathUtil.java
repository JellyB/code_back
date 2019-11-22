package com.huatu.ztk.scm.util;
/**
 * 获取路径的工具包
 * @author shaojieyue
 * @date 2013-08-21 10:22:24
 */
public class PathUtil {
	/**
	 * 获取当前服务的classpath路径
	 * @return
	 */
	public static String getClassesDir(){
		String path=PathUtil.class.getResource("").getPath();
		path=path.split("/classes/")[0]+"/classes/";
		return path;
	}
}
