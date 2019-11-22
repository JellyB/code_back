/**
 *
 */
package top.jbzm.index.task;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.jbzm.index.dto.BaiJiaYunResult;
import top.jbzm.index.service.BaiJiaYunService;
import top.jbzm.index.service.CourseListService;
import top.jbzm.index.util.BaiJiaYunUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 定时任务 定时访问百家云接口取数据
 * @author wangjian
 * @create 2018-03-19 19:19
 **/
@Component
@Slf4j
public class BaiJiaYunScheduling {

    private final BaiJiaYunService baiJiaYunService;

    private final CourseListService courseListService;

    @Autowired
    public BaiJiaYunScheduling(BaiJiaYunService baiJiaYunService, CourseListService courseListService) {
        this.baiJiaYunService = baiJiaYunService;
        this.courseListService = courseListService;
    }

    @Scheduled(cron = "0 0 0 * * ?  ") // 每天执行一次   课程列表
    public  void getPHPCourseList(){
        courseListService.getCourseLiveMap();
        log.info("get PHP courseList success");
    }

    //课程目录导入到mysql
    @Scheduled(cron = "0 30 0 * * ?  ")
    public void courseSetToSql(){
        courseListService.addCourseRepository();
        log.info(" courseList insert mysql success");
    }

    @Scheduled(cron = "0 0 1 * * ?  ") // 每天1点执行一次   百家云数据每天执行一次获取前一天数据
    public void scheduler() {
        Date yesterdayDate = BaiJiaYunUtil.getYesterday();
        SimpleDateFormat ymd=new SimpleDateFormat("yyyy-MM-dd");
        String date = ymd.format(yesterdayDate);
        String start_time=date+" 00:00:00";
        String end_time=date+" 23:59:59";
        //获取账号一天中每小时最高并发量
        baiJiaYunService.getHourPeakUser(date);
        //获取指定日期所有的直播间人次和最高并发量
        String result = baiJiaYunService.getAllRoomUserStat(date);

        BaiJiaYunResult bean= JSON.parseObject(result,BaiJiaYunResult.class);
        Map<String, Object> data = bean.getData();
        Map<String,Map> room_user_stat = (Map)data.get("room_user_stat");
        int pageSize=1000;
        for(Map.Entry m:room_user_stat.entrySet()){//有多少直播间循环多少次
            String room_id = (String)m.getKey();//直播间id
            baiJiaYunService.getRoomPeakUser(room_id,start_time,end_time);//指定房间一定时间内并发量
            String resultRoom_id = baiJiaYunService.exportLiveReport(date, room_id, "all", 1, pageSize);//指定房间播放记录
            BaiJiaYunResult beanRoom_id=JSON.parseObject(resultRoom_id,BaiJiaYunResult.class);
            Map<String, Object> dataRoom_id = beanRoom_id.getData();
            int total=(int)dataRoom_id.get("total");//总条数
            if(total>pageSize) {
                int count = total % pageSize==0?total/pageSize:total/pageSize+1;//计算总页数
                for(int i=2;i<=count;i++){    //根据页数读取全部直播数据
                    baiJiaYunService.exportLiveReport(date, room_id,"all", i, pageSize);
                }
            }
        }

        String playVideoResultString = baiJiaYunService.exportVideoReportBatch(start_time, end_time, 1, pageSize);//录播播放记录
        BaiJiaYunResult playVideoResultBean= JSON.parseObject(playVideoResultString,BaiJiaYunResult.class);
        Map<String, Object> playVideoResultData = playVideoResultBean.getData();
        int playTotal=(int)playVideoResultData.get("total");
        if(playTotal>pageSize) {
            int count = playTotal % pageSize==0?playTotal/pageSize:playTotal/pageSize+1;//计算总页数
            for(int i=2;i<=count;i++){    //根据页数读取全部播放数据
                baiJiaYunService.exportVideoReportBatch(start_time, end_time, i, pageSize);
            }
        }
        log.info("get baijiayun data success");
    }





    @Scheduled(cron = "0 30 1 13 4 ? ") // 手动补充4-10播放数据记录
    public void scheduler1() {
        String date="2018-04-10";
        scheduler(date);
    }

    public void scheduler(String date) {
        String start_time=date+" 00:00:00";
        String end_time=date+" 23:59:59";
        int pageSize=1000;
        String playVideoResultString = baiJiaYunService.exportVideoReportBatch(start_time, end_time, 1, pageSize);//直播播放记录
        BaiJiaYunResult playVideoResultBean= JSON.parseObject(playVideoResultString,BaiJiaYunResult.class);
        Map<String, Object> playVideoResultData = playVideoResultBean.getData();
        int playTotal=(int)playVideoResultData.get("total");
        if(playTotal>pageSize) {
            int count = playTotal % pageSize==0?playTotal/pageSize:playTotal/pageSize+1;//计算总页数b
            for(int i=2;i<=count;i++){    //根据页数读取全部播放数据
                baiJiaYunService.exportVideoReportBatch(start_time, end_time, i, pageSize);
            }
        }
        log.info("get baijiayun data success");
    }
}

