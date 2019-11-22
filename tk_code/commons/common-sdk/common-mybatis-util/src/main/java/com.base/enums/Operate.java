package com.base.enums;

/**
 * Created by junli on 2017/9/20.
 */
public enum Operate {

    LIKE("LK", " like "),
    LEFTLIKE("LL", " like "),
    RIGHTLIKE("RL", " like "),
    EQUAL("EQ", " = "),
    NOTEQUAL("NE", " != "),
    ISNULL("ISN", " is null "),
    NOTNULL("NN", " is not null "),
    IN("IN", " in "),
    NOTIN("NI", " not in "),
    LESS("L", " < "),
    LESSEQUAL("LE", " <= "),
    GREAT("G", " > "),
    GREATEQUAL("GE", " >= ");

    private final String simple;
    private final String sql;

    Operate(String simple, String sql) {
        this.simple = simple;
        this.sql = sql;
    }

    public String getSimple() {
        return simple;
    }

    public String getSql() {
        return sql;
    }

    /**
     * 判断需要添做通配的 连接词
     *
     * @param operate 连接词
     * @return 判断结果
     */
    public static boolean validateOperate(Operate operate) {
        return !(operate.equals(ISNULL) || operate.equals(NOTNULL) ||
                operate.equals(IN) || operate.equals(NOTIN))
                ;
    }

    /**
     * 判断可以接受空值的连接词
     *
     * @param operate 连接词
     * @return 判断结果
     */
    public static boolean validateOperateParamIsNull(Operate operate) {
        return operate.equals(ISNULL) || operate.equals(NOTNULL) ||
                operate.equals(IN) || operate.equals(NOTIN)
                ;
    }
}
