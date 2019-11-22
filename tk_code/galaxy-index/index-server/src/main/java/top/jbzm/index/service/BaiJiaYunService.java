package top.jbzm.index.service;

/**
 * @author: wangjian
 * @create: 2018-03-20 13:28
 **/
public interface BaiJiaYunService {

    /**直播
     * 获取指定日期所有的直播间人次和最高并发量
     * @param date
     * @return
     */
     String getAllRoomUserStat(String date);

    /**
     * * 导出直播教师学员观看记录
     * @param date
     * @param room_id
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
     String exportLiveReport(String date, String room_id, String type, int page, int pageSize);


    /**直播
     * 获取指定房间一段时间内的并发量 不能跨天
     * @param room_id
     * @param start_time
     * @param end_time
     * @return
     */
     String getRoomPeakUser(String room_id, String start_time, String end_time);



    /**直播
     * 获取账号一天中每小时最高并发量
     * @param date
     * @return
     */
    String getHourPeakUser(String date);



    /**点播
     * 获取账号所有视频观看记录 点播视频 不能跨天
     * @param start_time
     * @param end_time
     * @param page
     * @param page_size
     * @return
     */
    String exportVideoReportBatch(String start_time, String end_time, int page, int page_size);


    /**回放
     * 获取回放列表
     * @param page
     * @param page_size
     * @return
     */
    String playBackgetlist(int page, int page_size);


}
