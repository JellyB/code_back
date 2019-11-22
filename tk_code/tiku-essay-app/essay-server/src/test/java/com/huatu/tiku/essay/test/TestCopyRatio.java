package com.huatu.tiku.essay.test;

import org.apache.commons.lang3.StringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Enumeration;

/**
 * @author zhaoxi
 * @Description: 抄袭率测试类
 * @date 2018/8/3下午3:46
 */
public class TestCopyRatio {
    public static void main(String args[]){

//        String answer = "动在网络社区风生水起，求助帖、微博微信劝募、淘宝义卖等活动受到人们的极大关注。网络慈善利用互联网无边界、传播广等优势，通过多元主体参与、线上线下联动等方式，聚沙成塔，集腋成裘，最大限度汇聚着社会爱心，凝聚着社会求助力量，传递爱的正能量。\n" +
//                "       网络慈善有许多成功、感";
//
//        String material = "4.近年来，随着互联网的发展，各种名义的网上慈善活动在网络社区风生水起，求助帖、微博微信劝募、淘宝义卖等活动受到人们的极大关注。网络慈善利用互联网无边界、传播广等优势，通过多元主体参与、线上线下联动等方式，聚沙成塔，集腋成裘，最大限度汇聚着社会爱心，凝聚着社会求助力量，传递爱的正能量。\n" +
//                "       网络慈善有许多成功、感人的范例。例如，贵州、浙江等地民间公益人士发起的“免费午餐”计划通过网络民间慈善组织与公募基金联手的方式，把慈善账本“晒”在阳光下，募集到1000多万元资金，4000多名小学生因此而受益。\n" +
//                "       然而我们也应清醒看到，利用互联网这一高效便捷的慈善方式尚处于上升成长阶段，在运行过程中存在一些隐患和风险，需要审慎对待。比如，由于网络世界的虚拟性让爱心人士不能直接接触受救助对象，捐助者在网络捐赠时容易陷入“感情用事”的误区。而一些不法分子为了达到个人目的也不惜透支公众的信任和爱心，骗捐诈捐事件时有发生。\n" +
//                "       2015年6月，知乎网用户“ck小小”向网友自称是患有先天性心脏病的22岁女大学生，花光了父母积蓄进行的心脏介入手术却失败了，陷入绝望中，不久，拥有59600粉丝的知乎网大V“童瑶”在回答中鼓励“ck小小”，并表示自己也是先天性心脏病患者，愿意为“ck小小”提供经济上的帮助，这一回答获得了1.4万个赞。次日，“ck小小”公开了自己的支付宝账号，表示“假如我下次手术还能活下来，将来参加工作一定如数归还”。2016年1月14日，多位用户举报知乎网大V“童瑶”与“ck小小”疑似“唱双簧”，一位谎称疾病，另一位为其募捐，涉嫌诈骗数百用户捐款15万元。1月19日，在知乎网用“ck小小”与“童瑶”两个账号上演双簧苦情戏、骗取网友数十万元捐款的犯罪嫌疑人童某，向其户籍所在地公安局投案自首。\n" +
//                "       中国";
//        String longestCommonSubstring = longestCommonSubstring(answer, material);
//        double copyRatio = 0D;
//        if(StringUtils.isNotEmpty(answer)){
//            copyRatio = (double)longestCommonSubstring.length() / answer.length();
//        }
//        double copyRatio = 0.822222D;
//
//        copyRatio = KeepTwoDecimal(copyRatio);
//        System.out.println("====copyRatio===="+copyRatio);

        String serverIp = null;
        try {
            serverIp = getServerIp();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(serverIp);
    }


    private static String getServerIp() throws UnknownHostException {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                        return ia.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
//        InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址 //PC-20140317PXKX/192.168.0.121
//        String hostAddress = address.getHostAddress();//192.168.0.121
//        return hostAddress;
    }

    private static String longestCommonSubstring(String strA, String strB) {
        strA = strA.replaceAll("[^\\u4e00-\\u9fa5]","");//去除所有非中文字符
        strB = strB.replaceAll("[^\\u4e00-\\u9fa5]","");
        char[] chars_strA = strA.toCharArray();
        char[] chars_strB = strB.toCharArray();
        int m = chars_strA.length;
        int n = chars_strB.length;
        int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (chars_strA[i - 1] == chars_strB[j - 1])
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                else
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
            }
        }

        char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[m][n] == matrix[m][n - 1])
                n--;
            else if (matrix[m][n] == matrix[m - 1][n])
                m--;
            else {
                result[currentIndex] = chars_strA[m - 1];
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }



    /**
     * 小数点后保留两位小数
     * @param aDouble
     * @return
     */
    public static Double KeepTwoDecimal(Double aDouble) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (aDouble == null) {
            return 0x0.0p0;
        } else {
            return Double.valueOf(decimalFormat.format(aDouble));
        }
    }


}
