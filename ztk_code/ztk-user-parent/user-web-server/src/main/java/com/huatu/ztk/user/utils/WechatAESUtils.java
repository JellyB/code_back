package com.huatu.ztk.user.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 微信解密工具类
 * @author zhangchong
 *
 */
public class WechatAESUtils {

	public static String decryptWXAppletInfo(String sessionKey, String encryptedData, String iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		String result = null;
		byte[] encrypData = Base64.decodeBase64(encryptedData);
		byte[] ivData = Base64.decodeBase64(iv);
		byte[] sessionKeyB = Base64.decodeBase64(sessionKey);
		//Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivData);
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec keySpec = new SecretKeySpec(sessionKeyB, "AES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		byte[] doFinal = cipher.doFinal(encrypData);
		result = new String(doFinal);
		return result;
	}

}
