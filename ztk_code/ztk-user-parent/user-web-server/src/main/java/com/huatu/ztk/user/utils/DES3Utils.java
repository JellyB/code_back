package com.huatu.ztk.user.utils;

import com.alibaba.dubbo.common.URL;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.security.Credential;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DES3Utils {

	private static final String Algorithm = "DESede"; // 定义 加密算法,可用
														// DES,DESede,Blowfish
	private static final String APPLET_KEY = "3D12S4B41A43T8D4064C8Z3D";

	private static SecretKey key;
	static {

		try {
			key = new SecretKeySpec(build3DesKey(APPLET_KEY), Algorithm);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}





	//构建3DES密钥
	public static byte[] build3DesKey(String keyStr) throws UnsupportedEncodingException {
		byte[] key = new byte[24];    //声明一个24位的字节数组，默认里面都是0
		byte[] temp = keyStr.getBytes("UTF-8");    //将字符串转成字节数组
		/*
		 * 执行数组拷贝
		 * System.arraycopy(源数组，从源数组哪里开始拷贝，目标数组，拷贝多少位)
		 */
		if(key.length > temp.length){
			//如果temp不够24位，则拷贝temp数组整个长度的内容到key数组中
			System.arraycopy(temp, 0, key, 0, temp.length);
		}else{
			//如果temp大于24位，则拷贝temp数组24个长度的内容到key数组中
			System.arraycopy(temp, 0, key, 0, key.length);
		}
		return key;
	}
	/**
	 * 加密String明文输入,String密文输出
	 */
	public static String encrypt(String strMing) {
		byte[] byteMi = null;
		byte[] byteMing = null;
		String strMi = "";
		BASE64Encoder base64en = new BASE64Encoder();
		try {
			byteMing = strMing.getBytes("UTF8");
			byteMi = getEncCode(byteMing);
			strMi = base64en.encode(byteMi);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing SqlMap class. Cause: " + e);
		} finally {
			base64en = null;
			byteMing = null;
			byteMi = null;
		}
		return strMi;
	}

	/**
	 * 解密 以String密文输入,String明文输出
	 *
	 * @param strMi
	 * @return
	 */
	public static String decrypt(String strMi) {
		BASE64Decoder base64De = new BASE64Decoder();
		byte[] byteMing = null;
		byte[] byteMi = null;
		String strMing = "";
		try {
			byteMi = base64De.decodeBuffer(strMi);
			byteMing = getDesCode(byteMi);
			strMing = new String(byteMing, "UTF8");
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing SqlMap class. Cause: " + e);
		} finally {
			base64De = null;
			byteMing = null;
			byteMi = null;
		}
		return strMing;
	}

	/**
	 * 加密以byte[]明文输入,byte[]密文输出
	 *
	 * @param byteS
	 * @return
	 */
	private static byte[] getEncCode(byte[] byteS) {
		byte[] byteFina = null;
		Cipher cipher;
		try {//对比DES
			cipher = Cipher.getInstance(Algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byteFina = cipher.doFinal(byteS);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing SqlMap class. Cause: " + e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}

	/**
	 * 解密以byte[]密文输入,以byte[]明文输出
	 *
	 * @param byteD
	 * @return
	 */
	private static byte[] getDesCode(byte[] byteD) {
		Cipher cipher;
		byte[] byteFina = null;
		try {//对比DES
			cipher = Cipher.getInstance("DESede");

			cipher.init(Cipher.DECRYPT_MODE, key);
			byteFina = cipher.doFinal(byteD);
		} catch (Exception e) {
			throw new RuntimeException(
					"Error initializing SqlMap class. Cause: " + e);
		} finally {
			cipher = null;
		}
		return byteFina;
	}



	public static void main(String args[])  {
		DES3Utils des = new DES3Utils();
		String str1 = "18531990086";
		// DES加密
		String str2 = des.encrypt(str1);
		DES3Utils des1 = new DES3Utils();
		String encode = URL.decode("%2BZ8IIHc0JPLcycA9m6EBhQ%3D%3D");
		String deStr = des1.decrypt(str2);
		System.out.println("密文:" + UrlEncoded.encodeString(str2));
		// DES解密
		System.out.println("明文:" + deStr);
		System.out.println("明文:" + des1.decrypt(encode));

	}

}
