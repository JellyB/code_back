/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.dto;

/**
 *
 * @author wenpingliu
 * @version v 0.1 15/9/16 22:26 wenpingliu Exp $$
 */
public class TicketLog {
    int id;
    int ticketId;
    String note;
    String createBy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    @Override
    public String toString() {
        return "TicketLog{" +
               "id=" + id +
               ", ticketId=" + ticketId +
               ", note='" + note + '\'' +
               ", createBy='" + createBy + '\'' +
               '}';
    }
}
