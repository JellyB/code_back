package com.huatu.tiku.schedule.biz.service.intelligence;

import com.huatu.tiku.schedule.biz.service.intelligence.dataevent.TeacherDataEvent;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
public class DefaultSchedulePipLine implements SchedulePipLine {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSchedulePipLine.class);

    private volatile DefaultScheduleHandlerContext header;

    private volatile DefaultScheduleHandlerContext tail;


    public void execute(DefaultScheduleHandlerContext ctx, TeacherDataEvent event) {
        ctx.getScheduleHandler().work(ctx, event);
    }

    @Override
    public void execute(TeacherDataEvent event) {
        DefaultScheduleHandlerContext head = this.header;
        if (head == null) {
            LOG.warn(
                    "The pipeline contains no execute handlers; discarding: " + event);
            return;
        }
        execute(header, event);
    }

    @Override
    public void addLast(ScheduleHandler handler) {
        if(header == null){
            DefaultScheduleHandlerContext  ctx = new DefaultScheduleHandlerContext(null, null, handler);
            header = tail = ctx;
        } else {
            DefaultScheduleHandlerContext oldTail = tail;
            DefaultScheduleHandlerContext newTail = new DefaultScheduleHandlerContext(oldTail, null, handler);
            oldTail.next = newTail;
            tail = newTail;

        }
    }

    @Data
    private final class DefaultScheduleHandlerContext implements ScheduleHandlerContext {

        volatile DefaultScheduleHandlerContext next;
        volatile DefaultScheduleHandlerContext prev;
        private final ScheduleHandler scheduleHandler;

        DefaultScheduleHandlerContext(DefaultScheduleHandlerContext prev, DefaultScheduleHandlerContext next, ScheduleHandler handler){
            this.prev = prev;
            this.next = next;
            this.scheduleHandler = handler;
        }

        @Override
        public void executeNext(TeacherDataEvent teacherDataEvent) {

            List<TeacherDataEvent.TeacherData> teacherDatas = teacherDataEvent.getTeacherDates();

            DefaultScheduleHandlerContext next = this.next;
            if(next != null){
                DefaultSchedulePipLine.this.execute(next, teacherDataEvent);
            }

        }
    }

}
