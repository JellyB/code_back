package com.huatu.tiku.interview.service.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.interview.constant.NotificationTypeConstant;
import com.huatu.tiku.interview.constant.TemplateEnum;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.dto.NotificationVO;
import com.huatu.tiku.interview.entity.po.NotificationType;
import com.huatu.tiku.interview.entity.template.MyTreeMap;
import com.huatu.tiku.interview.entity.template.TemplateMap;
import com.huatu.tiku.interview.entity.template.WechatTemplateMsg;
import com.huatu.tiku.interview.repository.ClassInfoRepository;
import com.huatu.tiku.interview.repository.NotificationTypeRepository;
import com.huatu.tiku.interview.repository.UserClassRelationRepository;
import com.huatu.tiku.interview.repository.UserRepository;
import com.huatu.tiku.interview.repository.impl.UserRepositoryImpl;
import com.huatu.tiku.interview.service.NotificationService;
import com.huatu.tiku.interview.service.WechatTemplateMsgService;
import com.huatu.tiku.interview.util.GetAllParameter;
import com.huatu.tiku.interview.util.common.PageUtil;
import com.huatu.tiku.interview.util.json.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/18 20:17
 * @Description
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    ClassInfoRepository classInfoRepository;
    @Autowired
    WechatTemplateMsgService templateMsgService;
    @Value("${notify_view}")
    private String notifyView;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    UserClassRelationRepository userClassRelationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRepositoryImpl userRepositoryImpl;
    @Value("${report_hint}")
    private String reportHint;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;
//    @Override
//    public PageUtil<List<NotificationType>> findAll(Integer size,Integer page) {
//        PageRequest pageable = new PageRequest(page-1,size,new Sort(Sort.Direction.DESC,"gmtCreate"));
//        Specification<NotificationType> specification = selectRules();
//        Page<NotificationType> all = notificationTypeRepository.findByStatus(1,pageable);
//        List<NotificationVO> notificationVOs = GetAllParameter.test(all.getContent(), NotificationVO.class);
//        int pageNumber = pageable.getPageNumber();
//        int pageSize = pageable.getPageSize();
//        long totalElements = all.getTotalElements();
//        PageUtil resultPageUtil = PageUtil.builder().result(notificationVOs)
//                .total(totalElements)
//                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
//                .next(totalElements > pageSize * pageNumber ? 1 : 0)
//                .build();
//        return resultPageUtil;
//
//    }

    @Override
    public PageUtil<List<NotificationType>> findByLimit(Integer size, Integer page, String title,int type) {
        PageRequest pageable = new PageRequest(page-1,size,new Sort(Sort.Direction.DESC,"gmtCreate"));
        Specification<NotificationType> specification = selectRules(title,type,0);
        Page<NotificationType> all = notificationTypeRepository.findAll(specification, pageable);
        List<NotificationVO> notificationVOs = GetAllParameter.convert(all.getContent(), NotificationVO.class);
        for(NotificationVO vo:notificationVOs){
            Date now = new Date();
            if(vo.getPushTime() == null || (now.compareTo(vo.getPushTime()) > 0)){
                vo.setCanEdit(true);
            }else{
                vo.setCanEdit(false);
            }
        }

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        long totalElements = all.getTotalElements();
        PageUtil resultPageUtil = PageUtil.builder().result(notificationVOs)
                .total(totalElements)
                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
                .next(totalElements > pageSize * (pageNumber + 1) ? 1 : 0)
                .build();
        return resultPageUtil;
    }

    @Override
    public NotificationType get(Long id) {
        return notificationTypeRepository.findOne(id);
    }

    @Override
    public List<NotificationType> findByPushTime(Date date) {
        return null;
//                notificationTypeRepository.findByPushTime(date);
    }

    /**
     * 定时推送
     */
    @Override
    public void pushAuto() {
        //查询当前时间1分钟内的所有未推送的消息
        long now = System.currentTimeMillis();
        long start = now + TimeUnit.SECONDS.toMillis(-30);
        long end = now + TimeUnit.SECONDS.toMillis(30);

        List<NotificationType> notifyList = notificationTypeRepository.findByPushTime(new Date(start), new Date(end));
        //存在可用的模板消息
        if (CollectionUtils.isNotEmpty(notifyList)) {
            //查询需要推送的学员信息


            for (NotificationType notification : notifyList) {
                List<Map<String, Object>> userList = userRepositoryImpl.listForLimit(1, 100, "", notification.getClassId(), -1);
                if (CollectionUtils.isNotEmpty(userList)) {
                    for(Map user:userList){
                        String openId = (String)user.get("openId");
                        String name = (String)user.get("uname");
                        Long cid = (Long)user.get("cid");

                        if(cid!=null && cid.equals(notification.getClassId())){
                            pushNotification(notification,openId,name);
                        }
                    }
                }
                notification.setBizStatus(NotificationTypeConstant.BizStatus.PUSHED.getBizSatus());
                notificationTypeRepository.save(notification);
            }
        }
    }

    private void pushNotification(NotificationType notification,String openId,String name) {
        WechatTemplateMsg templateMsg = null;
        switch (notification.getType()) {
            //课程安排
            case 1: {
                break;
            }
            //鸡汤
            case 2: {
                templateMsg = new WechatTemplateMsg(openId, TemplateEnum.MorningReading);
                templateMsg.setUrl(notifyView+notification.getId());
                templateMsg.setData(
                        MyTreeMap.createMap(
                                new TemplateMap("first", WechatTemplateMsg.item("每天进步一点点，成功上岸不会远！\n", "#000000")),
                                new TemplateMap("keyword1", WechatTemplateMsg.item(name, "#000000")),
                                new TemplateMap("keyword2", WechatTemplateMsg.item(notification.getTitle(), "#000000")),
                                new TemplateMap("remark", WechatTemplateMsg.item("\n华图在线，你的上岸小帮手~", "#000000"))
                        )
                );
                break;
            }
            //报道通知
            case 3: {
                templateMsg = new WechatTemplateMsg(openId, TemplateEnum.ReportHint);
                templateMsg.setUrl(reportHint);
                templateMsg.setData(
                        MyTreeMap.createMap(
                                new TemplateMap("first", WechatTemplateMsg.item("亲爱的"+name+"同学，您购买的《面试封闭集训营》课程即将开课，请务必及时报到。\n", "#000000")),
                                new TemplateMap("keyword1", WechatTemplateMsg.item("面试封闭集训营", "#000000")),
                                new TemplateMap("keyword2", WechatTemplateMsg.item("2018年4月26日", "#000000")),
                                new TemplateMap("keyword3", WechatTemplateMsg.item("江苏南京", "#000000")),
                                new TemplateMap("keyword4", WechatTemplateMsg.item("400-817-6111", "#000000")),
                                new TemplateMap("remark", WechatTemplateMsg.item("\n如有疑问，请及时与我们取得联系", "#000000"))
                        )
                );
                break;
            }
        }
        //推送模板消息
        templateMsgService.sendTemplate(JsonUtil.toJson(templateMsg));

    }


//    private <T> Specification<T> selectRules(Long id) {
//        Specification specification = new Specification<T>() {
//            @Override
//            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//                List<Predicate> predicates = Lists.newArrayList();
//                if (id != null) {
//                    predicates.add(cb.equal(root.get("id"), id));
//                    predicates.add(cb.equal(root.get("status"),WXStatusEnum.Status.NORMAL.getStatus()));
//                }
//                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
//            }
//        };
//        return specification;
//    }
//    private <T> Specification<T> selectRules() {
//        Specification specification = new Specification<T>() {
//            @Override
//            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//                List<Predicate> predicates = Lists.newArrayList();
//                    predicates.add(cb.equal(root.get("status"),WXStatusEnum.Status.NORMAL.getStatus()));
//                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
//            }
//        };
//        return specification;
//    }

    private <T> Specification<T> selectRules(String title,int type,int id) {
        Specification specification = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = Lists.newArrayList();
                predicates.add(cb.equal(root.get("status"),WXStatusEnum.Status.NORMAL.getStatus()));
                //标题
                if (StringUtils.isNotEmpty(title)) {
                    predicates.add(cb.like(root.get("title"), "%"+title+"%"));
                }
                // 类型
                if (type!= -1) {
                    predicates.add(cb.equal(root.get("type"),type));
                }
                // 类型
                if (id != 0) {
                    predicates.add(cb.equal(root.get("id"),id));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }

    @Override
    public NotificationType saveRegisterReport(NotificationType registerReport) {
        registerReport.setCreator("admin");
        registerReport.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        registerReport.setBizStatus(NotificationTypeConstant.BizStatus.UN_PUSHED.getBizSatus());
        return notificationTypeRepository.save(registerReport);
    }



    @Override
    public int del(Long id) {
       return  notificationTypeRepository.updateToDel(id);
    }

    @Override
    public NotificationType findOne(Long id) {
        return notificationTypeRepository.findByIdAndStatus(id,WXStatusEnum.Status.NORMAL.getStatus());
    }
}
