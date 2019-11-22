package top.jbzm.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jbzm.index.dao.BaiJiaYunDao;
import top.jbzm.index.dto.BaiJiaYunResult;
import top.jbzm.index.dto.PlayLive;
import top.jbzm.index.dto.PlayVideo;
import top.jbzm.index.service.BaiJiaYunService;
import top.jbzm.index.util.BaiJiaYunUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.jbzm.index.util.BaiJiaYunAPI.*;


/**
 * @author: wangjian
 * @create: 2018-03-20 13:28
 **/
@Slf4j
@Service
public class BaiJiaYunServiceImpl implements BaiJiaYunService {

    @Autowired
    private BaiJiaYunDao baiJiaYunDao;

    // 获取指定日期所有的直播间人次和最高并发量
    @Override
    public  String getAllRoomUserStat(String date) {
        Map<String,Object> map=new HashMap();
        map.put("date",date);
        String result = BaiJiaYunUtil.postHtpps(GETALLROOMUSERSTAT, map, true);
        //  解析返回值加入es
        BaiJiaYunResult bean=JSON.parseObject(result,BaiJiaYunResult.class);
        Map<String, Object> data = bean.getData();
        Map<String,Map> room_user_stat = null;
        try {
            room_user_stat = (Map)data.get("room_user_stat");
            List list=new ArrayList();
            for(Map value:room_user_stat.values()){
                list.add(value);
            }
            baiJiaYunDao.getAllRoomUserStatByDate(list,date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    //导出直播教师学员观看记录
    @Override
    public  String  exportLiveReport(String date,String room_id,String type,int page,int pageSize ){
        Map map=new HashMap();
        map.put("date",date);
        map.put("room_id",room_id);
        map.put("type",type);
        map.put("page",String.valueOf(page));
        map.put("page_size",String.valueOf(pageSize));
        String result = BaiJiaYunUtil.postHtpps(EXPORTLIVEREPORT, map, true);
        //  解析返回值加入es
        BaiJiaYunResult bean=JSON.parseObject(result,BaiJiaYunResult.class);
        Map<String, Object> data = bean.getData();
        JSONArray arr = (JSONArray)data.get("room_user_info");
        List<PlayLive> room_user_info = arr.toJavaList(PlayLive.class);
        if(!room_user_info.isEmpty()){
            baiJiaYunDao.exportLiveReport(room_user_info,room_id);
        }
        return result;
    }

    @Override//指定房间一定时间内并发量
    public String getRoomPeakUser(String room_id,String start_time,String end_time) {
        Map map=new HashMap();
        map.put("start_time",start_time);
        map.put("end_time",end_time);
        map.put("room_id",room_id);
        String result = BaiJiaYunUtil.postHtpps(GETROOMPEAKUSER, map, true);
        BaiJiaYunResult bean=JSON.parseObject(result,BaiJiaYunResult.class);
        Map<String, Object> data = bean.getData();
        String date = null;
        Map<String,Integer> peak_user = null;
        try {
            date = (String)data.get("date");
            peak_user = (Map)data.get("peak_user");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(null!=peak_user&&!peak_user.isEmpty()){
            baiJiaYunDao.getRoomPeakUser(room_id,peak_user,date);
        }
        return result;
    }

    @Override
    public String getHourPeakUser(String date) {
        Map map=new HashMap();
        map.put("date",date);
        String result = BaiJiaYunUtil.postHtpps(GETHOURPEAKUSER, map, true);
        //2018/3/20  添加到es 每小时并发数
        BaiJiaYunResult bean=JSON.parseObject(result,new TypeReference<BaiJiaYunResult>() {});
        Map<String, Object> data = bean.getData();
        Map<String,Integer> peak_user = (Map)data.get("peak_user");
        try {
            baiJiaYunDao.getHourPeakUserByDate(peak_user,date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String exportVideoReportBatch(String start_time,String end_time,int page,int page_size) {
        Map map=new HashMap();
        map.put("start_time",start_time);
        map.put("end_time",end_time);
        map.put("page",String.valueOf(page));
        map.put("page_size",String.valueOf(page_size));
        String result = BaiJiaYunUtil.postHtpps(EXPORTVIDEOREPORTBATCH, map, true);
        BaiJiaYunResult bean=JSON.parseObject(result,BaiJiaYunResult.class);
        Map<String, Object> data = bean.getData();
        if(null==data||!bean.getMsg().equals("")){//data为空或者msg不为空则重新发送请求 TODO
            log.error("BaiJiaYun result fail{}",result);
            result = BaiJiaYunUtil.postHtpps(EXPORTVIDEOREPORTBATCH, map, true);
            bean=JSON.parseObject(result,BaiJiaYunResult.class);
            data = bean.getData();
        }
        JSONArray arr = null;
        try {
            arr = (JSONArray)data.get("list");
        } catch (Exception e) {
            log.error("BaiJiaYun result fail{}",result);
            log.error("skip page:"+page);
            BaiJiaYunResult baiJiaYunResult=new BaiJiaYunResult();
            Map<String,Object> resultMap=new HashMap();
            resultMap.put("total",100000);
            baiJiaYunResult.setData(resultMap);
            return JSON.toJSONString(baiJiaYunResult);
        }
        List<PlayVideo> list = arr.toJavaList(PlayVideo.class);
        if(!list.isEmpty()){
            try {
                baiJiaYunDao.exportVideoReportBatchByTime(list);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public String playBackgetlist(int page, int page_size) {
        Map map=new HashMap();
        map.put("page",String.valueOf(page));
        map.put("page_size",String.valueOf(page_size));
        String result = BaiJiaYunUtil.postHtpps(PLAYBACKGETLIST, map, true);
        // TODO: 2018/3/20  添加到es
        return result;
    }



}
