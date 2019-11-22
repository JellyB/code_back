package com.ht.galaxy.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 科目
 * 
 * @author Geek-S
 *
 */
public class Subject {

	public static final Map<String, String> SUBJECTS;

	static {
		SUBJECTS = new HashMap<>();
		SUBJECTS.put("1", "公务员考试");
		SUBJECTS.put("2", "事业单位考试");
		SUBJECTS.put("3", "教师考试");
		SUBJECTS.put("4", "事业单位-公共基础");
		SUBJECTS.put("5", "事业单位-行测");
		SUBJECTS.put("6", "医疗");
		SUBJECTS.put("7", "资格考试");
		SUBJECTS.put("8", "护士资格");
		SUBJECTS.put("9", "医师资格");
		SUBJECTS.put("10", "事业单位招聘");
		SUBJECTS.put("11", "公务员考试");
	}

	/**
	 * 根据ID返回科目
	 * 
	 * @param key
	 *            科目ID
	 * @return 科目
	 */
	public static String getSubject(String key) {
		return SUBJECTS.get(key);
	}
}
