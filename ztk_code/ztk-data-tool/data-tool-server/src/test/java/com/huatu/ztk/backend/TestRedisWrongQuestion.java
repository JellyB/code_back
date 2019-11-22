package com.huatu.ztk.backend;

import com.alibaba.fastjson.JSONObject;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 该类 用以
 * 1.导出 Redis 中存在的错误 试题节点-考题 信息
 * 2.导出 在mysql 中删除的节点 却在Redis 中依旧存在的数据
 * Created by junli on 2018/3/5.
 */
public class TestRedisWrongQuestion extends BaseTestW{

    /**
     * 类型
     */
    private static int SUBJECT_ID = 1;

    /**
     * 节点是否可用数据类型
     */
    private static int QUESTION_POINT_BB102 = 1;

    /**
     * 试题是否可用数据类型
     */
    private static int QUESTION_BB102 = 1;

    /**
     * 文件导出路径
     */
//    private static final String EXPORT_FILE_PATH = "/Users/junli/Tool/";
    private static final String EXPORT_FILE_PATH = "D:\\tmp\\";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 导出 Redis 中存在的错误"试题节点-考题"信息
     */
    @Test
    public void getRedisWrongQuestion() {

        //1.从数据库中获取所有的三级节点信息
        List<Module> moduleList = getAllModule(SUBJECT_ID, 0);
        //2.数据校验
        List<ResultData> resultDataList = new ArrayList<>();
        SetOperations opsForSet = redisTemplate.opsForSet();
        //moduleList.stream().limit(5) 如果只想测试前几条数据,使用这行代码
        moduleList.forEach(module -> {
            String pointQuestionKey = getPointQuestionIds(module.getId());
            //2-1 获取redis questionPoint下数据
            Set<String> redisSet = opsForSet.members(pointQuestionKey);
            //2-2 获取mysql questionPoint下数据
            List<String> mysqlList = getQuestionByPointIdFromMysql(module.getId());
            //2-3 获取某一个节点 存在redis 中却 不存在 mysql 中的数据
            String dataInRedisNotInMysql = redisSet.stream()
                    .filter(
                            questionId -> !mysqlList.stream().anyMatch(questionId::equals)
                    )
                    .collect(Collectors.joining(","));
            //2-4 获取某一个节点 存在mysql 中却 不存在 redis 中的数据
            String dataInMysqlNotInRedis = mysqlList.parallelStream()
                    .filter(
                            questionId -> !redisSet.stream().anyMatch(questionId::equals)
                    )
                    .collect(Collectors.joining(","));
            //添加缓存数据
            resultDataList.add(
                    new ResultData(
                            module.getId(),
                            module.getName(),
                            dataInRedisNotInMysql,
                            ""
                            // ,dataInMysqlNotInRedis
                    )
            );
        });
        //打印最终结果信息
        String jsonString = JSONObject.toJSONString(resultDataList);
        try {
            FileUtils.write(new File(EXPORT_FILE_PATH + "result_redisWrongQuestion.txt"), jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出 在mysql中删除的节点 却在Redis 中依旧存在的试题数据
     */
    @Test
    public void getUnUsedPointAndRedisQuestion() {
        QUESTION_POINT_BB102 = -1;
        List<Module> all3Modules = getAllModule(SUBJECT_ID, 2);
        //2.数据校验
        List<ResultData> resultDataList = new ArrayList<>();
        SetOperations opsForSet = redisTemplate.opsForSet();
        //moduleList.stream().limit(5) 如果只想测试前几条数据,使用这行代码
        all3Modules.forEach(module -> {
            String pointQuestionKey = getPointQuestionIds(module.getId());
            //2-1 获取redis questionPoint下数据
            Set<String> redisSet = opsForSet.members(pointQuestionKey);
            String dataInRedisNotInMysql = redisSet.stream().collect(Collectors.joining(","));
            //添加缓存数据
            resultDataList.add(
                    new ResultData(
                            module.getId(),
                            module.getName(),
                            dataInRedisNotInMysql,
                            ""
                    )
            );
        });
        //打印最终结果信息
        String jsonString = JSONObject.toJSONString(resultDataList);
        try {
            FileUtils.write(new File(EXPORT_FILE_PATH + "result_unUsedPointAndRedisQuestion.txt"), jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 移除系统中的错误的缓存数据
     */
    @Test
    public void removeWrongQuestionIdFromRedis() {
        //缓存所有的错误数据
        ArrayList<String> wrongDataList = new ArrayList<>();
        //1.从数据库中获取所有的三级节点信息
        List<Module> moduleList = getAllModule(SUBJECT_ID, 0);
        //2.数据校验
        SetOperations opsForSet = redisTemplate.opsForSet();
        //moduleList.stream().limit(5) 如果只想测试前几条数据,使用这行代码
        moduleList.forEach(module -> {
            String pointQuestionKey = getPointQuestionIds(module.getId());
            //2-1 获取redis questionPoint下数据
            Set<String> redisSet = opsForSet.members(pointQuestionKey);
            //2-2 获取mysql questionPoint下数据
            List<String> mysqlList = getQuestionByPointIdFromMysql(module.getId());
            List<String> wrongQuestionList = redisSet.stream()
                    .filter(questionId -> !mysqlList.contains(questionId))
                    .collect(Collectors.toList());
            if (null != wrongQuestionList && wrongQuestionList.size() > 0) {
                //缓存错误的节点信息
                wrongDataList.addAll(wrongQuestionList);
                //删除节点信息
                opsForSet.remove(pointQuestionKey, wrongQuestionList.toArray());
            }
        });

        //打印最终结果信息
        String collect = wrongDataList.stream().map(id -> "'" + id + "'").collect(Collectors.joining(","));
        try {
            FileUtils.write(new File(EXPORT_FILE_PATH + "result_removeWrongQuestionIdFromRedis.txt"), collect);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改节点下的长度统计信息
     */
    @Test
    public void changeQuestionPointNum() {
        //存储pointQuestionNum key值
        String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        //1.从数据库中获取所有的三级节点信息
        List<Module> moduleList = getAllModule(SUBJECT_ID, 0);
        //2.数据校验
        SetOperations opsForSet = redisTemplate.opsForSet();
        //moduleList.stream().limit(5) 如果只想测试前几条数据,使用这行代码
        moduleList.forEach(module -> {
            //1.获取现有长度信息
            String pointQuestionKey = getPointQuestionIds(module.getId());
            int size = opsForSet.size(pointQuestionKey).intValue();
            String oldSizeStr = hashOperations.get(pointSummaryKey, "" + module.getId());
            if (null != oldSizeStr && !oldSizeStr.equals("")) {
                int oldSize = Integer.valueOf(oldSizeStr);
                int reduceNum = oldSize - size;
                //数量有变化
                if (reduceNum > 0) {
                    List<String> rankPoint = getAllQuestionPointIdsBy3RankPoint(module.getId());
                    rankPoint.forEach((String questionPoint) -> {
                        //逐级修改缓存中的信息
                        String pointOldSizeStr = hashOperations.get(pointSummaryKey, questionPoint);
                        if (null != pointOldSizeStr && !pointOldSizeStr.equals("")) {
                            hashOperations.put(pointSummaryKey, questionPoint, (Integer.valueOf(pointOldSizeStr) - reduceNum) + "");
                        }
                    });
                }
            }
        });
    }


    /**
     * 统计出现有的所有节点数量(set.size)和 总数量不一致的情况
     */
    @Test
    public void getAllDifferenceNumPoint() {
        //存储pointQuestionNum key值
        String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();

        //1.从数据库中获取所有的三级节点信息
        List<Module> moduleList = getAllModule(SUBJECT_ID, 0);
        //2.数据校验
        SetOperations opsForSet = redisTemplate.opsForSet();
        //moduleList.stream().limit(5) 如果只想测试前几条数据,使用这行代码
        moduleList.forEach(module -> {
            String pointQuestionKey = getPointQuestionIds(module.getId());
            //1.获取现有长度信息
            int size = opsForSet.size(pointQuestionKey).intValue();
            String num = hashOperations.get(pointSummaryKey, "" + module.getId());
//            System.out.println("节点信息 : " + module.getName() + ",set 长度信息 : " + size + ", 统计数量 : " + num);
            if (null != num) {
                Integer oldSize = Integer.valueOf(num);
                int reduceNum = oldSize - size;

                if (reduceNum != 0) {
                    System.out.println("节点信息 : " + module.getName() + ",set 长度信息 : " + size + ", 统计数量 : " + oldSize);
                }
            }
        });
    }


    /**
     * 通过SubjectId 获取所有的考题一级节点信息
     *
     * @param subjectId 类型
     * @param initRank
     * @return 一级接待信息
     */
    private List<Module> getAllModule(int subjectId, int initRank) {
        String sql = "SELECT * FROM v_knowledge_point WHERE bl_sub=? and node_rank = " + initRank + " and BB102 = " + QUESTION_POINT_BB102 + " and BB1B1 = 1";

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, new Object[]{subjectId});
        List<Module> modules = new ArrayList<>();
        while (sqlRowSet.next()) {
            int id = sqlRowSet.getInt("PUKEY");
            String name = sqlRowSet.getString("name");
            modules.add(new Module(id, name));
        }
        return getAllRank3Points(modules, initRank);
    }

    /**
     * 通过通过当前级别节点ID 信息 查询下属所有的三级节点信息
     *
     * @param modules  初始化节点信息
     * @param initRank 初始化级别
     * @return
     */
    private List<Module> getAllRank3Points(List<Module> modules, int initRank) {
        if (modules == null || modules.size() == 0) {
            return new ArrayList<>();
        }
        if (initRank >= 2) {
            return modules;
        }
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT PUKEY,name,prev_kp FROM v_knowledge_point WHERE prev_kp in (");
        IntStream.range(0, modules.size() - 1).forEach(index -> sql.append(modules.get(index).getId()).append(","));
        sql.append(modules.get(modules.size() - 1).getId()).append(")");
        sql.append(" and BB102 = 1 and BB1B1 = " + QUESTION_POINT_BB102);
        sql.append(" order by prev_kp");

        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql.toString());
        List<Module> returnModules = new ArrayList<>();
        while (sqlRowSet.next()) {
            int id = sqlRowSet.getInt("PUKEY");
            String name = sqlRowSet.getString("name");
            //根据ID查询上级名称
            String parentKey = sqlRowSet.getString("prev_kp");
            Module parentModule = modules.stream()
                    .filter(module -> module.getId() == Integer.valueOf(parentKey))
                    .findFirst()
                    .get();

            returnModules.add(new Module(id, parentModule.getName() + "--" + name));
        }
        initRank++;
        if (returnModules == null || returnModules.size() == 0 || initRank == 2) {
            return returnModules;
        }
        return getAllRank3Points(returnModules, initRank);
    }

    /**
     * 获取Redis 中存在的考试节点信息
     *
     * @param questionPointId
     * @return
     */
    private static String getPointQuestionIds(int questionPointId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("point_qids_").append(questionPointId);
        return stringBuilder.toString();
    }

    /**
     * 知识点-试题个数 列表
     *
     * @return
     */
    private static final String getPointSummaryKey() {
        return "point_question_id";
    }

    /**
     * 根据知识点ID 从mysql 中查询该节点下的所有 questionID 信息
     *
     * @param questionPointId 知识点ID
     * @return 考题信息
     */
    private List<String> getQuestionByPointIdFromMysql(int questionPointId) {
        String sql = "select question_id from v_question_pk_r where BB102 = " + QUESTION_BB102 + " and pk_id = :id";
        Map<String, Object> parameters = new HashMap();
        parameters.put("id", questionPointId);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        List<Map<String, Object>> list = namedJdbcTemplate.queryForList(sql, parameters);

        if (null == list || list.size() == 0) {
            return new ArrayList<>();
        }
        List<String> questionIdList = list.stream()
                .map(map -> map.get("question_id").toString())
                .collect(Collectors.toList());
        return getQuestionByQuestionId(questionIdList);
    }

    /**
     * 根据 试题ID 获取试题信息
     *
     * @param questionId 试题ID
     * @return 试题信息
     */
    private List<String> getQuestionByQuestionId(List<String> questionId) {
        String sql = "select pukey,BB102 from v_obj_question where BB102 = " + QUESTION_BB102 + " and pukey in (:ids)";
        Map<String, Object> parameters = new HashMap();
        parameters.put("ids", questionId);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        List<Map<String, Object>> list = namedJdbcTemplate.queryForList(sql, parameters);
        List<String> pukeyList = list.stream().map(map -> map.get("pukey").toString()).collect(Collectors.toList());
        return pukeyList;
    }

    /**
     * 通过三级节点返回 一二级节点信息
     *
     * @param question3PointId 三级节点ID
     * @return 一级节点ID, 二级节点ID, 三级节点ID
     */
    private List<String> getAllQuestionPointIdsBy3RankPoint(int question3PointId) {
        Function<String, String> getSql = (String questionId) -> {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT PUKEY,name,prev_kp FROM v_knowledge_point WHERE PUKEY =").append(questionId);
            return sql.toString();
        };
        //获取二级的ID
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(getSql.apply(String.valueOf(question3PointId)));
        String pukey_2 = "";
        while (sqlRowSet.next()){
            pukey_2 = sqlRowSet.getString("prev_kp");
        }
        //获取一级的ID
        SqlRowSet sql2RowSet = jdbcTemplate.queryForRowSet(getSql.apply(pukey_2));
        String pukey_1 ="";
        while (sql2RowSet.next()){
            pukey_1 = sql2RowSet.getString("prev_kp");
        }
        return Arrays.asList(pukey_1, pukey_2, String.valueOf(question3PointId));
    }

    /**
     * 存储过滤完成 后的结果
     */
    private class ResultData {
        private int id;
        private String name;
        private String dataInRedisNotInMysql;
        private String dataInMysqlNotInRedis;

        public ResultData(int id, String name, String dataInRedisNotInMysql, String dataInMysqlNotInRedis) {
            this.id = id;
            this.name = name;
            this.dataInRedisNotInMysql = dataInRedisNotInMysql;
            this.dataInMysqlNotInRedis = dataInMysqlNotInRedis;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDataInRedisNotInMysql() {
            return dataInRedisNotInMysql;
        }

        public String getDataInMysqlNotInRedis() {
            return dataInMysqlNotInRedis;
        }
    }
}
