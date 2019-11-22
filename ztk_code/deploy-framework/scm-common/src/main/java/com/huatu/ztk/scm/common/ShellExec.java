package com.huatu.ztk.scm.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-15
 * Time: 下午4:43
 * To change this template use File | Settings | File Templates.
 */
public class ShellExec {
    private final static Logger logger = LoggerFactory.getLogger(ShellExec.class);

    /**
     * 执行脚本
     * @param args
     * @param shell
     * @return String[] [0]:shell exit value [1]:shell log
     */
    public static String[] exec( String[] shell,Map<String, String> args) {
        return exec(null,shell,args);
    }

    /**
     * 执行脚本
     * @param args
     * @param shell
     * @return String[] [0]:shell exit value [1]:shell log
     */
    public static String[] exec(String loggerName, String[] shell,Map<String, String> args){
        String[] arr = new String[2];
        StringBuilder buffer = new StringBuilder();
        System.out.println(Arrays.toString(shell));
        ProcessBuilder pb = new ProcessBuilder(shell);

        Map<String, String> sysenv = pb.environment();
        if (args != null) {
            sysenv.putAll(args);
        }

        pb.redirectErrorStream(true);

        Process process = null;
        BufferedReader br = null;
        try {
            process = pb.start();
            InputStream in = process.getInputStream();
            br = new BufferedReader(new InputStreamReader(in));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                buffer.append(tmp + "\r\n");
                logger.info("==> "+tmp);
                if(loggerName != null){
                    OperationResult.putByProjectName(loggerName, buffer.toString());
                }
            }
            in.close();
        } catch (IOException e) {
            logger.error("Exceprion",e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                logger.error("Exceprion",e);
            }
            if(process != null){
                process.destroy();
            }
            try{
                try {
                    arr[0]=process.waitFor()+"";
                } catch (InterruptedException e) {
                    arr[0]="1";
                    logger.error("Exceprion",e);
                }
            }catch (IllegalThreadStateException e) {//shell 脚本缺少exit
                arr[0]="0";
                logger.error("Exceprion",e);
            }
            arr[1] = buffer.toString();
        }
        logger.info("shell "+shell+" exec value="+arr[0]);
        return arr;
    }

    /**
     * 执行脚本
     * @param args
     * @param shell
     * @return String[] [0]:shell exit value [1]:shell log
     */
    public static String[] exec( String shell,Map<String, String> args){
        return exec(new String[]{shell},args);
    }

    public static String[] exec( String loggerName, String shell,Map<String, String> args){
        return exec(loggerName,new String[]{shell},args);
    }

    public static void main(String[] args) {
        String shell = "/home/shaojieyue/tools/shell/test.sh";
        exec(shell,null);
    }
}
