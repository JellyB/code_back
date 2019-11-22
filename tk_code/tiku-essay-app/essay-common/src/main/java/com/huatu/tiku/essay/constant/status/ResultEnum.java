package com.huatu.tiku.essay.constant.status;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 15:05
 * @Modefied By: 返回码啥的
 */
public enum ResultEnum {
    SUCCESS(20000, "成功"),
    ERROR(50000, "错误"),

    INSERT_FAIL(50001, "插入数据失败"),
    DELETE_FAIL(50002, "删除数据错误"),
    UPDATE_FAIL(50003, "更新数据错误"),
    FIND_FAIL(50004, "查询数据错误"),

    FORMAT_ERROR(50010, "格式错误"),
    TYPE_ERROR(50013, "类型错误"),
    FILE_ERROR(50011, "文件读写错误"),
    UPLOAD_FILE_FAILED(50012, "上传文件失败"),


//    未找到微信用户时，提示：服务故障，请取消关注后再重新关注
//    未知错误时，提示：服务器又调皮了，请稍后重试
//    参数错误时，提示：服务器又调皮了，请稍后重试
//    请求失败时，提示：服务器又调皮了，请稍后重试
//    验证码过期，提示：验证码已过期
//    验证码错误，提示：验证码不正确
//    验证通过，提示：验证成功


    PHP_ERROR(50020, "未知错误"),
    PHP_PARAMETER_ERROR(50021, "参数错误"),
    PHP_MOBILE_ERROR(50022, "您的账号未购买《2018国考封闭特训班》课程"),
    PHP_REQUEST_LOSED(50023, "请求失效"),

    CAPTCHA_EXPIRE(50030, "验证码已过期"),
    CAPTCHA_ERROR(50031, "验证码错误"),
    CAPTCHA_PASS(20000, "验证通过"),

    PARAMETER_NULL_ERROR(50040, "验证通过"),

    OPENID_ERROR(50041, "服务故障，请取消关注后再重新关注"),

    NOTIFICATION_TYPE_ERROR(50042, "推送通知类型错误"),
    PUSH_TIME_ERROR(50043, "推送通知类型错误"),
    LOGIN_ERROR(50044, "用户名或密码错误"),

    CLASS_CAST_ERROR(50050,"班级唯一标识无法识别"),
    Agreement_ERROR(50051,"用户不同意协议"),
    CLASS_UNIQUE_ERROR(50052,"学员所在班级不唯一"),
    CLASS_NULL_ERROR(50053,"学员不在任何班级"),
    CLASS_ARRANGEMENT_UNIQUE(50054,"一个班级只能创建一个"),

    ExcelTypeError(50060,"表格格式不对"),
    ExcelCellError(50061,"表格内容不对"),
    FileEmptyError(50062,"文件为空"),
    ;
    private Integer code;

    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
