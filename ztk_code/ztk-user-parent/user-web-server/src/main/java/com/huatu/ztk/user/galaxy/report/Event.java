package com.huatu.ztk.user.galaxy.report;

/**
 * @author zhengyi
 * @date 2019-01-10 17:12
 **/
public class Event {
    /**
     * user id
     */
    private long id;
    /**
     * user name
     */
    private String name;
    /**
     * create time
     */
    private final long createTime = System.currentTimeMillis();
    /**
     * event name
     */
    private String eventName;

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

    public long getCreateTime() {
        return createTime;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public static void main(String[] args) throws InterruptedException {
        Event event = new Event();
        System.out.println(event.getCreateTime());
        Thread.sleep(1000);
        System.out.println(new Event().getCreateTime());
        Thread.sleep(1000);
        System.out.println(new Event().getCreateTime());
        System.out.println(event.getCreateTime());
    }
}