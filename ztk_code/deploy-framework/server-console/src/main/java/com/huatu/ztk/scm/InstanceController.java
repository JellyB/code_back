package com.huatu.ztk.scm;

import com.google.common.collect.Sets;
import com.huatu.ztk.scm.common.*;
import com.huatu.ztk.scm.dao.*;
import com.huatu.ztk.scm.dto.*;
import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.util.Constant;
import com.huatu.ztk.scm.util.DeployerCache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.zookeeper.ZookeeperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务实例管理Controller
 *
 * @author shaojieyue
 * @date 2013-07-12 14:16:19
 */
@Controller
@RequestMapping("/instance")
public class InstanceController extends BaseController {
    private final static ConcurrentHashMap<String, AtomicInteger> lock      = new ConcurrentHashMap<String, AtomicInteger>();
    private final static ConcurrentHashMap<String, StringBuffer>  message   = new ConcurrentHashMap<String, StringBuffer>();
    /**ip列表分隔符*/
    public final static  String                                   IPS_SPLIT = ",";
    @Resource
    private ServerInstanceDao smcServerInstanceDao;

    @Resource
    private InstancePermissionsDao instancePermissionsDao;

    @Resource
    private InstanceIpDao instanceIpDao;

    @Resource
    private InstanceLogDao instanceLogDao;

    @Resource
    private ProjectDao projectDao;

    @RequestMapping("create.do")
    public String initServerCreate() {
        return "init-server-instance";
    }

    @RequestMapping("instanceManager.do")
    public String instanceManager() {
        return "server-manager";
    }

    @RequestMapping("initServerPackage.do")
    public String initServerPackage() {
        return "init-server-package";
    }

    /**
     * 获取所有实例模板
     * @return
     */
    @RequestMapping("getAllInstance.do")
    @ResponseBody
    public String getAllInstance(@RequestParam("userCode") String userCode) {
        List<ServerInstance> list =  smcServerInstanceDao.getAllSmcServerInstance(userCode);
        String json = JSONArray.fromObject(list).toString();
        return json;
    }

    /**
     * 查询实例模板
     * @return
     */
    @RequestMapping("queryInstance.do")
    @ResponseBody
    public String queryInstance(@RequestParam("serverMode") String serverMode) {


        Set<ServerInstance> set = Sets.newLinkedHashSet();
        List<ServerInstance> list = null;

        //用户授权实例
        List<ServerInstance> authList = smcServerInstanceDao.querySmcServerInstance(this.getCurrentPassport());
        if(CollectionUtils.isNotEmpty(authList)){
            set.addAll(authList);
        }

        //特殊授权实例
        if(getUser().getRole() == 3) {
            list = smcServerInstanceDao.getAllSmcServerInstance();
        }else if(getUser().getRole() == 4){
            list = smcServerInstanceDao.getSmcServerByServerMode(ServerModeEnum.ONLINE.getEnvStr());
        }

        if(CollectionUtils.isNotEmpty(list)){
            set.addAll(list);
        }

        List<ServerInstance> resultList = new ArrayList();
        for (ServerInstance instance : set) {
            if (instance.getServerMode().equals(serverMode)) {
                resultList.add(instance);
            }
        }
        String json = JSONArray.fromObject(resultList).toString();
        return json;
    }

    /**
     * 根据实例模板ID获取其下边的ip
     * @param instanceId
     * @return
     */
    @RequestMapping("getIps")
    @ResponseBody
    public String getIps(@RequestParam("instanceId")String instanceId){
		long start=System.currentTimeMillis();
    	List<InstanceIp> ips = instanceIpDao.queryByInstanceId(instanceId);
		long end=System.currentTimeMillis();
		System.out.println(end-start);
		start=System.currentTimeMillis();
		ServerInstance instance = smcServerInstanceDao.getServerInstanceById(instanceId);
		end=System.currentTimeMillis();
		System.out.println(end-start);
		start=System.currentTimeMillis();
        List<InstanceLog> logs = instanceLogDao.queryByInstanceId(instanceId);
		end=System.currentTimeMillis();
		System.out.println(end-start);

        String serverName = instance.getServerName();
        JSONObject jsonResult = new JSONObject();
    	JSONArray jsonArr = new JSONArray();
    	for(InstanceIp ip:ips){
            JSONObject json = JSONObject.fromObject(ip);
    		//动态获取server的运行状态
            org.json.JSONObject status = getServerStatus(ip.getIp(), serverName);
    		json.put("stat", status.optString("stat"));
            if(status.optLong("uptime",0)>0){
                json.put("uptime", DateFormatUtils.format(status.optLong("uptime"),"yyyy-MM-dd HH:mm:ss"));
            }else {
                json.put("uptime","");
            }

            String deployer = DeployerCache.getDeployer(serverName+ip.getIp());
            if(deployer==null) deployer="";
            json.put("deployer",deployer);
    		jsonArr.add(json);
    	}
        jsonResult.put("ips",jsonArr);

        jsonArr = new JSONArray();
        for(InstanceLog log:logs){

            JSONObject json = new JSONObject();
            json.put("logMessage", log.getLogMessage());
            json.put("createBy",log.getCreateBy());
            json.put("createDate", DateFormatUtils.format(log.getCreateDate().getTime(),"yyyy-MM-dd HH:mm:ss"));
            jsonArr.add(json);
        }
        jsonResult.put("logs",jsonArr);
        return jsonResult.toString();
    }
    
    /**
     * 校验动作是否可执行
     * @param ips
     * @param serverName
     * @param action 待执行动作
     * @return
     */
    @RequestMapping("verifyAction.do")
    @ResponseBody
    public String verifyAction(@RequestParam("ips")String ips,@RequestParam("serverName")String serverName,
    		@RequestParam("action")String action){
    	String[] ipArr = ips.split(",");
    	StringBuilder sb = new StringBuilder();
    	boolean isExec = true;
		String status = "UNKONW";
    	for(String ip:ipArr){
    		//服务当前运行状态
            try{
                status = getServerStatus(ip,serverName).optString("stat");
            }catch (Exception e){

            }
    		boolean b=verify(status,action);
    		if(!b){//不可执行
    			isExec=false;
    			sb.append(ip);
    			sb.append(",");
    			sb.append(serverName);
    			sb.append(",");
    			sb.append(status);
    			sb.append("<br>");
    		}
    	}
    	JSONObject json = new JSONObject();
    	json.put("result",isExec);
    	json.put("msg", sb.toString());
    	return json.toString();
    }

    /**
     * 获取部署后的log
     * @param ip
     * @param serverName
     * @return
     */
    @RequestMapping("/getShellDeployLog")
    @ResponseBody
    public String getShellDeployLog(@RequestParam("ip")String ip,@RequestParam("serverName")String serverName){
    	//获取上次action操作的shell脚本产生的日志
        String log = OperationResult.get(serverName,ip);
        if(log==null || "".equals(log.trim())){
            log = "no log";
        }
        JSONObject json = new JSONObject();
        json.put("header", "shell.log");
        json.put("msg", log);
        return json.toString();
    }
    
    /**
     * 校验期望动作的可执行性
     * @param expectAction 期望的动作
     * @return true:可执行 false:不可执行
     */
    private boolean verify(String status,String expectAction){
		status = StringUtils.trimToNull(status);
		boolean result = true;
		if(Action.DEPLOY.getCommand().equalsIgnoreCase(expectAction)){//部署命令
			result = true;
		}else if(Action.SERVER_DELETE.getCommand().equalsIgnoreCase(expectAction)){//删除命令
			result = true;
		}else if(Action.SERVER_INIT.getCommand().equalsIgnoreCase(expectAction)){//初始化命令
			result = true;
		}else if(Action.SERVER_RESTART.getCommand().equalsIgnoreCase(expectAction)){//重启命令
			//未知状态、初始状态、删除状态不能进行重启
			if(status==null|| Stats.INITED.name().equalsIgnoreCase(status)||
					Stats.DELETED.name().equalsIgnoreCase(status)){
				result = false;
			}else{
				result = true;
			}
		}else if(Action.SERVER_START.getCommand().equalsIgnoreCase(expectAction)){
			//只有server 停止 或者 started的状态下才能执行start操作
			if(Stats.STOPPED.name().equalsIgnoreCase(status)||Stats.STARTED.name().equalsIgnoreCase(status)){
				result = true;
			}else{
				result = false;
			}
		}else if(Action.SERVER_STOP.getCommand().equalsIgnoreCase(expectAction)){//停止server
			//server启动、server存在问题才可执行stop
			if(Stats.STARTED.name().equalsIgnoreCase(status)||Stats.SUSPECT.name().equalsIgnoreCase(status)){
				result = true;
			}else{
				result = false;
			}
        }else if(Action.SERVER_DUMP.getCommand().equalsIgnoreCase(expectAction)){//DUMPserver
            //server启动、server存在问题才可执行stop
            if(Stats.STARTED.name().equalsIgnoreCase(status)||Stats.SUSPECT.name().equalsIgnoreCase(status)){
                result = true;
            }else{
                result = false;
            }
		}else if(Stats.UNKONW.name().equalsIgnoreCase(status)){
            result = true;
        }else
        {//其他情况
			result = false;
		}
		return result;
    }
    
    /**
     * 获取server的运行状态
     * @param ip 
     * @param serverName
     * @return
     */
    private org.json.JSONObject getServerStatus(String ip, String serverName) {
        String path = String.format(ZookeeperUtil.statPath_format, ip, serverName);
        org.json.JSONObject json = ZookeeperUtil.getJson(path);
        String status = null;
        if (json == null) {
            json = new org.json.JSONObject();
            status = "UNKONW";
        }
        status = json.optString("stat");
        if (status == null || "".equals(status.trim())) {
            status = "UNKONW";
        }
        json.put("stat", status);
        return json;
    }
    
    /**
     * 添加一个实例模板
     * @param mainClass
     * @param mainArgs
     * @param jvmArgs
     * @param serverIps
     * @param serverName
     * @param
     * @param projectName
     * @return
     */
    @RequestMapping("addInstance.do")
    public String addInstance(Model model,
            @RequestParam("mainClass")String mainClass,
            @RequestParam("remark")String remark,
    		@RequestParam(value = "mainArgs", required = false,defaultValue="")String mainArgs,
            @RequestParam("jvmArgs")String jvmArgs,
    		@RequestParam("serverIps")String serverIps,
            @RequestParam("serverName")String serverName,
    		@RequestParam("moduleName")String module,
            @RequestParam("projectName")String projectName,
            @RequestParam("serverMode")String serverMode){
    	boolean  result=true;
		logger.info("aafffffff");
    	if(mainArgs==null){
    		mainArgs="";
    	}
    	if(lock.get(serverName)!=null){
			model.addAttribute("errMsg","有人正在部署"+serverName+" 请稍候再试");
			return "forward:err";
    	}
        String sourcePath = module + "-dist.zip";

    	//加锁
    	lock(serverName);
    	JSONObject json = new JSONObject();
    	String userName=this.getCurrentPassport();
    	ServerInstance instance = new ServerInstance();
    	String id=UUID.randomUUID().toString();
    	instance.setId(id);
    	instance.setMainClass(mainClass);
    	instance.setMainArgs(mainArgs);
    	instance.setJvmArgs(jvmArgs);
    	instance.setServerName(serverName);
    	instance.setSourcePath(sourcePath);
    	instance.setCreateBy(userName);
    	instance.setProjectName(projectName);
    	instance.setRemark(remark);
        instance.setServerMode(serverMode);
    	logger.info(userName + " add instance "+instance);
    	boolean isSuccess=false;
    	String msg="添加实例失败";
		try {
			isSuccess = smcServerInstanceDao.insert(instance);
			
			if (isSuccess) {
				//给予修改&可执行权限
				instancePermissionsDao.insert(userName, id, 3, userName);
				String[] ips = serverIps.split(IPS_SPLIT);
				InstanceIp instanceIp = new InstanceIp();
				instanceIp.setCreateBy(userName);
				instanceIp.setInstanceId(id);
				for (String ip : ips) {// 插入ip集合
					instanceIp.setIp(ip);
					isSuccess = instanceIpDao.insert(instanceIp);
					if (!isSuccess) {
						result = false;
					}
				}
			} else {
				result = false;
			}
		} catch (DuplicateKeyException e) {
			msg="已存在serverName:"+serverName;
			result=false;
			e.printStackTrace();
		} catch (Exception e) {
			result=false;
			msg="未知异常,请查看日志";
			e.printStackTrace();
		}
		
		if(isSuccess){//初始化实例组
			try{
                logger.info("init zoo instance");
                execDeploy(instance,Action.SERVER_INIT.getCommand(),serverIps,BatchDeployType.PARALLEL.type);
                //移除锁
        		msg = unlock(serverName);
			}catch (Exception e) {
				e.printStackTrace();
				msg="初始化数据到zookeepr失败";
				result=false;
			}
		}
		json.put("msg", msg);
		logger.info("add instance result: "+json);
		
		if(result){
			this.setSuccessMsg(msg);
			return "forward:instanceManager.do";
		}else{
			this.setErrorMsg(msg);
			return "forward:create.do";
		}
    }
    
    /**
     * 更新实例信息
     * @param mainArgs
     * @param jvmArgs
     * @param sourcePath
     * @param remark
     * @param instanceId
     * @return
     */
    @RequestMapping("updateInstance.do")
    @ResponseBody
    public String updateInstance(@RequestParam("mainArgs")String mainArgs,
    		@RequestParam("jvmArgs")String jvmArgs,
    		@RequestParam("sourcePath")String sourcePath,
    		@RequestParam("remark")String remark,
    		@RequestParam("instanceId")String instanceId){
    	boolean  result=true;
    	JSONObject json = new JSONObject();
    	String userName = this.getCurrentPassport();
        ServerInstance oldInstance = smcServerInstanceDao.getServerInstanceById(instanceId);
    	ServerInstance instance = new ServerInstance();
    	String id=UUID.randomUUID().toString();
    	instance.setId(instanceId);
    	instance.setMainArgs(mainArgs);
    	instance.setJvmArgs(jvmArgs);
    	instance.setSourcePath(sourcePath);
    	instance.setCreateBy(userName);
    	instance.setRemark(remark);
    	logger.info(userName+" update instance "+instance);
    	//更新实例信息
    	boolean isSuccess = smcServerInstanceDao.update(instance);
        createLogWithMessage(oldInstance, "", InstanceLog.OPER_TYPE_ENUM.UPDATE_INFO, oldInstance.toString() + " ---> " + instance.toString());

		String ips = getIpsByInstanceId(instanceId);

		//try to update
		updateServer(ips,instanceId);

		if(isSuccess){
    		json.put("type","success" );
    		json.put("msg", "更新成功");
    	}else{
    		json.put("type", "fail");
    		json.put("msg", "更新失败");
    	}
    	logger.info(" update instance "+instance+" "+json);
    	//TODO 发送到zookeeper
    	return json.toString();
    }

	private String updateServer(String ips,String instanceId){
		ServerInstance ins=smcServerInstanceDao.getServerInstanceById(instanceId);
		JSONObject json = new JSONObject();
		logger.info(this.getCurrentPassport() + " update ips " + ips + " instanceId is "+instanceId);
		String type = null;
		lock(ins.getServerName());
		try{
			createLog(ins, ips, InstanceLog.OPER_TYPE_ENUM.SERVEROP, Action.SERVER_UPDATE.getCommand());
			execDeploy(ins,Action.SERVER_UPDATE.getCommand(),ips,BatchDeployType.PARALLEL.type);
			type="success";
			//解锁
			json.put("msg", "成功");
		}catch (Exception e) {
			json.put("msg", "异常,请查看日志");
			type="fail";
			logger.error(e.getMessage(),e);
		}finally {
			unlock(ins.getServerName());
		}
		json.put("type", type);
		return json.toString();
	}


	private String getIpsByInstanceId(String instanceId){
		List<InstanceIp> list = instanceIpDao.queryByInstanceId(instanceId);
		StringBuilder sb = new StringBuilder();
		for(InstanceIp ip:list){
			sb.append(ip.getIp());
			sb.append(",");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
    
    /**
     * 删除一个实例模板，同时删除其对一个的server
     * @param instanceId 实例模板ID
     * @return
     */
    @RequestMapping("delInstance.do")
    @ResponseBody
    public String delInstance(@RequestParam("instanceId")String instanceId){
    	
    	logger.info(this.getCurrentPassport()+" del instance template , template id is "+instanceId);
    	JSONObject json = null;

		String ips = getIpsByInstanceId(instanceId);

        ServerInstance instance = smcServerInstanceDao.getServerInstanceById(instanceId);
        User user = getUser();
        if((user.getRole() != 3 && (user.getRole() != 2 )) && instance.getServerMode().equals("online")){
//            json = new JSONObject();
//            json.put("msg", "线上实例删除请联系运维申请。");
//            return json.toString();
        }

    	String jsonStr = delIp(ips, instanceId);
    	json = JSONObject.fromObject(jsonStr);
    	if("success".equals(json.getString("type"))){
    		
    		//删除模板
    		boolean isSuccess = smcServerInstanceDao.delete(instanceId);
    		if(isSuccess){
    			json.put("msg","删除模板成功" );
    		}else{
    			json.put("msg","删除模板下server成功，删除实例模板失败" );
    			json.put("type","fail" );
    		}
    	}else{
    		json.put("msg","删除失败,请查看日志" );
    	}
    	logger.info("del instance "+instanceId+" "+json);
    	return json.toString();
    }

    private InstanceDetail toDetail(ServerInstance instance, String action, String serverIp){
        InstanceDetail detail = new InstanceDetail();
        detail.setMainArgs(instance.getMainArgs());
        detail.setMainClass(instance.getMainClass());
        detail.setType(1);
        detail.setVmArgs(instance.getJvmArgs());
        detail.setSourcePath(instance.getSourcePath());
        detail.setProjectName(instance.getProjectName());
        detail.setServerName(instance.getServerName());
        detail.setAction(action);
        detail.setServerIp(serverIp);
        detail.setServerMode(instance.getServerMode());
        return detail;
    }
    
    private String execDeploy(ServerInstance ins,String action,String ips,int batchType){
    	String result = "";
    	List<Thread> threads = new ArrayList();
    	for(String ip:ips.split(",")){
			InstanceDetail detail = toDetail(ins,action,ip);
    		detail.setSourcePath(ins.getSourcePath());
    		detail.setMainArgs(ins.getMainArgs());
    		detail.setMainClass(ins.getMainClass());
    		detail.setType(1);
    		detail.setVmArgs(ins.getJvmArgs());
    		detail.setSourcePath(ins.getSourcePath());
    		detail.setProjectName(ins.getProjectName());
    		//启动线程
    		threads.add(new LaunchThread(detail));
    	}
    	//线程阻塞执行
    	blockingThread(threads, ins.getServerName(), batchType);
    	return result;
    }
    
    /**
     * 动态添加一组实例到指定的实例模板
     * @param ips
     * @param instanceId
     * @return
     */
    @RequestMapping("addIp.do")
    @ResponseBody
    public String addIp(@RequestParam("ips")String ips,@RequestParam("instanceId")String instanceId){
    	InstanceIp instanceIp = new InstanceIp();
    	ServerInstance ins=smcServerInstanceDao.getServerInstanceById(instanceId);
    	String serverName = ins.getServerName();
    	if(lock.get(serverName)!=null){
    		return "有人正在部署"+serverName+" 请稍候再试";
    	}
    	//加锁
    	lock(serverName);
        try {
            logger.info(this.getCurrentPassport() + "add ip " + ips + " to " + instanceId);
            instanceIp.setCreateBy(getCurrentPassport());
            instanceIp.setInstanceId(instanceId);
            boolean isSuccess = true;
            for (String ip : ips.split(IPS_SPLIT)) {
                instanceIp.setIp(ip);
                try {
                    isSuccess = instanceIpDao.insert(instanceIp);
                } catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                }
            }
            if (isSuccess) {
                createLog(ins, ips, InstanceLog.OPER_TYPE_ENUM.SERVEROP, Action.SERVER_INIT.getCommand());
                execDeploy(ins, Action.SERVER_INIT.getCommand(), ips, BatchDeployType.PARALLEL.type);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            unlock(serverName);
        }

    	JSONObject result = new JSONObject();
    	//解除锁
    	String msg = unlock(serverName);
    	result.put("msg", msg);
    	return result.toString();
    }
    
    @RequestMapping("delIp.do")
    @ResponseBody
    public String delIp(@RequestParam("ips")String ips,@RequestParam("instanceId")String instanceId){
    	ServerInstance ins=smcServerInstanceDao.getServerInstanceById(instanceId);
    	String serverName = ins.getServerName();
        JSONObject json = new JSONObject();

        if(lock.get(serverName)!=null){
            json.put("msg","有人正在部署"+serverName+" 请稍候再试");
            json.put("type", "fail");
            json.put("success", false);
            return json.toString();
        }
    	//加锁
    	lock(serverName);
		logger.info(this.getCurrentPassport() + " del ips " + ips + " instanceId is "+instanceId);
		String type = null;
		try{
            for(String ip:ips.split(IPS_SPLIT)) {
                if ("".equals(ip.trim())) {
                    continue;
                }
                instanceIpDao.delete(instanceId, ip);
                logger.info("--->删除server: " + ip + " " + ins.getServerName());
            }

            createLog(ins, ips, InstanceLog.OPER_TYPE_ENUM.SERVEROP ,Action.SERVER_DELETE.getCommand());

            execDeploy(ins,Action.SERVER_DELETE.getCommand(),ips,BatchDeployType.PARALLEL.type);
            type="success";
			//解锁
			String msg = unlock(serverName);
			json.put("msg", msg);
    	}catch (Exception e) {
    		json.put("msg", "异常,请查看日志");
    		type="fail";
    		e.printStackTrace();
		}finally{
            unlock(serverName);
        }
        json.put("type", type);
    	return json.toString();
    }

    private void lock(String serverName){
    	logger.info("server "+serverName+" lock");
    	//加锁
    	lock.put(serverName, new AtomicInteger(0));
    	//初始化日志信息
    	message.put(serverName, new StringBuffer());
    	logger.info("locks = "+lock);
    }
    
    private String unlock(String serverName){
    	logger.info("server "+serverName+" unlock");
    	String logMsg="";
    	//加锁
    	lock.remove(serverName);
    	StringBuffer sb = message.remove(serverName);
    	logger.info("locks = "+lock);
    	if(sb!=null){
    		logMsg =  sb.toString();
    	}
    	return logMsg;
    }


    private void createLogWithMessage(ServerInstance instance, String serverIps, InstanceLog.OPER_TYPE_ENUM operType, String message){
        createLog(instance,serverIps,operType,null,message);
    }

    private void createLog(ServerInstance instance, String serverIps, InstanceLog.OPER_TYPE_ENUM operType,String action){
        createLog(instance,serverIps,operType,action,null);
    }

    private void createLog(ServerInstance instance, String serverIps, InstanceLog.OPER_TYPE_ENUM operType,String action,String message){

        try{
            String userName = "None";
            int userId = 0;
            User user = this.getUser();
            if(user != null){
                userName = user.getUserName();
                userId = user.getId();
            }

            InstanceLog log = new InstanceLog();
            log.setInstanceId(instance.getId());
            log.setUserId(userId);
            log.setCreateBy(userName);
            log.setOperType(operType.getOperType());
            Project project = projectDao.getByName(instance.getProjectName());
            log.setProjectId(project.getId());

            String msg = null;

            if(message == null){
                ServerModeEnum serverMode = ServerModeEnum.getServerModeEnum(instance.getServerMode());
                if(serverMode == ServerModeEnum.ONLINE) {
                    msg = String.format("操作:%s, Ips:%s, Project:%s, Tag:%s", action, serverIps, instance.getProjectName(), project.getCurrentTag());
                }
                if(serverMode == ServerModeEnum.DEVELOP) {
                    msg = String.format("操作:%s, Ips:%s, Project:%s, Branch:%s  ", action, serverIps, instance.getProjectName(),project.getDevelopBranch());
                }
                if(serverMode == ServerModeEnum.TEST) {
                    msg = String.format("操作:%s, Ips:%s, Project:%s, Branch:%s  ", action, serverIps, instance.getProjectName(),project.getTestBranch());
                }
            }else{
                msg = message;
            }
            log.setLogMessage(msg);
            instanceLogDao.insert(log);
        }catch(Exception e){
            logger.error(e.getMessage(),e);
        }

    }

    /**
     * 部署server
     * @param action
     * @param serverIps
     * @return
     */
    @RequestMapping("deploy.do")
    @ResponseBody
    public String deploy(@RequestParam("action")String action,@RequestParam("instanceId")String instanceId,
    		@RequestParam("ips")String serverIps,@RequestParam("batchType") int batchType){
        long startms = System.currentTimeMillis();
    	JSONObject json = new JSONObject();
    	json.put("action",action);
    	json.put("serverIps", serverIps);
    	JSONObject result = new JSONObject();
    	ServerInstance ins=smcServerInstanceDao.getServerInstanceById(instanceId);
    	String projectName = ins.getProjectName();
    	String serverName = ins.getServerName();

		Project project = projectDao.getByName(ins.getProjectName());

        logger.info("action="+action+" instanceId="+instanceId+" serverIps="+serverIps+" batchType="+batchType);
    	if(lock.get(serverName)!=null){
            result.put("msg", "有人正在部署"+serverName+" 请稍候再试");
            return result.toString();
    	}

    	lock(serverName);
    	logger.debug(this.getCurrentPassport()+" exec action "+action +" to "+serverIps);

        createLog(ins, serverIps, InstanceLog.OPER_TYPE_ENUM.SERVEROP ,action);

        try {
            //最后部署 人缓存
            for(String ip:serverIps.split(",")){
                DeployerCache.setDeployer(serverName+ip,this.getUserName());
            }

            execDeploy(ins,action,serverIps,batchType);

		} catch (Exception e) {
			logger.error("deploy ex",e);
		}finally {
            //释放锁
            String msg = unlock(serverName);
            msg = msg + "<br><br>总耗时："+((System.currentTimeMillis()-startms)/1000)+" 秒";
            logger.info(msg);
            result.put("msg", msg);
        }

        //发送系统变更邮件
        if(false && StringUtils.equals(action,"deploy") && ins.getServerMode().equals("online")) {
            JSONObject emailJson = new JSONObject();

			try {
				String projectHome= Constant.PROJECT_BASE_PATH+projectName;
				emailJson.put("project", project.getProjectName());
				emailJson.put("tag", project.getCurrentTag());
				emailJson.put("taginfo", GitUtil.getTag(projectHome, project.getCurrentTag()).getRemark());
			}catch (Exception e){
				logger.error(e.getMessage(),e);
			}

			emailJson.put("ips", serverIps);
			emailJson.put("logMessage", result.optString("msg"));
			emailJson.put("createBy",this.getUserName());
			emailJson.put("createDate", DateFormatUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
          //  MailUtil.sendEmail("1001", "sarowliu@sohu-inc.com", "【"+serverName + "】线上系统发布通知", emailJson.toString());
        }


    	return result.toString();
    }
    
    private void blockingThread(List<Thread> threads,String serverName,int batchType){
        int i = 0;
        for(Thread thread:threads){
            if(BatchDeployType.SERIAL.type==batchType&&i>0){
                try {
                    //每次提交sleep，达到串行的目的
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    logger.error("sleep ex", e);
                }
            }
            i++;
            thread.start();
    	}
        int count = 0;
    	while (true) {//死循环来查看线程是否执行完
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
                logger.error("sleep ex",e1);
			}
            //还有线程没有执行玩
    		if(lock.get(serverName).get()<=0){
    			break;
    		}
            if(count++>60){//超时
                break;
            }
		}
    }
    
    static class LaunchThread extends Thread{
    	private InstanceDetail detail;
    	protected Logger logger = LoggerFactory.getLogger(LaunchThread.class);
		public LaunchThread(InstanceDetail detail) {
			super();
			this.detail=detail;
		}
		@Override
		public void run() {
			String msg=null;
			try{
				lock.get(detail.getServerName()).incrementAndGet();
				logger.info(detail.getServerName()+" "+detail.getAction()+" to "+detail.getServerIp());
                Action action = Action.getAction(detail.getAction());
                if(action == Action.SERVER_INIT){//初始化server节点
                    msg = ConsoleCommand.initializeInstance(detail);
                }else {
                    msg = ConsoleCommand.deploy(detail,action.getTimeOut());
                }
			}catch (Exception e) {
				msg=detail.getServerIp()+" 执行异常";
				logger.error("proccess command ex",e);
			}finally{
				lock.get(detail.getServerName()).decrementAndGet();
			}
			
			message.get(detail.getServerName()).append("<br>").append(msg);
		}
    	
    }

    // 批量部署类型
    public enum BatchDeployType {
        //串行部署
        SERIAL(1),
        //并行部署
        PARALLEL(0);
        int type;

        BatchDeployType(int idx) {
            this.type = idx;
        }

        public int getType() {
            return type;
        }
        }

}
