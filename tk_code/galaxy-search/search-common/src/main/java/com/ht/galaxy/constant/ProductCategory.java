package com.ht.galaxy.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 课程分类
 * 
 * @author Geek-S
 *
 */
public class ProductCategory {

	public static final Map<String, String> PRODUCT_CATEGORY;

	static {
		PRODUCT_CATEGORY = new HashMap<>();
		PRODUCT_CATEGORY.put("1000", "公务员");
		PRODUCT_CATEGORY.put("1001", "事业单位");
		PRODUCT_CATEGORY.put("1002", "公检法");
		PRODUCT_CATEGORY.put("1003", "教师");
		PRODUCT_CATEGORY.put("1004", "医疗");
		PRODUCT_CATEGORY.put("1005", "金融");
		PRODUCT_CATEGORY.put("1006", "其他");
		PRODUCT_CATEGORY.put("1007", "招警");
		PRODUCT_CATEGORY.put("1008", "遴选");
		PRODUCT_CATEGORY.put("1009", "军转");
		PRODUCT_CATEGORY.put("1010", "国家电网");
	}

	/**
	 * 根据ID返回分类
	 * 
	 * @param key
	 *            分类ID
	 * @return 分类
	 */
	public static String getCategory(String key) {
		return PRODUCT_CATEGORY.get(key);
	}

}
