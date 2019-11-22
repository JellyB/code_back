package com.huatu.tiku.course.service.v1.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.ActivityStatusEnum;
import com.huatu.tiku.course.consts.ActivityUserInfo;
import com.huatu.tiku.course.dao.manual.AppStoreEvaluationRecordMapper;
import com.huatu.tiku.course.netschool.api.UserAccountServiceV1;
import com.huatu.tiku.course.service.v1.ActivityService;
import com.huatu.tiku.course.service.v6.SensorsService;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.ztk.api.v4.user.UserServiceV4;
import com.huatu.tiku.entity.AppStoreEvaluationRecord;
import com.huatu.tiku.springboot.basic.reward.RewardAction;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author zhangchong
 *
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

	@Autowired
	private RedisTemplate redisTemplate;

	@Value("${activity.sign.conf}")
	private String activityConfig;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private SensorsService sensorsService;

	@Autowired
	private UserAccountServiceV1 userAccountServiceV1;

	@Autowired
	private UserServiceV4 userServiceV4;
	
	@Autowired
	private AppStoreEvaluationRecordMapper appStoreEvaluationRecordMapper;

	private String CACHENAME = "618";

	private String CACHEPREFIX = "activity_618_";

	private String CACHE_PREFIX_HASH_KEY = "activity_618_hash_key";
	
	//appstore 赠送金币数量
	private int APP_STORE_GIVE_COINS = 30;

	private LoadingCache<Object, JSONObject> configObjectCache = CacheBuilder.newBuilder().maximumSize(10)
			.expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<Object, JSONObject>() {
				@Override
				public JSONObject load(Object key) throws Exception {
					return JSONObject.parseObject(activityConfig);
				}
			});

	@Override
	public int signGiveCoin(String userName, String ucId) {
		try {
			JSONObject configObject = configObjectCache.get(CACHENAME);
			String currentKey = LocalDate.now().toString();
			Integer coin = (Integer) configObject.get(currentKey);
			if (coin != null) {
				String activityKey = CACHEPREFIX + currentKey;
				String activityHashKey = CACHE_PREFIX_HASH_KEY + currentKey;
				final SetOperations<String, String> setOperations = redisTemplate.opsForSet();
				final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
				if (!setOperations.isMember(activityKey, userName)) {
					RewardMessage msg = RewardMessage.builder().gold(coin)
							.action(RewardAction.ActionType.ACTIVTY.name()).experience(1).bizId(currentKey).uname(userName)
							.timestamp(System.currentTimeMillis()).build();
					rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);
					setOperations.add(activityKey, userName);
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					ActivityUserInfo userInfo = new ActivityUserInfo(userName, ucId, simpleDateFormat.format(new Date()), coin.intValue(), currentKey);
					hashOperations.put(activityHashKey, userName, JSONObject.toJSONString(userInfo));
					redisTemplate.expire(activityKey, 7, TimeUnit.DAYS);
					redisTemplate.expire(activityHashKey, 7, TimeUnit.DAYS);
					log.info("618活动用户 userName:{}赠送图币{},bizId为:{},ucId:{}", userName, coin, currentKey, ucId);
					return ActivityStatusEnum.SUCCESS.getCode();
				} else {
					log.info("618活动用户uname:{}已经赠送过图币{},bizId为:{}", userName, coin, currentKey);
					return ActivityStatusEnum.SIGNED.getCode();

				}
			}
			return ActivityStatusEnum.END.getCode();
		} catch (Exception e) {
			log.error("signGiveCoin error:{}", e);
		}
		return ActivityStatusEnum.ERROR.getCode();
	}

	/**
	 * 签到记录
	 */
	@Override
	public Object signList(String uname) {
		try {
			final SetOperations<String, String> setOperations = redisTemplate.opsForSet();
			JSONObject configObject = configObjectCache.get(CACHENAME);
			Set<String> it = configObject.keySet();
			Map<String, Integer> signMap = Maps.newHashMap();
			it.forEach(key -> {
				String activityKey = CACHEPREFIX + key;
				LocalDate activityDate = LocalDate.parse(key);
				if (setOperations.isMember(activityKey, uname)) {
					// 已经签到
					signMap.put(key, ActivityStatusEnum.SIGNED.getCode());
				} else if (LocalDate.now().compareTo(activityDate) > 0) {
					// 已过期
					signMap.put(key, ActivityStatusEnum.END.getCode());
				} else {
					// 未签到
					signMap.put(key, ActivityStatusEnum.UNDO.getCode());
				}
			});
			return signMap;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return Maps.newHashMap();
	}

	/**
	 * 活动数据上报
	 *
	 * @param userName
	 * @param terminal
	 * @param cv
	 * @param currentKey
	 * @return
	 */
	@Override
	public Object report(String userName, int terminal, String cv, String currentKey) {
		try{
			log.info("数据上报处理userName:{}, terminal:{},cv:{},currentKey:{}",userName, terminal,cv,currentKey);
			String activityKey = CACHEPREFIX + currentKey;
			String activityHashKey = CACHE_PREFIX_HASH_KEY + currentKey;
			final SetOperations<String, String> setOperations = redisTemplate.opsForSet();
			final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
			Set<String> userNames = setOperations.members(activityKey);
			log.info("开始处理当前时间:{}的任务,需要处理:{}条数据", currentKey, userNames.size());
			List<String> result = hashOperations.multiGet(activityHashKey, userNames);

			List<String> filterResult = result.stream().filter(item -> StringUtils.isNotEmpty(item)).collect(Collectors.toList());
			List<ActivityUserInfo> activityUserInfos = Lists.newArrayList();
			String message;
			if(CollectionUtils.isEmpty(filterResult) && CollectionUtils.isEmpty(userNames)){
				message = "hash key:" + activityHashKey + "中没有需要处理的数据";
			}else if(CollectionUtils.isEmpty(filterResult) && CollectionUtils.isNotEmpty(userNames)){
				activityUserInfos.addAll(dealAbnormalActivityUserInfo(userNames, currentKey));
				message = "处理非正常数据" + activityHashKey + "数据量:" + userNames.size();
			}else{
				activityUserInfos.addAll(dealNormalActivityUserInfo(result));
				message = "处理正常数据" + activityHashKey + "数据量:" + result.size();
			}
			sensorsService.reportActivitySign(activityUserInfos);
			return SuccessMessage.create(message);
		}catch (Exception e){
			log.error("ActivityServiceImpl sign report error:{}", e);
			return SuccessMessage.create("数据上报异常" + currentKey);
		}
	}

	/**
	 * 处理规范数据
	 * @param result
	 * @return
	 */
	private List<ActivityUserInfo> dealNormalActivityUserInfo(List<String> result){
		List<ActivityUserInfo> activityUserInfos = Lists.newArrayList();
		try{
			for(String str : result){
				ActivityUserInfo userInfo = JSONObject.parseObject(str, ActivityUserInfo.class);
				if(null != userInfo){
					activityUserInfos.add(userInfo);;
				}
			}
			return activityUserInfos;
		}catch (Exception e){
			return activityUserInfos;
		}
	}
	/**
	 * 处理不规范的数据
	 * @param members
	 * @return
	 */
	private List<ActivityUserInfo> dealAbnormalActivityUserInfo(Set<String> members, String currentKey){
		log.info("dealActivityUserInfo size:{}", members.size());
		List<ActivityUserInfo> activityUserInfos = Lists.newArrayList();
		try{
			List<String> userNames = Lists.newArrayList(members);
			List<String> userIds = Lists.newArrayList();
			//1. 获取 userId
			NetSchoolResponse userIdResponse = userAccountServiceV1.getUIdByUsernameBatch(userNames);
			if(ResponseUtil.isFailure(userIdResponse)){
				return activityUserInfos;
			}
			List<LinkedHashMap<String,Object>> data = (List<LinkedHashMap<String,Object>>) userIdResponse.getData();
			log.info("obtain data from userAccountServiceV1 size:{}", data.size());
			if(CollectionUtils.isEmpty(data)){
				return activityUserInfos;
			}
			for(LinkedHashMap<String,Object> current : data){
				userIds.add(String.valueOf(current.getOrDefault("userId", "0")));
			}
			log.info("dealActivityUserInfo.userIds.size:{}", userIds.size());
			// 2 获取 ucId
			NetSchoolResponse ucIdResponse = userServiceV4.getUserLevelBatch(userIds);
			List<Map<String, Object>> userInfoList = (List<Map<String, Object>>) ucIdResponse.getData();
			List<Integer> mobileIsNull = Lists.newArrayList();
			for(Map<String, Object> map : userInfoList){
				String mobile = MapUtils.getString(map, "mobile");
				if(null == mobile || mobile.equals("null")){
					Integer id = MapUtils.getInteger(map, "id");
					mobileIsNull.add(id);
					continue;
				}
				String uname = MapUtils.getString(map, "name");
				ActivityUserInfo activityUserInfo = ActivityUserInfo.builder()
						.time(currentKey + " 00:00:00")
						.currentKey(currentKey)
						.coins(500)
						.ucId(mobile)
						.uname(uname)
						.build();
				activityUserInfos.add(activityUserInfo);
			}
			log.error("上报用户手机号为空的数据,待处理size:{},手机号为空size:{},手机号为空userId:{}", userInfoList.size(), mobileIsNull.size(), mobileIsNull);
			log.info("request 2 step obtain activityUserInfos.size:{}", activityUserInfos.size());
			return activityUserInfos;
		}catch (Exception e){
			log.error("dealActivityUserInfo caught an exception:{}", e);
			return activityUserInfos;
		}
	}

	@Override
	public Object appStoreEvalution(int uid, String uname, Integer terminal, String cv) {
		int count = appStoreEvaluationRecordMapper.selectCount(AppStoreEvaluationRecord.builder().userId(uid).build());
		if (count <= 0) {
			// 未评价过
			RewardMessage msg = RewardMessage.builder().gold(APP_STORE_GIVE_COINS)
					.action(RewardAction.ActionType.APP_STORE_EVALUTION.name()).experience(1).bizId(uid + "").uname(uname)
					.timestamp(System.currentTimeMillis()).build();
			rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);

			AppStoreEvaluationRecord record = AppStoreEvaluationRecord.builder().userId(uid).userName(uname).build();
			record.setGmtCreate(new Timestamp(System.currentTimeMillis()));
			count = appStoreEvaluationRecordMapper.insert(record);
			log.info("username:{},appStoreEvalution success");
		}
		return count;
	}
}
