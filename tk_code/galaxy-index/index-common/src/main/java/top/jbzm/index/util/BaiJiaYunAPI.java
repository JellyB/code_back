package top.jbzm.index.util;

/**
 * 百家云接口参数
 * @author: wangjian
 * @create: 2018-03-21 11:01
 **/
public class BaiJiaYunAPI {

    // 获取指定日期所有的直播间人次和最高并发量
//    partner_id 	int 	是 		合作方id
//    date 	string 	是 		格式如：2017-11-23
//    timestamp 	int 	是 		当前时间，unix时间戳
//    sign 	string 	是 		签名
    public static final String GETALLROOMUSERSTAT="https://api.baijiayun.com/openapi/room_data/getAllRoomUserStat";    // 获取指定日期所有的直播间人次和最高并发量

    //导出直播间学员观看记录，该记录会有1小时的延时。
    //    partner_id	int	是		合作方用户名
//    room_id	int	是		教室号
//    type	string	否	student	可选值 all:所有用户 student:学员 teacher:老师 admin:助教，默认只导出学员观看记录
//    page	int	否	1	分页参数
//    page_size	int	否	0	每页返回条数，如果不传则返回所有的
//    date	string	否	0	查询日期，格式如：2018-03-02
//    timestamp	int	是		当前时间，unix时间戳
//    sign	string	是		签名
    public static final String EXPORTLIVEREPORT="https://api.baijiayun.com/openapi/room_data/exportLiveReport";//导出直播间学员观看记录，该记录会有1小时的延时。

    //    获取指定房间一段时间内的并发量
//    partner_id 	int 	是 		合作方id
//    room_id 	int 	是 		教室号
//    start_time 	string 	是 		格式如：2017-11-23 10:00:00
//    end_time 	string 	是 		格式如：2017-11-23 15:00:00，查询时间范围不能跨天
//    timestamp 	int 	是 		当前时间，unix时间戳
//    sign 	string 	是 		签名
    public static final String GETROOMPEAKUSER="    https://api.baijiayun.com/openapi/room_data/getRoomPeakUser";

    //    查询账号一段时间内每天的最高并发量
//    partner_id 	int 	是 		合作方id
//    start_date 	string 	是 		查询起始日期，格式如：2017-12-12
//    end_date 	string 	是 		查询结束日期，格式如：2017-12-28
//    timestamp 	int 	是 		当前时间，unix时间戳
//    sign 	string 	是 		签名
    public static final String GETDAYPEAKUSER="    https://api.baijiayun.com/openapi/live_account/getDayPeakUser";

    //    获取账号一天中每小时最高并发量
//    partner_id 	int 	是 		合作方id
//    date 	string 	是 		查询日期，格式如：2017-12-12
//    timestamp 	int 	是 		当前时间，unix时间戳
//    sign 	string 	是 		签名
    public static final String GETHOURPEAKUSER="    https://api.baijiayun.com/openapi/live_account/getHourPeakUser";

    //    获取房间列表
//    partner_id 	int 	是 		合作方ID
//    page 	int 	否 	1 	页数，参加码数量过多时，可以分多页来获取，每页取limit条。默认值为1
//    limit 	int 	否 	100 	每页获取的条数，默认值100，最大值不能超过1000
//    timestamp 	int 	是 		当前时间, unix时间戳
//    sign 	string 	是 		签名
    public static final String ROOM_LIST="    https://api.baijiayun.com/openapi/room/list";


    //    获取账号所有视频观看记录   点播视频 不能跨天
    //该接口用于获取账号下所有视频的详细播放记录（包括点播和回放），每次播放都会有一条记录。
//    partner_id 	int 	是 		合作方id
//    start_time 	string 	是 		查询起始时间，格式如：2017-09-08 00:30:00。
//    end_time 	string 	是 		查询结束时间，格式如：2017-09-08 23:59:59。查询时间不能跨天
//    page 	int 	否 	1 	页码，从1开始，默认值是1
//    page_size 	int 	否 	100 	每页获取的记录条数，默认100，最大值不能超过1000
//    timestamp 	int 	是 		当前时间，unix时间戳
//    sign 	string 	是 	test123 	签名
    public static final String EXPORTVIDEOREPORTBATCH="    https://api.baijiacloud.com/openapi/video_data/exportVideoReportBatch";

    //    获取指定视频观看记录
//    partner_id 	int 	是 		合作方id
//    video_id 	int 	是 		点播视频ID
//    start_time 	string 	是 		查询起始时间，格式如：2017-09-08 00:30:00。
//    end_time 	string 	是 		查询结束时间，格式如：2017-09-08 23:59:59。查询时间不能跨天
//    page 	int 	否 	1 	页码，从1开始，默认值是1
//    page_size 	int 	否 	100 	每页获取的记录条数，默认100，最大值不能超过1000
//    timestamp 	int 	是 		当前时间，unix时间戳
//    sign 	string 	是 		签名
    public static final String GETVIDEOPLAYRECORD ="    https://api.baijiayun.com/openapi/video_data/getVideoPlayRecord";

    //    获取所有分类
//    partner_id 	int 	是 	123456 	合作方用户名
    public static final String GETCATEGORYLIST="    https://api.baijiayun.com/openapi/video/getCategoryList";


    //    获取指定分类下的视频
//    partner_id 	string 	是 	123456 	合作方用户名
//    category_id 	int 	是 	12345 	分类ID
//    page_size 	int 	否 	20 	每页条数，不得超过100，默认值20
//    page 	int 	否 	1 	页码，默认1
    public static final String GETCATEGORYVIDEO="    https://api.baijiayun.com/openapi/video/getCategoryVideo";

    //    获取回放列表
//    partner_id 	int 	是 	123456 	合作方用户名
//    page 	int 	是 	123456 	页码，从1开始
//    page_size 	int 	是 	123456 	每一页返回的条数，不得超过1000
//    timestamp 	int 	是 	1460426400 	当前时间，unix时间戳
//    sign 	string 	是 	test123 	签名
    public static final String PLAYBACKGETLIST="        https://api.baijiayun.com/openapi/playback/getList";

}
