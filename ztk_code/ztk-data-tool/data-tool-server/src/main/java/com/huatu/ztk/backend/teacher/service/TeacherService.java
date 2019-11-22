package com.huatu.ztk.backend.teacher.service;

import com.huatu.ztk.backend.teacher.bean.Teacher;
import com.huatu.ztk.backend.teacher.dao.TeacherDao;
import com.huatu.ztk.commons.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-12  11:11 .
 */
@Service
public class TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    @Autowired
    private TeacherDao teacherDao;

    /**
     * 查找所有教师
     * @return
     */
    public List<Teacher> findAllTeacher(){
        return teacherDao.findAllTeacher();
    }

    /**
     * 根据名字，查找老师
     * @param name
     * @return
     */
    public Object findTeacherByName(String name){
        List<Teacher> teachers = new ArrayList<>();
        teachers.add(teacherDao.findTeacherByName(name));
        return teachers;
    }

    /**
     * 根据id，查找教师
     * @param id
     * @return
     */
    public Object findTeacherById(int id){
        return teacherDao.findTeacherById(id);
    }

    /**
     * 根据id，删除老师
     * @param id
     */
    public void delete(int id){
        teacherDao.delete(id);
    }

    /**
     * 根据传输进来的json串，编辑老师信息
     * @param str
     */
    public void edit(String str){
        logger.info("教师编辑 传输过来的str={}",str);
        Map<String,Object> result = JsonUtil.toMap(str);

        int id = -1;
        String name="",avatar="",trait="",begood="",des="";

        if(result.get("id")!=null){
            id = Integer.parseInt(String.valueOf(result.get("id")));
            if(result.get("name")!=null){
                name = String.valueOf(result.get("name"));
            }
            if(result.get("avatar")!=null){
                avatar = String.valueOf(result.get("avatar"));
            }
            if(result.get("trait")!=null){
                trait = String.valueOf(result.get("trait"));
            }
            if(result.get("begood")!=null){
                begood = String.valueOf(result.get("begood"));
            }
            if(result.get("des")!=null){
                des = String.valueOf(result.get("des"));
            }
            teacherDao.edit(id,name,avatar,des,begood,trait);
        }
    }

    /**
     * 根据传输进来的json串，新增老师信息
     * @param str
     */
    public void insert(String str){
        logger.info("教师新增 传输过来的str={}",str);
        Map<String,Object> result = JsonUtil.toMap(str);

        String name="",avatar="",trait="",begood="",des="";

        if(result.get("name")!=null){
            name = String.valueOf(result.get("name"));
        }
        if(result.get("avatar")!=null){
            avatar = String.valueOf(result.get("avatar"));
        }
        if(result.get("trait")!=null){
            trait = String.valueOf(result.get("trait"));
        }
        if(result.get("begood")!=null){
            begood = String.valueOf(result.get("begood"));
        }
        if(result.get("des")!=null){
            des = String.valueOf(result.get("des"));
        }
        teacherDao.insert(name,avatar,begood,trait,des);
    }

}
