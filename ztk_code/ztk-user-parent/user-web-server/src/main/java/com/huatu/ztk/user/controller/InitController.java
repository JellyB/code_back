package com.huatu.ztk.user.controller;

import org.springframework.web.bind.annotation.RestController;

/**
 * Created by shaojieyue
 * Created time 2016-05-11 17:45
 */

@RestController
@Deprecated
public class InitController {
//    private static final Logger logger = LoggerFactory.getLogger(InitController.class);
//    ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 60, 2000, TimeUnit.HOURS,
//            new ArrayBlockingQueue<Runnable>(200000));
//
//
//    public static final String USER_MIN_IMPORT_ID = "user_min_import_id";
//    @Autowired
//    private MobileUserDao mobileUserDao;
//    @Autowired
//    private UserDao userDao;
//
//    @Resource
//    private RedisTemplate<String,String> redisTemplate;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private JdbcTemplate ucenterJdbcTemplate;
//
//    @Autowired
//    private UcenterDao ucenterDao;
//
//    @Autowired
//    private UserSessionService userSessionService;
//
//    @Autowired
//    private ActivityService activityService;
//
//    public static void main(String[] args) throws Exception {
//        loadData();
////        max();
//    }
//
//    private static void loadData() throws SQLException {
//        Connection conn = null;
//        String sql = "SELECT uname\n" +
//                "FROM v_qbank_user GROUP BY uname HAVING count(1)>1 ORDER BY PUKEY DESC ";
//        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
//        // 避免中文乱码要指定useUnicode和characterEncoding
//        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
//        // 下面语句之前就要先创建javademo数据库
//        String url = "jdbc:mysql://192.168.100.18/vhuatu?characterEncoding=UTF-8&transformedBitIsBoolean=false&tinyInt1isBit=false";
//        try {
//            // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
//            // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
//            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
//            System.out.println("成功加载MySQL驱动程序");
//            // 一个Connection代表一个数据库连接
//            conn = DriverManager.getConnection(url,"vhuatu","vhuatu_2013");
//            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
//            Statement stmt = conn.prepareStatement(sql);
//            final ResultSet resultSet = stmt.executeQuery(sql);
//            File file = new File("/home/shaojieyue/bb.txt");
//            List<String> names = new ArrayList<>();
//            while (resultSet.next()){
//                final String uname = resultSet.getString("uname");
//                names.add("\""+uname+"\",");
////                names.add(uname);
//            }
//            FileUtils.forceDelete(file);
//            FileUtils.touch(file);
//            FileUtils.writeLines(file,names);
//        } catch (SQLException e) {
//            System.out.println("MySQL操作错误");
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            conn.close();
//        }
//        System.out.println("写入完成");
//    }
//
//    public static void max() throws Exception {
////        user-session-master
//        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("user-session-master", Sets.newHashSet("192.168.100.110:16381","192.168.100.111:16381"));
//        final Jedis jedis = jedisSentinelPool.getResource();
//
//        String sql = "SELECT *" +
//                "        FROM v_qbank_user where reg_phone=? ";
//        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
//        // 避免中文乱码要指定useUnicode和characterEncoding
//        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
//        // 下面语句之前就要先创建javademo数据库
//        String url = "jdbc:mysql://192.168.100.18/vhuatu?characterEncoding=UTF-8&transformedBitIsBoolean=false&tinyInt1isBit=false";
//        // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
//        // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
//        Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
//        System.out.println("成功加载MySQL驱动程序");
//        File file = new File("/home/shaojieyue/bb.txt");
//        final List<String> readLines = FileUtils.readLines(file);
//        Connection conn = null;
//        // 一个Connection代表一个数据库连接
//        conn = DriverManager.getConnection(url,"vhuatu","vhuatu_2013");
//        for (String uname : readLines) {
////            System.out.println(uname);
//
//            try {
//
//                // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
//                final PreparedStatement stmt = conn.prepareStatement(sql);
//                stmt.setString(1,uname);
//                final ResultSet resultSet = stmt.executeQuery();
//                List<List<Object>> list =  new ArrayList<>();
//
//                while (resultSet.next()){
//                    List bean = Lists.newArrayList();
//                    final long pukey = resultSet.getLong("pukey");
//                    final long bb105 = resultSet.getLong("bb105");
//                    final long bb108 = resultSet.getLong("bb108");
//                    final String reg_phone = resultSet.getString("reg_phone");
//                    final String reg_mail = resultSet.getString("reg_mail");
//                    final String fb1z5 = resultSet.getString("fb1z5");
//                    bean.add(pukey);
//                    bean.add(bb105);
//                    bean.add(reg_phone);
//                    bean.add(reg_mail);
//                    bean.add(bb108);
//                    bean.add(fb1z5);
//                    list.add(bean);
//                }
//                long skipId = 0;
//
//                for (List<Object> objects : list) {
//                    if (jedis.exists(String.format(UserSessionService.USER_TOKEN_KEY, objects.get(0)))) {//登录
//                        skipId=(Long) objects.get(0);
//                    }
//                }
//
//
//                if (skipId == 0) {
//                    for (List<Object> objects : list) {
//                        if ((Long)objects.get(1)>0) {//登录
//                            skipId=(Long) objects.get(0);
//                        }
//                    }
//                }
//
//                if (skipId == 0) {
//                    for (List<Object> objects : list) {
//                        if ((Long)objects.get(4)>0) {//登录
//                            skipId=(Long) objects.get(0);
//                        }
//                    }
//                }
//                int count = 0;
//                if (skipId == 0) {
//                    continue;
//                }
//                String[] bb = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
//                for (List<Object> objects : list) {
//                    long id = (Long) objects.get(0);
//                    if (id == skipId) {//不处理最小的
//                        continue;
//                    }
//                    final String sql1 = "UPDATE v_qbank_user set reg_phone='"+(bb[count++]+StringUtils.substring(uname,1))+"' WHERE PUKEY=" + id;
//                    System.out.println(sql1+", uname="+uname);
//                    String userTokenKey = String.format(UserSessionService.USER_TOKEN_KEY, id);
//                    final String token = jedis.get(userTokenKey);
//                    if (StringUtils.isNotBlank(token)) {
//                        jedis.del(token);
//                        jedis.del(userTokenKey);
//                    }
//                    conn.prepareStatement(sql1).executeUpdate();
//                }
//
//            } catch (SQLException e) {
//                System.out.println("MySQL操作错误");
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//
//            }
//        }
//        conn.close();
//
//        System.out.println("处理完成");
//    }
//
//    public static void bb108() throws Exception {
////        user-session-master
//        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("user-session-master", Sets.newHashSet("192.168.100.110:16381","192.168.100.111:16381"));
//        final Jedis jedis = jedisSentinelPool.getResource();
//
//        String sql = "SELECT *" +
//                "        FROM v_qbank_user where reg_phone=? ";
//        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
//        // 避免中文乱码要指定useUnicode和characterEncoding
//        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
//        // 下面语句之前就要先创建javademo数据库
//        String url = "jdbc:mysql://192.168.100.18/vhuatu?characterEncoding=UTF-8&transformedBitIsBoolean=false&tinyInt1isBit=false";
//        // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
//        // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
//        Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
//        System.out.println("成功加载MySQL驱动程序");
//        File file = new File("/home/shaojieyue/bb.txt");
//        final List<String> readLines = FileUtils.readLines(file);
//        Connection conn = null;
//        // 一个Connection代表一个数据库连接
//        conn = DriverManager.getConnection(url,"vhuatu","vhuatu_2013");
//        for (String uname : readLines) {
////            System.out.println(uname);
//
//            try {
//
//                // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
//                final PreparedStatement stmt = conn.prepareStatement(sql);
//                stmt.setString(1,uname);
//                final ResultSet resultSet = stmt.executeQuery();
//                List<List<Object>> list =  new ArrayList<>();
//                while (resultSet.next()){
//                    List bean = Lists.newArrayList();
//                    final long pukey = resultSet.getLong("pukey");
//                    final long bb105 = resultSet.getLong("bb105");
//                    final long bb108 = resultSet.getLong("bb108");
//                    final String reg_phone = resultSet.getString("reg_phone");
//                    final String reg_mail = resultSet.getString("reg_mail");
//                    final String fb1z5 = resultSet.getString("fb1z5");
//                    bean.add(pukey);
//                    bean.add(bb105);
//                    bean.add(reg_phone);
//                    bean.add(reg_mail);
//                    bean.add(bb108);
//                    bean.add(fb1z5);
//                    list.add(bean);
//                }
//                if (list.size() !=2) {
//                    continue;
//                }
//                long deleteId = 0;
//                final List<Object> bean1 = list.get(0);
//                final List<Object> bean2 = list.get(1);
//                int unloginCount = 0;
//                if (!jedis.exists(String.format(UserSessionService.USER_TOKEN_KEY, bean1.get(0)))) {//未登录
//                    deleteId=(Long) bean1.get(0);
//                    unloginCount++;
//                }
//                if (!jedis.exists(String.format(UserSessionService.USER_TOKEN_KEY, bean2.get(0)))) {
//                    deleteId=(Long) bean2.get(0);
//                    unloginCount++;
//                }
////                if (unloginCount == 2) {//两个都未登录
////                    deleteId = 0;
////                    unloginCount = 0;
////                    if ((Long)bean1.get(1) == 0) {//bb105
////                        deleteId=(Long) bean1.get(0);
////                        unloginCount ++;
////                    }
////
////                    if ((Long)bean2.get(1) == 0) {//bb105
////                        deleteId=(Long) bean2.get(0);
////                        unloginCount ++;
////                    }
////                }
////
////                if (unloginCount == 2) {//两个都没bb108
////                    deleteId = 0;
////                    unloginCount = 0;
////                    if ((Long)bean1.get(4) == 0) {//bb105
////                        deleteId=(Long) bean1.get(4);
////                        unloginCount ++;
////                    }
////
////                    if ((Long)bean2.get(4) == 0) {//bb105
////                        deleteId=(Long) bean2.get(4);
////                        unloginCount ++;
////                    }
////                }
//
//                if (unloginCount == 2) {//两个都没bb108
//                    deleteId = Math.max((Long)bean2.get(0),(Long)bean2.get(0));
//                }
//                final Map<String, String> map = jedis.hgetAll(jedis.get(String.format(UserSessionService.USER_TOKEN_KEY, bean1.get(0))));
//                final Map<String, String> map1 = jedis.hgetAll(jedis.get(String.format(UserSessionService.USER_TOKEN_KEY, bean2.get(0))));
//                System.out.println(map);
//                System.out.println(map1);
//                if (Long.valueOf(map.get("expireTime")) > Long.valueOf(map1.get("expireTime"))) {
//                    deleteId = (Long)bean2.get(0);
//                }else {
//                    deleteId = (Long)bean1.get(0);
//                }
//
//                if (deleteId == 0) {
//                    continue;
//                }
//                final String sql1 = "UPDATE v_qbank_user set reg_phone='"+("a"+StringUtils.substring(uname,1))+"' WHERE PUKEY=" + deleteId;
//                System.out.println(sql1+", uname="+uname);
//                String userTokenKey = String.format(UserSessionService.USER_TOKEN_KEY, deleteId);
//                final String token = jedis.get(userTokenKey);
//                if (StringUtils.isNotBlank(token)) {
//                    jedis.del(token);
//                    jedis.del(userTokenKey);
//                }
//                conn.prepareStatement(sql1).executeUpdate();
//            } catch (SQLException e) {
//                System.out.println("MySQL操作错误");
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//
//            }
//        }
//        conn.close();
//
//        System.out.println("处理完成");
//    }
//
//    public static void same() throws Exception {
//
//        String sql = "SELECT *" +
//                "        FROM v_qbank_user where uname=? ";
//        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
//        // 避免中文乱码要指定useUnicode和characterEncoding
//        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
//        // 下面语句之前就要先创建javademo数据库
//        String url = "jdbc:mysql://192.168.100.18/vhuatu?characterEncoding=UTF-8&transformedBitIsBoolean=false&tinyInt1isBit=false";
//        // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
//        // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
//        Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
//        System.out.println("成功加载MySQL驱动程序");
//        File file = new File("/home/shaojieyue/bb.txt");
//        final List<String> readLines = FileUtils.readLines(file);
//        Connection conn = null;
//        // 一个Connection代表一个数据库连接
//        conn = DriverManager.getConnection(url,"vhuatu","vhuatu_2013");
//        for (String uname : readLines) {
////            System.out.println(uname);
//
//            try {
//
//                // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
//                final PreparedStatement stmt = conn.prepareStatement(sql);
//                stmt.setString(1,"app_549cf2cc");
//                final ResultSet resultSet = stmt.executeQuery();
//                List<List<Object>> list =  new ArrayList<>();
//                while (resultSet.next()){
//                    List bean = Lists.newArrayList();
//                    final long pukey = resultSet.getLong("pukey");
//                    final long bb105 = resultSet.getLong("bb105");
//                    final String reg_phone = resultSet.getString("reg_phone");
//                    final String reg_mail = resultSet.getString("reg_mail");
//                    bean.add(pukey);
//                    bean.add(bb105);
//                    bean.add(reg_phone);
//                    bean.add(reg_mail);
//                    list.add(bean);
//                }
//                if (list.size() !=2) {
//                    continue;
//                }
//                final List<Object> bean1 = list.get(0);
//                final List<Object> bean2 = list.get(1);
//                long max = Math.max((Long) bean1.get(0),(Long) bean2.get(0));
//                if (bean1.get(1).equals(bean2.get(1)) && bean1.get(2).equals(bean2.get(2)) && bean1.get(3).equals(bean2.get(3))) {
//                    final String sql1 = "DELETE FROM v_qbank_user WHERE PUKEY=" + max;
//                    System.out.println(sql1);
//                    conn.prepareStatement(sql1).executeUpdate();
//                }
//            } catch (SQLException e) {
//                System.out.println("MySQL操作错误");
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//
//            }
//        }
//        conn.close();
//
//        System.out.println("处理完成");
//    }
//
//    /**
//     * 将移动端和pc端用户整合到一起
//     */
//    @RequestMapping("/mergeUsers")
//    public void mergeUsers(HttpServletRequest httpServletRequest){
//        String remoteAddr = httpServletRequest.getRemoteAddr();
//        String url = httpServletRequest.getRequestURL().toString();
//        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
//        if(true){
//           return;
//        }
//        boolean more = true;
//        long start = 0;
//        int size = 500;
//        final String userMinImportId = redisTemplate.opsForValue().get(USER_MIN_IMPORT_ID);
//        if (StringUtils.isNoneBlank(userMinImportId)) {
//            start = Integer.parseInt(userMinImportId);
//        }
//
//        while (more){
//            final List<UserDto> userDtos = mobileUserDao.findForPage(start, size);
//            if (userDtos== null || userDtos.size()==0) {
//                logger.info("proccess user success.");
//                break;
//            }
//            for (UserDto userDto : userDtos) {
//                start = Math.max(start,userDto.getId());
//                try {
//                    executor.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                proccessUser(userDto);
//                            } catch (Exception e) {
//                                logger.error("ex", e);
//                            }
//                        }
//                    });
//                }catch (RejectedExecutionException executionException){
//                    try {
//                        Thread.sleep(20000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    executor.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                proccessUser(userDto);
//                            } catch (Exception e) {
//                                logger.error("ex", e);
//                            }
//                        }
//                    });
//
//                }
//            }
//            //报错处理点
//            redisTemplate.opsForValue().set(USER_MIN_IMPORT_ID,start+"");
//            //设置100天有效期
//            redisTemplate.expire(USER_MIN_IMPORT_ID,100, TimeUnit.DAYS);
//        }
//    }
//
//    private void proccessUser(UserDto userDto) {
//        //判断是否是无效用户
//        if (userDto.getUcenterId() == 0 && "OLKkG1KUJ9+tgZghaSNYyA==".equals(userDto.getPassword())
//                && StringUtils.trimToNull(userDto.getMobile()) ==null && StringUtils.trimToEmpty(userDto.getEmail()).endsWith("@ztk.com")) {
//            logger.info("skip user:{}",userDto);
//            return;
//        }
//        try {
//            UserDto pcUser = null;
//            //优先以用户手机号为准
//            if (StringUtils.isNoneBlank(userDto.getMobile()) && userDto.getMobile().trim().length() == 11) {
//                pcUser = userDao.find(userDto.getMobile());
//            }else if (StringUtils.isNoneBlank(userDto.getEmail())) {
//                pcUser = userDao.find(userDto.getEmail().trim());
//            }
//
//            if (pcUser == null) {//没有则根据名称查
//                pcUser= userDao.findByName(StringUtils.trimToEmpty(userDto.getName()));
//            }
//
//            if (pcUser == null) {//用户不存在,创建用户
//                final UserDto newUser = UserDto.builder()
//                        .name(userDto.getName())
//                        .nick(userDto.getName())
//                        .mobile(StringUtils.trimToEmpty(userDto.getMobile()))
//                        .email(StringUtils.trimToEmpty(userDto.getEmail()))
//                        .createTime(System.currentTimeMillis() / 1000)
//                        .subject(1)
//                        .ucenterId(userDto.getUcenterId())
//                        .status(1)
//                        .area(-9)//默认设置全国
//                        .nativepwd("")
//                        .mobileUserId(userDto.getId()).build();
//                userDao.insert(newUser);
//            }else {
//                if (pcUser.getMobileUserId() > 0) {//处理过的不进行处理
//                    return;
//                }
//                //手机号以pc为准
//                String mobile = StringUtils.trimToNull(pcUser.getMobile())!=null?StringUtils.trimToEmpty(pcUser.getMobile()):StringUtils.trimToEmpty(userDto.getMobile());
//                //邮箱以pc为准
//                String email = StringUtils.trimToNull(pcUser.getEmail())!=null?StringUtils.trimToEmpty(pcUser.getEmail()):StringUtils.trimToEmpty(userDto.getEmail());
//                userDao.updateMobileId(userDto.getId(),pcUser.getId(),userDto.getUcenterId(),mobile,email,userDto.getName());
//            }
//        }catch (Exception e){
//            logger.error("proccess fail. data={}",userDto,e);
//        }
//    }
//
//    /**
//     * 批量赠送课程
//     * @param createTime 用户注册时间
//     */
//    @RequestMapping(value = "sendFreeCourse")
//    public void sendCourse(@RequestParam (defaultValue = "-1") int createTime,
//                           @RequestParam (defaultValue = "-1") String uname,
//                           HttpServletRequest httpServletRequest,
//                           @RequestParam (defaultValue = "-1") long uid) {
//        String remoteAddr = httpServletRequest.getRemoteAddr();
//        String url = httpServletRequest.getRequestURL().toString();
//        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
//        //FB1Z5 != ''为新app用户
//        String sql = "SELECT pukey,uname FROM v_qbank_user WHERE FB1Z5 != ''";
//
//        if (createTime > 0) {
//            sql = "SELECT pukey,uname FROM v_qbank_user WHERE FB1Z5 != '' AND bb103 >=" + createTime;
//        }
//
//        if (StringUtils.isNoneBlank(uname) && !uname.equals("-1")) {
//            sql =  String.format("SELECT pukey,uname FROM v_qbank_user WHERE uname='%s'", uname);
//        }
//
//        if (uid > 0) {
//            sql = "SELECT pukey,uname FROM v_qbank_user WHERE pukey=" + uid;
//        }
//
//        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
//
//        int count = 0;
//        while (sqlRowSet.next()) {
//            long userId = sqlRowSet.getLong("pukey");
//            String username = sqlRowSet.getString("uname");
//            //简单生成终端类型
//            int terminal = userId % 2 == 0 ? CourseSourceType.IOS : CourseSourceType.ANDROID;
//            activityService.sendCourse(username,terminal, CourseType.LOGIN, CatgoryType.GONG_WU_YUAN);
//            count++;
//
//            if (count % 100 == 0)  {
//                try {
//                    Thread.sleep(3 * 1000);
//                } catch (Exception e) {
//                    logger.error("ex", e);
//                }
//            }
//        }
//
//        logger.info("send course count={}",count);
//    }
//
//
//    @RequestMapping(value = "diffUc")
//    public void diffUc(HttpServletRequest httpServletRequest) {
//        String remoteAddr = httpServletRequest.getRemoteAddr();
//        String url = httpServletRequest.getRequestURL().toString();
//        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
//        boolean more = true;
//        long start = 0;
//        int count = 1000;
//
//        while (more) {
//            String sql = "SELECT * FROM v_qbank_user ORDER by pukey DESC LIMIT ?,? ";
//            Object[] params = {
//                    start,
//                    count
//            };
//            SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql, params);
//            more = proccess1(resultSet);
//            start = start + count - 1;
//            System.out.println("--->proocess=" + start);
//        }
//
//    }
//
//    private boolean proccess1(SqlRowSet sqlRowSet) {
//        boolean moreData = false;
//        try {
//            File file = new File("/tmp/diff_uc_0914.txt");
//            FileWriter fileWriter = new FileWriter(file, true);
//            while (sqlRowSet.next()) {
//                moreData = true;
//                String localName = sqlRowSet.getString("uname");
//                String localPhone = sqlRowSet.getString("reg_phone");
//                String localEmail = sqlRowSet.getString("reg_mail");
//                Long localId = sqlRowSet.getLong("pukey");
//                String ucName = "";
//                logger.info("current uid={},uname={}", localId, localName);
//                try {
//                    if (StringUtils.isNoneBlank(localPhone)) {
//                        UcenterBind bind = ucenterDao.findBind(localPhone);
//                        if (bind != null) {
//                            ucName = bind.getUsername();
//                            if (StringUtils.isNoneBlank(ucName) && !ucName.equals(localName)) {
//                                logger.info("diff username,query by phone={} >>> local={},localId={},uc={},ucId={}",
//                                        localPhone, localName, localId, ucName, bind.getUserid());
//
//                                String s = String.format("%s,%s,%d,%s,%d\n", localPhone, localName, localId, ucName, bind.getUserid());
//                                fileWriter.write(s);
//                            }
//                        }
//                    }
//
//                    if (StringUtils.isBlank(ucName) && StringUtils.isNoneBlank(localEmail)) {
//                        UcenterMember member = ucenterDao.findMemberByEmail(localEmail);
//
//                        if (member != null) {
//                            ucName = member.getUsername();
//                            if (StringUtils.isNoneBlank(ucName) && !ucName.equals(localName)) {
//                                logger.info("diff username,query by email={} >>> local={},localId={},uc={},ucId={}",
//                                        localEmail, localName, localId, ucName, member.getUid());
//
//                                String s = String.format("%s,%s,%d,%s,%d\n", localEmail, localName, localId, ucName, member.getUid());
//                                fileWriter.write(s);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//            fileWriter.flush();
//            fileWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return moreData;
//    }
//
//
//    /**
//     * 更新与ucenter不对应的用户名
//     */
//    @RequestMapping(value = "update_diff_user")
//    public void updateDiffUser(HttpServletRequest httpServletRequest) {
//        String remoteAddr = httpServletRequest.getRemoteAddr();
//        String url = httpServletRequest.getRequestURL().toString();
//        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
//        if(true){
//            return;
//        }
//        try {
//            File file = new File("/tmp/diff_uc_0914.txt");
//            if (!file.exists()) {
//                logger.info("no such file.");
//                return;
//            }
//
//            File updateLogFile = new File("/tmp/update_log.txt");
//            File repeatedLogFile = new File("/tmp/repeated_log.txt");
//
//            FileWriter updateWriter = new FileWriter(updateLogFile);
//            FileWriter repeatedWriter = new FileWriter(repeatedLogFile);
//
//            String allRecords = FileUtils.readFileToString(file);
//            String[] records = allRecords.split("\\n");
//            for (String record : records) {
//
//                String[] result = record.split(",");
//                String account = result[0];
//                String localName = result[1];
//                long localId = Long.parseLong(result[2]);
//                String ucName = result[3];
//                long ucId = Long.parseLong(result[4]);
//
//                if (account.matches(RegexConfig.MOBILE_PHONE_REGEX)) {
//                    String phone = account;
//                    List<UcenterBind> binds = findBindsByPhone(phone);
//
//
//
//                    if (binds.size() == 1) {
//                        UcenterBind bind = binds.get(0);
//                        logger.info("update uname, localId={},oldName={},new={}",localId,localName,ucName);
//                        updateUserName(localId,ucName,ucId);
//                        String s = String.format("%s,%s,%d,%s,%d\n", phone, localName, localId, ucName, bind.getUserid());
//                        updateWriter.write(s);
//                        updateWriter.flush();
//                    } else if (binds.size() > 1){
//                        //重复记录的打印日志
//                        for (UcenterBind bind : binds) {
//                            logger.info("repeated record, localId={},phone={},username={}",localId,phone,bind.getUsername());
//                            String s = String.format("%s,%s,%d,%s,%d\n", phone, localName, localId, ucName, bind.getUserid());
//                            repeatedWriter.write(s);
//                            repeatedWriter.flush();
//                        }
//                    } else {
//                        continue;
//                    }
//
//                } else  {
//                    String email = account;
//                    List<UcenterMember> members = findMembersByEmail(email);
//
//                    //本地email重复的暂不处理
//                    if (getEmailCount(email) > 1) {
//                        logger.info("local repeated email={}" ,email);
//                        continue;
//                    }
//
//                    if (members.size() == 1) {
//                        logger.info("update uname, localId={},oldName={},new={}",localId,localName,ucName);
//                        updateUserName(localId,ucName,ucId);
//                        String s = String.format("%s,%s,%d,%s,%d\n", email, localName, localId, ucName, members.get(0).getUid());
//                        updateWriter.write(s);
//                        updateWriter.flush();
//
//                    } else if (members.size() > 1){
//                        for (UcenterMember member : members) {
//                            logger.info("repeated record, localId={},email={},username={}",localId,email,member.getUsername());
//                            String s = String.format("%s,%s,%d,%s,%d\n", email, localName, localId, ucName, member.getUid());
//                            repeatedWriter.write(s);
//                            repeatedWriter.flush();
//                        }
//                    } else {
//                        continue;
//                    }
//                }
//
//            }
//
//            repeatedWriter.close();
//            updateWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void updateUserName(long localId, String ucName, long ucId) {
//        try {
//            File diffUcidLogFile = new File("/tmp/diff_bb105_log.txt");
//            FileWriter diffUcidWriter = new FileWriter(diffUcidLogFile);
//
//
//            String sql1 = "SELECT bb105 FROM v_qbank_user WHERE PUKEY=" + localId;
//            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql1);
//            while (rowSet.next()) {
//                long bb105 = rowSet.getLong("BB105");
//
//                //bb105>0且与查询到ucid不一致的不处理
//                if (bb105 > 0 && bb105 != ucId) {
//                    logger.info("diff bb105,localId={},bb105={},ucId={}",localId,bb105,ucId);
//                    String s = String.format("%d,%s,%d\n", localId, ucName, ucId);
//                    diffUcidWriter.write(s);
//                    diffUcidWriter.flush();
//                    return;
//                }
//                diffUcidWriter.close();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            String sql = "UPDATE v_qbank_user set uname=?, bb105=? WHERE PUKEY=?";
//            Object[] params = {
//                    ucName,
//                    ucId,
//                    localId
//            };
//            jdbcTemplate.update(sql, params);
//
//            String token = userSessionService.getTokenById(localId);
//
//            if (StringUtils.isNoneBlank(token)) {
//                userService.logout(token);
//            }
//        } catch (Exception e) {
//            logger.error("update uname fail.localId={},ucName={},ucId={}", localId, ucName, ucId,e);
//
//            try {
//                FileWriter errorWriter = new FileWriter(new File("/tmp/update_error_log.txt"),true);
//                String s = String.format("%d,%s,%d\n", localId, ucName, ucId);
//                errorWriter.write(s);
//                errorWriter.flush();
//                errorWriter.close();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//    }
//
//
//    public List<UcenterMember> findMembersByEmail(String email) {
//        String sql = "SELECT * FROM uc_members WHERE email=? ";
//        String[] params = {
//                email
//        };
//        SqlRowSet sqlRowSet = ucenterJdbcTemplate.queryForRowSet(sql, params);
//
//        List<UcenterMember> results = new ArrayList<>();
//        while (sqlRowSet.next()) {
//            String username = sqlRowSet.getString("username");
//            int uid = sqlRowSet.getInt("uid");
//
//            UcenterMember ucenterMember = UcenterMember.builder()
//                    .uid(uid)
//                    .username(username)
//                    .email(email)
//                    .build();
//            results.add(ucenterMember);
//        }
//        return results;
//    }
//
//    public List<UcenterBind> findBindsByPhone(String phone) {
//        String sql = "SELECT * FROM common_user_bd WHERE phone = ?";
//        String[] params = {
//                phone
//        };
//        SqlRowSet sqlRowSet = ucenterJdbcTemplate.queryForRowSet(sql, params);
//
//        List<UcenterBind> results = new ArrayList<>();
//        while (sqlRowSet.next()) {
//            String username = sqlRowSet.getString("username");
//            int userId = sqlRowSet.getInt("userid");
//            UcenterBind ucenterBind = UcenterBind.builder()
//                    .userid(userId)
//                    .phone(phone)
//                    .username(username).build();
//            results.add(ucenterBind);
//        }
//
//        return results;
//    }
//
//    /**
//     * 相同email的账号个数
//     * @param email
//     * @return
//     */
//    public int getEmailCount(String email) {
//        String sql = "SELECT count(1) FROM v_qbank_user WHERE reg_mail=?" ;
//        Object[] params = {
//                email
//        };
//        return jdbcTemplate.queryForObject(sql, params,Integer.class);
//    }
//}
}