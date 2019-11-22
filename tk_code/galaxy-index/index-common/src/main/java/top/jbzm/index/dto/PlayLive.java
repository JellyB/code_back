package top.jbzm.index.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 直播观看实体类
 * @author: wangjian
 * @create: 2018-03-26 12:29
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayLive{

    private Date date;//日期
    private Integer user_number;//用户的ID号
    private String user_name;//昵称
    private Integer user_role;//0:学生 1:老师 2:助教
    private Date first_time;//最早进入教室时间
    private Date last_time;//最晚离开时间
    private Date first_heartbeat_time;//最早开始听课时间
    private Date last_heartbeat_time;//	最晚开始听课时间
    private Integer actual_listen_time;//实际听课时长（单位秒)
    private String user_ip;//	用户IP
    private String network_operator;//使用的网络运营商
    private Integer client_type;//0:PC网页 1:pc客户端 2:m站 3:ios 4:android 5:mac客户端
    private String area;//用户所属省份
    private String city;//	用户所属城市

}
