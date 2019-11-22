package com.huatu.ztk.scm.common;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-16
 * Time: 下午6:56
 * To change this template use File | Settings | File Templates.
 */
public class Paths {
    public final static String HOME = System.getProperty("HOME", "/opt/scm/");
    public final static String BIN_HOME = "/servers/agent/bin";
    public final static String APP_HOME = HOME + "/app";
    public final static String LOG_HOME = HOME + "/log";
    public final static String PID_HOME = HOME + "/run";

}
