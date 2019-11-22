package com.huatu.tiku.essay.util.pay;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * 支付宝工具类
 * 
 * @author zhangchong
 *
 */
public class AliPayUtil {

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";
	
	private static final String ALGORITHM = "RSA";
	
	 private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";



	/**
	 * 签名
	 * 
	 * @param content
	 * @param flag    0在线 1教育
	 * @return
	 */
	public static String sign(String content, Integer flag) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(flag == 0 ? AliPayConfig.private_key : AliPayConfig.edu_private_key));
			KeyFactory keyf = KeyFactory.getInstance(AliPayConfig.sign_type);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(AliPayConfig.input_charset));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	   
	 /**
	  * 新版支付签名
	  * @param content
	  * @param privateKey
	  * @param rsa2
	  * @return
	  */
	public static String signV2(String content,Integer flag) {
	      try {
	         PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
	               Base64.decode(AliPayConfig.private_key_v2));
	         KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
	         PrivateKey priKey = keyf.generatePrivate(priPKCS8);

	         java.security.Signature signature = java.security.Signature
	               .getInstance(SIGN_SHA256RSA_ALGORITHMS);

	         signature.initSign(priKey);
	         signature.update(content.getBytes(AliPayConfig.input_charset));

	         byte[] signed = signature.sign();

	         return Base64.encode(signed);
	      } catch (Exception e) {
	         e.printStackTrace();
	      }

	      return null;
	   }

	/**
	 * 新帐号下的rsa1签名方式
	 * @param content
	 * @param flag
	 * @return
	 */
	public static String signV3(String content, Integer flag) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(flag == 0 ? AliPayConfigNew.private_key_rsa1 : AliPayConfig.edu_private_key));
			KeyFactory keyf = KeyFactory.getInstance(AliPayConfig.sign_type);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(AliPayConfig.input_charset));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static void main(String[] args) {
		String s = signV3("111", 0);
		System.out.println(s);
	}


	

}
