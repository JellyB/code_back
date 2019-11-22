package com.huatu.ztk.knowledge.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointChange;
import com.huatu.ztk.knowledge.common.PointStatus;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识点dao层
 * Created by shaojieyue
 * Created time 2016-05-06 18:31
 */

@Repository
public class QuestionPointDao {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPointDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public QuestionPoint findById(int pointId){
        String[] params = new String[]{
                pointId+""
        };
        String sql = "SELECT PUKEY,name,question_num,prev_kp,node_rank,BB102,BB1B1 FROM v_knowledge_point WHERE PUKEY = ? ";
        final List<QuestionPoint> questionPoints = jdbcTemplate.query(sql,params, new QuestionPointRowMapper());
        QuestionPoint questionPoint = null;
        if (questionPoints != null && questionPoints.size() > 0) {
            questionPoint = questionPoints.get(0);
            List<Integer> children = new ArrayList<>();
            //查询该节点下所有的子节点
            String childrenSql = "SELECT PUKEY FROM v_knowledge_point WHERE prev_kp = ? and node_rank < 3";
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(childrenSql,questionPoint.getId());
            while (sqlRowSet.next()){
                final int pukey = sqlRowSet.getInt("PUKEY");
                children.add(pukey);
            }
            //设置子节点id
            questionPoint.setChildren(children);
        }
        return questionPoint;
    }

    class QuestionPointRowMapper implements RowMapper<QuestionPoint>{

        @Override
        public QuestionPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
            final int id = rs.getInt("PUKEY");
            final int questionQum = rs.getInt("question_num");
            final int prevKp = rs.getInt("prev_kp");
            final String name = rs.getString("name");
            final int level = rs.getInt("node_rank");
            int bb102 = rs.getInt("BB102");
            int bb1B1 = rs.getInt("BB1B1");

            int status;
            if (bb102 > 0 && bb1B1 > 0) {
                status = PointStatus.AUDIT_SUCCESS;
            } else if (bb1B1 > 0 && bb102 == -2) {
                status = PointStatus.HIDE;
            } else {
                status = PointStatus.DELETED;
            }

            final QuestionPoint questionPoint = QuestionPoint.builder()
                    .name(name)
                    .id(id)
                    .parent(prevKp)
                    .qnumber(questionQum)
                    .level(level)
                    .status(status)
                    .build();
            return questionPoint;
        }
    }


    /**
     * 科目的顶级知识点
     * @param subject
     * @return
     */
    public List<Module> findModule(int subject) {
        //有效的顶级知识点
        String sql = "SELECT * FROM v_knowledge_point WHERE bl_sub=? and node_rank=0 and BB102=1 and BB1B1=1";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, new Object[]{subject});

        List<Module> modules = new ArrayList<>();
        while (sqlRowSet.next()) {
            int id = sqlRowSet.getInt("PUKEY");
            String name = sqlRowSet.getString("name");

            modules.add(new Module(id, name));
        }

        return modules;
    }
    public List<QuestionPointChange> findChangeLog(int subject, long start, long end) {
        String sql = "select * from v_question_pk_r_change_log where bb1b1 = -1 and bb102 = -1 and pukey >= ? and pukey <= ? order by pukey ";
        Object[] param = {start,end};
        SqlRowSet sqlRowSet  = jdbcTemplate.queryForRowSet( sql,param );
        List<QuestionPointChange> changes = Lists.newArrayList();
        while(sqlRowSet.next()){
            int id = sqlRowSet.getInt("PUKEY");
            int question_id = sqlRowSet.getInt("question_id");
            int new_pk_id = sqlRowSet.getInt("pk_id");
            int old_pk_id = sqlRowSet.getInt("pk_old_id");
            int level = sqlRowSet.getInt( "point_level" );
            changes.add(QuestionPointChange.builder()
                    .id( id )
                    .level( level )
                    .questionId( question_id )
                    .newPointId( new_pk_id )
                    .subject( subject )
                    .oldPointId( old_pk_id ).build());
        }
        logger.info( "query table v_question_pk_r_change_log！" );
        return changes;
    }
}
