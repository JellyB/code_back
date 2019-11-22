/**
 * Created by huangqingpeng on 2019/4/3.
 *
 * 活动模考的标签-》（v1/tag/{subject}）
 * 维护常变量JSON,json内容为接口（/pand/match/tag）的返回值
 * 返回值根据subject，返回本科目下的标签列表
 * 》》》 需求变动 20190403 (http://app.htexam.com/wangxiao/app7.1.11/#g=1&p=模考大赛-教师资格证中小学)
 * 变动后:返回值改为根据所属的考试类型所有标签，并且所在科目的标签排在前面
 * 修改方式，JSON内加入考试类型字段（category）,然后做查询
 *
 * 》》》 需求变动，导致部分需要subject参数的接口不能再使用token和header中的值，所以捕获路径中的subjectId作为优先级最高的科目ID
 * 需要捕捉请求路径subjectId的接口有：
 * 模考大赛首页接口、模考大赛我的报告接口、往期模考列表接口
 */
package com.huatu.tiku.match.web.controller.v1.tag;