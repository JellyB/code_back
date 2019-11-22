package top.jbzm.index.dao;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.jbzm.index.dto.PlayLive;
import top.jbzm.index.dto.PlayVideo;
import top.jbzm.index.util.PHPBaiJiaYunTools;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: wangjian
 * @create: 2018-03-21 16:27
 **/
@Component
public class BaiJiaYunDao {
    @Autowired
    private TransportClient transportClient;

    private static SimpleDateFormat ymd=new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat ymdHms=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 将指定日期所有的直播间人次和最高并发量添加es
     * @param list
     * @param date
     * @throws ParseException
     */
    public void getAllRoomUserStatByDate(List<Map> list,String date) throws ParseException {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        Date  parseDate= ymd.parse(date);
        Date creatTime = new Date();
        for(Map m:list){
            String room_id=String.valueOf((long)m.get("room_id"));
            Set<String > set=(Set) PHPBaiJiaYunTools.courseLiveMap.get(room_id);
            String string="";
            if(null!=set&&!set.isEmpty()) {
                for (String str : set) {
                    string += str + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            try {
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                        .field("course_id", string)
                        .field("room_id", m.get("room_id"))
                        .field("student_count",  m.get("student_count"))
                        .field("total_user_count", m.get("total_user_count"))
                        .field("peak_user",m.get("peak_user"))
                        .field("date", parseDate)
                        .field("creatTime", creatTime)
                        .endObject();
                IndexRequestBuilder galaxy = transportClient.prepareIndex("roomuser_date", "roomuser_date").setSource(xContentBuilder.string(), XContentType.JSON);
                bulkRequestBuilder.add(galaxy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bulkRequestBuilder.get();
    }

    /**
     *账号一天中每小时最高并发量插入到es
     * @param map
     * @param date
     * @throws ParseException
     */
    public void getHourPeakUserByDate(Map<String,Integer> map,String date) throws ParseException {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        Date  parseDate= ymd.parse(date);
        Date creatTime = new Date();
        for(Map.Entry m:map.entrySet()){
            try {
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                        .field("hour", Integer.valueOf((String)m.getKey()))
                        .field("peak", m.getValue())
                        .field("date", parseDate)
                        .field("creatTime",creatTime)
                        .endObject();
                IndexRequestBuilder galaxy = transportClient.prepareIndex("hourpeakuser_date", "hourpeakuser_date").setSource(xContentBuilder.string(), XContentType.JSON);
                bulkRequestBuilder.add(galaxy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bulkRequestBuilder.get();
    }

    /**
     * 指定直播房间一定时间并发量
     * @param room_id
     * @param maps
     * @param date 日期
     */
    public void getRoomPeakUser(String room_id,Map<String,Integer> maps,String  date){
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        Date creatTime = new Date();
        for(Map.Entry m:maps.entrySet()) {
            String time = (String) m.getKey();
            Set<String> set = (Set) PHPBaiJiaYunTools.courseLiveMap.get(room_id);
            String string="";
            if(null!=set&&!set.isEmpty()) {
                for (String str : set) {
                    string += str + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            try {
                Date peakTime = ymdHms.parse(date + " " + time);
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                            .field("course_id", string)
                            .field("room_id", room_id)
                            .field("time", peakTime)
                            .field("peak", m.getValue())
                            .field("creatTime", creatTime)//导入时间
                            .endObject();
                IndexRequestBuilder galaxy = transportClient.prepareIndex("roompeakuser_time", "roompeakuser_time").setSource(xContentBuilder.string(), XContentType.JSON);
                bulkRequestBuilder.add(galaxy);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bulkRequestBuilder.get();
    }

    /**
     * 根据room_id将直播播放记录导入es
     * @param room_user_info
     * @param room_id
     */
    public void exportLiveReport(List<PlayLive> room_user_info, String room_id) {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        Date creatTime = new Date();
        for(PlayLive playVideo:room_user_info) {
            Set<String> set = (Set) PHPBaiJiaYunTools.courseLiveMap.get(room_id);
            String string="";
            if(null!=set&&!set.isEmpty()) {
                for (String str : set) {
                    string += str + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            try {
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                        .field("course_id", string)
                        .field("room_id", room_id)
                        .field("date", playVideo.getDate())
                        .field("user_number", playVideo.getUser_number())
                        .field("user_name", playVideo.getUser_name())
                        .field("user_role", playVideo.getUser_role())
                        .field("first_time", playVideo.getFirst_time())
                        .field("last_time", playVideo.getLast_time())
                        .field("first_heartbeat_time", playVideo.getFirst_heartbeat_time())
                        .field("last_heartbeat_time", playVideo.getLast_heartbeat_time())
                        .field("actual_listen_time", playVideo.getActual_listen_time())
                        .field("user_ip", playVideo.getUser_ip())
                        .field("network_operator", playVideo.getNetwork_operator())
                        .field("client_type", playVideo.getClient_type())
                        .field("area", playVideo.getArea())
                        .field("city", playVideo.getCity())
                        .field("creatTime", creatTime)
                        .endObject();
                IndexRequestBuilder galaxy = transportClient.prepareIndex("play_live", "play_live").setSource(xContentBuilder.string(), XContentType.JSON);
                bulkRequestBuilder.add(galaxy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bulkRequestBuilder.get();
    }

    /**
     * 根据时间将账号所有视频观看记录导入es 点播视频 不能跨天
     * @param list
     * @throws ParseException
     */
    public void exportVideoReportBatchByTime(List<PlayVideo> list ) throws ParseException {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        Date creatTime = new Date();
        for(PlayVideo playVideo:list) {
            String video_id = String.valueOf(playVideo.getVideo_id());
            Set<String> set = (Set) PHPBaiJiaYunTools.coursePlayMap.get(video_id);
            String string="";
            if(null!=set&&!set.isEmpty()) {
                for (String str : set) {
                    string += str + ",";
                }
                string = string.substring(0, string.length() - 1);
            }
            try {
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject()
                        .field("course_id", string)
                        .field("guid", playVideo.getGuid())//一次播放的唯一标志，32位字符串
                        .field("uuid", playVideo.getUuid())//客户端随机生成的用户的唯一标志
                        .field("video_id", playVideo.getVideo_id())//回放对应的视频id
                        .field("user_name", playVideo.getUser_number())//用户名
                        .field("user_number", playVideo.getUser_number())//用户number号
                        .field("play_begin_time", playVideo.getPlay_begin_time())//起始播放时间，格式如：2017-09-08 10:00:07
                        .field("play_end_time", playVideo.getPlay_end_time())//结束播放时间，格式如：2017-09-08 10:00:07
                        .field("client_type", playVideo.getClient_type())//客户端类型1：iphone 2:ipad 3：Android 4：手机M站 5：PC 网页 0:未知
                        .field("play_length", playVideo.getPlay_length())//实际观看时间，单位：秒
                        .field("user_ip", playVideo.getUser_ip())//用户IP
                        .field("area", playVideo.getArea())//用户所在地域，格式如：省份 市。可能会为空
                        .field("room_id", playVideo.getRoom_id())//回放教室号（只有回放视频才有该字段）
                        .field("creatTime", creatTime)//导入时间
                        .endObject();
                IndexRequestBuilder galaxy = transportClient.prepareIndex("play_video", "play_video").setSource(xContentBuilder.string(), XContentType.JSON);
                bulkRequestBuilder.add(galaxy);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bulkRequestBuilder.get();
    }



}
