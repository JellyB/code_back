package com.huatu.tiku.position.biz.service.impl;

import com.alicp.jetcache.anno.Cached;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.huatu.tiku.position.base.exception.BadRequestException;
import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.constant.JetCacheConstant;
import com.huatu.tiku.position.biz.constant.RedisConstant;
import com.huatu.tiku.position.biz.domain.*;
import com.huatu.tiku.position.biz.dto.BooleanDto;
import com.huatu.tiku.position.biz.dto.PositionInfoDto;
import com.huatu.tiku.position.biz.enums.*;
import com.huatu.tiku.position.biz.respository.BrowseRecordRepository;
import com.huatu.tiku.position.biz.respository.PositionRepository;
import com.huatu.tiku.position.biz.respository.ScoreLineRepository;
import com.huatu.tiku.position.biz.service.AreaService;
import com.huatu.tiku.position.biz.service.BrowseRecordService;
import com.huatu.tiku.position.biz.service.PositionService;
import com.huatu.tiku.position.biz.util.DateformatUtil;
import com.huatu.tiku.position.biz.vo.PageVo;
import com.huatu.tiku.position.biz.vo.PositionVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@Slf4j
@Service
public class PositionServiceImpl extends BaseServiceImpl<Position, Long> implements PositionService {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private AreaService areaService;

    @Autowired
    private BrowseRecordService browseRecordService;

    @Autowired
    private ScoreLineRepository scoreLineRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BrowseRecordRepository browseRecordRepository;

    private Cache<String, List<Position>> findPositionCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    @Override
    public Page<Position> findPosition(PositionType type, List<Long> areas, Education education, Degree degree, Political political,
                                       Exp exp, BaseExp baseExp, Sex sex, Integer year, PositionStatus status, Pageable page,
                                       String search,
                                       Integer searchType, Nature nature) {
        Pageable pageable = new PageRequest(page.getPageNumber()
                , page.getPageSize(), new Sort(Sort.Direction.DESC, "year")
        );//倒序

        return positionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(search) && null != searchType) {//有类型 有关键字
                switch (searchType) {
                    case -1:
//                        Join<Position, Specialty> specialtysBean = root.join("specialtys");
                        predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("specialtyString"), "%" + search + "%"),
                                criteriaBuilder.like(root.get("name"), "%" + search + "%")));
                        break;
                    case 1:
//                        Join<Position, Specialty> specialtys = root.join("specialtys");
//                        predicates.add(criteriaBuilder.like(specialtys.get("name"), "%" + search + "%"));//专业名称搜索
                        predicates.add(criteriaBuilder.like(root.get("specialtyString"), "%" + search + "%"));//专业名称搜索
                        break;
                    case 2:
                        Join<Position, Department> departments = root.join("department");
                        predicates.add(criteriaBuilder.like(departments.get("name"), "%" + search + "%"));//单位名称搜索
                        break;
                    case 3:
                        predicates.add(criteriaBuilder.like(root.get("nameStr"), "%" + search + "%"));//职位名称搜索
                        break;
                }
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (areas != null && !areas.isEmpty()) {
                //取出全部子集
                ArrayList list = new ArrayList(areaService.getAllByIds(areas));
                predicates.add(root.get("area").in(list));
            }
            if (education != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxEducation"), education));
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("minEducation"), education));

            }
            if (degree != null) {
                predicates.add(criteriaBuilder.equal(root.get("degree"), degree));
            }
            if (political != null) {
                predicates.add(criteriaBuilder.like(root.get("political"), "%" + political.getText() + "%"));//政治面貌
            }
            if (exp != null) {
                predicates.add(criteriaBuilder.equal(root.get("exp"), exp));//工作经验
            }
            if (baseExp != null) {
                predicates.add(criteriaBuilder.like(root.get("baseExp"), "%" + baseExp.getText() + "%"));//基层工作经历
            }
            if (sex != null) {
                if (Sex.BX == sex) {
                    predicates.add(criteriaBuilder.equal(root.get("sex"), sex));
                } else {
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("sex"), sex),
                            criteriaBuilder.equal(root.get("sex"), Sex.BX)));
                }
            }
            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("year"), year));
            }
            if (status != null) {
                Date now = new Date();
                switch (status) {
                    case JXZ:
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), now));
                        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("beginDate"), now));
                        break;
                    case WKS:
                        predicates.add(criteriaBuilder.greaterThan(root.get("beginDate"), now));
                        break;
                    case YJS:
                        predicates.add(criteriaBuilder.lessThan(root.get("endDate"), now));
                        break;
                }
            }
            if (nature != null) {
                predicates.add(criteriaBuilder.equal(root.get("nature"), nature));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, pageable);
    }

    public List<Position> findPosition(PositionType type, List<Long> areas, Education education, Degree degree, Political political,
                                       Exp exp, BaseExp baseExp, Sex sex, Integer year, PositionStatus status, Nature nature) {
        Expression expression = new SpelExpressionParser()
                .parseExpression("'.type.' + #type + '.areas.' + #areas + '.education.' + #education + " +
                        "'.degree.' + #degree + '.political.' + #political + '.exp.' + #exp + " +
                        "'.baseExp.' + #baseExp + '.sex.' + #sex + '.year.' + #year + " +
                        "'.status.' + #status");
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        standardEvaluationContext.setVariable("type", type);
        standardEvaluationContext.setVariable("areas", areas);
        standardEvaluationContext.setVariable("education", education);
        standardEvaluationContext.setVariable("degree", degree);
        standardEvaluationContext.setVariable("political", political);
        standardEvaluationContext.setVariable("exp", exp);
        standardEvaluationContext.setVariable("baseExp", baseExp);
        standardEvaluationContext.setVariable("sex", sex);
        standardEvaluationContext.setVariable("year", year);
        standardEvaluationContext.setVariable("status", status);

        return findPositionCache.get(expression.getValue(standardEvaluationContext).toString(), key -> {
            return positionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {

                criteriaQuery.distinct(true);
                List<Predicate> predicates = new ArrayList<>();

                if (type != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                }
                if (areas != null && !areas.isEmpty()) {
                    //取出全部子集
                    ArrayList list = new ArrayList(areaService.getAllByIds(areas));
                    predicates.add(root.get("area").in(list));
                }
                if (education != null) {//学历
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("maxEducation"), education));
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("minEducation"), education));

                }
                if (degree != null) {//学位
                    predicates.add(criteriaBuilder.or(criteriaBuilder.lessThanOrEqualTo(root.get("degree"), degree),
                            criteriaBuilder.equal(root.get("degree"), Degree.BX)));
                }
                if (political != null) {//政治面貌
                    predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("political"), "%" + political.getText() + "%"),
                            criteriaBuilder.equal(root.get("political"), Political.BX.getText())));
                }
                if (exp != null) {//经验
                    predicates.add(criteriaBuilder.or(criteriaBuilder.lessThanOrEqualTo(root.get("exp"), exp),//TODO
                            criteriaBuilder.equal(root.get("exp"), Exp.BX)));
                }
                if (baseExp != null) {//基层工作经验
                    predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("baseExp"), "%" + baseExp.getText() + "%"),
                            criteriaBuilder.equal(root.get("baseExp"), BaseExp.BX.getText())));//基层工作经历
                }
                if (sex != null) {//性别
                    if (Sex.BX == sex) {
                        predicates.add(criteriaBuilder.equal(root.get("sex"), sex));
                    } else {
                        predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("sex"), sex),
                                criteriaBuilder.equal(root.get("sex"), Sex.BX)));
                    }
                }
                if (nature != null) {
                    predicates.add(criteriaBuilder.equal(root.get("nature"), nature));
                }
                Date now = new Date();
                String nowDateString = DateformatUtil.format0(now);
                Date nowDate=DateformatUtil.parse0(nowDateString);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), nowDate));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }, new Sort(Sort.Direction.DESC, "id"));
        });

    }

//    @Override
//    public List<BigInteger> scoreLine(Long id) {
//        return positionRepository.scoreLine(id);
//    }

    /**
     * 计算该职位对用户推荐等级
     *
     * @param position 职位
     * @param user     用户
     */
    @Override
    public PositionInfoDto getRecommendationRank(Position position, User user) {
        Double count = 0D;//总星数
        return computeCount(count, position, user);
    }

    @Override
    public Page<Position> findEnrollPositions(Pageable page, Long userId) {
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(Sort.Direction.DESC, "id"));//倒序

        return positionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            Join<Position, Enroll> enrolls = root.join("enrolls");
            predicates.add(criteriaBuilder.equal(enrolls.get("userId"), userId));//判断用户id
            predicates.add(criteriaBuilder.equal(enrolls.get("status"), (byte) 1));

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, pageable);
    }

    @Override
    public Page<Position> findCollectionPositions(Pageable page, Long userId) {
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(Sort.Direction.DESC, "id"));//倒序

        return positionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {

            criteriaQuery.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            Join<Position, BrowseRecord> browseRecords = root.join("browseRecords");
            predicates.add(criteriaBuilder.equal(browseRecords.get("userId"), userId));//判断用户id
            predicates.add(criteriaBuilder.isTrue(browseRecords.get("collectionFlag")));//条件为已收藏

            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        }, pageable);
    }

    @Override
    public List<ScoreLine> findByPositionId(Long id) {
        return scoreLineRepository.findByPositionId(id);
    }

    @Override
    public Integer findCount() {
        return positionRepository.findCount();
    }

    /**
     * 判断条件 计算总分数
     *
     * @param count    分数
     * @param position 职位
     * @param user     用户数据
     * @return 星数
     */
    private PositionInfoDto computeCount(Double count, Position position, User user) {
        try {
//            Long id = position.getId();
//            if(id==72133){
//                System.out.println("asdasdasda");
//            }
            AtomicInteger accordCount=new AtomicInteger(0);
            BooleanDto flag=new BooleanDto();
            count = computeSpecialty(count,position.getSpecialtyString(), position.getSpecialtys(), user.getSpecialty(),accordCount);//计算专业分值
            count = computeEducation(count, position.getMaxEducation().ordinal(),
                    position.getMinEducation().ordinal(), user.getEducation(), accordCount);//学历
            count = computeArea(count, position.getArea(), user.getAreas(),accordCount);//地区
            count = computeNumber(count, position.getNumber(),accordCount);//招生人数
            count = computeExp(count, position.getExp(), user.getExp(),accordCount);//工作经验   以上五项比必填项  以下为选填项 顺序与excel保持一致
            //户籍生源地
            count = computeBirthArea(count, position.getBirthAreaIds(), user.getBirthAreaId(),
                    accordCount,position.getDomicileAreaIds(), user.getRegisterAreaId(),
                    user.getBirthArea(),user.getRegisterArea(), flag);//生源
//            count = computeRegisterArea(count, position.getDomicileAreaId(), user.getRegisterAreaId(),accordCount);//户籍

            count = computeBaseExp(count, position.getBaseExp(), user.getBaseExp(),  flag,accordCount);//计算基层服务经验
            count = computePolitical(count, position.getPolitical(), user.getPolitical(), flag,accordCount);//计算政治面貌分值
            count = computeRecent(count, position.getRecent(), user.getRecent(), flag,accordCount);//计算应届生分值
            count = computeSex(count, position.getSex(), user.getSex(),  flag,accordCount);//计算性别分值
            count = computeGraduationYear(count, position.getGraduationYear(), user.getGraduationYear(),  flag,accordCount);//毕业年份
            count = computeBirthYear(count, position.getBirthYear(), user.getBirthdayYear(),  flag,accordCount);//年龄 出生年份
            count = computeCertificate(count, position.getCertificate(), user.getCertificate(),  flag,accordCount);//证书
            count = computeEnglishType(count, position.getEnglishType(), user.getEnglishType(),  flag,accordCount);//英语水平
            count = computeNation(count, position.getNation(), user.getNation(),  flag,accordCount);//民族

            List<String> label = computeLabel(position, user,accordCount);//标签
            PositionInfoDto dto=new PositionInfoDto();
            dto.setStart(computeStart(count, position, user.getId(), flag));

            dto.setLabel(label);//计算标签
            return dto;
        } catch (BadRequestException e) {//出现异常 不符合条件 返回0星
//            log.error("computeCount exception", e);
            throw e;
        }
    }



    /**
     * 根据职位与用户计算标签
     * @return 标签集合
     */
    private List<String> computeLabel(Position position, User user,AtomicInteger accordCount) {
        List<String> result= Lists.newArrayList();
        List<Specialty> specialtys = position.getSpecialtys();
        if(null!=specialtys&&specialtys.contains(user.getSpecialty())){
            result.add("定制专业");
        }
        Set<Area> areas = user.getAreas();
        if(null!=areas&&areas.contains(position.getArea())){
            result.add("目标城市");
        }
        if(null==specialtys&&"大专及以上".equals(position.getEducation())&&null==position.getDomicileAreaIds()){
            result.add("三不限");
        }
//        if(1==1){
//            result.add("匹配几项要求");
//        }
        if(null!=accordCount&&0!=accordCount.intValue()){  //匹配几项要求
            result.add("匹配"+accordCount.intValue()+"项要求");
        }
        if(null!=position.getSchoolType()){  //国立,私立
            result.add(position.getSchoolType().getText());
        }
        return result;
    }

    /**
     * 计算民族
     */
    private Double computeNation(Double count, String positionNation, String userNation, BooleanDto flag,AtomicInteger accordCount) {
        if(StringUtils.isNotBlank(positionNation)){
            if("不限".equals(positionNation)){
                return count;
//            } else if ("少数民族".equals(positionNation)) {
            } else if (positionNation.contains("少数民族")) {
                if (null == userNation) {
                     flag.setResult(true);
                    return count;
                }else if(!"汉族".equals(userNation)){
                    count += 2;
                    accordCount.getAndIncrement();
                }else{
                    throw new BadRequestException("position nation exception");//不符合条件
                }
            }else{
                if (null == userNation) {
                    flag.setResult(true);
                    return count;
                }else if(positionNation.contains(userNation)){  //指定民族包含用户民族
                    count += 2;
                    accordCount.getAndIncrement();
                }else{
                    throw new BadRequestException("position nation exception");//不符合条件
                }
            }
        }

        return count;
    }

    /**
     * 计算英语 多对多情况
     */
    private Double computeEnglishType(Double count, String positionEnglishType, String userEnglishType, BooleanDto flag, AtomicInteger accordCount) {
        if (StringUtils.isNotBlank(positionEnglishType)) {    //职位有英语要求
            if (StringUtils.isNotBlank(userEnglishType)) {    //用户有英语数据
                if(userEnglishType.contains("英语六级")){
                    userEnglishType=userEnglishType+",英语四级";
                }
                String[] split = userEnglishType.split(",");
                for (String s : split) {  //用户英语中有要求英语
                    if(positionEnglishType.contains(s)){
                        count += 2;
                        accordCount.getAndIncrement();
                        return count;
                    }
                }
            }else {
                flag.setResult(true);
                return count;
            }
            throw new BadRequestException("position englishType exception");//不符合条件
        }
        return count;
    }

    /**
     * 计算证书 多对多情况
     */
    private Double computeCertificate(Double count, String positionCertificate, String userCertificate, BooleanDto flag, AtomicInteger accordCount) {
        if (StringUtils.isNotBlank(positionCertificate)) {    //职位有证书要求
            if (StringUtils.isNotBlank(userCertificate)) {    //用户有证书数据
                String[] split = userCertificate.split(",");
                for (String s : split) {  //用户证书中有要求证书
                    if(positionCertificate.contains(s)){
                        count += 2;
                        accordCount.getAndIncrement();
                        return count;
                    }
                }
            }else {
                flag.setResult(true);
                return count;
            }
            throw new BadRequestException("position certificate exception");//不符合条件
        }
        return count;
    }

    /**
     * 计算出生年份
     */
    private Double computeBirthYear(Double count, Integer positionBirthdayYear, Integer userBirthdayYear, BooleanDto flag, AtomicInteger accordCount) {
        if (null != positionBirthdayYear) {   //职位有年龄限制
            if (null != userBirthdayYear) {  //用户填写了出生年
                if (userBirthdayYear >= positionBirthdayYear) {  //符合条件 比指定年龄小 即出生年大于等于指定年
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }
            }else  {
                flag.setResult(true);
                return count;
            }
            throw new BadRequestException("position birthyear exception");//不符合条件
        }
        return count;
    }

    /**
     * 计算毕业年份
     */
    private Double computeGraduationYear(Double count, Integer positionGraduationYear, Integer userGraduationYear, BooleanDto flag, AtomicInteger accordCount) {

        if (null != positionGraduationYear) {//不限 为0分
            if(null!=userGraduationYear){
                if (userGraduationYear >= positionGraduationYear) {  //符合条件
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }
            }else {
                 flag.setResult(true);
                return count;
            }
            throw new BadRequestException("position graduationyear exception");//不符合条件
        }

        return count;
    }

//    /**
//     * 计算户籍分值
//     */
//    private Double computeRegisterArea(Double count, Long positionDomicileAreaId, Long userRegisterAreaId, AtomicInteger accordCount) {
//        if (null != positionDomicileAreaId && positionDomicileAreaId == userRegisterAreaId) {   //职位户籍要求限制并且和用户户籍相同
//            count += 2;
//            accordCount.getAndIncrement();
//        }
//        return count;
//    }

    /**
     * 计算生源分值
     */
    private Double computeBirthArea(Double count, String positionBirthAreaId, Long userBirthAreaId,
                                    AtomicInteger accordCount, String positionDomicileAreaId, Long userRegisterAreaId,
                                    Area birthArea, Area registerArea, BooleanDto flag) {
        if(StringUtils.isBlank(positionBirthAreaId)&&StringUtils.isBlank(positionDomicileAreaId)){ //两个要求都没有 直接返回
            return count;
        }
        if (StringUtils.isNotBlank(positionBirthAreaId)) {
            String[] split = positionBirthAreaId.split(",");
            ArrayList<String> areaList = Lists.newArrayList(split);
            if (null == userBirthAreaId) {  //用户没填信息 直接返回
                flag.setResult(true);
                return count;
            } else {
                if (areaList.contains(String.valueOf(userBirthAreaId))) {
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }else if(areaList.contains(String.valueOf(birthArea.getParentId()))){
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }else if(areaList.contains(String.valueOf(birthArea.getParentArea().getParentId()))){
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }

            }
        }

        if (StringUtils.isNotBlank(positionDomicileAreaId)) {
            String[] split = positionDomicileAreaId.split(",");
            ArrayList<String> areaList = Lists.newArrayList(split);
            if (null == userRegisterAreaId) {  //用户没填信息 直接返回
                flag.setResult(true);
                return count;
            } else {
                if (areaList.contains(String.valueOf(userRegisterAreaId))) {
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }else if(areaList.contains(String.valueOf(registerArea.getParentId()))){
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }else if(areaList.contains(String.valueOf(registerArea.getParentArea().getParentId()))){
                    count += 2;
                    accordCount.getAndIncrement();
                    return count;
                }

            }
        }

        throw new BadRequestException("position computeBirthArea exception");


//        if (null != positionBirthAreaId && null!=userBirthAreaId&&positionBirthAreaId .contains(String.valueOf(userBirthAreaId))) {   //职位生源要求限制并且和用户生源相同
//            count += 2;
//            accordCount.getAndIncrement();
//            return count;
//        }else{
//            if(StringUtils.isBlank(positionBirthAreaId)){
//                return count;
//            }
//            if(userBirthAreaId!=null){
//                if(positionBirthAreaId .contains(String.valueOf(birthArea.getParentId()))||positionBirthAreaId .contains(String.valueOf(birthArea.getParentArea().getParentId()))){
//                    count += 2;
//                    accordCount.getAndIncrement();
//                    return count;
//                }
//            }else{
//                flag.setResult(true);
//                return count;
//            }
//        }

//        if (null != positionDomicileAreaId && null!=userRegisterAreaId&&positionDomicileAreaId.contains(String.valueOf(userRegisterAreaId))) {   //职位户籍要求限制并且和用户户籍相同
//            count += 2;
//            accordCount.getAndIncrement();
//            return count;
//        }else{
//            if(StringUtils.isBlank(positionDomicileAreaId)){
//                return count;
//            }
//            if(userRegisterAreaId!=null){
//                if(positionDomicileAreaId .contains(String.valueOf(registerArea.getParentId()))||positionDomicileAreaId .contains(String.valueOf(registerArea.getParentArea().getParentId()))){
//                    count += 2;
//                    accordCount.getAndIncrement();
//                    return count;
//                }
//            }else{
//                flag.setResult(true);
//                return count;
//            }
//        }
//        throw new BadRequestException("position computeBirthArea exception");
//        return count;
    }

    /**
     * 计算性别分值
     */
    private Double computeSex(Double count, Sex positionSex, Sex userSex, BooleanDto flag, AtomicInteger accordCount) {
        if (null == positionSex || Sex.BX.equals(positionSex)) {//不限或要求为空 为0分
            return count;
        } else {
            if (positionSex.equals(userSex)) { //性别相同
                count += 2;
                accordCount.getAndIncrement();
                return count;
            } else if (null == userSex) {
                flag.setResult(true);
                return count;
            } else {
                throw new BadRequestException("position sex exception");
            }
        }
    }

    /**
     * 计算应届生分值
     */
    private Double computeRecent(Double count, Boolean positionRecent, Boolean userRecent,BooleanDto flag,AtomicInteger accordCount) {
        if (null == positionRecent ) {//不限或要求为否 为0分
            return count;
        } else {
            if (positionRecent == userRecent) {
                count += 2;
                accordCount.getAndIncrement();
                return count;
            } else if (null == userRecent) {
                flag.setResult(true);
                return count;
            }else {
                throw new BadRequestException("position Recent exception");
            }
        }
    }

    /**
     * 计算政治面貌分值
     */
    private Double computePolitical(Double count, String positionPolitical, Political userPolitical, BooleanDto flag, AtomicInteger accordCount) {
        if (StringUtils.isBlank(positionPolitical) || Political.BX.getText().equals(positionPolitical)) {//不限或要求为空 为0分
            return count;
        } else if (null == userPolitical) {
            flag.setResult(true);
            return count;
        } else {
//            String[] split = positionPolitical.split(",");
//            List<String> politicals = Arrays.asList(split);
            if (positionPolitical.contains(userPolitical.getText())) {
                count += 2;
                accordCount.getAndIncrement();
                return count;
            } else {
                throw new BadRequestException("position Political exception");
            }
        }
    }

    /**
     * 计算基层服务经验分值
     */
    private Double computeBaseExp(Double count, String positionBaseExp, BaseExp userBaseExp,BooleanDto flag,AtomicInteger accordCount) {
        if (!StringUtils.isNotBlank(positionBaseExp) || BaseExp.BX.getText().equals(positionBaseExp)) {  //不限或要求为空 为0分
            return count;
        } else if (null == userBaseExp) {
            flag.setResult(true);
            return count;
        } else if (null != userBaseExp && positionBaseExp.contains(userBaseExp.getText())) {//完全相同  或者包含
            count += 2;
            accordCount.getAndIncrement();
            return count;
        }
        throw new BadRequestException("position BaseExp exception");

    }

    /**
     * 计算专业分值
     */
    private Double computeSpecialty(Double count,String specialtyString, List<Specialty> Specialtys, Specialty specialty,AtomicInteger accordCount) {
//        if (null == Specialtys || Specialtys.isEmpty()|) {//专业为不限
        if (specialtyString.contains("不限") && specialtyString.length() < 4) {//专业为不限
            count += 12 * 0.3;
        } else if (null == specialty) {  //未设置专业情况 放到空值集合 抛出异常
            throw new BadRequestException("specialty exception");
        } else {  //判断是否符合专业
            List ids=Specialtys.stream().map(bean->bean.getId()).collect(Collectors.toList());
            if (ids.contains(specialty.getId())) {//完全匹配
//            if (Specialtys.contains(specialty)) {//完全匹配
                if (1 == Specialtys.size()) {
                    count += 12 * 1.0;  //且只有一个专业
                    accordCount.getAndIncrement();
                } else {
                    count += 12 * 0.7;//有多个专业
                    accordCount.getAndIncrement();
                }
            } else {  //没有此专业 判断是否二级专业
                if (ids.contains(specialty.getParentId())) { //与父级专业匹配
//                if (Specialtys.contains(specialty.getParentId())) { //与父级专业匹配
                    count += 12 * 0.7;
                } else { //没有此专业 判断是否一级专业
                    Specialty parentSpecialty = specialty.getParentSpecialty();//父
                    if (null == parentSpecialty) {
                        throw new BadRequestException("specialty exception");
                    }
                    if (ids.contains(parentSpecialty.getParentId())) {//与父级专业的父级专业匹配
//                    if (Specialtys.contains(parentSpecialty.getParentId())) {//与父级专业的父级专业匹配
                        count += 12 * 0.5;
                    } else { //不是同一级专业抛出异常
                        throw new BadRequestException("specialty exception");
                    }
                }
                accordCount.getAndIncrement();
            }
        }
        return count;
    }

    /**
     * 计算工作经验分值
     */
    private Double computeExp(Double count, Exp positionExp, Exp userExp, AtomicInteger accordCount) {
        if (null==positionExp||Exp.BX.equals(positionExp)) {//要求为不限或为空
            return count;
        }
        Integer positionIndex = positionExp.ordinal();
        Integer userIndex = userExp.ordinal();
        if (userIndex >= positionIndex) {
            count += 2;
            accordCount.getAndIncrement();
        }
        return count;
    }

    /**
     * 计算学历分值
     */
    private Double computeEducation(Double count, Integer max, Integer min, Education education, AtomicInteger accordCount) {
        Integer index = education.ordinal();
        if (index.equals(max) && index.equals(min)) {
            count += 6;
            accordCount.getAndIncrement();
        } else if (index >= min && index <= max) {
            count += 6 * 0.5;
            accordCount.getAndIncrement();
        } else {
            count += 6 * 0.2;
        }
        return count;
    }

    /**
     * 计算报考地区分值
     */
    private Double computeArea(Double count, Area positionArea, Set<Area> userAreas, AtomicInteger accordCount) {
        if (userAreas.isEmpty() || (userAreas.iterator().next().getId().equals(0L))) {
            count += 8 * 0.2;
            accordCount.getAndIncrement();
        } else if (userAreas.stream().anyMatch(area -> area.getId().equals(positionArea.getId()))) {  //报考地区 包含完全相等的地区
            count += 8;
            accordCount.getAndIncrement();
        } else if (userAreas.stream().anyMatch(area -> area.getId().equals(positionArea.getParentId()))) {//选择地区为该地区上级
            count += 8 * 0.2;
            accordCount.getAndIncrement();
        } else {
//            Long parentId = positionArea.getParentArea().getParentId();
            Area parentArea = areaService.findOne(positionArea.getParentId());
            Long parentId = parentArea.getParentId();//该地区上上级地区id
            if (userAreas.stream().anyMatch(area -> area.getId().equals(parentId))) {
                count += 8 * 0.05;
                accordCount.getAndIncrement();
            } else {
                throw new BadRequestException("area exception");
            }
        }
        return count;
    }

    /**
     * 计算招生人数分值
     */
    private Double computeNumber(Double count, Integer number, AtomicInteger accordCount) {
        if (null == number || number <= 0) {
            throw new BadRequestException("position number exception");
        } else if (number == 1) {
            count += 6 * 0.2;
            accordCount.getAndIncrement();
        } else if (number == 2) {
            count += 6 * 0.5;
            accordCount.getAndIncrement();
        } else if (number == 3) {
            count += 6 * 0.6;
            accordCount.getAndIncrement();
        } else if (number == 4) {
            count += 6 * 0.7;
            accordCount.getAndIncrement();
        } else if (10 > number && number >= 5) {
            count += 6 * 0.8;
            accordCount.getAndIncrement();
        } else {
            count += 6;
            accordCount.getAndIncrement();
        }
        return count;
    }

    /**
     * 根据分数计算星级
     */
    private Double computeStart(Double count, Position position, Long id,BooleanDto flag) {
        if (null!=position.getIsMark()&&position.getIsMark()) { //有额外条件 2分  如果符合额外条件在计算分数
            BrowseRecord browseRecord = browseRecordService.findByUserIdAndPositionId(id, position.getId());
            if (null==browseRecord||browseRecord.getAccordFlag()==null) {   // 不符合额外条件或未填写备注符合情况
                if (flag!=null&&flag.getResult()) {  //空信息中有此用户
                    return 1.0;
                }
                return 2.0;
            }else{
                if(!browseRecord.getAccordFlag()){
                    throw new BadRequestException("position mark null accordFlag exception");
                }
            }
        }
        if (count > 34) {
            return 5.0;
        } else if (count > 28) {
            return 4.5;
        } else if (count > 20) {
            return 4.0;
        } else if (count > 12) {
            return 3.5;
        } else if (count > 6.4) {
            return 3.0;
        } else {  //TODO 两星一星待定
            return 0.0;
        }
    }

    @Override
    public void renderDynamicData(PageVo<PositionVo> positionPage) {
        positionPage.getContent().forEach(positionVo -> {
            String key = RedisConstant.POSITION_BROWSE_COUNTER.replace("{id}", positionVo.getId().toString());

            String count = stringRedisTemplate.opsForValue().get(key);

            if (count == null) {
                positionVo.setBrowseCount(browseRecordRepository.countByPositionId(positionVo.getId()));
            } else {
                positionVo.setBrowseCount(Integer.parseInt(count));
            }
        });
    }

    @Cached(name = JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME,
            key = JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_KEY,
            expire = 6 * 60 * 60)
    @Override
    public PageVo<PositionVo> findPositionForCache(PositionType type, List<Long> areas, Education education, Degree degree, Political political, Exp exp, BaseExp baseExp, Sex sex, Integer year, PositionStatus status, Pageable page, String search, Integer searchType, Nature nature) {
        Expression expression = new SpelExpressionParser()
                .parseExpression("'.type.' + #type + '.areas.' + #areas + '.education.' + #education + " +
                        "'.degree.' + #degree + '.political.' + #political + '.exp.' + #exp + " +
                        "'.baseExp.' + #baseExp + '.sex.' + #sex + '.year.' + #year + " +
                        "'.status.' + #status + '.page.' + #page.getPageNumber() + '.size.' + #page.getPageSize() + " +
                        "'.search.' + #search + '.searchType.' + #searchType + '.nature.' + #nature");
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        standardEvaluationContext.setVariable("type", type);
        standardEvaluationContext.setVariable("areas", areas);
        standardEvaluationContext.setVariable("education", education);
        standardEvaluationContext.setVariable("degree", degree);
        standardEvaluationContext.setVariable("political", political);
        standardEvaluationContext.setVariable("exp", exp);
        standardEvaluationContext.setVariable("baseExp", baseExp);
        standardEvaluationContext.setVariable("sex", sex);
        standardEvaluationContext.setVariable("year", year);
        standardEvaluationContext.setVariable("status", status);
        standardEvaluationContext.setVariable("page", page);
        standardEvaluationContext.setVariable("search", search);
        standardEvaluationContext.setVariable("searchType", searchType);
        standardEvaluationContext.setVariable("nature", nature);

        stringRedisTemplate.opsForSet().add(JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME, expression.getValue(standardEvaluationContext).toString());
        stringRedisTemplate.expire(JetCacheConstant.POSITION_CONTROLLER_FIND_POSITION_NAME, 6 * 60 * 60, TimeUnit.SECONDS);

        Page<Position> positions = findPosition(type, areas, education, degree, political, exp, baseExp, sex, year, status, page, search, searchType, nature);

        return new PageVo(positions, positions.getContent().stream().map(PositionVo::new).collect(Collectors.toList()));
    }

    @Override
    public void purgePositionCache() {
        findPositionCache.invalidateAll();
    }
}
