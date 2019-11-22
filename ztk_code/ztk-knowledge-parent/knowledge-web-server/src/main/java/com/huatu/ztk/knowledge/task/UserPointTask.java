///*
//package com.huatu.ztk.knowledge.task;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import com.google.common.primitives.Ints;
//import com.huatu.ztk.commons.exception.BizException;
//import com.huatu.ztk.knowledge.bean.QuestionPoint;
//import com.huatu.ztk.knowledge.bean.QuestionPointChange;
//import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceDataGetUtil;
//import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceUtil;
//import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
//import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
//import com.huatu.ztk.knowledge.dao.QuestionPointDao;
//import com.huatu.ztk.knowledge.service.QuestionPointService;
//import com.yxy.ssdb.client.SsdbConnection;
//import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
//import org.apache.commons.collections.CollectionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.HashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SetOperations;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
//
//*/
///**
// * Created by lenovo on 2017/10/1.
// *//*
//
//@Service
//public class UserPointTask {
//    private final static Logger logger = LoggerFactory.getLogger( UserPointTask.class);
//    private final static int MAX_POINT_ID = 999999;
//    private final static int POINT_SIZE = 1000000;     //知识点数量最大限制（ssdb区间查询用户完成的知识点key时用到）
//    private final static int QUESTION_SIZE = 1000000;   //某一知识点下的试题数的最大限制
//    private final static int POINT_CHANGE_MAX_SIZE = 50000;
//    //subject   存储变动的日志ID
//    Cache<Integer, Set<Integer>> POINT_CHANGE_ID_CACHE =
//            CacheBuilder.newBuilder()
//                    .maximumSize(500)
//                    .expireAfterWrite(10, TimeUnit.DAYS)
//                    .build();
//    //存储所有变动日志的日志id，和对应的日志
//    Cache<Integer, QuestionPointChange> QUESTION_POINT_CHANGE_CACHE =
//            CacheBuilder.newBuilder()
//                    .maximumSize(50000)
//                    .expireAfterWrite(10, TimeUnit.DAYS)
//                    .build();
//    @Resource
//    private RedisTemplate redisTemplate;
//    @Autowired
//    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;
//    @Autowired
//    private QuestionPointService questionPointService;
//    @Autowired
//    private QuestionPointDao questionPointDao;
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//    @Autowired
//    private QuestionPersistenceUtil questionPersistenceUtil;
//    @Autowired
//    private QuestionPersistenceDataGetUtil questionPersistenceDataGetUtil;
//
//    private final String USER_OFFSETS = "user_offsets";
//    public void getUserPointChangeCache(int subject, int start, int end){
//        final Set<Integer> idSet = POINT_CHANGE_ID_CACHE.getIfPresent( subject );
//        if(idSet==null||idSet.isEmpty()||(!idSet.contains( end )&&!idSet.contains( start+POINT_CHANGE_MAX_SIZE ))){
//            synchronized (POINT_CHANGE_ID_CACHE){
//                if(idSet==null||idSet.isEmpty()||(!idSet.contains( end )&&!idSet.contains( start+POINT_CHANGE_MAX_SIZE ))){
//                    if(end-start>POINT_CHANGE_MAX_SIZE){
//                        logger.error( "一次处理的变动不能超过"+POINT_CHANGE_MAX_SIZE );
//                        end = start + POINT_CHANGE_MAX_SIZE;
//                    }
//                    List<QuestionPointChange> changes = questionPointDao.findChangeLog(subject,start,end );
//                    final Set<Integer> changeIdSet = Sets.newHashSet();
//                    final Map<Integer,QuestionPointChange> changeMap = Maps.newHashMap();
//                    for(QuestionPointChange change:changes){
//                        if(change.getOldPointId()==change.getNewPointId()){
//                            continue;
//                        }
//                        int id = change.getId();
//                        changeIdSet.add(id);
//                        changeMap.put( id,change );
//                    }
//                    POINT_CHANGE_ID_CACHE.put( subject,changeIdSet );
//                    QUESTION_POINT_CHANGE_CACHE.putAll( changeMap );
//                }
//            }
//        }
//
//    }
//
//    */
///**
//     * 处理size个用户的所有试题知识点更新
//     *
//     * @throws BizException
//     * @param userId
//     * @param subject
//     *//*
//
//    public boolean  bathUpdateUserPoint(int userId, int subject) throws BizException{
//        long start = System.currentTimeMillis();
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        Integer userOffSets = 0;
//        if(hashOperations.get(USER_OFFSETS,userId+"")!=null){
//            userOffSets = Integer.parseInt( String.valueOf( hashOperations.get(USER_OFFSETS,userId+"") ));
//        }
//        Integer maxOffSet = 0;
//        if(hashOperations.get(USER_OFFSETS,"-1")!=null){
//            maxOffSet = Integer.parseInt( String.valueOf( hashOperations.get(USER_OFFSETS,"-1")));
//        }
//        if(maxOffSet==userOffSets){
//            return false;
//        }
//        Integer minOffSets = 0;
//        if(hashOperations.get(USER_OFFSETS,"-2")!=null){
//            minOffSets = Integer.parseInt( String.valueOf( hashOperations.get(USER_OFFSETS,"-2")));
//        }
//        long start0 = System.currentTimeMillis();
//        logger.info( "用户偏移量查询，用时={}",start0-start );
//        getUserPointChangeCache(subject,minOffSets,maxOffSet);
//        Set<Integer> logIds = POINT_CHANGE_ID_CACHE.getIfPresent( subject );
//        List<Integer> newLogIds = Lists.newArrayList();
//        int maxId = userOffSets;
//        for(int id:logIds){
//            if(userOffSets<id){
//                newLogIds.add( id );
//            }
//            if(maxId<id){
//                maxId = id;
//            }
//        }
//        Map<Integer,QuestionPointChange> tempMap = QUESTION_POINT_CHANGE_CACHE.getAllPresent( newLogIds );
//        if(tempMap==null||tempMap.isEmpty()){
//            logger.info( "用户{}无需要处理的日志",userId );
//            return false;
//        }
//        long start1 = System.currentTimeMillis();
//        logger.info( "获取偏移量后的日志，用时={}",start1-start0 );
//        Set<Integer> questionFromlog = Sets.newHashSet();    //待更新的日志中涉及到的试题id
//        Map<Integer,List<QuestionPointChange>> changeMap = Maps.newHashMap();  //涉及到的试题id对应的日志记录集合
//        for(Map.Entry<Integer,QuestionPointChange> entry:tempMap.entrySet()){
//            QuestionPointChange change = entry.getValue();    //日志信息
//            questionFromlog.add( change.getQuestionId() );
//            //将日志按试题id分类存储
//            if(changeMap.get(change.getQuestionId())==null){
//                List<QuestionPointChange> tempList = Lists.newArrayList();
//                tempList.add( change );
//                changeMap.put( change.getQuestionId(),tempList );
//            }else{
//                List<QuestionPointChange> tempList = changeMap.get( change.getQuestionId() );
//                tempList.add( change );
//            }
//        }
//        long start2 = System.currentTimeMillis();
//        logger.info( "获取需要处理的试题及相关的日志，试题数量{}，用时={}",questionFromlog.size(),start2-start1 );
//        updateUserFinishedPoint(subject,userId,changeMap,questionFromlog);
//        updateUserWrongPoint(userId,changeMap,questionFromlog);
//        updateUserCollectPoint(userId,changeMap,questionFromlog);
//        //更新用户知识点更新日志便偏移量
//        hashOperations.put( USER_OFFSETS,userId+"",maxId+"" );
//        long end = System.currentTimeMillis();
//        logger.info( "用户完成知识点更新，总用时={}",end-start );
//        return true;
//    }
//
//    private void updateUserCollectPoint( int userId, Map<Integer, List<QuestionPointChange>> changeMap, Set<Integer> questionFromlog) {
//        long start = System.currentTimeMillis();
//        String collectCountKey =RedisKnowledgeKeys.getCollectCountKey( userId );
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
//        //知识点->试题个数
//        final Set<Integer> oldPointSet =Sets.newHashSet();
//        hashOperations.entries(collectCountKey).forEach((key,value) -> oldPointSet.add( Ints.tryParse(String.valueOf( key ) )) );
//        final Set<Integer> oldQuestionSet = Sets.newHashSet();  //redis中涉及到的题目
//        final Map<Integer,Set<Integer>> questionMap = Maps.newHashMap();  //存储试题reids中所属的知识点
//        for(int pointId:oldPointSet){
//            String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey( userId,pointId );
//            Set<String> questions = zSetOperations.range( collectSetKey,0,QUESTION_SIZE ); //知识点下的试题id
//            for(String questionId:questions){
//                int question =Integer.parseInt( questionId );
//                oldQuestionSet.add(question);
//                if(questionMap.get( question )==null){
//                    Set<Integer> temp = Sets.newHashSet();   //试题对应的层级和知识点id
//                    temp.add( pointId );
//                    questionMap.put( question,temp );
//                }else{
//                    Set<Integer> temp =questionMap.get( question );
//                    temp.add( pointId );
//                }
//            }
//        }
//        //跟改动的试题id取交集，得到用户涉及改变的试题
//        Collection<Integer> changeQuestion = CollectionUtils.intersection( questionFromlog,oldQuestionSet );
//        if(CollectionUtils.isEmpty( changeQuestion )){
//            return;
//        }
//        logger.info( "用户{}需要修改的收藏试题有{}道",userId,changeQuestion.size() );
//        updateCollectInRedis(changeQuestion,changeMap,questionMap,userId);
//        long end = System.currentTimeMillis();
//        logger.info( "修改收藏的试题，用时={}",end-start );
//    }
//
//
//    private void updateCollectInRedis(Collection<Integer> changeQuestion, Map<Integer, List<QuestionPointChange>> changeMap, Map<Integer, Set<Integer>> questionMap, int userId) {
//        for(int questionId:changeQuestion){
//            Set<Integer> points = questionMap.get( questionId );
//            List<QuestionPointChange>  changes = changeMap.get( questionId );
//            if(CollectionUtils.isEmpty( changes )){
//                continue;
//            }
//            Set<Integer> fromPoints = Sets.newHashSet();
//            Set<Integer> newPoints = Sets.newHashSet();
//            Integer[] toPoints = {-1,-1,-1};
//            Integer[] changeId = {0,0,0};
//            for(QuestionPointChange change:changes){
//                fromPoints.add( change.getOldPointId() );
//                int index = change.getLevel();
//                if(change.getId()>changeId[index]){
//                    changeId[index] = change.getId();
//                    toPoints[index] = change.getNewPointId();
//                }
//            }
//            for(int id:toPoints){
//                if(id!=-1){
//                    newPoints.add( id );
//                }
//            }
//            try {
//                updateCollectQuestion(fromPoints,newPoints,points,questionId,userId);
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.error( "redis>>>用户做错试题修改失败，userId={},questionId={}",userId,questionId );
//                logger.error( "ex",e );
//            }
//        }
//    }
//
//    private void updateCollectQuestion(Set<Integer> fromPoints, Set<Integer> newPoints, Set<Integer> points, int questionId, int userId) {
//        logger.info( "fromPoint={},newPoint={},point={}",fromPoints,newPoints,points );
//        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        Collection<Integer> collection = CollectionUtils.intersection( fromPoints,points );   //日志变动中涉及到的旧知识点和reids中存储该试题的知识点,即变动前的知识点
//        Collection<Integer> ignoreCollection = CollectionUtils.intersection( collection,newPoints );   //变动前的知识点和变动后的知识点的交集，这部分可以省略不做删除添加操作
//        newPoints.removeAll( ignoreCollection );
//        collection.removeAll( ignoreCollection );
//        String collectCountKey = RedisKnowledgeKeys.getCollectCountKey( userId );
//        for(int pointId:collection){
//            logger.info( "collect<<<remove questionIds {},from points {}",questionId ,pointId);
//            String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey( userId,pointId );
//            zSetOperations.remove(collectSetKey,questionId+"");
//            long size = zSetOperations.size( collectSetKey );
//            hashOperations.put( collectCountKey,pointId+"",size+"" );
//            */
///**
//             * 缓存收藏数据 2018-04-16
//             *//*
//
//            questionPersistenceUtil.addCollectQuestionPersistence(collectSetKey);
//
//        }
//        for(int pointId:newPoints){
//            logger.info( "collect<<<add questionIds {},to point {}",questionId , pointId);
//            String collectSetKey = RedisKnowledgeKeysAdapter.getInstance().getCollectSetKey( userId,pointId );
//            zSetOperations.add( collectSetKey,questionId+"",System.currentTimeMillis() );
//            long size = zSetOperations.size( collectSetKey );
//            hashOperations.put( collectCountKey,pointId+"",size+"" );
//            */
///**
//             * 缓存收藏数据 2018-04-16
//             *//*
//
//            questionPersistenceUtil.addCollectQuestionPersistence(collectSetKey);
//        }
//    }
//
//
//
//    private void updateUserWrongPoint(int userId, Map<Integer, List<QuestionPointChange>> changeMap, Set<Integer> questionFromlog) {
//        long start = System.currentTimeMillis();
//        String wrongCountKey =RedisKnowledgeKeys.getWrongCountKey( userId );   //用户错题数量统计key
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
//        final Set<Integer> oldPointSet =Sets.newHashSet();    //所有redis中存储的知识点
//        hashOperations.entries(wrongCountKey).forEach((key,value) -> oldPointSet.add( Ints.tryParse(String.valueOf( key ) )) );
//        final Set<Integer> oldQuestionSet = Sets.newHashSet();  //redis中涉及到的题目
//        final Map<Integer,Set<Integer>> questionMap = Maps.newHashMap();  //存储试题reids中所属的知识点
//        for(int pointId:oldPointSet){
//            String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey( userId,pointId );
//            Set<String> questions = zSetOperations.range(wrongSetKey, 0, QUESTION_SIZE);
//            for(String questionId:questions){
//                int question =Integer.parseInt( questionId );
//                oldQuestionSet.add(question);
//                if(questionMap.get( question )==null){
//                   Set<Integer> temp = Sets.newHashSet();   //试题对应的层级和知识点id
//                    temp.add( pointId );
//                    questionMap.put( question,temp );
//                }else{
//                    Set<Integer> temp =questionMap.get( question );
//                    temp.add( pointId );
//                }
//            }
//        }
//        //跟改动的试题id取交集，得到用户涉及改变的试题
//        Collection<Integer> changeQuestion = CollectionUtils.intersection( questionFromlog,oldQuestionSet );
//        if(CollectionUtils.isEmpty( changeQuestion )){
//            return;
//        }
//        logger.info( "用户{}需要修改的做错试题有{}道",userId,changeQuestion.size() );
//        updateWrongInRedis(changeQuestion,changeMap,questionMap,userId);
//        long end = System.currentTimeMillis();
//        logger.info( "修改做错的试题，用时={}",end-start );
//    }
//
//    */
///**
//     *
//     * @param changeQuestion  涉及到的错题id
//     * @param changeMap        改变日志存储（试题id对应日志集合）
//     * @param questionMap       试题id对应一二三级知识点
//     * @param userId
//     *//*
//
//    private void updateWrongInRedis(Collection<Integer> changeQuestion, Map<Integer, List<QuestionPointChange>> changeMap, Map<Integer, Set<Integer>> questionMap, int userId) {
//        for(int questionId:changeQuestion){
//            Set<Integer> points = questionMap.get( questionId );
//            List<QuestionPointChange>  changes = changeMap.get( questionId );
//            if(CollectionUtils.isEmpty( changes )){
//                continue;
//            }
//            Set<Integer> fromPoints = Sets.newHashSet();
//            Set<Integer> newPoints = Sets.newHashSet();
//            Integer[] toPoints = {-1,-1,-1};
//            Integer[] changeId = {0,0,0};
//            for(QuestionPointChange change:changes){
//                fromPoints.add( change.getOldPointId() );
//                int index = change.getLevel();
//                if(change.getId()>changeId[index]){
//                    changeId[index] = change.getId();
//                    toPoints[index] = change.getNewPointId();
//                }
//            }
//            for(int id:toPoints){
//                if(id!=-1){
//                    newPoints.add( id );
//                }
//            }
//            try {
//                updateWrongQuestion(fromPoints,newPoints,points,questionId,userId);
//            } catch (Exception e) {
//                e.printStackTrace();
//                logger.error( "redis>>>用户做错试题修改失败，userId={},questionId={}",userId,questionId );
//                logger.error( "ex",e );
//            }
//        }
//    }
//
//    */
///**
//     *
//     * @param fromPoints   所有变动日志的旧知识点集合
//     * @param newPoints     变动日志中最后的一二三级新知识点集合
//     * @param points        reids目前存储的试题对应的知识点集合
//     * @param questionId    试题id
//     * @param userId        用户id
//     * @throws Exception
//     *//*
//
//    private void updateWrongQuestion(Set<Integer> fromPoints, Set<Integer> newPoints, Set<Integer> points, int questionId,int userId) throws  Exception {
//        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        Collection<Integer> collection = CollectionUtils.intersection( fromPoints,points );   //日志变动中涉及到的旧知识点和reids中存储该试题的知识点,即变动前的知识点
//        Collection<Integer> ignoreCollection = CollectionUtils.intersection( collection,newPoints );   //变动前的知识点和变动后的知识点的交集，这部分可以省略不做删除添加操作
//        newPoints.removeAll( ignoreCollection );
//        collection.removeAll( ignoreCollection );
//        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey( userId );
//        for(int pointId:collection){
//            logger.info( "wrong<<<remove questionIds {},from points {}",questionId ,pointId);
//            String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey( userId,pointId );
//            String wrongCursor = RedisKnowledgeKeys.getWrongCursor(userId, pointId);
//            zSetOperations.remove(wrongSetKey,questionId+"");
//            zSetOperations.remove(wrongCursor,questionId+"");
//            long size = zSetOperations.size( wrongSetKey );
//            hashOperations.put( wrongCountKey,pointId+"",size+"" );
//
//            */
///**
//             * 缓存需要持化的key 值信息
//             * add by lijun 2018-03-20
//             *//*
//
//            questionPersistenceUtil.addWrongQuestionPersistence(wrongSetKey);
//        }
//        for(int pointId:newPoints){
//            logger.info( "wrong<<<add questionIds {},to point {}",questionId , pointId);
//            String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey( userId,pointId );
//            String wrongCursor = RedisKnowledgeKeys.getWrongCursor(userId, pointId);
//            zSetOperations.add( wrongSetKey,questionId+"",System.currentTimeMillis() );
//            zSetOperations.add( wrongCursor,questionId+"",System.currentTimeMillis() );
//            long size = zSetOperations.size( wrongSetKey );
//            hashOperations.put( wrongCountKey,pointId+"",size+"" );
//
//            */
///**
//             * 缓存需要持化的key 值信息
//             * add by lijun 2018-03-20
//             *//*
//
//            questionPersistenceUtil.addWrongQuestionPersistence(wrongSetKey);
//        }
//
//    }
//
//
//    */
///**
//     * 修改用户完成的试题和错题的统计数据
//     * @param subject    更新试题的科目
//     * @param userId        用户id
//     * @param changeMap     用户需要检查更新的试题及具体的信息
//     * @param questionFromlog     用户需要检查更新的试题
//     *//*
//
//    private void updateUserFinishedPoint(int subject, int userId, Map<Integer, List<QuestionPointChange>> changeMap, Set<Integer> questionFromlog) {
//        long start = System.currentTimeMillis();
//        //获得用户知识点ID
//        final Set<Integer> userFinishedPointId = questionPointService.findUserPoints( userId, subject );
//        if(userFinishedPointId.isEmpty()){
//            return;
//        }
//        //得到用户的试题id
//        SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
//        Set<Integer> questionIds = Sets.newHashSet();   //用户完成的试题id集合
//        Map<Integer,Integer> questionPointMap = Maps.newHashMap();    //试题id与知识点的映射关系
//        //整理用户完成试题的数据，得到完成的试题集合和试题与知识点的对应关系
//        try{
//            //遍历所有的知识点，查询用户完成的知识点下的试题id集合
//            for(int pointId:userFinishedPointId){
//                String finishedSetKey = RedisKnowledgeKeys.getFinishedSetKey( userId,pointId );
//                //得到用户的某一知识点下的所有试题id和时间戳的对应关系（只有试题ID是有用的）
//                Map<String,String> map = connection.zscan( finishedSetKey,"",(double)0,(double)System.currentTimeMillis(), QUESTION_SIZE );
//                if(map==null||map.isEmpty()){
//                    continue;
//                }
//                //某一个知识点下的所有试题id集合
//                Set<Integer> ids = map.keySet().stream().map(a->Integer.parseInt( a )).collect( Collectors.toSet() );
//                if(!CollectionUtils.isEmpty( ids)){
//                    questionIds.addAll( ids );
//                    for(int id:ids){
//                        //将试题id和知识点的对应关系记录下来（旧的对应关系）----map集合
//                        questionPointMap.put( id,pointId );
//                    }
//                }
//            }
//        }catch (Exception e){
//            logger.error( "ssdb>>>查询用户某一个知识点下的所有试题出错，userId ={}",userId );
//            e.printStackTrace();
//        }finally {
//            ssdbPooledConnectionFactory.returnConnection( connection );
//        }
//
//        //跟改动的试题id取交集，得到用户涉及改变的试题
//        Collection<Integer> changeQuestion = CollectionUtils.intersection( questionFromlog,questionIds);
//        if(CollectionUtils.isEmpty( changeQuestion )){
//            return;
//        }
//        logger.info( "用户{}需要修改的完成试题有{}道",userId,changeQuestion.size() );
//        updateFinishedInSsdb(changeQuestion,changeMap,questionPointMap,userId);
//        countFinishedPointFromSsdb( userId,subject );
//        long end = System.currentTimeMillis();
//        logger.info( "修改完成的试题，用时={}",end-start );
//    }
//
//    */
///**
//     * 用户完成数据全量重新计算一次
//     * @param userId
//     * @param subject
//     *//*
//
//    private void countFinishedPointFromSsdb(int userId, int subject) {
//        String countKey = RedisKnowledgeKeys.getFinishedCountKey( userId ); //用户完成试题的数量统计key
//        String pointKey = RedisKnowledgeKeys.getFinishedPointKey( userId ); //用户完成的知识点id统计key
//        SetOperations setOperations = redisTemplate.opsForSet();
//        Map<String,String> newPointMap = Maps.newHashMap();  //新统计的知识点下的试题数量（知识点对应试题数量）
//         //查询知识点信息，所以必须知识点缓存中知识点是最新的
//        List<QuestionPoint> pointList = questionPointService.getQuestionPoints( subject );
//        final Map<Integer,QuestionPoint> questionPointMap = Maps.newHashMap();    //用户知识点信息存储，知识点信息中无子知识点数据
//        for(QuestionPoint point:pointList){
//            questionPointMap.put( point.getId(),point );
//        }
//
//        //通过ssdb的scan模糊查询出用户涉及到的所有存储试题id的知识点key
//        final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
//        String startKey = RedisKnowledgeKeys.getFinishedSetKey( userId,0 );
//        String endKey = RedisKnowledgeKeys.getFinishedSetKey( userId,MAX_POINT_ID );
//        try{
//            //获得某一用户所有存储试题id的知识点key
//            List<String> keys = connection.zlist( startKey,endKey, POINT_SIZE);
//            Map<Integer,List<Integer>> twoLevelPoint = Maps.newHashMap();
//            for(String key :keys){
//                String[] temp = key.split( "_" );
//                String pointStr =  temp[temp.length-1] ;    //知识点id的字符串
//                //判断是否知识点在正常的知识树上，如果不在，则不统计
//                if(questionPointMap.get( Integer.parseInt( pointStr ) )==null){
//                    continue;
//                }
//                int size = connection.zsize( key );        //用户做过的某一知识点下的试题数量
//                newPointMap.put( pointStr,size+"" );       //存储知识点下试题的统计数量
//                int parentId = 0;
//                try{
//                    parentId = questionPointMap.get( Integer.parseInt( pointStr ) ).getParent();
//                }catch (Exception e){
//                    logger.error( "finished<<<知识点id有问题，无父知识点：{}->{}",pointStr,questionPointMap.get( pointStr ));
//                    e.printStackTrace();
//                }
//                //将三级知识点分类存储到他的父知识点下（twoLevelPoint）
//                if(twoLevelPoint.get( parentId )==null){
//                    List<Integer> list = Lists.newArrayList();
//                    list.add( Integer.parseInt( pointStr ) );
//                    twoLevelPoint.put( parentId,list);
//                }else{
//                    List<Integer> list = twoLevelPoint.get( parentId );
//                    list.add( Integer.parseInt( pointStr ) );
//                }
//            }
//            Map<Integer,List<Integer>> oneLevelPoint = Maps.newHashMap();
//            for(Map.Entry<Integer,List<Integer>> entry:twoLevelPoint.entrySet()){
//                String key = entry.getKey()+"";         //二级知识点id
//                if(questionPointMap.get( Integer.parseInt( key ) )==null){
//                    logger.info( "finished<<<二级知识点不存在，pointId={}",key );
//                    continue;
//                }
//                int size = 0;
//                for(int value:entry.getValue()){
//                    size += Integer.parseInt( newPointMap.get( value+"" ) );
//                }
//                if(size!=0){
//                    newPointMap.put( key,size+"" );
//                    QuestionPoint questionPoint = questionPointMap.get( Integer.parseInt( key ) );
//                    int parent = 0;
//                    try{
//                        parent = questionPoint.getParent();
//                    }catch (Exception e){
//                        logger.error( "finished<<<二级知识点有问题，无父知识点：{}->{}",key,questionPoint);
//                        e.printStackTrace();
//                    }
//                    if(oneLevelPoint.get( parent )==null){
//                        List<Integer> temp = Lists.newArrayList();
//                        temp.add( Integer.parseInt( key ) );
//                        oneLevelPoint.put( parent,temp );
//                    }else{
//                        List<Integer> temp = oneLevelPoint.get( parent );
//                        temp.add( Integer.parseInt( key ) );
//                    }
//                }
//            }
//            for(Map.Entry<Integer,List<Integer>> entry:oneLevelPoint.entrySet()){
//                String key = entry.getKey()+"";
//                int size = 0;
//                for(int value:entry.getValue()){
//                    size += Integer.parseInt( newPointMap.get( value+"" ) );
//                }
//                if(size!=0) {
//                    newPointMap.put( key, size+"" );
//                }
//            }
//            Map<String,String> oldPointMap = connection.hgetall( countKey );
//            Set<String> oldPointSet = oldPointMap.keySet();    //redis中存的某一用户的所有记录试题数据的知识点
//            Set<String> newPointSet = newPointMap.keySet();     //新统计的某一个用户的所有记录试题的有效知识点
//            Set<String> addPointSet = Sets.newHashSet();     //redis需要更新的知识点
//            addPointSet.addAll( newPointSet );
//            addPointSet.removeAll( oldPointSet );
//            for(String id:addPointSet){
//                setOperations.add( pointKey,id );
//            }
//            logger.info( "用户需要添加的知识点id={},用户需要修改知识点数据={}",addPointSet,newPointMap );
//            connection.hset( countKey,newPointMap );
//        }catch (Exception e){
//            logger.error( "ssdb>>>用户知识点下试题数量统计出错，userId={}",userId );
//            e.printStackTrace();
//        }finally {
//            ssdbPooledConnectionFactory.returnConnection( connection );
//        }
//
//    }
//
//    */
///**
//     * 以试题为单位从旧的知识点下删除，并添加到新的知识点下
//     * @param changeQuestion    用户涉及到的改动的试题id集合
//     * @param changeMap         试题id对应的所有的变动日志集合存储（试题id对应变动日志集合）
//     * @param questionPointMap  试题id对应旧知识点id的存储
//     * @param userId            用户id
//     *//*
//
//    private void updateFinishedInSsdb(Collection<Integer> changeQuestion, Map<Integer, List<QuestionPointChange>> changeMap, Map<Integer, Integer> questionPointMap, int userId) {
//        for(int questionId:changeQuestion){
//            //得到某一道试题相关的所有变动日志，然后按照生成顺序排序，
//            List<QuestionPointChange> changes = changeMap.get( questionId );
//            int maxId = 0;
//            Integer newPointId = 0;
//            for(QuestionPointChange change:changes){
//                if (change.getLevel()==2&&maxId<change.getId()) {
//                    newPointId = change.getNewPointId();
//                    maxId = change.getId();
//                }
//            }
//            if(newPointId==0){
//                continue;
//            }
//            final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
//            try{
//                logger.info( "update question {} from {} to {}",questionId,questionPointMap.get( questionId ),newPointId );
//                String oldKey = RedisKnowledgeKeys.getFinishedSetKey( userId,questionPointMap.get( questionId ) );
//                String newKey = RedisKnowledgeKeys.getFinishedSetKey( userId,newPointId);
//                connection.zdel( oldKey,String.valueOf( questionId ) );
//                connection.zset( newKey,String.valueOf( questionId ),System.currentTimeMillis() );
//            }catch (Exception e){
//                logger.error( "ssdb>>>用户试题知识点修改失败：questionId={},uid={}",questionId,userId );
//            }finally {
//                ssdbPooledConnectionFactory.returnConnection( connection );
//            }
//        }
//    }
//
//
//    public List<Integer> getNextUserIds(long start,long end) {
//        Object[] params = {start,end};
//        String sql = "select pukey from v_qbank_user limit ?,?";
//        try {
//            List<Integer> ids = jdbcTemplate.queryForList( sql,params,Integer.class );
//            return ids;
//        } catch(Exception e){
//            logger.error(">>> fetch user error,start is {},end is {}",start,end);
//            throw new RuntimeException(">>> fetch user error,start is {},end is {}",e);
//        }
//    }
//
//    public void fillPointQuestion(int userId, int subject) {
//        long start = System.currentTimeMillis();
//        HashOperations hashOperations = redisTemplate.opsForHash();
//        SetOperations setOperations = redisTemplate.opsForSet();
//        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
//        SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
//        String countQuestionKey = RedisKnowledgeKeys.getPointSummaryKey();
//        Map<String,String> countMap = hashOperations.entries( countQuestionKey );  //试题数统计
//        Map<String,Set<String>> setMap = Maps.newHashMap();
//        final Map<String,Set<String>> totalMap = Maps.newHashMap();
//        List<QuestionPoint> questionPoints = questionPointService.getQuestionPoints( subject );
//        Map<Integer,QuestionPoint> questionPointMap = Maps.newHashMap();
//        for(QuestionPoint questionPoint:questionPoints){
//            questionPointMap.put( questionPoint.getId(),questionPoint );
//        }
//        Set<Integer> set = Sets.newHashSet();
//        for(Map.Entry<String,String> entry:countMap.entrySet()){
//            int pointId = Integer.parseInt( entry.getKey() );
//            if(questionPointMap.get( pointId )==null){
//                logger.info( "知识点id:{},非有效知识点",pointId );
//                continue;
//            }
//            QuestionPoint point = questionPointMap.get( pointId );
//            if(point.getLevel()!=2){
//                continue;
//            }
//            String setQuestionKey = RedisKnowledgeKeys.getPointQuesionIds( pointId );
//            Set<String> questionIds = setOperations.members( setQuestionKey );
//            setMap.put( pointId+"",questionIds );
//            totalMap.put( pointId+"",questionIds );
//            int parent = putParent(questionPointMap,pointId,questionIds,totalMap);
//            int oneLevelPoint = putParent(questionPointMap,parent,questionIds,totalMap);
//            logger.info( "pointId={},parent={},oneLevelPoint={}",pointId,parent,oneLevelPoint );
//            set.add( pointId );
//            set.add( parent );
//            set.add( oneLevelPoint );
//        }
//        for(Map.Entry<String,Set<String>> entry:totalMap.entrySet()){
//            countMap.put( entry.getKey(),entry.getValue().size()+"" );
//        }
//        hashOperations.putAll( countQuestionKey,countMap );
//        logger.info( "需要处理的知识点总数为：{}",set.size() );
//        Set<String> pointSet = totalMap.keySet();
//        logger.info( "一二三级知识点共计:{}",pointSet.size() );
//        logger.info( "三级知识点共计:{}",setMap.size() );
//        //完成相关试题填充
//        String finishedPointKey = RedisKnowledgeKeys.getFinishedPointKey( userId );
//        for(String point:pointSet){
//            setOperations.add( finishedPointKey,point );
//        }
//        String finishedCountkey = RedisKnowledgeKeys.getFinishedCountKey( userId );
//        try{
//            connection.hset( finishedCountkey,countMap );
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            ssdbPooledConnectionFactory.returnConnection( connection );
//        }
//        batchWriteFinishedPoint(userId,setMap);
//        //错题和收藏相关填充
//        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey( userId );
//        String collectCountKey = RedisKnowledgeKeys.getCollectCountKey( userId );
//        hashOperations.putAll( wrongCountKey,countMap );
//        hashOperations.putAll( collectCountKey,countMap );
//        long total = 0;
//        for(Map.Entry<String,Set<String>> entry:totalMap.entrySet()){
//            int pointId = Integer.parseInt( entry.getKey() );
//            String wrongSetKey = RedisKnowledgeKeys.getWrongSetKey( userId,pointId );
//            String collectSetKey = RedisKnowledgeKeys.getCollectSetKey( userId,pointId );
//            zSetOperations.removeRange(wrongSetKey,0,100000 );
//            zSetOperations.removeRange(collectSetKey,0,100000 );
//            long time = System.currentTimeMillis()/1000;
//            total += entry.getValue().size();
//            hashOperations.put( wrongCountKey,pointId+"",entry.getValue().size()+"" );
//            hashOperations.put( collectCountKey,pointId+"",entry.getValue().size()+"" );
//            logger.info( "start insret redis for pointId :{}，num={}",pointId,entry.getValue().size() );
//            for(String questionId:entry.getValue()){
//                zSetOperations.add( collectSetKey,questionId,time );
//                zSetOperations.add( wrongSetKey,questionId,time );
//            }
//            logger.info( "tailed insret redis for pointId :{}",pointId );
//            */
///**
//             * 缓存收藏数据 2018-04-16
//             *//*
//
//            questionPersistenceUtil.addCollectQuestionPersistence(collectSetKey);
//            questionPersistenceUtil.addWrongQuestionPersistence(wrongSetKey);
//        }
//        logger.info( "reidis插入请求数量：{}",total );
//        long end = System.currentTimeMillis();
//        logger.info( "fill user question to point ,use time ={}",(end-start) );
//    }
//
//    private void batchWriteFinishedPoint(int userId, Map<String, Set<String>> setMap) {
//        for(Map.Entry<String,Set<String>> entry:setMap.entrySet()){
//            int pointId = Integer.parseInt( entry.getKey() );
//            String finishedSetkey = RedisKnowledgeKeys.getFinishedSetKey( userId,pointId );
//            SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
//            try{
//                connection.zclear( finishedSetkey );
//                */
///**
//                 * 缓存需要持化的key 值信息
//                 * add by lijun 2018-03-20
//                 *//*
//
//                questionPersistenceUtil.addFinishQuestionPersistence(finishedSetkey);
//            }catch (Exception e ){
//                e.printStackTrace();
//            }finally {
//                ssdbPooledConnectionFactory.returnConnection( connection );
//            }
//            long time = System.currentTimeMillis()/1000;
//            Map<String,Double> temp = Maps.newHashMap();
//            for(String questionId:entry.getValue()){
//                temp.put( questionId+"",new Double( time ) );
//                if(temp.size()==100){
//                    ssdbZset(finishedSetkey,temp);
//                    temp.clear();
//                    */
///**
//                     * 缓存需要持化的key 值信息
//                     * add by lijun 2018-03-20
//                     *//*
//
//                    questionPersistenceUtil.addFinishQuestionPersistence(finishedSetkey);
//                }
//            }
//            if(temp.size()!=0){
//                ssdbZset(finishedSetkey,temp);
//                */
///**
//                 * 缓存需要持化的key 值信息
//                 * add by lijun 2018-03-20
//                 *//*
//
//                questionPersistenceUtil.addFinishQuestionPersistence(finishedSetkey);
//            }
//        }
//    }
//
//    private void ssdbZset(String finishedSetkey, Map<String, Double> temp) {
//        SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
//        try{
//            connection.zset( finishedSetkey,temp );
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            ssdbPooledConnectionFactory.returnConnection( connection );
//        }
//        logger.info( "zseting……" );
//    }
//
//    private int putParent(Map<Integer, QuestionPoint> questionPointMap, int pointId, Set<String> questionIds,final Map<String, Set<String>> totalMap) {
//        int parent = 0;
//        try{
//            parent = questionPointMap.get( pointId ).getParent();
//        }catch (Exception e){
//            logger.error( "知识点id有问题：{}->{}",pointId,questionPointMap.get( pointId ));
//            e.printStackTrace();
//        }
//        if(parent==0){
//            logger.info( "parent=0,pointId= {}" ,pointId);
//            return -1;
//        }
//        putTolalMap(totalMap,parent,questionIds);
//        return parent;
//    }
//
//    private void putTolalMap(Map<String, Set<String>> totalMap, int parent, Set<String> questionIds) {
//        if(totalMap.get( parent+"" )==null){
//            Set<String> set = Sets.newHashSet();
//            totalMap.put( parent+"",set );
//            set.addAll( questionIds );
//        }else{
//            Set<String> set =totalMap.get( parent+"" );
//            set.addAll( questionIds );
//        }
//    }
//
//    public int getUserCount() {
//        String sql = "select count(1) from v_qbank_user";
//        int count = jdbcTemplate.queryForObject( sql,Integer.class );
//        return count;
//    }
//
//}
//*/
