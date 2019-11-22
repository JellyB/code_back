package com.huatu.ztk.pc.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Locale;

/**
 * 加解密 3Des，与课程相对应
 * Created by ht on 2016/10/12.
 */
public class Crypt3Des {

    //密钥
    private static final String key="0123456789QWEQWEEWQQ1234";
    private static final String Algorithm = "DESede"; // 定义 加密算法,可用  DES,DESede,Blowfish

    /**
     * 加密
     * @param ciphertext 密文的字符串
     * @return
     */
    public static String encryptMode(String ciphertext){
        try {
            byte[] keyBytes = key.getBytes();
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keyBytes, Algorithm);
            // 加密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return bytesToHexString(c1.doFinal(ciphertext.getBytes()));
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return "";
    }
    /**
     * 解密
     * @param plaintext  明文字符串
     * @return
     */
    public static String decrytpMode(String plaintext){
        try{
            byte[] keyBytes = key.getBytes();
            SecretKey deskey=new SecretKeySpec(keyBytes,Algorithm);
            // 解密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return  new String(c1.doFinal(hexToBytes(plaintext)));
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return "";
    }
    //转换成十六进制字符串
    public static String byte2hex(byte[] bytes) {
        String hs="";
        String stmp="";

        for (int n=0;n<bytes.length;n++) {
            stmp=(java.lang.Integer.toHexString(bytes[n] & 0XFF));
            if (stmp.length()==1) hs=hs+"0"+stmp;
            else hs=hs+stmp;
            if (n<bytes.length-1)  hs=hs+":";
        }
        return hs.toUpperCase();
    }

    /**
     * 将字节转换为字符串
     * @param bytes
     * @return
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 将字符转换为字节码
     * @param src
     * @return
     */
    public static byte[] hexStr2Bytes(String src) {
		/* 对输入值进行规范化整理 */
        src = src.trim().replace(" ", "").toUpperCase(Locale.US);
        // 处理值初始化
        int m = 0, n = 0;
        int iLen = src.length() / 2; // 计算长度
        byte[] ret = new byte[iLen]; // 分配存储空间

        for (int i = 0; i < iLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = (byte) (Integer.decode("0x" + src.substring(i * 2, m)
                    + src.substring(m, n)) & 0xFF);
        }
        return ret;
    }

    /**
     * 20161216
     * 将字符转换为16进制
     * @param hexString
     * @return
     */
    public static byte[] hexToBytes(String hexString) {
	/* 对输入值进行规范化整理 */
        hexString = hexString.trim().replace(" ", "").toUpperCase(Locale.US);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        String hexDigits = "0123456789ABCDEF";
        for (int i = 0; i < length; i++) {
            int pos = i * 2; // 两个字符对应一个byte
            int h = hexDigits.indexOf(hexChars[pos]) << 4; // 注1
            int l = hexDigits.indexOf(hexChars[pos + 1]); // 注2
            if(h == -1 || l == -1) { // 非16进制字符
                return null;
            }
            bytes[i] = (byte) (h | l);
        }
        return bytes;
    }



    public static void main(String[] args){
      //加密
        String plainText="rid=54191&username=app_55556101";
        System.out.println(encryptMode(plainText));

      //解密
        String cipherText="D9CE956381352AD3A26B4A5885AF36E41FA7CBC269184341C6B71D6F70C444A02085A293B54666C5";
        System.out.println(decrytpMode(cipherText));

        //解密
        System.out.println(decrytpMode("52663435799F9AAF220F4233DE3F67F4895BB044843258BB1FB56040922788D4F62D244D367E69F41810E3B7F7AEDD8D1A4BACB9347D48190864176B9F795A66FEFEED19EB9AF0C7E4B23BA5139629B22DF60900E09BCE524BD12844AE893B2B35DF4DD306CC1356BA6ECCA011E4359059C1F0DABE1A5C90C14ACE7AE557F10D5C4E64CAD1FB9056A59C95EE62F130996DCAD614FEEF21BDEDF6D99EF9ED4C07DFE253CBA998FF0A9F3C34AC95D1EC2716B3247EF353B0914268F153D4121CB4915C025B8F2D381D0FE2F42C0B77A0CB2FE57D1741DD81FC909155A9CE5D529B319BE0469338C3698BFD6D8317228284D4B6A2338F6DA53E391AE7014E057023133D1C0A998B8CC11F0D60208BF8B8FEEDD67206E6EF6B9189001B275E0509A75B52254B46A3D41E3699582D703948AD6558FE83263E72EF81E1F13897A1363AECC7E55CC82F65A338A568BDE80BF42C00ADFB4134D7FAEF2D462819D821BBC7996CD7B1FBBDE48B339CB8F4438902AD"));

        System.out.println(decrytpMode("52663435799F9AAF220F4233DE3F67F4E847ACC033F0252794D742A898672BF3EBD1105CCAE5E1676727FC829AFE8521D9E3199F8BDA277F7B7E6DCE88F42BA6CBD4CB6767EBBBDB89A3F5330BF63485FBA99041C826AACC7C9D23575D58DC174E85C25CBC975E2301BA1EA3C83A5B6E92B6E31DD936661A4E8D0E4EAF5E640E9ED2D7EAC9500560CB0C6DD56603BA4A3D07CD67E139779A4FE0FBFDBB5BA486139789348640BD1F312997840E92A9EAC31683E0E29986B1E64CB9064A6A48DAA7202B867389E94AC2DE21D6AE871AD16375D7836159967E7344D5FBA933AB0C4ACD3E4A38911029D2D7148CB6297467C9DBE0A5DA32D646596DCA4CF009253BC30E26503E99290FD6D9E706DB831C49D83F0A04F8AC7D8962A965F00D93A3E36F026C5AA7F7FB80B157C74B764F415FA945451FD6DD437C99DED79B0FE1C4D747635F000F038E12049D7C181E94ACF243E354F0C6FBD0D88462532B9B284697D8F41292917A94718032BF353BCA05F4A62BC56C3E0FC50A1F862A61A71D91A5432971640E5D5372EF97912DDB5A94FD3EDD3033DB32AF86C410B15563A311383434DB5E7F49747D411D9792B15723F8571491E47B82E9E4E0075FA241B8816C85D7B703D80F9BF885139077C2338CFF6BE1ED4693F5873F0342753447DCCCA989CF2C645B3ABF672E2A46634B12FC065AEE1E9C828B07325126141B1636F750C49A0B02D9C5439A6AE5ABFF94F48E9EEB5ED0F52B759EFEEEB8EA2CC2D15432B50FCD0D88079A254451388BD6AE842B80DFA657C2FAB808B2756314134B08540D9068C9045F1C0AA5E0AAFA4C0DA5A00DEDB7B63C105ECD"));

        System.out.println(decrytpMode("52663435799F9AAFC2DE21D6AE871AD16375D7836159967E53ABFDAB4C949266"));

        System.out.println(decrytpMode("52663435799F9AAFC2DE21D6AE871AD16375D7836159967E7344D5FBA933AB0C7533AD1A32621AB9D2D7148CB62974677AAE06C24F1DDB24378D5121810BC92B652E2CA25F1E8314094ADDC57C7830BD5682BB2FF0307166DB6243EE80FBD9E16DD2FFEBF089EBA102F880858F24F7C243FC812585A78C14569C9E8E004F00C0A1958FA8E05A563282CAFCEA24FFECDDB5DAA3AB30B90B450EF8231B625AF2501F862A61A71D91A5284D2AA74D9D30E29AC7F008679FC698D9B38E922F1C1063F73A69D09033FF17A8270FBCCDA68A7B3F88C4F951F25114FAF7DFF8BCA43FC0C9ADA841D2DB5666E0075FA241B8816C451ADAAD7BBBDBCB9F7EC67C2358D5A26B08FE91DEAF04B42A11E105809606D16B714F87CBF83AA26F94D94058AB45C3204314AC52F1541AAA49C885293B08B049C98819CCC948BF1E54E060EDA1FA4E004EC9A222487FC593EC9C7BC396857732AA5D518EE648538CA7A23D6B0C2EA464B49C0DB5BB6563FD0F3D32C187A90780FB4498132A2109FD2969B1CDAC88B7395AFE35B8FDE3CCBFDC26AB47A08F52EAAFEDD328C58CEAA17680D3254472F494252FEF72EDC138AAB39DA9452B37A71690256CAFA981EC8EA4A9E59CF5D2D3222A2ECDD13080026E991E5D2F4DE2F8FA9F37003C461EA96CF14E8D697A305C54E9570FB79861BECF4521E27D5407CD18368006AD5769278A0E6406664491DF14F3B2299256A7CB54BAE63EE29B4B47B0781409B802E50CCCD245F6D8EEE4A7FB233AC5A199564F8B19940AC37FEF7CBA68FA1E2CBA94C743236B5A82D8D2ED9FCAC5828088A714D2C3D2F1D6C95274"));
         }
}
