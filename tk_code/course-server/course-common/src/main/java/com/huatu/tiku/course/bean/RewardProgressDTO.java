package com.huatu.tiku.course.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author hanchao
 * @date 2017/10/14 13:48
 */
@Data
@Builder
public class RewardProgressDTO {
    private int time;
    private int limit;
    private boolean over;
    private String bizName;
    private String action;
    private List<String> bizIdList;


    public boolean isOver(){
        return limit <= time ;
    }
}
