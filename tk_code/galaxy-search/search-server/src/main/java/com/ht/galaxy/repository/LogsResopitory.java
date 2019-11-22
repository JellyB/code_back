package com.ht.galaxy.repository;

import com.ht.galaxy.common.ClassPv;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author gaoyuchao
 * @create 2018-08-06 9:33
 */
@Component
public class LogsResopitory {
    @Resource(name="conn3")
    private Connection conn;

    public Object selectClassCvr(String classId) throws Exception {
        Statement statement1 = conn.createStatement();
        Statement statement2 = conn.createStatement();
        Statement statement3 = conn.createStatement();
        Statement statement4 = conn.createStatement();
        Statement statement5 = conn.createStatement();

        String sql1 = "select * from huatu04classid where classId=" + classId;
        ResultSet rs1 = statement1.executeQuery(sql1);

        String sql2 = "select * from huatucvr12classid where classId=" + classId;
        ResultSet rs2 = statement2.executeQuery(sql2);

        String sql3 = "select * from huatu05classid where classId=" + classId;
        ResultSet rs3 = statement3.executeQuery(sql3);

        String sql4 = "select * from huatucvr23classid where classId=" + classId;
        ResultSet rs4 = statement4.executeQuery(sql4);

        String sql5 = "select * from huatu06classid where classId=" + classId;
        ResultSet rs5 = statement5.executeQuery(sql5);

        ClassPv classPv = null;
        while(rs1.next() && rs2.next() && rs3.next() && rs4.next() && rs5.next()) {
            classPv = new ClassPv(rs1.getString("classId"), rs1.getInt("pv"), rs1.getInt("uv"),
                    rs3.getInt("pv"), rs3.getInt("uv"),
                    rs5.getInt("pv"), rs5.getInt("uv"),
                    rs2.getDouble("pv_cvr"), rs2.getDouble("uv_cvr"),
                    rs4.getDouble("pv_cvr"), rs4.getDouble("uv_cvr"));
        }

        statement1.close();
        statement2.close();
        statement3.close();
        statement4.close();
        statement5.close();
        return classPv;
    }

}
