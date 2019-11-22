package com.huatu.ztk.scm.common;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-7-26
 * Time: 上午12:24
 * To change this template use File | Settings | File Templates.
 */
public class PackDto {
    private final int type; //1: 线上+tag+develop分支, 2: 开发+分支(不指定tag), 3: 开发+tag,   4: 测试+tag,  5:测试+分支
    private final String git_home;
    private final String project;
    private final String module;
    private final String environment;
    private final String tag;
    private final String branch;
    private final String remark;
    private final boolean updateDependency;
    private int pack_type = 1;//打包类型，1：zip， 2.war

    public PackDto(int type, String git_home, String project, String module, String environment, String tag, String branch,String remark,boolean updateDependency) {
        this.type = type;
        this.git_home = git_home;
        this.project = project;
        this.module = module;
        this.tag = tag;
        this.environment=environment;
        this.branch = branch;
        this.remark = remark;
        this.updateDependency = updateDependency;
    }

    int getPack_type() {
        return pack_type;
    }

    void setPack_type(int pack_type) {
        this.pack_type = pack_type;
    }

    int getType() {
        return type;
    }

    String getGit_home() {
        return git_home;
    }

    String getProject() {
        return project;
    }

    String getModule() {
        return module;
    }

    String getTag() {
        return tag;
    }

    String getBranch() {
        return branch;
    }

	public String getEnvironment() {
		return environment;
	}

	@Override
	public String toString() {
		return "PackDto [type=" + type + ", git_home=" + git_home
				+ ", project=" + project + ", module=" + module
				+ ", environment=" + environment + ", tag=" + tag + ", branch="
				+ branch + ", pack_type=" + pack_type + "]";
	}

	public String getRemark() {
		return remark;
	}

	public boolean getUpdateDependency() {
		return updateDependency;
	}

}
