package com.huatu.hadoop.conf.presto;

import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class PrestoConfiguration {


    @Bean
    public Statement getPrestoStmt() throws ClassNotFoundException {

        return getStatement();
    }
    @Bean
    public Statement getPrestoStmt2() throws ClassNotFoundException {

        return getStatement();
    }
    @Bean
    public Statement getPrestoStmt3() throws ClassNotFoundException {

        return getStatement();
    }

    @Nullable Statement getStatement() throws ClassNotFoundException {
        Connection connection = null;
        Statement statement = null;

        Class.forName("com.facebook.presto.jdbc.PrestoDriver");
        try {
            connection = DriverManager.getConnection(
                    "jdbc:presto://huatu68:9999/hive/default", "hive", "");

            //connect mysql server tutorials database here
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }
}
