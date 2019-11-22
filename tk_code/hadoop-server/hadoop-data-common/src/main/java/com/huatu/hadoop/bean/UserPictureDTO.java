package com.huatu.hadoop.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPictureDTO {

    //eq ne co nc
    private List<ConditionDTO> cons;


//    terminal_id             int
//    subject_id              bigint
//    area_id                 bigint
//    category_id             int

    public String area2sql() throws Exception {

        String sql = " 1 = 1 ";
        for (ConditionDTO con : cons) {

            if (con.getType().equals("area_id")) {

                Integer[] value = con.getValues();
                String condition = con.getSymbol();

                if (value.length < 1) {
                    break;
                }
                StringBuffer sb = new StringBuffer(0);
                for (Integer i : value) {
                    sb.append(i).append(",");
                }

                String s = sb.deleteCharAt(sb.length() - 1).toString();

                if (condition.equals("in")) {
                    sql = " USER_PICTURE.AREA_ID IN (" + s + ") ";
                } else {
                    sql = " USER_PICTURE.AREA_ID NOT IN (" + s + ") ";
                }
                break;
            }
        }
        return sql;
    }


    public String subject2sql() throws Exception {

        String sql = " 1 = 1 ";
        for (ConditionDTO con : cons) {
            if (con.getType().equals("subject_id")) {

                Integer[] value = con.getValues();
                String condition = con.getSymbol();

                if (value.length < 1) {
                    break;
                }
                StringBuffer sb = new StringBuffer(0);
                for (Integer i : value) {
                    sb.append(i).append(",");
                }

                String s = sb.deleteCharAt(sb.length() - 1).toString();

                if (condition.equals("in")) {
                    sql = " USER_PICTURE.SUBJECT_ID IN (" + s + ") ";
                } else {
                    sql = " USER_PICTURE.SUBJECT_ID NOT IN (" + s + ") ";
                }
                break;
            }
        }
        return sql;
    }


    public String terminal2sql() throws Exception {

        String sql = " 1 = 1 ";
        for (ConditionDTO con : cons) {
            if (con.getType().equals("terminal_id")) {

                Integer[] value = con.getValues();
                String condition = con.getSymbol();

                if (value.length < 1) {
                    break;
                }
                StringBuffer sb = new StringBuffer(0);
                for (Integer i : value) {
                    sb.append(i).append(",");
                }

                String s = sb.deleteCharAt(sb.length() - 1).toString();

                if (condition.equals("in")) {
                    sql = " USER_PICTURE.TERMINAL_ID IN (" + s + ") ";
                } else {
                    sql = " USER_PICTURE.TERMINAL_ID NOT IN (" + s + ") ";
                }
                break;
            }
        }
        return sql;
    }


    public String category2sql() throws Exception {

        String sql = " 1 = 1 ";
        for (ConditionDTO con : cons) {
            if (con.getType().equals("category_id")) {

                Integer[] value = con.getValues();
                String condition = con.getSymbol();

                if (value.length < 1) {
                    break;
                }
                StringBuffer sb = new StringBuffer(0);
                for (Integer i : value) {
                    sb.append(i).append(",");
                }

                String s = sb.deleteCharAt(sb.length() - 1).toString();

                if (condition.equals("in")) {
                    sql = " USER_PICTURE.CATEGORY_ID IN (" + s + ") ";
                } else {
                    sql = " USER_PICTURE.CATEGORY_ID NOT IN (" + s + ") ";
                }
                break;
            }
        }
        return sql;
    }

    public String createSql() throws Exception {
        return this.area2sql() + " and "
                + this.subject2sql() + " and "
                + this.terminal2sql() + " and "
                + this.category2sql();
    }

}
