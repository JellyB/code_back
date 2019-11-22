package com.huatu.ztk.user.galaxy;

/**
 * @author zhengyi
 * @date 2018/7/25 3:00 PM
 **/
public class UserRegister {
    /**
     * user id
     */
    private long id;
    /**
     * user name
     */
    private String name;
    /**
     * register from
     */
    private String regFrom;
    /**
     * create time
     */
    private long createTime;
    /**
     * create terminal
     */
    private int terminal;
    /**
     * user register url
     */
    private String url;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegFrom() {
        return regFrom;
    }

    public void setRegFrom(String regFrom) {
        this.regFrom = regFrom;
    }

    public int getTerminal() {
        return terminal;
    }

    public void setTerminal(int terminal) {
        this.terminal = terminal;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UserRegister(long id, String name, String regFrom, long createTime, String url, int terminal) {
        this.id = id;
        this.name = name;
        this.regFrom = regFrom;
        this.createTime = createTime;
        this.url = url;
        this.terminal = terminal;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}