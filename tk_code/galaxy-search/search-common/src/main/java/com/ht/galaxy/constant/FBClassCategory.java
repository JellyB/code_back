package com.ht.galaxy.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程分类
 *
 * @author Geek-S
 *
 */
public class FBClassCategory {

	public static final Map<String, String> SUBJECT_CATEGORY;

	static {
		SUBJECT_CATEGORY = new HashMap<>();
		SUBJECT_CATEGORY.put("gwy", "公务员");
		SUBJECT_CATEGORY.put("zj", "事业单位");
		SUBJECT_CATEGORY.put("kaoyan", "考研");
		SUBJECT_CATEGORY.put("kuaiji", "会计");
		SUBJECT_CATEGORY.put("jzs", "招警");
		SUBJECT_CATEGORY.put("yixue", "医学");
		SUBJECT_CATEGORY.put("yingyu", "英语");
		SUBJECT_CATEGORY.put("sikao", "法律");
		SUBJECT_CATEGORY.put("it", "计算机");

	}

	/**
	 * 根据ID返回分类
	 *
	 * @param key
	 *            分类ID
	 * @return 分类
	 */
	public static String getCategory(String key) {
		return SUBJECT_CATEGORY.get(key);
	}

}
