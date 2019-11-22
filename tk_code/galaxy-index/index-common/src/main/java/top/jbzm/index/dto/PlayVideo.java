package top.jbzm.index.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 点播观看实体类
 * @author: wangjian
 * @create: 2018-03-26 12:29
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayVideo {

    private String guid;//一次播放的唯一标志，32位字符串
    private String uuid;//客户端随机生成的用户的唯一标志
    private Integer video_id;//回放对应的视频id
    private String user_name;//用户名（需要客户在接入播放器的时候传入用户的信息）
    private String user_number;//用户number号（需要客户在接入播放器的时候传入用户的信息）
    private Date play_begin_time;//起始播放时间，格式如：2017-09-08 10:00:07
    private Date play_end_time;//起始播放时间，格式如：2017-09-08 10:00:07
    private Integer client_type;//客户端类型1：iphone 2:ipad 3：Android 4：手机M站 5：PC 网页 0:未知
    private Integer play_length;//实际观看时间，单位：秒
    private String user_ip;//	用户IP
    private String area;//用户所在地域，格式如：省份 市。可能会为空
    private Integer room_id;//	回放教室号（只有回放视频才有该字段）

}
