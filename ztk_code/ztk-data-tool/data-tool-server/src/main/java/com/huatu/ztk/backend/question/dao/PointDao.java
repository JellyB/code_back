package com.huatu.ztk.backend.question.dao;

import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.question.service.PointService;
import com.huatu.ztk.knowledge.common.PointStatus;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-02-04  11:32 .
 */
@Repository
public class PointDao {
    private static final Logger logger = LoggerFactory.getLogger(PointDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 根据id，删除知识点，即修改知识点状态
     */
    public boolean deletePoint(int id){
        String sql = "UPDATE v_knowledge_point SET BB102 = -3 WHERE PUKEY=?";
        Object[] param = {id};
        try {
            jdbcTemplate.update(sql,param);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据id，name，修改知识点名字
     */
    public boolean editPoint(int id,String name){
        String sql = "UPDATE v_knowledge_point SET name=? WHERE PUKEY=?";
        Object[] param = {name,id};
        boolean result = true;
        try {
            jdbcTemplate.update(sql,param);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<QuestionPointTreeMin> findErrorPoints(){
        String sql = "SELECT * FROM v_knowledge_point AS atable  WHERE bl_sub!=(SELECT bl_sub FROM v_knowledge_point WHERE PUKEY=atable.prev_kp);";
        List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,new QuestionPoinTreeTRowMapper());
        return points;
    }
    public void correctPoint(QuestionPointTreeMin point){
        logger.info("问题知识点={}",point);
        String sqlForFind = "SELECT * FROM v_knowledge_point WHERE bl_sub=? AND name=(SELECT name FROM v_knowledge_point WHERE PUKEY=?) AND BB102 IN (-2,1) and BB1B1=1 AND node_rank=?";
        Object[] paramForFind = {point.getSubject(),point.getParent(),point.getLevel()-1};
        List<QuestionPointTreeMin> points = jdbcTemplate.query(sqlForFind,paramForFind,new QuestionPoinTreeTRowMapper());
        if(CollectionUtils.isNotEmpty(points)){
            String sqlForUpdate = "UPDATE v_knowledge_point SET prev_kp=? WHERE PUKEY=?";
            Object[] paramForUpdate = {points.get(0).getId(),point.getId()};
            logger.info("找到出问题的point父节点={}",points.get(0));
            jdbcTemplate.update(sqlForUpdate,paramForUpdate);
        }
    }

    /**
     * 根据name，parentId，level，插入新知识点
     */
    public int insertPoint(String name,int parentId,int level,int subject){
        final String sql = "INSERT INTO v_knowledge_point(name,prev_kp,node_rank,difficult_grade,EB1B1,BB102,BB1B1,bl_sub) VALUES (?,?,?,?,?,?,?,?)";
        int difficult_grade = 0;//难度默认为0
        String temp = "";//数据库设计‘EB1B1’列无默认值，临时加该参数
        int bb102 = 1,bb1b1=1;//表示该知识点可用
        final Object[] param = {name,parentId,level,difficult_grade,temp,bb102,bb1b1,subject};
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{ "name" ,"prev_kp","node_rank",
                            "difficult_grade","EB1B1","BB102","BB1B1","bl_sub"});
            for(int i=0;i<param.length;i++){
                ps.setObject(i+1,param[i]);
            }
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        logger.info("新插入的知识点id={}",id);
        return id;
    }

    /**
     * 根据name，parentId，level，插入新知识点
     */
    public int insertPointForint(String name,int parentId,int level,int subject){
        String sql = "INSERT INTO v_knowledge_point_copy(name,prev_kp,node_rank,difficult_grade,EB1B1,BB102,BB1B1,bl_sub) VALUES (?,?,?,?,?,?,?,?)";
        Object[] param = {name,parentId,level,0,"",1,1,subject};
        System.out.println(jdbcTemplate.update(sql,param));
        String sqlForInt = "SELECT LAST_INSERT_ID()";
        return jdbcTemplate.queryForObject(sqlForInt,Integer.class);
    }


    /**
     * 根据知识点名字，找寻该知识点父级知识点
     * @param pointName
     * @return
     */
    public QuestionPointTreeMin findPointParent(String pointName){
        String sql = "SELECT * FROM v_knowledge_point WHERE BB102 IN (-2,1) and BB1B1=1 and PUKEY IN" +
                " (SELECT prev_kp FROM v_knowledge_point WHERE  BB102 IN (-2,1) and BB1B1=1 and  name=?) ";
        Object[] param = {pointName};
        QuestionPointTreeMin pointParent = null;
        final List<QuestionPointTreeMin> pointsParent = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        if(CollectionUtils.isNotEmpty(pointsParent)){
            pointParent = pointsParent.get(0);
        }
        return pointParent;
    }
    public QuestionPointTreeMin findPointParentById(int pointId){
        String sql = "SELECT * FROM v_knowledge_point WHERE BB102 IN (-2,1) and BB1B1=1 and PUKEY IN" +
                " (SELECT prev_kp FROM v_knowledge_point WHERE  BB102 IN (-2,1) and BB1B1=1 and  PUKEY=?) ";
        Object[] param = {pointId};
        QuestionPointTreeMin pointParent = null;
        final List<QuestionPointTreeMin> pointsParent = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        if(CollectionUtils.isNotEmpty(pointsParent)){
            pointParent = pointsParent.get(0);
        }
        return pointParent;
    }
    /**
     * 根据知识点名字，找到该知识点
     * @param pointName
     * @return
     */
    public QuestionPointTreeMin findPointByName(String pointName){
        String sql = " SELECT * FROM v_knowledge_point WHERE  BB102 IN (-2,1) and BB1B1=1 and  name=?";
        Object[] param = {pointName};
        QuestionPointTreeMin point = null;
        final List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        if(CollectionUtils.isNotEmpty(points)){
            point = points.get(0);
        }
        return point;
    }

    /**
     * 根据知识点名字，找到该知识点
     * @param pointId
     * @return
     */
    public QuestionPointTreeMin findPointById(int pointId){
        String sql = " SELECT * FROM v_knowledge_point WHERE  BB102 IN (-2,1) and BB1B1=1 and  PUKEY=?";
        Object[] param = {pointId};
        QuestionPointTreeMin point = null;
        final List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        if(CollectionUtils.isNotEmpty(points)){
            point = points.get(0);
        }
        return point;
    }

    /**
     * 根据知识点详细信息：名字，层级，父知识点，找到该知识点
     * @param name
     * @param level
     * @param parentId
     * @return
     */
    public QuestionPointTreeMin findPointByDetail(String name,int parentId,int level,int subject){
        String sql = " SELECT * FROM v_knowledge_point WHERE  BB102 IN (-2,1) and BB1B1=1 and  name=? AND prev_kp=? AND node_rank=? AND bl_sub=?";
        Object[] param = {name,parentId,level,subject};
        QuestionPointTreeMin point = null;
        final List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        if(CollectionUtils.isNotEmpty(points)){
            point = points.get(0);
        }
        return point;
    }


    /**
     * 查询所有已经审核并有效的知识点
     * @return
     */
    public List<QuestionPointTreeMin> findAllPonits(){
        String sql = "SELECT * FROM v_knowledge_point WHERE BB102 IN (-2,1) and BB1B1=1";
        final List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,new QuestionPoinTreeTRowMapper());
        return points;
    }


    /**
     * 查询所有已经审核并有效的知识点
     * @return
     */
    public List<QuestionPointTreeMin> findAllPonitsBySubject(int subject){
        if(subject==-1){
            return findAllPonits();
        }
        String sql = "SELECT * FROM v_knowledge_point WHERE BB102 IN (-2,1) and BB1B1=1 and bl_sub=?";
        Object[] param = {subject};
        final List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        return points;
    }

    /**
     * 查询所有已经审核并有效的知识点
     * @return
     */
    public List<QuestionPointTreeMin> findAllPonitsBySubjectForCopy(int subject){
        String sql = "SELECT * FROM v_knowledge_point_copy WHERE BB102 IN (-2,1) and BB1B1=1 and bl_sub=?";
        Object[] param = {subject};
        final List<QuestionPointTreeMin> points = jdbcTemplate.query(sql,param,new QuestionPoinTreeTRowMapper());
        return points;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }


    class QuestionPoinTreeTRowMapper implements RowMapper<QuestionPointTreeMin> {

        @Override
        public QuestionPointTreeMin mapRow(ResultSet rs, int rowNum) throws SQLException {
            final int id = rs.getInt("PUKEY");
            final int questionQum = rs.getInt("question_num");
            final int prevKp = rs.getInt("prev_kp");
            final String name = rs.getString("name");
            final int level = rs.getInt("node_rank");
            final int subject = rs.getInt("bl_sub");
            final int status = rs.getInt("BB102") > 0 ? PointStatus.AUDIT_SUCCESS : PointStatus.DELETED;

            final QuestionPointTreeMin questionPoint = QuestionPointTreeMin.builder()
                    .name(name)
                    .id(id)
                    .parent(prevKp)
                    .level(level)
                    .subject(subject)
                    .build();
            return questionPoint;
        }
    }
}
