
package com.huatu.ztk.scm.common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * the Service payload
 */
//@JsonRootName("instance_payload")
public class InstanceDetail {

    private int type = 1;//1: java application; 2. web war
    private int stat = 0;//0: init, 1: online, 2: offline,

    private String action;
    private String serverName;
    private String serverIp;
    private String projectName;

    private String mainClass;
    private String mainArgs;
    private String vmArgs;
    private String sourcePath;
    private String serverMode;
    public enum Key {
        main_class, main_args, jvm_args, payload, project_name, server_ip, server_name, action, stat, source_path, server_mode
    }

    /**
     * instance's status
     */
    enum Stat {

        INIT(0), RUNNING(1), STOPPED(2), OFFLINE(3), DELETED(4);

        public int Value;

        Stat(int i) {
            this.Value = i;
        }
    }

    public InstanceDetail() {
    }

    public InstanceDetail(String action, String serverName, String serverIp) {
        this.action = action;
        this.serverName = serverName;
        this.serverIp = serverIp;
    }

    public int getStat() {
        return stat;
    }

    public InstanceDetail setStat(int stat) {
        this.stat = stat;
        return this;
    }

    public String getMainClass() {
        return mainClass;
    }

    public InstanceDetail setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public String getMainArgs() {
        return mainArgs;
    }

    public InstanceDetail setMainArgs(String mainArgs) {
        this.mainArgs = mainArgs;
        return this;
    }

    public String getVmArgs() {
        return vmArgs;
    }

    public InstanceDetail setVmArgs(String vmArgs) {
        this.vmArgs = vmArgs;
        return this;
    }

    public int getType() {
        return type;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public InstanceDetail setType(int type) {
        this.type = type;
        return this;
    }

    public String getAction() {
        return action;
    }

    public String getProjectName() {
        return projectName;
    }

    public InstanceDetail setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }


    public String getServerIp() {
        return serverIp;
    }

    public String getServerName() {
        return serverName;
    }


    public String getSourcePath() {
        return sourcePath;
    }

    public InstanceDetail setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }


    public String getServerMode() {
        return serverMode;
    }

    public void setServerMode(String serverMode) {
        this.serverMode = serverMode;
    }


    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Key.main_class.name(),mainClass);
            jsonObject.put(Key.main_args.name(),mainArgs);
            jsonObject.put(Key.jvm_args.name(),vmArgs);
            jsonObject.put(Key.project_name.name(),projectName);
            jsonObject.put(Key.server_ip.name(),serverIp);
            jsonObject.put(Key.server_name.name(),serverName);
            jsonObject.put(Key.action.name(),action);
            jsonObject.put(Key.stat.name(),stat);
            jsonObject.put(Key.source_path.name(),sourcePath);
            jsonObject.put(Key.server_mode.name(),serverMode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


}
