package com.huatu.ztk.backend.teacher.dao;

import com.huatu.ztk.backend.teacher.bean.Teacher;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-05-12  11:10 .
 */
@Repository
public class TeacherDao {
    private static final Logger logger = LoggerFactory.getLogger(TeacherDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查找所有老师
     * @return
     */
    public List<Teacher> findAllTeacher(){
        String sql = "SELECT * FROM v_c_sys_teacher WHERE status=1";
        return jdbcTemplate.query(sql,new TeacherMapper());
    }

    /**
     * 根据名字，查找老师
     * @param name
     * @return
     */
    public Teacher findTeacherByName(String name){
        String sql = "SELECT * FROM v_c_sys_teacher WHERE status=1 AND name=?";
        Object[] params = {name};
        List<Teacher> teachers = jdbcTemplate.query(sql,params,new TeacherMapper());
        Teacher teacher = new Teacher();
        if (CollectionUtils.isNotEmpty(teachers)) {
            teacher = teachers.get(0);
        }
        return teacher;
    }

    /**
     * 根据id，查找老师
     * @param id
     * @return
     */
    public Teacher findTeacherById(int id){
        String sql = "SELECT * FROM v_c_sys_teacher WHERE status=1 AND id=?";
        Object[] params = {id};
        List<Teacher> teachers = jdbcTemplate.query(sql,params,new TeacherMapper());
        Teacher teacher = new Teacher();
        if (CollectionUtils.isNotEmpty(teachers)) {
            teacher = teachers.get(0);
        }
        return teacher;
    }

    /**
     * 插入新教师
     * @param name
     * @param avatar
     * @param des
     */
    public void insert(String name,String avatar,String begood,String trait,String des){
        String sql = "INSERT v_c_sys_teacher(name,avatar,begood,trait,des,status) VALUES(?,?,?,?,?,?)";
        Object[] params = {name,avatar,begood,trait,des,1};
        jdbcTemplate.update(sql,params);
    }

    /**
     * 根据id，删除老师（既修改status为0)
     * @param id
     */
    public void delete(int id){
        String sql = "UPDATE v_c_sys_teacher SET status=0 WHERE id=?";
        Object[] params = {id};
        jdbcTemplate.update(sql,params);
    }

    /**
     * 修改老师信息
     * @param id
     * @param name
     * @param avatar
     * @param des
     */
    public void edit(int id,String name,String avatar,String des,String begood,String trait){
        String sql = "UPDATE v_c_sys_teacher SET name=?,avatar=?,des=?,begood=?,trait=? WHERE id=?";
        Object[] params = {name,avatar,des,begood,trait,id};
        jdbcTemplate.update(sql,params);
    }


    class TeacherMapper implements RowMapper<Teacher> {
        @Override
        public Teacher mapRow(ResultSet rs, int rowNum) throws SQLException {

            String format =  "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sf = new SimpleDateFormat(format);
            Date createTime = new Date((rs.getLong("create_time"))*1000);

            Teacher teacher = Teacher.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .avatar(rs.getString("avatar"))
                    .des(rs.getString("des"))
                    .trait(rs.getString("trait"))
                    .begood(rs.getString("begood"))
                    .createTime(sf.format(createTime))
                    .status(rs.getInt("status"))
                    .build();
            return teacher;
        }
    }
}
