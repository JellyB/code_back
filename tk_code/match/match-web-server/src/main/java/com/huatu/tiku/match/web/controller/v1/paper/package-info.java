/**
 * 试卷相关接口处理
 * 创建答题卡、保存试卷、提交试卷等
 * Created by lijun on 2018/10/31
 * <p>
 * * @modified by huangqingpeng 2019/05/31
 * * 模考大赛交卷接口升级，交卷逻辑完全异步处理
 * * 调用submit2Queue方法发送消息队列，同时存储待消费的交卷答题卡ID缓存，在消费被消费后，再删除交卷答题卡ID缓存
 * * 定时自动交卷任务在消费被完全消费（即待消费答题卡ID缓存清空后，再实现系统自动交卷功能）
 */
package com.huatu.tiku.match.web.controller.v1.paper;