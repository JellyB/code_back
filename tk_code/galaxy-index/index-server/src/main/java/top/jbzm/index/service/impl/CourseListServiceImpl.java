package top.jbzm.index.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jbzm.index.dto.PHPBjyBean;
import top.jbzm.index.entity.videoEntity.Course;
import top.jbzm.index.repository.CourseRepository;
import top.jbzm.index.service.CourseListService;
import top.jbzm.index.util.PHPBaiJiaYunTools;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangjian
 **/
@Slf4j
@Service
public class CourseListServiceImpl  implements CourseListService {

    @Autowired
    private CourseRepository courseRepository;


    /**
     * 课程添加至数据库
     */
    @Override
    public void addCourseRepository(){
        MapAddCourseRepository(PHPBaiJiaYunTools.courseSet);
        MapAddCourseRepository(PHPBaiJiaYunTools.suitSet);
    }

    /**
     * 将课程集合存储到数据库并打印日志
     * @param datas 课程集合
     */
    public void MapAddCourseRepository(Set<Map> datas){
        for(Map map: datas){
            String rid=(String)map.get("rid");
            String IsSuit=(String)map.get("IsSuit");
            String NetClassCategoryId=(String) map.get("NetClassCategoryId");
            String Title=(String)map.get("Title");
            String status=(String)map.get("status");
            Integer cateId=(Integer)map.get("cateId");
            Course course=new Course().builder().rid(rid).IsSuit(IsSuit).NetClassCategoryId(NetClassCategoryId).Title(Title).status(status).cateId(cateId).build();
            courseRepository.save(course);
            log.info(course.toString()+"insert");
        }
    }

    /**
     * 循环取php课程列表
     * @return
     */
    @Override
    public  boolean getCourseLiveMap(){
        PHPBaiJiaYunTools.courseSet.clear();
        int i=1;
        while (getPHPCourseSet(i++,PHPBaiJiaYunTools.url));
        i=1;
        while (getPHPCourseSet(i++,PHPBaiJiaYunTools.liveUrl));
        return true;
    }




    /**
     * 取php课程目录 还有下一个返回true
     * @param i
     * @return
     */
    private  boolean getPHPCourseSet(int i,String url){
        String resultString = PHPBaiJiaYunTools.getConnectPhp(url, String.valueOf(i));
        PHPBjyBean bean = JSON.parseObject(resultString, PHPBjyBean.class);
        Map dataMap = (Map)bean.getData();
        List<Map> courseMap = (List<Map>)dataMap.get("data");
        for (Map map:courseMap){
            String IsSuit=(String) map.get("IsSuit");
            String rid=(String) map.get("rid");
            if("0".equals(IsSuit)){
                PHPBaiJiaYunTools.courseSet.add(map);//添加到课程列表
                coursesAddMap((List<Map>)map.get("lessons"),rid);
            }else{//说明是套餐
                PHPBaiJiaYunTools.suitSet.add(map);//添加到套餐列表
                List<Map> packeag = (List<Map>) map.get("package");
                for(Map m:packeag){//取出套餐子课程
                    PHPBaiJiaYunTools.sonCourseSet.add(map);//添加到子课程列表
                    String courseId=(String) m.get("rid");//子课程id
                    List<Map> leeeons=(List<Map>)m.get("lessons");
                    coursesAddMap(leeeons,courseId);//绑定套餐id
                    List<Map> live=(List<Map>)m.get("live");
                    coursesAddMap(live,courseId);
                    Set set=(Set)PHPBaiJiaYunTools.suitSonCourseMap.get(courseId);
                    if(null==set){
                        set=new HashSet();
                    }
                    set.add(rid);
                    PHPBaiJiaYunTools.suitSonCourseMap.put(courseId,set);
                }
            }
        }
        String next_page_url = (String)dataMap.get("next_page_url");
        if(null!=next_page_url){
            return true;
        }
        return false;
    }
    /**
     * 将课程集合和百家云绑定
     * @param lists
     * @param rid
     */
    private  void coursesAddMap(List<Map> lists,String rid){
        for(Map map:lists){
            String bjyRoomId=(String)map.get("bjyRoomId");
            String bjyVideo1=(String)map.get("bjyVideo1");
            String bjyVideo2=(String)map.get("bjyVideo2");
            if(null!=bjyRoomId&&!"".equals(bjyRoomId)) {
                Set set = (Set) PHPBaiJiaYunTools.courseLiveMap.get(bjyRoomId);
                if(null==set){
                    set=new HashSet();
                }
                set.add(rid);
                PHPBaiJiaYunTools.courseLiveMap.put(bjyRoomId,set);
            }
            if(null!=bjyVideo1&&!"".equals(bjyVideo1)) {
                Set set = (Set) PHPBaiJiaYunTools.coursePlayMap.get(bjyVideo1);
                if(null==set){
                    set=new HashSet();
                }
                set.add(rid);
                PHPBaiJiaYunTools.coursePlayMap.put(bjyVideo1,set);
            }
            if(null!=bjyVideo2&&!"".equals(bjyVideo2)) {
                Set set = (Set) PHPBaiJiaYunTools.coursePlayMap.get(bjyVideo2);
                if(null==set){
                    set=new HashSet();
                }
                set.add(rid);
                PHPBaiJiaYunTools.coursePlayMap.put(bjyVideo2,set);
            }
        }
    }


}
