package com.huatu.tiku.interview.entity.qrcode;

/**
 * Created by Administrator on 2016/11/10.
 */
public class Qrode {
    private String  expire_seconds ;//该二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为30秒。
    private String  action_name ;//二维码类型，QR_SCENE为临时,QR_LIMIT_SCENE为永久,QR_LIMIT_STR_SCENE为永久的字符串参数值
    private Qrode[] actioninof;
    private String  scene_id;//场景值ID，临时二维码时为32位非0整型，永久二维码时最大值为100000（目前参数只支持1--100000）
    private String  scene_str ;//场景值ID（字符串形式的ID），字符串类型，长度限制为1到64，仅永久二维码支持此字段
    private String  ticket;//获取的二维码ticket，凭借此ticket可以在有效时间内换取二维码。
    private String  url;//二维码图片解析后的地址，开发者可根据该地址自行生成需要的二维码图片

    public String getExpire_seconds() {
        return expire_seconds;
    }

    public String getAction_name() {
        return action_name;
    }

    public String getScene_id() {
        return scene_id;
    }

    public String getScene_str() {
        return scene_str;
    }

    public String getTicket() {
        return ticket;
    }

    public String getUrl() {
        return url;
    }

    public void setExpire_seconds(String expire_seconds) {
        this.expire_seconds = expire_seconds;
    }

    public void setAction_name(String action_name) {
        this.action_name = action_name;
    }

    public void setScene_id(String scene_id) {
        this.scene_id = scene_id;
    }

    public void setScene_str(String scene_str) {
        this.scene_str = scene_str;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setActioninof(Qrode[] actioninof) {
        this.actioninof = actioninof;
    }

    public Qrode[] getActioninof() {
        return actioninof;
    }
}
