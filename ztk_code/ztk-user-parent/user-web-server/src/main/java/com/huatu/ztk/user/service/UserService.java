package com.huatu.ztk.user.service;

import com.esotericsoftware.minlog.Log;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.sms.MDSmsUtil;
import com.huatu.ztk.sms.SmsUtil;
import com.huatu.ztk.user.bean.*;
import com.huatu.ztk.user.common.*;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.galaxy.SendRegister;
import com.huatu.ztk.user.galaxy.report.SendEvent;
import com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext;
import com.huatu.ztk.user.utils.Crypt3Des;
import com.huatu.ztk.user.utils.SensorsUtils;
import com.huatu.ztk.user.utils.UcenterUtils;
import com.huatu.ztk.user.utils.WechatAESUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.ztk.user.common.UcenterConfig.UCENTER_MEMBERS_TEACHER_APPID;
import static com.huatu.ztk.user.galaxy.report.UserGetCaptchaContext.UserGetCaptchaEvent.CHECK_CAPTCHA;

/**
 * Created by shaojieyue Created time 2016-04-24 09:43
 */

@Service
public class UserService {
    public static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // token 过期时间 单位：s 7776000=3个月,此处一定要定义为long，不然×1000存在越界
    public static final long USER_TOKEN_TIME_OUT = 7776000;
    public static final int DEFAULT_AREA = -9;
    public static final String REGISTER_USER_PHP = "8";

    public static final String ESSAY_GOODS_FREE_KEY = "essay-server.essay_goods_free";

    // 语音答题开关
    public static final String VOICE_ANSWER_KEY = "essay-server.voice_answer";// (已停用)
    public static final String VOICE_ANSWER_KEY_ANDROID = "essay-server.voice_answer_android";
    public static final String VOICE_ANSWER_KEY_IOS = "essay-server.voice_answer_ios";

    // 拍照答题开关
    public static final String PHOTO_ANSWER_KEY = "essay-server.photo_answer";// (已停用)
    public static final String PHOTO_ANSWER_KEY_ANDROID = "essay-server.photo_answer_android";
    public static final String PHOTO_ANSWER_KEY_IOS = "essay-server.photo_answer_ios";
    public static final String PHOTO_ANSWER_KEY_IOS_OLD = "essay-server.photo_answer_ios_old";
    public static final String PHOTO_ANSWER_KEY_ANDROID_OLD = "essay-server.photo_answer_android_old";

    // 拍照答题对接第三方
    public static final String PHOTO_ANSWER_TYPE_IOS = "essay-server.photo_answer_type_ios";
    public static final String PHOTO_ANSWER_TYPE_ANDROID = "essay-server.photo_answer_type_android";
    public static final String PHOTO_ANSWER_MSG = "essay-server.photo_answer_msg";

    //首次注册用户
    public static final String REGISTER_KEY = "register:first_login:key";

    //初始化密码值
    public static final String DEFAULT_INT_PASSWORD = "huatuessay20180226";
    public static final int ESSAY_YOU_TU = 0;
    public static final int ESSAY_HAN_WANG = 1;

    public static final int ESSAY_GOODS_FREE = 1;
    public static final int VOICE_ANSWER_ON = 0;
    public static final int PHOTO_ANSWER_ON = 0;
    public static final int ESSAY_GOODS_NOT_FREE = 0;
    public static final int VOICE_ANSWER_OFF = 1;
    public static final int PHOTO_ANSWER_OFF = 1;

    // 用户终端类型
    public static final String CENTER_REG_FROM_M = "7";// M 站
    // 中心默认的来源信息
    public static final String CENTER_REG_FROM = "9";
    //教师网来源
    public static final String REG_FROM_TEACHER = "101";
    // 小程序
    public static final String SMALL_PROGRAM = "21";// 小程序
    //默认用户ID
    public static final Long DEFAULT_USER_ID = -1L;

    private static final String DEFAULT_AVATAR = "http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png";
    private static final String NEW_AVATAR = "http://tiku.huatu.com/cdn/images/vhuatu/avatars/default2.png";

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserSessionService userSessionService;

    @Resource(name = "sessionRedisTemplate")
    private RedisTemplate<String, String> sessionRedisTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private UcenterService ucenterService;

    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UserRewardService userRewardService;

    @Autowired
    private SendRegister sendRegister;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UserServerConfig userServerConfig;

    @Autowired
    private RegisterFreeCourseDetailConfig registerFreeCourseDetailConfig;

    @Autowired
    private SendEvent sendEvent;

    @Autowired
    private ThirdTaskComponent thirdTaskComponent;

    @Resource(name = "coreRedisTemplate")
    private RedisTemplate coreRedisTemplate;

    private final String userPhoneStr = "15504959253,15587160425,15587204399,15757101766,15825251898,15957965251,18254883826,18389926797,18508770313,18857064494,13567116070,15108743472,15152351356,15270011075,15707902396,15757469821,17853539812,18713587780,18788404091,13512205389,13526907235,13529773877,13726286198,15118706926,15602112785,17754902830,18068121668,18392300902,18630630387,18757124703,18896651349,18906290128,13051998066,13666787393,15257436872,15649589867,18167109189,18468044590,18589344441,18838151023,17331490022,17538252161,17645107429,18072928951,18080589926,13972606479,15065141018,15087540752,15148920708,15172473909,17852025208,18589337007,18647321555,13103610393,13207449797,13211056420,13262083065,15113338759,15393718831,15860832598,17695921859,17853260882,17854253039,18088243364,18159963899,18239546563,18263897738,18387119786,18974908597,13516101325,15114589591,15961280199,17370079016,17853503506,18126991080,18766383637,18862970907,13260951299,15647489916,17390956796,13170805002,13633715995,13851970058,15057560989,15312882121,15607251838,15727433952,15764258125,15776481387,17352625683,18039675999,18143462429,18164634005,18856278696,13030041530,13123721666,13205623710,13653296223,13996012834,15131297127,15617857305,15750662177,15815853666,15968527385,15980156995,17631549161,17764218574,18170598001,18875138579,15252511601,13305630884,13359695700,13578667097,13638625603,15073989911,15079917766,15223391056,15238263186,15239697699,15241868688,15359661858,17309719077,17377975303,18288486426,18338756501,18531063936,18875055867,13588703539,13604527696,13623678754,13750253089,13821531719,15037619785,15047420598,15137114273,15157922829,15513999517,15586186031,15676730929,15895918081,15959618786,17857032933,18233457390,18314293668,18637607120,18698537707,18873299522,13556244327,13191191422,13733922009,15031510068,15191550121,15236738961,15251871722,15344130601,15374597692,15610886968,15649858797,17692624800,17798536626,17839987241,18580461617,18630187056,18791620366,19862725677,13337349924,13393631839,13504651207,13769032519,13936278720,15218856490,15989453719,17743032080,17786665090,18088927440,18403412771,18468273725,18706749375,18766437852,18987533320,19939948031,13512166395,13780202201,15284489889,15605870986,15834177573,17317810941,17692124827,18164157139,18296402447,18795911675,18845576361,18862013562,18870734832,18913103105,13411363714,13632270812,13968969606,15150510702,15210822961,18229698813,18247006176,18313749242,18434753919,18567465908,18823995989,13183015886,13676025189,15042138161,15144912341,15219141431,15254102563,15521609639,15524431182,15559736516,15641671891,15661364945,15698995798,18213104832,18588400581,18896539877,13129186604,13273195356,13409174787,15776371952,15816608914,15819696991,13072665608,13513715819,15038362431,15093237895,15137370862,15238135777,15239506979,15937125507,16603845069,17865352311,18338768081,18839145009,13435889846,13630704904,13758379807,13810936708,13836229891,14787030310,15013865970,15021185527,15217229416,15833002668,15879436506,17373537208,17634866600,17853503272,18064828867,18108881798,18320586838,18332978102,18800201052,18869742250,13229444604,13624178101,13770335099,15080857710,15116605096,15898809930,15951865321,17631732779,17720162357,18186127747,18569558376,18625777856,18663107522,18851593006,13994594999,13327382750,15087030357,15111131447,15344892731,15398759130,17795032589,18088921525,18574632546,18873301068,13436502738,13588091965,15801266836,17887183367,18339522521,13051322920,15394223631,15915531169,18028082588,13750561497,13226677673,13301203437,13556645365,13750007466,15218199610,18506611545,18643839447,18162046785,15617437801,18804316186,13708996286,15738717187,18816213988,17315389097,18168029510,13161081295,13570340715,13430704218,18316473196,13751636738,15006358365,15123101139,15195976939,15987197700,17327190782,17854253265,18143065232,18305176757,18816128502,18906160353,15005156908,15056975531,15852715360,18852734015,13726217715,15622908694,13163755755,15161101882,15251975303,17755507529,18388694822,19801265162,15064152720,15168249295,15195810808,18824433215,13229282993,13229443593,15988280793,18443983578,18801505233,18852894477,13049145118,15975525879,15907633866,13166666089,15840333666,15869510900,15896651680,15969843361,17801166525,17824233576,17853316339,18636242496,19907693079,17865921503,18811122196,18817661195,13566358387,17855833407,13049189507,13065059838,13439682213,13654582590,13760390510,13857823523,13988411587,15057402451,15102357052,15194166170,15528250960,15605128816,15826423311,15858172409,18014718075,18705722458,18806959501,18857525516,13966399701,15558958850,18857514956,13626803142,13757571895,15042624417,15620119516,15864265959,15895957950,17771167872,17875304257,18811139629,13588821609,15696267750,18318338040,13680575896,13853810979,15868091628,13168326040,13288237210,13466190192,13486098935,13512017146,13703036499,13736480451,13780101189,13798957381,13868923599,13958028089,15067269115,15986917671,17858379551,17858984538,18023850975,18026716525,18168036907,18319295111,18355428708,18468112064,18875218952,13378733023,13454730191,13681042778,13821306987,13836059661,15259146609,15754503070,18905122369,19851915250,17351028730,18179377903,13967540613,15604612925,13532115831,13628685525,13987787729,15735402995,13206646695,13529330681,13648780843,13814686320,13888919351,13988344698,15012337633,15106122025,15151831910,15152250799,15152364169,15170057498,15360593605,15370334791,15987413550,15987590566,17090110471,17352916405,17354312841,17662616098,17743910523,17855304966,18049603051,18186851587,18587376695,18603622336,18758222842,18788361439,18862721483,18873060355,18907290803,15201825197,13003782613,13037111185,13163299966,13256192995,13349968542,13710394485,14787596589,15064152851,15758536949,15797734676,15858886815,17327758596,18112131068,18896204101,19187191026,13189776076,13577824699,13861330261,13931860588,13958072955,15860767229,15862951776,15869565049,18208859378,18372651265,18995690307,13031058835,13378681542,13835436461,15019156043,15911587462,15926955419,15943806087,17390088668,18162411826,18213425744,18288235308,18329952772,18565500409,18800579867,18806957836,18986305899,13113738178,13143106882,13267956295,13420057514,13728097181,13797889588,13828739768,15288062511,15631988294,15976237066,18008863776,18016899945,18107131995,18162434003,18164683893,18165367153,18258156091,18669099184,18725129354,18758965752,18852517420,18883145826,19987482017,13338840493,15007149951,15198847792,15357682888,15602763261,15659988041,15911897327,17384768029,17788831799,17851112235,17862810186,18136176996,18313821209,18725516549,18846165716,13064295008,13433625389,13616235949,13714174026,13839048070,13860239995,13928251152,13957656279,15013045595,15087894762,15187827307,15267137263,15295737733,15297864038,15331569905,15800041585,15854073666,15912849677,15950326031,15972928291,17361734916,18297997280,18805841811,18831081096,18883309238,18926301336,18988380038,13078429414,13202067818,13211957551,15038985666,15072938181,";

    /**
     * 用户登录
     *
     * @param account  账户
     * @param password 密码，注意此处password为明文密码
     * @param catgory  科目
     * @param terminal 终端类型
     * @return
     */
    public UserSession login(String account, String password, int terminal, int catgory, String deviceToken)
            throws BizException {
        BiPredicate<UserDto, String> nativeCheck = ((user, pwd) ->
                StringUtils.isNotBlank(user.getNativepwd())
                        && user.getNativepwd().equals(pwd));
        //正常登陆逻辑改阶段不会返回session
        BiFunction<UserDto, Integer, UserSession> getUserSession = ((userDto, i) -> null);
        return login(account, password, terminal, catgory, deviceToken, nativeCheck, getUserSession);

    }

    public UserSession loginVirtual(String mobile, int terminal) {
        //尝试获取userssion
        BiFunction<UserDto, Integer, UserSession> getUserSession = ((userDto, i) -> loginWithoutPassWd(userDto, i));
        BiPredicate<UserDto, String> nativeCheck = ((user, pwd) -> true);
        try {
            return login(mobile, DEFAULT_INT_PASSWORD, terminal, -1, "", nativeCheck, getUserSession);
        } catch (BizException e) {
            logger.info("loginVirtual is error,mobile={},message={}", mobile, e.getMessage());
        }
        return null;
    }

    /**
     * @param account
     * @param password
     * @param terminal
     * @param catgory
     * @param deviceToken
     * @param nativeCheck    本地密码校验逻辑
     * @param getUserSession 直接根据本地用户数据，获取session值
     * @return
     * @throws BizException
     */
    private UserSession login(String account, String password, int terminal, int catgory, String deviceToken, BiPredicate<UserDto, String> nativeCheck, BiFunction<UserDto, Integer, UserSession> getUserSession) throws BizException {
        boolean isPwdCorrect = false;
        // 根据手机号，用户名，邮箱查询
        UserDto userDto = userDao.findAny(account);
        logger.info("findAny userDto account ：{} password:{}", account, password);

        UserSession apply = getUserSession.apply(userDto, terminal);
        if (null != apply) {
            return apply;
        }
        if (null != userDto && nativeCheck.test(userDto, password)) {// ztk登录优先
            // 更新用户最后登录时间,如果用户头像为默认头像并更新头像
            if (StringUtils.isNotEmpty(userDto.getAvatar()) && userDto.getAvatar().equals(DEFAULT_AVATAR)) {
                userDao.updateLastLoginTimeAndAvatar(userDto.getId(), NEW_AVATAR);
                userDto.setAvatar(NEW_AVATAR);
            } else {
                userDao.updateLastLoginTime(userDto.getId());
            }
            isPwdCorrect = true;
            // 增加获取ucenterid
            if (userDto.getUcenterId() == 0) {
                logger.info(" account :{} old ucenterId :{}", account, userDto.getUcenterId());
                // 需要去ucenter获取id
                UcenterMember ucmember = null;
                final UcenterBind ucenterBind = ucenterService.findAnyBind(account);
                if (ucenterBind != null) {
                    ucmember = ucenterService.findMemberByUsername(ucenterBind.getUsername());
                } else {
                    ucmember = ucenterService.findMemberByUsername(account);
                }
                if (ucmember != null) {
                    userDto.setUcenterId(ucmember.getUid());
                    // 更新表中ucenterid
                    userDao.updateBB105(userDto.getId(), userDto.getUcenterId());
                }
                logger.info(" account :{} new ucenterId :{}", account, userDto.getUcenterId());
            }
        } else {
            userDto = ucenterLogin(account, password, userDto, terminal, catgory, deviceToken);
            isPwdCorrect = true;
        }
        if (!isPwdCorrect || userDto == null) {
            throw new BizException(UserErrors.LOGIN_FAIL);
        }
        logger.info("用户:{}使用密码非首次登录", account);
        //透传注册时间
        SensorsUtils.setMessage("createTime", userDto.getCreateTime());
        return getLoginSession(userDto, catgory, terminal);
    }

    /**
     * ucenter 登录
     *
     * @param account
     * @param password
     * @param userDto
     * @return
     */
    private UserDto ucenterLogin(String account, String password, UserDto userDto, int terminal, int catgory,
                                 String deviceToken) throws BizException {
        boolean isPwdCorrect = false;
        // 此处username (acount) 可能是用户名，也可能是手机号，邮箱
        String username = account;

        UcenterMember ucmember = null;
        final UcenterBind ucenterBind = ucenterService.findAnyBind(account);
        if (ucenterBind != null) {
            username = ucenterBind.getUsername();
            ucmember = ucenterService.findMemberByUsername(username);
        } else {
            ucmember = ucenterService.findMemberByUsername(username);
        }

        logger.info("ucenter ucmember = {}", JsonUtil.toJson(ucmember));

        if (ucmember == null && userDto == null) {
            throw new BizException(UserErrors.USER_NOT_EXIST);
        }

        if (ucmember != null) {
            isPwdCorrect = ucenterService.checkUcenterPassword(ucmember, password);
            if (((isPwdCorrect && userDto == null))
                    // 密码不正确，但是密码是特殊格式（）
                    || (!isPwdCorrect && userDto == null && password.equals(DEFAULT_INT_PASSWORD))) {

                // 如果ucenter密码正确，但本地数据库不存在用户，则新建一个账号,使用ucenter用户的密码作为本地数据库的密码
                logger.info("ucenter exists user, but local not exists user , ucmember={}", JsonUtil.toJson(ucmember));

                String phone = "";
                if (account.matches(RegexConfig.MOBILE_PHONE_REGEX)) {
                    phone = account;
                } else if (ucenterBind != null) {
                    phone = StringUtils.trimToEmpty(ucenterBind.getPhone());
                }

                String email = "";
                if (account.matches(RegexConfig.EMIAL_REGEX)) {
                    email = account;
                } else if (ucenterBind != null) {
                    email = StringUtils.trimToEmpty(ucenterBind.getEmail());
                }

                if (StringUtils.isEmpty(email)) {
                    email = StringUtils.trimToEmpty(ucmember.getEmail());
                }
                /**
                 * update by lijun 此处是已经确定进行中心注册
                 */
                return createZtkUser(phone, ucmember.getUid(), ucmember.getUsername(), password, email, UserStatus.INIT,
                        CENTER_REG_FROM, "", terminal, deviceToken);
            } else if (isPwdCorrect && userDto != null) {
                // 不管输入的密码是否与本地密码匹配，都更新本地密码
//                userDao.resetpwd(userDto.getId(), password);
//                //更新最后登录
//                userDao.updateLastLoginTime(userDto.getId());
                userDao.resetPasswordAndLastLoginTime(userDto.getId(), password);
                return userDto;
            }

        }

        return null;
    }

    public UserSession loginWithoutPassWd(UserDto userDto, int terminal) {
        if (null == userDto) {
            return null;
        }
        UserConfig lastConfig = userConfigService.getLastConfig(userDto.getId());
        // 没有配置
        int catgory = lastConfig != null ? lastConfig.getCategory() : 1;
        return getLoginSession(userDto, catgory, terminal);
    }

    /**
     * 将原来的token设置为旧token标示,以方便进行消息提示
     *
     * @param uid 用户id
     */
    public void changeOldToken(long uid, int terminal) {
        // 保证唯一
        String token = userSessionService.getTokenById(uid, terminal);
        logger.info(" changeOldToken = uid-{},terminal - {}", uid, terminal);
        if (StringUtils.isBlank(token)) {// token不存在不需要处理
            return;
        }

        final HashOperations hashOperations = sessionRedisTemplate.opsForHash();
        if (sessionRedisTemplate.hasKey(token)) {// token 存在则设置
            // 设置旧token标示 1:标示有新的token生成,说明有新设备登录(不过同一设备连续登录无法判断)
            hashOperations.put(token, UserRedisSessionKeys.oldToken, "1");
            // 新设备登录时间
            hashOperations.put(token, UserRedisSessionKeys.newDiveceLoginTime, System.currentTimeMillis() + "");
        }
    }

    /**
     * 保存session到redis
     *
     * @param userDto
     * @param catgory 科目
     * @return
     */
    public UserSession createSession(UserDto userDto, int catgory, int terminal) {
        UserSession userSession = getUserSession(userDto, catgory, terminal);

        UserConfig config = userConfigService.findByUidAndCatgory(userDto.getId(), catgory);
        if (config == null) {
            config = userConfigService.getDefaultUserConfig(catgory);
            config.setId(userConfigService.getConfigId(userDto.getId(), catgory));
            config.setUid(userDto.getId());

            if (catgory > 0) {
                userConfigService.save(config);
            }

            String areaName = StringUtils.trimToEmpty(AreaConstants.getFullAreaNmae(config.getArea()));
            userSession.setAreaName(areaName);
            userSession.setSubjectName(subjectService.getSubjectName(config.getSubject()));
        }

        final String token = generateToken();
        userSession.setToken(token);
        // 设置过期时间 session失效时间比redis删除key的时间早一分钟
        userSession.setExpireTime(System.currentTimeMillis() + (USER_TOKEN_TIME_OUT * 1000) - 60000);
        userSession.setSubject(config.getSubject());
        userSession.setQcount(config.getQcount());// 抽题个数
        userSession.setErrorQcount(config.getErrorQcount());
        userSession.setArea(config.getArea());
        userSession.setCatgory(catgory);
        userSession.setRegFrom(userDto.getRegFrom());
        saveSessionToRedis(userSession, catgory, terminal);
        return userSession;
    }

    /**
     * 保持session到redis里面
     *
     * @param userSession
     */
    private void saveSessionToRedis(UserSession userSession, int catgory, int terminal) {
        List<String> allTokenKeyByUserId = UserSessionService.getAllTokenKeyByUserId(userSession.getId());
        String tokenKeyByUserId = UserSessionService.getTokenKeyByUserId(userSession.getId(), terminal);

        final UserConfig config = userConfigService.findByUidAndCatgory(userSession.getId(), catgory);
        allTokenKeyByUserId.forEach(userTokenKey -> {
            Map<String, String> sessionInfo = new HashMap();
            //****同步基础信息开始*****//
            sessionInfo.put(UserRedisSessionKeys.email, StringUtils.trimToEmpty(userSession.getEmail()));
            // 用户信息id
            sessionInfo.put(UserRedisSessionKeys.id, userSession.getId() + "");
            // 设置登录时间
            sessionInfo.put(UserRedisSessionKeys.expireTime, userSession.getExpireTime() + "");
            sessionInfo.put(UserRedisSessionKeys.mobile, StringUtils.trimToEmpty(userSession.getMobile()));
            sessionInfo.put(UserRedisSessionKeys.nick, StringUtils.trimToEmpty(userSession.getNick()));
            sessionInfo.put(UserRedisSessionKeys.uname, userSession.getUname());
            String ucId = SensorsUtils.defaultUcId;
            if (!StringUtils.isEmpty(userSession.getMobile())) {
                ucId = userSession.getMobile();
            }
            sessionInfo.put(UserRedisSessionKeys.ucId, ucId);
            //****同步基础信息结束*****//

            if (config != null) {
                // 设置知识点类目
                if (tokenKeyByUserId.equals(userTokenKey)) {
                    sessionInfo.put(UserRedisSessionKeys.catgory, config.getCategory() + "");
                    sessionInfo.put(UserRedisSessionKeys.subject, config.getSubject() + "");
                    sessionInfo.put(UserRedisSessionKeys.area, config.getArea() + "");
                }
                sessionInfo.put(UserRedisSessionKeys.qcount, config.getQcount() + "");
                sessionInfo.put(UserRedisSessionKeys.errorQcount, config.getErrorQcount() + "");
            }
            final HashOperations<String, Object, Object> hashOperations = sessionRedisTemplate.opsForHash();
            try {
                hashOperations.putAll(userSession.getToken(), sessionInfo);
            } catch (Exception e) {
                logger.error("ex,sessionInfo={}", JsonUtil.toJson(sessionInfo), e);
            }
            sessionRedisTemplate.expire(userSession.getToken(), USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
            if (tokenKeyByUserId.equals(userTokenKey)) {
                sessionRedisTemplate.opsForValue().set(userTokenKey, userSession.getToken(), USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
            }
        });

    }

    /**
     * 修改用户session，保存到redis
     *
     * @param config,token
     */
    public void updateUserConfigSession(UserConfig config, int terminal) throws BizException {
        List<String> allTokenKeyByUserId = UserSessionService.getAllTokenKeyByUserId(config.getUid());
        String tokenKeyByUserId = UserSessionService.getTokenKeyByUserId(config.getUid(), terminal);
        final HashOperations<String, Object, Object> hashOperations = sessionRedisTemplate.opsForHash();
        allTokenKeyByUserId.forEach(userTokenKey -> {
            Map<String, String> sessionInfo = new HashMap();
            if (tokenKeyByUserId.equals(userTokenKey)) {
                sessionInfo.put(UserRedisSessionKeys.subject, config.getSubject() + "");
                sessionInfo.put(UserRedisSessionKeys.catgory, config.getCategory() + "");
                sessionInfo.put(UserRedisSessionKeys.area, config.getArea() + "");
            }
            sessionInfo.put(UserRedisSessionKeys.qcount, config.getQcount() + "");
            sessionInfo.put(UserRedisSessionKeys.errorQcount, config.getErrorQcount() + "");
            final String token = sessionRedisTemplate.opsForValue().get(userTokenKey);
            if (StringUtils.isNotBlank(token)) {
                try {
                    hashOperations.putAll(token, sessionInfo);
                } catch (Exception e) {
                    logger.error("ex,sessionInfo={}", JsonUtil.toJson(sessionInfo), e);
                }
                sessionRedisTemplate.expire(token, USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
                if (tokenKeyByUserId.equals(userTokenKey)) {
                    sessionRedisTemplate.opsForValue().set(userTokenKey, token, USER_TOKEN_TIME_OUT, TimeUnit.SECONDS);
                }
            }
        });
    }

    private UserSession getUserSession(UserDto userDto, int catgory, int terminal) {
        if (catgory < 0) {
            catgory = 1;
        }
        UserSession userSession = new UserSession();
        userSession.setEmail(userDto.getEmail());
        userSession.setId(userDto.getId());
        userSession.setMobile(StringUtils.trimToEmpty(userDto.getMobile()));
        String nick = StringUtils.trimToEmpty(userDto.getNick());
        userSession.setNick(nick);
        userSession.setUname(userDto.getName());
        userSession.setStatus(userDto.getStatus());// 用户状态
        String ucId = SensorsUtils.defaultUcId;
        if (!StringUtils.isEmpty(userDto.getMobile())) {
            ucId = userDto.getMobile();
        }
        userSession.setUcId(ucId);
        userSession.setAvatar(StringUtils.trimToEmpty(userDto.getAvatar()));

        final String token = userSessionService.getTokenById(userDto.getId(), terminal);
        if (StringUtils.isNoneBlank(token)) {
            userSession.setExpireTime(userSessionService.getExpireTime(token));
            userSession.setToken(token);
        }
        final UserConfig userConfig = userConfigService.findByUidAndCatgory(userDto.getId(), catgory);
        if (userConfig != null) {
            userSession.setSubject(userConfig.getSubject());
            userSession.setQcount(userConfig.getQcount());// 抽题个数
            userSession.setArea(userConfig.getArea());
            String areaName = StringUtils.trimToEmpty(AreaConstants.getFullAreaNmae(userConfig.getArea()));
            userSession.setAreaName(areaName);// 设置所属区域名字
            userSession.setSubjectName(subjectService.getSubjectName(userConfig.getSubject()));
            userSession.setErrorQcount(userConfig.getErrorQcount());
        }
        userSession.setCatgory(catgory);
        return userSession;
    }

    /**
     * 生成token
     *
     * @return
     */
    private String generateToken() {
        final String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }

    /**
     * 发送短信
     *
     * @param mobile   手机号
     * @param clientIp
     * @throws BizException
     */
    public void sendCaptcha(String mobile, String clientIp, boolean isZtk) throws BizException {
        mobile = StringUtils.trimToEmpty(mobile);

        final SetOperations operations = redisTemplate.opsForSet();

        /**
         * 验证请求合法性
         */
        if (operations.isMember(UserRedisKeys.REJECT_MOBILES, mobile)) {
            logger.info("it is robot,reject mobile={}", mobile);
            return;
        }

        if (StringUtils.isNoneBlank(clientIp) && operations.isMember(UserRedisKeys.REJECT_IPS, clientIp)) {
            logger.info("it is robot,reject clientIp={}", clientIp);
            return;
        }

        final boolean isMobile = Pattern.matches(RegexConfig.MOBILE_PHONE_REGEX, mobile);
        if (!isMobile) {// 非法的手机号
            throw new BizException(UserErrors.ILLEGAL_MOBILE);
        }

        String captchaKey = String.format(UserRedisKeys.CAPTCHA_MOBILE, mobile);
        String captchaMarkKey = String.format(UserRedisKeys.USER_CAPTCHA_MARK, mobile);

        final ValueOperations valueOperations = redisTemplate.opsForValue();
        final Object markObj = redisTemplate.opsForValue().get(captchaMarkKey);

        // markObj存在，说明距离上次发送验证码的时间未超过1分钟
        if (markObj != null) {
            throw new BizException(UserErrors.CAPTCHA_SEND_TOO_FREQUENT);
        }

        // 随机生成验证码
        String captcha = RandomStringUtils.randomNumeric(6);
        while (true) {
            if (!captcha.startsWith("0")) {// 验证码不能以0开头
                break;
            }
            captcha = RandomStringUtils.randomNumeric(6);
        }

        // 将验证码设置到redis里面,有效期设置为3分钟
        valueOperations.set(captchaKey, captcha, 3, TimeUnit.MINUTES);
        valueOperations.set(captchaMarkKey, mobile, 1, TimeUnit.MINUTES);
        // 发送验证码
        logger.info("userService sendCaptcha captcha:{}", captcha);
        if (isZtk) {
            SmsUtil.sendCaptcha(mobile, captcha);
        } else {
            MDSmsUtil.sendCaptcha(mobile, captcha);
        }
        UserGetCaptchaContext.get().setCaptcha(captcha);
    }

    /**
     * 新建一个本地用户
     *
     * @param mobile
     * @param ucenterId
     * @param username
     * @param password
     * @return
     */
    public UserDto createZtkUser(String mobile, long ucenterId, String username, String password, String email,
                                 int status, String regFrom, String regIp, int terminal, String deviceToken) {
        UserDto userDto = null;
        long millis = System.currentTimeMillis();
        // 先检查用户是否存在
        if (StringUtils.isNoneBlank(mobile)) {
            userDto = userDao.findAny(mobile);
            if (userDto != null) {
                return userDto;
            }
        }
        long l1 = System.currentTimeMillis();
        //logger.info("8-1.createZtkUser findAny - {},,account = {},time = {}",l1 - millis,mobile,System.currentTimeMillis());
        if (StringUtils.isNoneBlank(username)) {
            userDto = userDao.findAny(username);
            if (userDto != null) {
                if (StringUtils.isNoneBlank(mobile) && StringUtils.isBlank(userDto.getMobile())) {
                    userDao.updateMobile(username, mobile);
                }
                return userDto;
            }
        }
        long l2 = System.currentTimeMillis();
        //logger.info("8-2.createZtkUser updateMobile - {},account = {},time = {}",l2  - l1,mobile,System.currentTimeMillis());

        boolean isMobile = StringUtils.trimToEmpty(mobile).matches(RegexConfig.MOBILE_PHONE_REGEX);

        // 默认昵称
        String nick = username;

        // 当用户使用邮箱或用户名登陆，而本地不存在用户的时候，mobile可能为空
        if (isMobile) {
            nick = new StringBuilder().append(mobile.substring(0, 3)).append("****").append(mobile.substring(7, 11))
                    .toString();
        }

        userDto = UserDto.builder().password(DigestUtils.sha1Hex("")).nativepwd(password)// 记录原始密码,为后边的密码加密同一做准备
                .createTime(System.currentTimeMillis()).nick(nick).area(DEFAULT_AREA)// 设置默认区域 全国
                .mobile(StringUtils.trimToNull(mobile)) // 插入数据库时，如果手机号为空字符串，转换为null
                .email(StringUtils.trimToEmpty(email)).status(status).ucenterId(ucenterId).name(username) // username为ztk_app+数字的形式
                .regFrom(regFrom).deviceToken(deviceToken).build();
        // 如果不是php来的用户，会通知php进行创建 3 是pc 6 是微信 TerminalType.WEI_XIN 移除!CENTER_REG_FROM_M.equals(regFrom)
        if (!REGISTER_USER_PHP.equals(regFrom) && !"3".equals(regFrom)
                && !"6".equals(regFrom)) {
            thirdTaskComponent.syncUserData(email, mobile, username);
        }
        // 插入新用户
        long l31 = System.currentTimeMillis();
        userDao.insert(userDto);
        long l4 = System.currentTimeMillis();
        //logger.info("8-4.createZtkUser userDao.insert - {},account = {},time = {}",l4 - l31,mobile,System.currentTimeMillis());

        // username唯一，根据username重新查询
        UserDto userDtoNew = userDao.findByName(username);

        //logger.info("create new user : {}", JsonUtil.toJson(userDtoNew));

        // 发送加积分消息
        userRewardService.sendRegisterMessage(userDtoNew.getId(), userDtoNew.getName());
        // 发送用户注册消息
        sendRegister.send(userDto, regIp, terminal);
        //用户注册送课 改为批量送课
        // thirdTaskComponent.createUserRegisterOrder(userDto.getName());
        return userDtoNew;
    }


    /**
     * 创建uc 用户
     *
     * @param mobile
     * @param regip
     * @param appId
     * @param isEncypt 密码是否已加密
     * @return
     */
    public UcenterMember createUCUser(String mobile, String password, String regip, int appId, boolean isEncypt, Supplier<String> suppler) {
        // ucenter,使用手机号注册，username为空，生成username
        String username = suppler.get();

        // 防止重合
        while (true) {
            if (ucenterService.findMemberByUsername(username) == null) {
                break;
            }
            username = suppler.get();
        }

        // 如果为空，生成6位随机数字密码
        password = StringUtils.isBlank(password) ? UcenterUtils.salt_get(6) : password;

        // ucenter保存用户信息
        UcenterMember ucenterMember = ucenterService.saveMember(username, password, regip, appId, isEncypt);

        // ucenter绑定用户名和手机号，不绑定email
        ucenterService.ucBind(ucenterMember.getUid(), username, mobile, "");
        return ucenterMember;
    }

    /**
     * 使用验证码,使用后删除,验证码不对则抛出异常
     *
     * @param mobile  手机号
     * @param captcha 用户输入的验证码
     * @throws BizException
     */
    public void userCaptcha(String mobile, String captcha) throws BizException {

        // 验证码对应的key
        Object actualCaptcha = validateCapcha(mobile, captcha);
        if (actualCaptcha == null) {
            throw new BizException(UserErrors.CAPTCHA_EXPIRE);
        }

        // 验证码错误
        if (!captcha.equals(actualCaptcha.toString())) {
            throw new BizException(UserErrors.CAPTCHA_ERROR);
        }

    }

    /**
     * 校验验证码
     *
     * @param mobile  手机号
     * @param captcha 校验码
     * @return
     */
    public Object validateCapcha(String mobile, String captcha) {
        // 验证码对应的key
        String captchaKey = String.format(UserRedisKeys.CAPTCHA_MOBILE, mobile);
        // 实际验证码
        final Object actualCaptcha = redisTemplate.opsForValue().get(captchaKey);
        {
            UserGetCaptchaContext.get().setEventName(CHECK_CAPTCHA);
            UserGetCaptchaContext.get().setPhone(mobile);
            UserGetCaptchaContext.get().setCaptcha(captcha);
        }
        sendEvent.send(UserGetCaptchaContext.get());
        return actualCaptcha;
    }

    /**
     * 退出登录操作
     *
     * @param token
     */
    public void logout(String token, int terminal) throws BizException {
        try {
            userSessionService.assertSession(token);
        } catch (BizException e) {
            logger.error("logout error:{}", e.getMessage());
            return;
        }
        // 保证唯一
        final long uid = userSessionService.getUid(token);
        if (uid < 0) {// uid 不存在说明session已经过期
            return;
        }

        logger.info("uid={} logout,token={}", uid, token);
        String userTokenKey = UserSessionService.getTokenKeyByUserId(uid, terminal);

        /**
         * 设置过期时间,此处把过期时间设置为当前之前的时间, 来达到token过期的目的
         * 这里不采用直接把key删除的方式来过期,目的是方式还有程序通过token访问 session数据,如果直接删除,就会报错
         */
        sessionRedisTemplate.opsForHash().put(token, UserRedisSessionKeys.expireTime,
                String.valueOf(System.currentTimeMillis() - 10000));
        // 重新设置过期时间
        sessionRedisTemplate.expire(token, 100, TimeUnit.SECONDS);
        sessionRedisTemplate.opsForValue().set(userTokenKey, token);
        // 重新设置过期时间
        sessionRedisTemplate.expire(userTokenKey, 100, TimeUnit.SECONDS);

    }

    /**
     * 重置密码
     *
     * @param mobile
     * @param captcha  验证码
     * @param password
     * @throws BizException
     */
    public void resetpwd(String mobile, String captcha, String password) throws BizException {
        if (mobile == null || password == null || captcha == null) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        checkPwd(password);

        userCaptcha(mobile, captcha);

        final UserDto userDto = userDao.findByMobile(mobile);

        UcenterBind ucenterBind = ucenterService.findBind(mobile);

        UcenterMember ucenterMember = null;
        if (ucenterBind != null) {
            ucenterMember = ucenterService.findMemberByUsername(ucenterBind.getUsername());
        }

        if (userDto == null && ucenterMember == null) {// 账户不存在
            throw new BizException(UserErrors.USER_NOT_EXIST);
        }

        // 重置ucenter密码
        if (ucenterMember != null) {
            ucenterService.resetPwd(ucenterMember, password);

        }

        // 重置密码
        if (userDto != null) {
            userDao.resetpwd(userDto.getId(), password);
        }
    }

    public UserDto findById(long userId) {
        return userDao.findById(userId);
    }

    /**
     * 完善个人信息
     *
     * @param uid      用户id
     * @param password 密码
     * @param nick     昵称
     * @throws BizException
     */
    public UserSession complete(long uid, String password, String nick, int catgory, int terminal) throws BizException {
        if (password == null || nick == null) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        checkPwd(password);
        long t1 = System.currentTimeMillis();
        UserDto userDto = userDao.findById(uid);
        logger.info("findBannerList UserDto expendTime={}", System.currentTimeMillis() - t1);

        password = StringUtils.trimToNull(password);
        // 传入为null则使用旧昵称
        nick = Optional.ofNullable(StringUtils.trimToNull(nick)).orElse(userDto.getNick());
        userDto.setNick(nick);
        if (!nick.matches(RegexConfig.NICK_NAME_REGEX)) {
            throw new BizException(UserErrors.NICKNAME_PATTERN_ERROR);
        }

        if (isSensitive(nick)) {
            throw new BizException(UserErrors.NICKNAME_SENSITIVE);
        }
        long t2 = System.currentTimeMillis();
        userDao.updateCommon(uid, password, nick, UserStatus.AVAILABLE);
        logger.info("updateCommon expendTime={}", System.currentTimeMillis() - t2);
        // 更新ucenter密码
        ucenterService.setPwd(userDto, userDto.getMobile(), password);

        long t6 = System.currentTimeMillis();
        final UserSession userSession = getUserSession(userDto, catgory, terminal);
        saveSessionToRedis(userSession, catgory, terminal);
        logger.info("complete createSession expendTime={}", System.currentTimeMillis() - t6);
        return userSession;
    }

    /**
     * 更新用户考试科目信息
     *
     * @param userId
     * @param area
     * @param catgory
     */
    public UserSession updateSubject(long userId, int area, int catgory, int terminal) {
        UserConfig config = userConfigService.findByUidAndCatgory(userId, catgory);
        if (config == null || config.getCategory() != catgory) {
            config = userConfigService.getDefaultUserConfig(catgory);
            config.setId(userConfigService.getConfigId(userId, catgory));
            config.setUid(userId);
        }
        config.setArea(area);
        userConfigService.save(config);
        UserDto userDto = userDao.findById(userId);// 重新查询用户信息
        // 更新用户session信息
        final UserSession userSession = getUserSession(userDto, catgory, terminal);
        saveSessionToRedis(userSession, catgory, terminal);
        return userSession;
    }

    /**
     * 通过验证码登录
     *
     * @param mobile   注册手机号
     * @param captcha  验证码
     * @param regip    注册ip
     * @param terminal 终端类型
     * @param catgory  科目
     * @return
     * @throws BizException
     */
    public UserSession loginFromCaptcha(String mobile, String captcha, String regip, int terminal, int catgory,
                                        String regFrom, String deviceToken) throws BizException {
        // 使用验证码
        long t1 = System.currentTimeMillis();
        userCaptcha(mobile, captcha);
        logger.info("1.userCaptcha expendTime={},account = {},time = {}", System.currentTimeMillis() - t1, mobile, System.currentTimeMillis());

        long t9 = System.currentTimeMillis();
        UserDto userDto = userDao.find(mobile);
        logger.info("2.mobile findBannerList userDto expendTime={},account = {},time = {}", System.currentTimeMillis() - t9, mobile, System.currentTimeMillis());
        Integer firstLogin = registerFreeCourseDetailConfig.getTestFirstLogin();
        //是否需要上报注册位置 教育同步也需要上报
        boolean registerForPosition = false;
        // 本地查询不到
        if (userDto == null) {
            registerForPosition = true;
            // 首次登录标识
            logger.info("账号:{mobile} 使用验证码首次登录,terminel为:{}", mobile, terminal);
            SensorsUtils.setMessage("loginFirst", true);
            long t8 = System.currentTimeMillis();
            // 根据手机号查询绑定表
            final UcenterBind ucenterBind = ucenterService.findBind(mobile);
            logger.info("3.findBind expendTime={},account = {},time = {}", System.currentTimeMillis() - t8, mobile, System.currentTimeMillis());

            UcenterMember ucmember = null;

            // 绑定表有记录，再根据绑定表的username查询ucmember
            if (ucenterBind != null) {
                long t2 = System.currentTimeMillis();
                ucmember = ucenterService.findMemberByUsername(ucenterBind.getUsername());
                logger.info("4.findMemberByUsername expendTime={},account = {},time = {}", System.currentTimeMillis() - t2, mobile, System.currentTimeMillis());
            }

            // 未查询到ucmember，考虑用户名是手机号的情况
            if (ucmember == null) {
                long t3 = System.currentTimeMillis();
                ucmember = ucenterService.findMemberByUsername(mobile);
                logger.info("5.findMemberByUsername and username is mobile expendTime={},account = {},time = {}",
                        System.currentTimeMillis() - t3, mobile, System.currentTimeMillis());
            }

            // ucenter存在，而本地不存在
            if (ucmember != null) {
                //logger.info("6.captcha pass and ucenter exists, but local not exist.");
                // 使用6位的临时密码
                String password = UcenterUtils.salt_get(6);
                long t7 = System.currentTimeMillis();
                /**
                 * update by lijun 此处确定是从中心获取数据,则直接使用默认值
                 */
                userDto = createZtkUser(mobile, ucmember.getUid(), ucmember.getUsername(), password,
                        ucmember.getEmail(), UserStatus.INIT, CENTER_REG_FROM, regip, terminal, deviceToken);
                //设置uc同步标示
                logger.info("用户:{mobile} 来自uc同步");
                SensorsUtils.setMessage("fromUc", true);
                logger.info("7.ucmember != null and createZtkUser expendTime={},account = {},time = {}", System.currentTimeMillis() - t7, mobile, System.currentTimeMillis());

            } else {
                // 两边都查询不到,自动注册账号
                firstLogin = 1;
                long t4 = System.currentTimeMillis();
                final UcenterMember ucUser = createUCUser(mobile, null, regip, UcenterConfig.UCENTER_MEMBERS_APPID, false);
                logger.info("8.createUCUser expendTime={},account = {},time = {}", System.currentTimeMillis() - t4, mobile, System.currentTimeMillis());
                long t5 = System.currentTimeMillis();
                userDto = createZtkUser(mobile, ucUser.getUid(), ucUser.getUsername(), ucUser.getPassword(),
                        ucUser.getEmail(), UserStatus.INIT, regFrom, regip, terminal, deviceToken);
                logger.info("9.createZtkUser expendTime={},account = {},time = {}", System.currentTimeMillis() - t5, mobile, System.currentTimeMillis());
            }
        }
        //透传注册时间
        SensorsUtils.setMessage("createTime", userDto.getCreateTime());


        // 将原来的token设置为旧token标示,以方便进行消息提示
        long t0 = System.currentTimeMillis();
        UserSession session = getLoginSession(userDto, catgory, terminal);
        logger.info("loginFromCaptcha changeOldToken expendTime={}", System.currentTimeMillis() - t0);

        if (firstLogin == 1) {
            long t7 = System.currentTimeMillis();
            Integer isOpenRegisterFreeCourse = registerFreeCourseDetailConfig.getOpenRegisterFreeCourse();
            logger.info("新注册用户送课状态为:{}", isOpenRegisterFreeCourse);
            if (isOpenRegisterFreeCourse == 1) {
                // 开启送课
                logger.info("新注册用户{}送课", userDto.getId());
                RegisterFreeCourseDetailVo detail = new RegisterFreeCourseDetailVo();
                detail.setRcoin(registerFreeCourseDetailConfig.getCoin());
                detail.setRgrowUpValue(registerFreeCourseDetailConfig.getGrowUpValue());
                detail.setRtitle(registerFreeCourseDetailConfig.getTitle());
                List<CourseInfo> courseList = JsonUtil.toList(registerFreeCourseDetailConfig.getCourseList(),
                        CourseInfo.class);
                detail.setRcourseList(courseList);
                session.setRegisterFreeCourseDetailVo(detail);
                //发送送课信息
                thirdTaskComponent.createUserRegisterOrderV2(userDto.getName());
                logger.info("注册送课 expendTime={}", System.currentTimeMillis() - t7);
            }
        }
        // 设置是否需要上报注册位置
        if (registerForPosition) {
            session.setFirstLogin(true);
        }
        return session;
    }

    /**
     * 登陆操作根据终端获取session对象
     *
     * @param userDto
     * @param category
     * @param terminal
     * @return
     */
    private UserSession getLoginSession(UserDto userDto, int category, final int terminal) {
        if (category < 0) { // 不指定考试类型
            UserConfig lastConfig = userConfigService.getLastConfig(userDto.getId());
            category = lastConfig != null ? lastConfig.getCategory() : -1;
        }
        BiFunction<UserDto, Integer, UserSession> newTokenLogin = ((user, categoryId) -> {        //生成新token的终端登陆方式
            changeOldToken(user.getId(), terminal);

            // 更新最后登录时间
            userDao.updateLastLoginTime(user.getId());
            UserSession session = createSession(user, categoryId, terminal);
            return session;
        });
        BiFunction<UserDto, Integer, UserSession> oldTokenLogin = ((user, categoryId) -> {        //优先使用之前生成的token的终端登陆方式
            String token = userSessionService.getTokenById(user.getId(), terminal);
            if (StringUtils.isBlank(token)) {
                //没有在线的token，创建新的token
                UserSession session = createSession(user, categoryId, terminal);
                saveSessionToRedis(session, categoryId, terminal);
                return session;
            } else {
                //使用已有的token
                UserSession userSession = getUserSession(user, categoryId, terminal);
                if (null == userSession || userSession.getExpireTime() < System.currentTimeMillis()) {
                    return createSession(user, categoryId, terminal);
                }
                return userSession;
            }
        });
        switch (terminal) {
            case TerminalType.ANDROID:
            case TerminalType.ANDROID_IPAD:
            case TerminalType.PC:
            case TerminalType.IPHONE:
            case TerminalType.IPHONE_IPAD:
                return newTokenLogin.apply(userDto, category);
            case TerminalType.MOBILE:
            case TerminalType.WEI_XIN:
            case TerminalType.WEI_XIN_APPLET:
            default:
                return oldTokenLogin.apply(userDto, category);
        }

    }

    /**
     * 用户修改密码
     *
     * @param userId 用户id
     * @param oldpwd 老密码
     * @param newpwd 新密码
     */
    public void modifypwd(long userId, String oldpwd, String newpwd) throws BizException {
        if (oldpwd == null || newpwd == null) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        // 只检查新密码
        checkPwd(newpwd);

        final UserDto userDto = userDao.findById(userId);
        if (!oldpwd.equals(userDto.getNativepwd())) {// 旧密码不对
            throw new BizException(UserErrors.OLD_PASSWORD_ERROR);
        }

        // 修改ucenter密码
        ucenterService.updateMemberPwd(userDto, oldpwd, newpwd);

        // 重置密码
        userDao.resetpwd(userDto.getId(), newpwd);
        // 修改完密码,需要重新登录
        // logout(token);
    }

    public void modifyPassword(UserDto userDto, String password) throws BizException {
        // 检查新密码
        checkPwd(password);
        // 修改ucenter密码
        UcenterMember ucenterMember = ucenterService.findMemberByUsername(userDto.getName());
        ucenterService.resetPwd(ucenterMember, password);
        // 修改本地密码
        userDao.resetpwd(userDto.getId(), password);
    }

    /**
     * 修改昵称
     *
     * @param userId   用户id
     * @param nickname 新昵称
     * @throws BizException
     */
    public UserSession modifyNickname(long userId, String nickname, int catgory, int terminal) throws BizException {
        nickname = StringUtils.trimToEmpty(nickname);

        if (!nickname.matches(RegexConfig.NICK_NAME_REGEX)) {
            throw new BizException(UserErrors.NICKNAME_PATTERN_ERROR);
        }

        if (isSensitive(nickname)) {
            throw new BizException(UserErrors.NICKNAME_SENSITIVE);
        }

        userDao.modifyNickname(userId, nickname);
        final UserDto userDto = userDao.findById(userId);
        final UserSession userSession = getUserSession(userDto, catgory, terminal);
        saveSessionToRedis(userSession, catgory, terminal);
        return userSession;
    }

    /**
     * 检查敏感词
     *
     * @param nick
     * @return
     */
    private boolean isSensitive(String nick) {
        nick = nick.replaceAll("[_0-9a-zA-Z]", "");
        List<String> words = SensitiveWords.initList.isEmpty() ? Arrays.asList(SensitiveWords.words)
                : SensitiveWords.initList;
        for (String word : words) {
            if (nick.indexOf(word) > -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * 密码长度检查
     *
     * @param newpwd
     * @throws BizException
     */
    private void checkPwd(String newpwd) throws BizException {
        if (newpwd.length() > PasswordConfig.MAX_LENGTH || newpwd.length() < PasswordConfig.MIN_LENGTH) {
            throw new BizException(UserErrors.PASSWORD_LENGTH_ERROR);
        }
    }

    /**
     * 启动后读取minganci.txt
     */
    @PostConstruct
    public void readMinganciTxt() {
        try {
            WebApplicationContext applicationContext = ContextLoader.getCurrentWebApplicationContext();
            if (applicationContext == null) {
                return;
            }

            ServletContext servletContext = applicationContext.getServletContext();
            String basePath = servletContext.getRealPath("");
            String filePath = basePath + "/minganci.txt";

            String ret = FileUtils.readFileToString(new File(filePath));
            String[] words = ret.split("\n");

            for (String word : words) {
                SensitiveWords.initList.add(word);
            }

            logger.info("init SensitiveWords size :{}", SensitiveWords.initList.size());
        } catch (Exception ex) {
            logger.error("read minganci.txt fail.", ex);
        }
    }

    /**
     * 判断是否ios审核版本
     *
     * @param catgory
     * @return
     */
    public boolean isIosAudit(int catgory, int terminal, String cv) {
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            String iosAuditSetKey = VersionRedisKey.getIosAuditSetKey(catgory);
            return redisTemplate.opsForSet().isMember(iosAuditSetKey, cv);
        }

        return false;
    }

    /**
     * 判断是否ios审核版本 - 课程服务等判断调用
     *
     * @return
     */
    public boolean isIosAudit(String cv) {
        String iosAuditSetKey = VersionRedisKey.getIosAuditSetKey();
        return redisTemplate.opsForSet().isMember(iosAuditSetKey, cv);
    }

    /**
     * 判断是否开启ios轮播广告图
     *
     * @return
     */
    public boolean isIosAuditAd(int catgory, int terminal, String cv) {
        // 不是内购版本，一定开启轮播图
        if (StringUtils.isBlank(cv) || !isIosAudit(catgory, terminal, cv)) {
            return true;
        } else {
            if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
                // 不存在则开启
                if (!redisTemplate.hasKey(VersionRedisKey.getIosAdKey())
                        || !cv.equals(redisTemplate.opsForValue().get(VersionRedisKey.getIosAdKey()))) {
                    return true;
                } else if (redisTemplate.hasKey(VersionRedisKey.IOS_AUDIT_AD_OPEN_KEY)) { // 是内购再加一个开关，是否开启轮播图
                    // 2017-12-20 09:40:16 zw
                    // 存在key则开启
                    return true;
                }
                return false;

            }
        }
        return true;
    }

    /**
     * 简单获得注册ip
     *
     * @param request
     * @return
     */
    public String getRegip(HttpServletRequest request) {
        String regip = "";
        ArrayList<String> headers = new ArrayList<>();
        headers.add("x-forwarded-for");
        headers.add("Proxy-Client-IP");
        headers.add("WL-Proxy-Client-IP");

        for (String header : headers) {
            regip = request.getHeader(header);
            if (StringUtils.isNoneBlank(regip) && !regip.equals("unknown")) {
                break;
            }
        }

        if (StringUtils.isBlank(regip)) {
            regip = StringUtils.trimToEmpty(request.getRemoteAddr());
        }

        // 有逗号说明不只是一个ip，如10.198.25.72, 120.197.126.145
        if (regip.indexOf(",") > -1) {
            regip = regip.split(",")[1].trim();
        }

        return regip;
    }

    public UserSession register(String mobile, String captcha, String password, String regIp, int catgory, int regFrom,
                                String deviceToken, int terminal) throws BizException {
        if (mobile == null || captcha == null) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        // 检查验证码
        userCaptcha(mobile, captcha);
        return register(mobile, password, regIp, catgory, regFrom + "", terminal, deviceToken);
    }

    /**
     * 带密码的注册
     *
     * @param mobile
     * @param password
     * @param regIp
     * @return
     */
    public UserSession register(String mobile, String password, String regIp, int catgory, String regFrom, int terminal,
                                String deviceToken) throws BizException {

        Predicate isForPHP = (regForm) -> StringUtils.isNoneBlank(regFrom) && (
//                regFrom.equals(CENTER_REG_FROM_M) ||// M站
                regFrom.equals(TerminalType.PC + "") || // PC
                        regForm.equals(SMALL_PROGRAM)// 小程序
        );
        // 查询v_qbank_user
        final UserDto user = userDao.find(mobile);
        if (user != null && isForPHP.test(regFrom)) {
            UserSession userSession = new UserSession();
            userSession.setUname(user.getName());
            userSession.setEmail(user.getEmail());
            userSession.setMobile(user.getMobile());
            return userSession;
        }
        // 查询ucenter绑定表，是否存在该手机号
        final UcenterBind ucenterBind = ucenterService.findBind(mobile);
        if (ucenterBind != null) {
            logger.info("ucenter bind table exists mobile phone :{}", mobile);
            if (isForPHP.test(regFrom)) {
                UserSession userSession = new UserSession();
                userSession.setUname(ucenterBind.getUsername());
                userSession.setEmail(ucenterBind.getEmail());
                userSession.setMobile(ucenterBind.getPhone());
                return userSession;
            }
        }

        // username是手机号的情况
        UcenterMember member = ucenterService.findMemberByUsername(mobile);
        if (member != null && isForPHP.test(regFrom)) {
            UserSession userSession = new UserSession();
            userSession.setUname(member.getUsername());
            userSession.setEmail(member.getEmail());
            userSession.setMobile(member.getUsername());
            return userSession;
        }
        if (ucenterBind != null || user != null || member != null) {
            throw new BizException(UserErrors.USER_EXISTS);
        }

        // 创建uc用户
        final UcenterMember ucUser = createUCUser(mobile, password, regIp, UcenterConfig.UCENTER_MEMBERS_APPID, false);
        // 创建砖题库用户,和uc进行关联,status为1说明不再设置密码/完善信息
        final UserDto ztkUser = createZtkUser(mobile, ucUser.getUid(), ucUser.getUsername(), ucUser.getPassword(), "",
                UserStatus.AVAILABLE, regFrom, regIp, terminal, deviceToken);
        //是否开启送课
        Integer isOpenRegisterFreeCourse = registerFreeCourseDetailConfig.getOpenRegisterFreeCourse();
        if (isOpenRegisterFreeCourse == 1) {
            //发送送课信息
            thirdTaskComponent.createUserRegisterOrderV2(ztkUser.getName());
            if (regFrom.equals(TerminalType.PC + "")) {
                // pc需要特殊处理
                String registerKey = REGISTER_KEY;
                SetOperations<String, String> opsForSet = coreRedisTemplate.opsForSet();
                opsForSet.add(registerKey, ztkUser.getName());
                coreRedisTemplate.expire(registerKey, 30, TimeUnit.MINUTES);
            }
        }
        return createSession(ztkUser, catgory, terminal);
    }


    /**
     * 教育用户信息同步
     *
     * @param mobile
     * @param password
     * @param regIp
     * @return
     */
    public Object userInfoAsync(String mobile, String password, String regIp, String regFrom, int terminal,
                                String deviceToken) throws BizException {
        // 查询v_qbank_user
        final UserDto user = userDao.find(mobile);
        if (user != null) {
            logger.info("userInfoAsync user find :{}", user.getName());
            return user;
        } else {
            UcenterBind findBind = ucenterService.findBind(mobile);
            UcenterMember member = null;
            if (findBind == null) {
                // 创建uc用户
                member = createUCUser(mobile, password, regIp, UcenterConfig.UCENTER_MEMBERS_APPID, false);
                logger.info("userInfoAsync findBind  is null mobile is:{}", mobile);
            } else {
                member = ucenterService.findMemberByUsername(findBind.getUsername());
                logger.info("userInfoAsync findMemberByUsername :{},uid is:{}", member.getUsername(), member.getUid());
            }
            // 创建砖题库用户,和uc进行关联,status为1说明不再设置密码/完善信息
            final UserDto ztkUser = createZtkUser(mobile, member.getUid(), member.getUsername(), member.getPassword(),
                    "", UserStatus.AVAILABLE, CENTER_REG_FROM, regIp, terminal, deviceToken);
            logger.info("userInfoAsync  createZtkUser :{}", ztkUser.getName());
            // 同步php
            thirdTaskComponent.syncUserData(ztkUser.getEmail(), mobile, ztkUser.getName());
            return ztkUser;
        }
    }

    /**
     * 更新绑定手机号
     *
     * @param userId
     * @param uname
     * @param mobile
     * @param catgory
     * @param captcha
     * @return
     * @throws BizException
     */
    public UserSession updateMobile(long userId, String uname, String mobile, int catgory, String captcha, int terminal)
            throws BizException {
        userCaptcha(mobile, captcha);

        if (!mobile.matches(RegexConfig.MOBILE_PHONE_REGEX)) {
            throw new BizException(UserErrors.ILLEGAL_MOBILE);
        }

        UserDto dto = userDao.findAny(mobile);
        UcenterBind ucenterBind = ucenterService.findAnyBind(mobile);

        // 检查是否已经绑定
        if (dto != null || ucenterBind != null) {
            throw new BizException(UserErrors.BIND_EXISTS);
        }

        userDao.updateMobile(uname, mobile);
        ucenterService.updateMobile(uname, mobile);

        final UserDto userDto = userDao.findById(userId);
        final UserSession userSession = getUserSession(userDto, catgory, terminal);
        saveSessionToRedis(userSession, catgory, terminal);
        return userSession;
    }

    /**
     * 判断申论批改是否免费
     */
    public int correctFree() {

        Object obj = redisTemplate.opsForValue().get(ESSAY_GOODS_FREE_KEY);
        if (null == obj) {
            return ESSAY_GOODS_FREE;
        } else {
            return Integer.parseInt(obj.toString());
        }
    }

    /**
     * 判断是否支持语音答题（0支持 1不支持）
     */
    public int voiceAnswer() {

        Object obj = redisTemplate.opsForValue().get(VOICE_ANSWER_KEY);
        if (null == obj) {
            return VOICE_ANSWER_ON;
        } else {
            return Integer.parseInt(obj.toString());
        }
    }

    /**
     * 判断是否支持拍照答题（0 支持 1不支持）
     */
    public int photoAnswer() {

        Object obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY);
        if (null == obj) {
            return PHOTO_ANSWER_ON;
        } else {
            return Integer.parseInt(obj.toString());
        }
    }

    /**
     * 判断是否支持语音答题V2（0支持 1不支持） 不同客户端分开控制
     */
    public int voiceAnswerV2(int terminal, String cv) {

        Object obj = null;
        if (TerminalType.ANDROID == terminal || TerminalType.ANDROID_IPAD == terminal) {
            // ANDROID客户端
            obj = redisTemplate.opsForValue().get(VOICE_ANSWER_KEY_ANDROID);
        } else if (TerminalType.IPHONE == terminal || TerminalType.IPHONE_IPAD == terminal) {
            // IOS客户端
            obj = redisTemplate.opsForValue().get(VOICE_ANSWER_KEY_IOS);
        }
        if (null == obj) {
            return VOICE_ANSWER_ON;
        } else {
            return Integer.parseInt(obj.toString());
        }

    }

    /**
     * 判断是否支持拍照答题V2（0支持 1不支持） 不同客户端分开控制
     */
    public int photoAnswerV2(int terminal, String cv) {
        // 安卓客户端
        Object obj = null;
        if (TerminalType.ANDROID == terminal || TerminalType.ANDROID_IPAD == terminal) {
            // 04-03zx修改 安卓6.2以前的版本。拍照识别开关关掉
            if (cv.compareTo("6.2") < 0) {
                obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_ANDROID_OLD);
                if (obj == null) {
                    return PHOTO_ANSWER_OFF;
                }
                return Integer.parseInt(obj.toString());
            }
            obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_ANDROID);
        } else if (TerminalType.IPHONE == terminal || TerminalType.IPHONE_IPAD == terminal) {
            // 04-03zx修改 IOS6.1.1以前的版本。拍照识别开关关掉
            if (cv.compareTo("6.1.1") < 0) {
                obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_IOS_OLD);
                if (obj == null) {
                    return PHOTO_ANSWER_OFF;
                }
                return Integer.parseInt(obj.toString());
            }
            obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_IOS);
        }
        if (null == obj) {
            return PHOTO_ANSWER_ON;
        } else {
            return Integer.parseInt(obj.toString());
        }

    }

    /*
     * 拍照识别对接第三方（0 汉王 1优图） 不同客户端分开控制
     */
    public int photoAnswerType(int terminal) {
        Object obj = null;
        if (TerminalType.ANDROID == terminal || TerminalType.ANDROID_IPAD == terminal) {
            obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_TYPE_ANDROID);
        } else if (TerminalType.IPHONE == terminal || TerminalType.IPHONE_IPAD == terminal) {
            obj = redisTemplate.opsForValue().get(PHOTO_ANSWER_TYPE_IOS);
        }
        if (null == obj) {
            return ESSAY_YOU_TU;
        } else {
            return Integer.parseInt(obj.toString());
        }
    }

    public String getPhotoAnswerMsg() {
        String msg = (String) redisTemplate.opsForValue().get(PHOTO_ANSWER_MSG);
        return msg;

    }

    /**
     * 根据用户手机号获取本地库的用户信息
     *
     * @return
     */
    public UserDto getUserInfoByMobile(String mobile) {
        UserDto userDto = userDao.findAny(mobile);
        return userDto;
    }

    /**
     * PHP端 批量注册接口 --
     *
     * @return 注册成功返回注册后的数据, true 注册成功，false 已经存在的数据
     */
    public Map<Boolean, List<Object>> registerForPHP(List<UserDto> registerList, final String regIp,
                                                     String deviceToken) {
        Function<UserDto, Object> register = (registerDto) -> {
            try {
                final String mobile = registerDto.getMobile();
                final String password = registerDto.getPassword();

                // 查询v_qbank_user
                final UserDto user = userDao.find(mobile);
                if (null != user) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("username", user.getName());
                    map.put("phone", user.getMobile());
                    map.put("email", user.getEmail());
                    return map;
                }
                // 查询ucenter绑定表，是否存在该手机号
                final UcenterBind ucenterBind = ucenterService.findBind(mobile);
                if (null != ucenterBind) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("username", ucenterBind.getUsername());
                    map.put("phone", ucenterBind.getPhone());
                    map.put("email", ucenterBind.getEmail());
                    return map;
                }
                // username是手机号的情况
                UcenterMember member = ucenterService.findMemberByUsername(mobile);
                if (null != member) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("username", member.getUsername());
                    map.put("phone", member.getUsername());
                    map.put("email", member.getEmail());
                    return map;
                }

                // 创建uc用户
                final UcenterMember ucUser = createUCUser(mobile, password, regIp, UcenterConfig.UCENTER_MEMBERS_APPID, false, () -> Optional.ofNullable(registerDto.getName()).orElse(UcenterUtils.getUsername()));
                // 创建砖题库用户,和uc进行关联,status为1说明不再设置密码/完善信息
                // 此处 目前尚不明确用途
//                if (StringUtils.isNoneBlank(registerDto.getNick())) {
//                    ucUser.setUsername(registerDto.getNick());
//                }
                /* 批量代注册 terminal:566 代表php代报 8 */
                final UserDto ztkUser = createZtkUser(mobile, ucUser.getUid(), ucUser.getUsername(),
                        ucUser.getPassword(), "", UserStatus.AVAILABLE, REGISTER_USER_PHP, regIp, 566, deviceToken);

                return ztkUser;
            } catch (Exception e) {
                logger.info("e = {}", e);
                HashMap<String, Object> map = new HashMap<>();
                map.put("username", registerDto.getName());
                map.put("phone", registerDto.getMobile());
                map.put("email", registerDto.getEmail());
                return map;
            }
        };
        logger.info("registerList = {}", registerList);
        Map<Boolean, List<Object>> listMap = registerList.parallelStream()
                .filter((userDto) -> null != userDto && StringUtils.isNoneBlank(userDto.getPassword())
                        && StringUtils.isNoneBlank(userDto.getMobile()))
                .map(register).filter(o -> null != o)// 可能出现 插入异常导致返回为 null的数据
                .collect(Collectors.groupingBy(o -> o instanceof UserDto));// 注册成功的数据返回 UserDto
        return listMap;
    }

    /**
     * 为PHP 提供获取用户信息接口
     *
     * @param account 查询条件 手机号 or 用户名
     * @return
     */
    public HashMap getUserInfoForPhp(String account) throws BizException {
        // 查询v_qbank_user
        final UserDto user = userDao.findAny(account);
        HashMap<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("uname", user.getName());
            map.put("email", user.getEmail());
            map.put("mobile", user.getMobile());
            map.put("password", user.getPassword());
            map.put("createTime", user.getCreateTime());
            map.put("id", user.getId());
            map.put("dataSource", "v_qbank_user");
            return map;
        }
        // 查询ucenter绑定表，是否存在该手机号
        final UcenterBind ucenterBind = ucenterService.findAnyBind(account);
        if (ucenterBind != null) {
            map.put("uname", ucenterBind.getUsername());
            map.put("email", ucenterBind.getEmail());
            map.put("mobile", ucenterBind.getPhone());
            map.put("dataSource", "common_user_bd");
            map.put("id", ucenterBind.getUserid());

            return map;
        }

        // username是手机号的情况
        UcenterMember member = ucenterService.findMemberByNameAndEmail(account);
        if (member != null) {
            map.put("uname", member.getUsername());
            map.put("email", member.getEmail());
            map.put("mobile", member.getUsername());
            map.put("password", member.getPassword());
            map.put("createTime", member.getRegdate());
            map.put("dataSource", "uc_members");
            return map;
        }
        if (ucenterBind == null && user == null && member == null) {
            throw new BizException(UserErrors.USER_NOT_EXIST);
        }
        return map;
    }

    public UserDto findByMobile(String mobile) {
        return userDao.findByMobile(mobile);
    }

    public Object searchUserListForRegFromAndTime(UserSearchRequest userSearchRequest) {
        return userDao.findUserListForRegFromAndTime(userSearchRequest);
    }

    /**
     * PHP 通过手机号查询 用户名信息
     */
    public Map<String, List<String>> getUserNameByPhoneForPHP(List<String> phoneList) {
        return phoneList.parallelStream().filter(StringUtils::isNoneBlank).distinct().collect(HashMap::new,
                (map, phone) -> {
                    ArrayList<String> list = Lists.newArrayList();
                    UserDto userDto = userDao.findByMobile(phone);
                    if (null != userDto) {
                        list.add(userDto.getName());
                    }
                    UcenterBind ucenterBind = ucenterService.findBind(phone);
                    if (null != ucenterBind) {
                        list.add(ucenterBind.getUsername());
                    }
                    map.put(phone, list.stream().distinct().collect(Collectors.toList()));
                }, Map::putAll);
    }

    public Object saveUserSimple(HashMap<String, String> userMap) {
        String username = userMap.get("username");
        String mobile = userMap.getOrDefault("mobile", "");
        UserDto userDto = null;
        // 先检查用户是否存在
        if (StringUtils.isNoneBlank(mobile)) {
            userDto = userDao.findAny(mobile);
            if (userDto != null) {
                return userDto;
            }
        }

        userDto = UserDto
                .builder()
                .password(DigestUtils.sha1Hex(""))
                .nativepwd("123456")// 记录原始密码,为后边的密码加密同一做准备
                .createTime(System.currentTimeMillis())
                .nick(username)
                .area(DEFAULT_AREA)// 设置默认区域 全国
                .mobile(StringUtils.trimToNull(mobile)) // 插入数据库时，如果手机号为空字符串，转换为null
                .email(StringUtils.trimToEmpty(""))
                .status(UserStatus.INIT)
                .ucenterId(0).name(username) // username为ztk_app+数字的形式
                .regFrom(CENTER_REG_FROM)
                .deviceToken("").build();
        userDao.insert(userDto);
        return userDto;
    }

    /**
     * 分页查询内容
     *
     * @param userIds
     * @return
     */
    public List<UserDto> getUserBatchByUserIds(String userIds) {
        if (StringUtils.isEmpty(userIds)) {
            return Lists.newArrayList();
        }
        //分页查询
        List<String> collect = Arrays.stream(userIds.split(",")).collect(Collectors.toList());
        int allCount = collect.size();
        int pageSize = 2000;
        int totalPage = (int) Math.ceil((double) allCount / (double) pageSize);

        List<UserDto> userDtoList = new ArrayList<>();
        for (int page = 0; page < totalPage; page++) {
            int startIndex = page * pageSize;
            int endIndex = Math.min((page + 1) * pageSize, allCount);
            collect.subList(startIndex, endIndex);
            List<UserDto> userDtos = userDao.findByIds(userIds);
            if (CollectionUtils.isNotEmpty(userDtos)) {
                userDtoList.addAll(userDtos);
                logger.info("分页开始索引:{}，结束索引：{}", startIndex, endIndex);
            }
        }
        return userDtoList;
    }

    /**
     * pc端获取注册送课信息
     *
     * @param uname
     * @return
     */
    public RegisterFreeCourseDetailVo getRegisterGiveCourseForPc(String uname) {
        SetOperations<String, String> opsForSet = coreRedisTemplate.opsForSet();
        boolean flag = opsForSet.isMember(REGISTER_KEY, uname);
        if (flag) {
            opsForSet.remove(REGISTER_KEY, uname);
            RegisterFreeCourseDetailVo detail = new RegisterFreeCourseDetailVo();
            detail.setRcoin(registerFreeCourseDetailConfig.getCoin());
            detail.setRgrowUpValue(registerFreeCourseDetailConfig.getGrowUpValue());
            detail.setRtitle(registerFreeCourseDetailConfig.getTitle());
            List<CourseInfo> courseList = JsonUtil.toList(registerFreeCourseDetailConfig.getCourseList(),
                    CourseInfo.class);
            detail.setRcourseList(courseList);
            return detail;
        }
        return null;
    }

    /**
     * 同步错误用户名信息
     *
     * @return
     */
    public Object syncUcInfo() {
        List<String> asList = Arrays.asList(userPhoneStr.split(","));
        asList.forEach(phone -> {
            List<UcenterBind> bindList = ucenterService.findBindList(phone);
            if (bindList.size() == 1) {
                UserDto findByMobile = userDao.findByMobile(bindList.get(0).getPhone());
                if (findByMobile != null) {
                    if (!bindList.get(0).getUsername().equals(findByMobile.getName())) {
                        userDao.modifyUname(findByMobile.getId(), bindList.get(0).getUsername());
                        logger.info("本地用户{}修改用户名为{}", findByMobile.getName(), bindList.get(0).getUsername());
                    }
                } else {
                    logger.info("uc用户{},手机号{}在本地不存在", bindList.get(0).getId(), bindList.get(0).getPhone());
                }
            } else {
                Optional<UcenterBind> findFirst = bindList.stream().filter(bind -> bind.getUsername().contains("xue"))
                        .findFirst();
                if (findFirst.isPresent()) {
                    UcenterBind ucenterBind = findFirst.get();
                    UserDto findByMobile = userDao.findByMobile(ucenterBind.getPhone());
                    if (findByMobile != null) {
                        userDao.modifyUname(findByMobile.getId(), bindList.get(0).getUsername());
                        logger.info("本地用户{}修改用户名为{}", findByMobile.getName(), ucenterBind.getUsername());
                    } else {
                        logger.info("uc用户{},手机号{}在本地不存在", ucenterBind.getId(), ucenterBind.getPhone());
                    }
                } else {
                    logger.info("用户手机号{}在UC中不存在xue开头的绑定关系,且绑定关系大于1", phone);
                }
            }
        });
        return null;
    }

    /**
     * 删除用户
     *
     * @return
     */
    public Object delUcInfo() {
        List<String> asList = Arrays.asList(userPhoneStr.split(","));
        asList.forEach(phone -> {
            List<UcenterBind> bindList = ucenterService.findBindList(phone);
            if (bindList.size() > 1) {

                Optional<UcenterBind> findFirst = bindList.stream().filter(bind -> bind.getUsername().contains("xue"))
                        .findFirst();
                if (findFirst.isPresent()) {
                    UcenterBind ucenterBind = findFirst.get();
                    UserDto findByMobile = userDao.findByMobile(ucenterBind.getPhone());
                    if (findByMobile != null) {
                        userDao.modifyUname(findByMobile.getId(), bindList.get(0).getUsername());
                        logger.info("本地用户{}修改用户名为{}", findByMobile.getName(), ucenterBind.getUsername());
                    } else {
                        logger.info("uc用户{},手机号{}在本地不存在", ucenterBind.getId(), ucenterBind.getPhone());
                    }
                } else {
                    logger.info("用户手机号{}在UC中不存在xue开头的绑定关系,且绑定关系大于1", phone);
                }

            }
        });
        return null;
    }

    /**
     * 根据用户名清除token信息
     *
     * @param uname
     * @return
     */
    public Object delSessionByUname(String uname) {
        UserDto findAny = userDao.findAny(uname);
        if (findAny != null) {
            userSessionService.getAllTokenKeyByUserId(findAny.getId()).forEach(token -> {
                if (token != null) {
                    String tokenKey = sessionRedisTemplate.opsForValue().get(token);
                    sessionRedisTemplate.delete(token);
                    if (tokenKey != null) {
                        sessionRedisTemplate.delete(tokenKey);

                    }

                }
            });
        }
        return 1;
    }

    /**
     * 根据用户名/手机号查询
     *
     * @param params 用户名/手机号
     * @return 用户列表
     */
    public Object getUserBatchByByUsernameOrMobile(List<String> params) {
        return userDao.findByUsernameOrMobile(params);
    }

    public Object delUCMemberByUname(String uname) {
        ucenterService.findMemberByUsername(uname);
        return null;
    }

    /**
     * 检测是否为压测用户 手机号以1100开头
     *
     * @param uName
     * @return
     */
    public Object checkUserIsMock(String uName) {
        UserDto user = userDao.findByName(uName);
        boolean isMock = null != user.getMobile() && user.getMobile().startsWith("1100");
        return isMock;
    }

    /**
     * 批量登录创建token
     *
     * @return
     */
    public Object loginForTest() {
        List<String> tokenList = Lists.newArrayList();
        List<UserDto> findUsers = userDao.findUsers(0, 10000);
        findUsers.stream().forEach(user -> {
            UserSession session = loginWithoutPassWd(user, 1);
            String token = session.getToken();
            tokenList.add(token);
        });
        create(tokenList);
        return null;
    }

    public void create(List<String> tokenList) {

        BufferedWriter fileOutputStream = null;

        try {
            String fullPath_Edge = "/data/logs/user-web-server/" + File.separator + "token.csv";
            File tokenFile = new File(fullPath_Edge);
            if (!tokenFile.getParentFile().exists()) {
                tokenFile.getParentFile().mkdirs();
            }
            if (tokenFile.exists()) {
                tokenFile.delete();
            }
            tokenFile = new File(fullPath_Edge);
            tokenFile.createNewFile();
            fileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tokenFile), "UTF-8"),
                    1024);
            fileOutputStream.write("token");
            fileOutputStream.newLine();
            for (String token : tokenList) {
                fileOutputStream.write(token);
                fileOutputStream.newLine();
            }
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * 教师网用户同步接口
     *
     * @param params
     * @return
     */
    public Object syncTeacherUserInfo(List<String> params) {
        for (String userStr : params) {
            String decryptMode = Crypt3Des.decryptMode(userStr);
            logger.info("解密前:{},解密后:{}", userStr, decryptMode);
            TeacherUserDto userDto = JsonUtil.toObject(decryptMode, TeacherUserDto.class);
            logger.info("转换后:{}", userDto.toString());
            switch (userDto.getSource()) {
                case 0:
                    // 无第三方绑定
                    createTeacherUser(userDto);
                    break;
                case 1:
                    // QQ绑定
                    break;
                case 2:
                    // 微信绑定
                default:
                    break;
            }
        }

        return null;
    }

    /**
     * 同步教师无第三方绑定的用户
     *
     * @param userDto
     */
    public void createTeacherUser(TeacherUserDto userDto) {
        String account = userDto.getMobile() == null ? userDto.getEmail() : userDto.getMobile();
        // 查询v_qbank_user
        final UserDto user = userDao.findAny(account);
        if (user != null) {
            logger.info("syncTeacherUserInfo user find :{}", user.getName());
            return;
        } else {
            UcenterBind findBind = ucenterService.findBind(userDto.getMobile());
            UcenterMember member = null;
            if (findBind == null) {
                // 创建uc用户
                member = createUCUser(userDto.getMobile(), userDto.getPassword(), "", UCENTER_MEMBERS_TEACHER_APPID,
                        true);
                logger.info("syncTeacherUserInfo findBind  is null mobile is:{}", account);
            } else {
                member = ucenterService.findMemberByUsername(findBind.getUsername());
                logger.info("syncTeacherUserInfo findMemberByUsername :{},uid is:{}", member.getUsername(),
                        member.getUid());
            }
            // 创建砖题库用户,和uc进行关联,status为1说明不再设置密码/完善信息
            final UserDto ztkUser = createZtkUser(userDto.getMobile(), member.getUid(), member.getUsername(),
                    member.getPassword(), "", UserStatus.AVAILABLE, REG_FROM_TEACHER, null, userDto.getRegFrom(),
                    null);
            logger.info("syncTeacherUserInfo  createZtkUser :{}", ztkUser.getName());
            // 同步php
            thirdTaskComponent.syncUserData(ztkUser.getEmail(), userDto.getMobile(), ztkUser.getName());
        }
    }

    /**
     * 为小程序提供登录接口
     *
     * @param appId
     * @param encryptedData
     * @param iv
     * @param regip
     * @param terminal
     * @param catgory
     * @return
     * @throws BizException
     */
    public UserSession loginForWechat(String appId, String encryptedData, String iv, String regip, int terminal, int catgory) throws BizException {
        String sessionKey = (String) redisTemplate.opsForValue().get(appId);
        logger.info("appid :{},sessionKey:{}", appId, sessionKey);
        if (StringUtils.isNoneBlank(sessionKey)) {
            String decryptWXAppletInfo = "";
            try {
                decryptWXAppletInfo = WechatAESUtils.decryptWXAppletInfo(sessionKey, encryptedData, iv);
                logger.info("appId:{},解密后信息为:{}", appId, decryptWXAppletInfo);
			} catch (Exception e) {
				logger.error("用户信息解密失败:{}", e);
				throw new BizException(ErrorResult.create(1100122, "授权过期,请重新登录"));
			}
            Map wechatUserMap = JsonUtil.toMap(decryptWXAppletInfo);
            String mobile = (String) wechatUserMap.get("purePhoneNumber");
            UserSession userSession = loginVirtual(mobile, terminal);
            if (userSession != null) {
                return userSession;
            } else {
                // 创建uc用户
                String password = UUID.randomUUID().toString();
                password = password.replace("-", "").substring(0, 8);
                final UcenterMember ucUser = createUCUser(mobile, password, regip, UcenterConfig.UCENTER_MEMBERS_APPID,
                        false);
                // 创建砖题库用户,和uc进行关联,status为1说明不再设置密码/完善信息
                final UserDto ztkUser = createZtkUser(mobile, ucUser.getUid(), ucUser.getUsername(),
                        ucUser.getPassword(), "", UserStatus.AVAILABLE, TerminalType.WEI_XIN_APPLET + "", regip,
                        terminal, "");
                // 是否开启送课
                Integer isOpenRegisterFreeCourse = registerFreeCourseDetailConfig.getOpenRegisterFreeCourse();
                if (isOpenRegisterFreeCourse == 1) {
                    // 发送送课信息
                    thirdTaskComponent.createUserRegisterOrderV2(ztkUser.getName());
                }
                return createSession(ztkUser, catgory, terminal);
            }
        } else {
            throw new BizException(ErrorResult.create(1100123, "授权过期,请重新登录"));
        }
    }

    public UcenterMember createUCUser(String phone, String defaultPassword, String regIp, int appId, boolean isEncypt) {
        return createUCUser(phone, defaultPassword, regIp, appId, isEncypt, UcenterUtils::getUsername);
    }

}
