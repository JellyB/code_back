/**
 * 切换服务实现策略：
 * ModuleDubboServiceImpl -> ModuleDubboServiceImplV2
 * ---变动不大，引用KnowledgeService 变为 KnowledgeServiceV2
 * PointSummaryDubboServiceImpl -> PointSummaryDubboServiceImpl
 * PointSummary find(long uid, int subject, int point) ---该方法需要查询用户错题信息和用户已完成题量信息，需要使用新逻辑（QuestionPointServiceV1&&QuestionFinishServiceV1）
 * QuestionPointDubboServerImpl -> 待定
 * QuestionPointDubboServerImpl 暂时将知识树的缓存一道knowledgeService中，涉及到的接口改动需要酌情看看要不要通过写的类去实现
 * QuestionPointService -> 待定
 * QuestionStrategyDubboServiceImpl -> 待定
 */
package com.huatu.ztk.knowledge.service.v2.impl;