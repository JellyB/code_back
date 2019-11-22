package com.arj.monitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author zhouwei
 * @Description: 机器注册中心表
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monitor_server_info")
public class ServerInfo extends BaseEntity {
    /**
     * 服务名称
     */
    private String name;
    /**
     * 服务机器ip
     */
    private String ip;
    /**
     * 服务机器端口
     */
    private String port;
    /**
     * 报警类型 0 钉钉报警 1 短信报警
     */
    @Column(columnDefinition = "smallint default 0")
    private int alarmType;
    /**
     * url类型 0 健康检查  1自定义
     */
    @Column(columnDefinition = "smallint default 0")
    private int type;
    /**
     * 报警频率 一分钟多少次异常报警
     */
    @Column(columnDefinition = "int default 10")
    private int frequency;;
    /**
     * 报警通知电话支持多个英文逗号分隔
     */
    private String telephone;
    /**
     * 报警通知电话支持多个英文逗号分隔
     */
    private String email;
    /**
     * 访问地址
     */
    private String url;

}
