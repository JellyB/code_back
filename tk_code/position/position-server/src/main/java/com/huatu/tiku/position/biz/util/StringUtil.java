package com.huatu.tiku.position.biz.util;

/**
 * 字符串工具类
 * 
 * @author geek-s
 *
 */
public class StringUtil {

	/**
	 * 获取字符串长度
	 * 
	 * @param string
	 *            字符串
	 * @return 字节长度（中文2字节）
	 */
	public static int getLength(String string) {
		int length = 0;

		for (int i = 0; i < string.length(); i++) {
			int ascii = Character.codePointAt(string, i);
			if (ascii >= 0 && ascii <= 255)
				length++;
			else
				length += 2;
		}

		return length;
	}
}
