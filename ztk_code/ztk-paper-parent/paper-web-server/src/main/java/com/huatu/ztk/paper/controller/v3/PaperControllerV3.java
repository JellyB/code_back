package com.huatu.ztk.paper.controller.v3;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperSummary;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.controller.BaseController;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.util.PersonalityAreaUtil;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v3/papers")
public class PaperControllerV3 extends BaseController<Paper> {

    private static final Logger logger = LoggerFactory.getLogger(PaperControllerV3.class);

    @Autowired
    private PaperService paperService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 试卷信息汇总 v3
     * 如：北京 25套
     *
     * @param type 试卷类型 @PaperType
     * @return
     */
    @RequestMapping(value = "summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object summary(//默认真题
                          @RequestParam(required = false, defaultValue = PaperType.TRUE_PAPER + "") int type,
                          @RequestHeader(required = false, defaultValue = "") String token,
                          @RequestParam(defaultValue = "-1") int subject) throws BizException {
//        /**
//         * 调用V2 数据
//         */
//        Object v2Data = new PaperControllerV2().summary(type, token, subject);
//        List<PaperSummary> paperSummaries = (List<PaperSummary>) v2Data;
        int area = AreaConstants.QUAN_GUO_ID;
        if(StringUtils.isNotEmpty(token)){
            userSessionService.assertSession(token);
            area = userSessionService.getArea(token);
        }
        subject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
        subject = userSessionService.convertChildSubjectToParentSubject(subject);
        List<Integer> subjects = Lists.newArrayList(subject);

        logger.info("事业单位科目最终是:{}", subjects);
        List<PaperSummary> paperSummaries = paperService.summaryNew(subjects, area, type);
        Integer subjectId = Optional.of(subjects).filter(i -> i.contains(1)).isPresent()?1:subjects.get(0);
        paperSummaries.forEach(i-> PersonalityAreaUtil.changeName(subjectId,i));
        /**
         * update by lijun 2018-05-08 处理返回数据中的各个地区的版本问题-用以显示是否有新的数据
         */
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        final Set<ZSetOperations.TypedTuple> set = zSetOperations.rangeWithScores("new:true:paper:area:version:" + subject, 0, -1);
        //logger.info("请求key = {},长度 = {}","new:true:paper:area:version:" + subject,set.size());
        List<HashMap<String, Object>> list = paperSummaries.stream()
                .map(paperSummary -> {
                    Optional<ZSetOperations.TypedTuple> first = set.stream().filter(versionData -> versionData.getValue().equals(String.valueOf(paperSummary.getArea()))).findFirst();
                    //logger.info("区域 = {}，结果 = {}",String.valueOf(paperSummary.getArea()),first.isPresent());
                    HashMap<String, Object> hashMap = toMap(paperSummary);
                    hashMap.put("version", first.isPresent() ? first.get().getValue() : -1);
                    return hashMap;
                })
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 根据给定条件查询试卷列表v3
     * 与v1不同的是subject参数
     * <p>
     * update  header中必须传递subject
     *
     * @param token
     * @param page      页码
     * @param area
     * @param size
     * @param year
     * @param subject   科目id,默认公务员行测
     * @param paperType 试卷类型,默认真题
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaperList(@RequestHeader(required = false) String token,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "-1") int area,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "-1") int year,
                               @RequestParam(defaultValue = "-1") int subject,
                               @RequestParam(defaultValue = PaperType.TRUE_PAPER + "") int paperType) throws BizException {

        PageBean<Paper> pageBean = new PageBean<>(Lists.newArrayList(), 0, 0);
        if (StringUtils.isNotEmpty(token)) {
            userSessionService.assertSession(token);

            //取得用户ID
            final long uid = userSessionService.getUid(token);
            if (subject < 0) {
                //事业单位,将（ABC非联考科目,转化为科目为职测）
                subject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subject);
            }

            subject = userSessionService.convertChildSubjectToParentSubject(subject);
            List<Integer> subjects = Lists.newArrayList(subject);

            pageBean = paperService.findForPage(subjects, area, year, paperType, page, size, uid);
        } else {
            //游客模式
            List<Integer> subjects = Lists.newArrayList(subject);
            Long uid = -1L;
            pageBean = paperService.findForPage(subjects, area, year, paperType, page, size, uid);

        }

        /**
         * update by lijun  2018-05-08 标记各个试卷是否为新试卷
         */
        List<Paper> results = pageBean.getResutls();
        if (null != results) {
            Function<Integer, String> getNewTruePaperKey = (paperId) ->
                    new StringBuilder().append("new:true:paper:")
                            .append(paperId).toString();
            List<HashMap<String, Object>> list = results.stream()
                    .map(paper -> {
                        Long expire = redisTemplate.getExpire(getNewTruePaperKey.apply(paper.getId()));
                        HashMap<String, Object> map = toMap(paper);
                        map.put("isNew", expire == null || expire == 0 ? 0 : 1);
                        return map;
                    })
                    .collect(Collectors.toList());
            return new PageBean<>(list, pageBean.getCursor(), pageBean.getTotal());
        }
        return pageBean;
    }


    public static HashMap<String, Object> toMap(Object bean) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (null == bean) {
            return map;
        }
        Class<?> clazz = bean.getClass();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Method method = descriptor.getReadMethod();
                Object result;
                try {
                    result = method.invoke(bean);
                    if (null != result) {
                        map.put(propertyName, result);
                    } else {
                        map.put(propertyName, null);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }

    @RequestMapping(value = "/idList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaperIdsGroupBySubjectAndType() {
        return paperService.getPaperIdsGroupBySubjectAndType();
    }
}
