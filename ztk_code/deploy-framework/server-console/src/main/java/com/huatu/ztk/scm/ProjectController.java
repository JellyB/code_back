package com.huatu.ztk.scm;

import com.google.common.base.Preconditions;
import com.huatu.ztk.scm.analysis.jar.JarConflictAnalysis;
import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.common.*;
import com.huatu.ztk.scm.dao.ProjectDao;
import com.huatu.ztk.scm.dao.ProjectPermissionsDao;
import com.huatu.ztk.scm.dto.Project;
import com.huatu.ztk.scm.dto.User;
import com.huatu.ztk.scm.util.Constant;
import com.huatu.ztk.scm.util.XMLUtil;
import com.huatu.ztk.scm.util.ZipUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * project管理Controller
 *
 * @author shaojieyue
 * @date 2013-07-12 17:01:40
 */
@Controller
@RequestMapping("/project")
public class ProjectController extends BaseController {
    private final static ConcurrentHashMap<String, String> lock = new ConcurrentHashMap<String, String>();
    @Resource
    private ProjectPermissionsDao projectPermissionsDao ;
    @Resource
    private ProjectDao projectDao;
    
    @RequestMapping("queryAllProject.do")
    @ResponseBody
    public String queryAllProject(@RequestParam("userCode")String userCode){
        List<Project> projects = projectDao.getAllProject(userCode);
        JSONArray json = JSONArray.fromObject(projects);
        return json.toString();
    }

    /**
     * 获取本用户可操作的project
     *
     * @return json串
     */
    @RequestMapping("queryProject.do")
    @ResponseBody
    public String queryProject() {
        List<Project> projects;
        if(getUser().getRole() == 3){
            projects = projectDao.getAllProject();
        }else {
            projects = projectDao.queryProject(getCurrentPassport());
        }
        JSONArray json = JSONArray.fromObject(projects);
        return json.toString();
    }
    
    @RequestMapping("getSingleProject.do")
    @ResponseBody
    public String getSingleProject(@RequestParam("projectId") String projectId){
    	Project project = projectDao.get(projectId);
    	if(project!=null){
    		return JSONObject.fromObject(project).toString();
    	}else{
    		return "{}";
    	}
    }

    @RequestMapping("initCreateProject.do")
    public String initCreateProject() {
        return "create_project";
    }
    
    @RequestMapping("queryTags")
    @ResponseBody
    public String queryTags(@RequestParam("projectName") String projectName){
    	String projectHome= Constant.PROJECT_BASE_PATH+projectName;
    	List<Tag> tags = null;
    	try {
    		tags= GitUtil.listTag(projectHome);
            if(tags!=null&&tags.size()>50){
                tags = tags.subList(0,49);
            }
		} catch (Exception e) {
			tags = new ArrayList(0);
			e.printStackTrace();
		}
    	JSONArray json = JSONArray.fromObject(tags);
    	return json.toString();
    }
    
    /**
     * 自动生成一个项目的tag号
     * @param projectId
     * @return
     */
    @RequestMapping("autoGenerateTag.do")
    @ResponseBody
    public String autoGenerateTag(@RequestParam("projectId")String projectId){
    	Project project = projectDao.get(projectId);
    	String newestTag = project.getNewestTag();
        if(StringUtils.trimToNull(newestTag)==null){
            newestTag =  project.getCurrentTag();
        }

        if(StringUtils.trimToNull(newestTag)==null){
    		newestTag="r_01.01.0";
    	}
    	String newTag = "";
    	JSONObject json = new JSONObject();
    	try {
			newTag = GitUtil.autoGenerateTag(Constant.PROJECT_BASE_PATH+project.getProjectName(),newestTag);
			json.put("newTag", newTag);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return json.toString();
    }

    @RequestMapping("createProject.do")
    public String createProject(@RequestParam("gitUrl") String gitUrl) {
        Preconditions.checkNotNull(gitUrl);
        String userName = this.getCurrentPassport();
        logger.info(userName+" add project "+gitUrl);
        Project project = new Project();
        project.setGitUrl(gitUrl);
        project.setCreateBy(userName);
        String msg = "添加成功";
        boolean result = false;
        try {
            project.setProjectName(getProjectNameFromGitUrl(gitUrl));
            //初始化project
            boolean res= ConsoleCommand.initProject(Constant.bin_home,project.getGitUrl(),project.getProjectName());
            if(res){
            	result = projectDao.insert(project);
            	if (!result) {
            		msg = "添加失败";
            	}else{
            		project = projectDao.getByGitUrl(gitUrl);
            		//操作人给予写&可执行权限
            		projectPermissionsDao.insert(userName, project.getId()+"", 3, userName);
            	}
            }else{
            	msg = "初始化失败";
            }
            
        } catch (Exception e) {
            result = false;
            msg = "添加出现异常,请查看日志";
            e.printStackTrace();
        }
        logger.info("add project "+gitUrl+" result: "+msg);
        if(result){
        	this.setSuccessMsg(msg);
        	return "forward:/instance/initServerPackage.do";
        }else{
        	this.setErrorMsg(msg);
        	return "create_project";
        }
        
    }

    @RequestMapping(value = "package.do")
    @ResponseBody
    public String compileAndPackage(@RequestParam("branch") String branch,
    								@RequestParam("projectId") String projectId,
                                    @RequestParam("environment") String environment,
                                    @RequestParam(value = "tag", required = false) String tag,
                                    @RequestParam("gitUrl") String gitUrl,
                                    @RequestParam("projectName") String projectName,
                                    @RequestParam("moduleName") String moduleName,
                                    @RequestParam(value = "remark", required = false) String remark,
                                    @RequestParam(value = "updateDependency" , required = false) String updateDependency) {
        Preconditions.checkNotNull(branch);
        Preconditions.checkNotNull(environment);
        Preconditions.checkNotNull(gitUrl);
        Preconditions.checkNotNull(moduleName);
        OperationResult.putByProjectName(moduleName, "打包开始");

        String userName = this.getCurrentPassport();
        boolean update = false;
        //更新全部依赖
        if("on".equalsIgnoreCase(updateDependency)){
        	update = true ;
        }
        String message = "";
        remark =  moduleName + "##"+this.getUserName()+"##" +remark+"##"+branch;
        boolean isLocked = lock.get(gitUrl) != null;

        logger.info("package.go:" + remark);

        //部署权限控制
        User user = getUser();
//        if((user.getRole() != 3 && user.getRole() != 2 ) && "online".equals(environment)){
//            return "线上编译权限已经回收,请联系运维及各组leader申请发布。";
//        }

        if (isLocked) {
            return userName + "正在打包部署";
        }
        try {

            lock.put(gitUrl, gitUrl + userName);
            int type = 2;
            //线上环境
            if ("online".equals(environment)) {
                type = 1;
            }
            
            PackDto pack = new PackDto(type, gitUrl, projectName, moduleName, environment, tag, branch,remark,update);
            logger.info(userName+" package "+pack);

            message = ConsoleCommand.pack(moduleName,Constant.bin_home, pack);

            String packagePath = "/data/projects/deploy_scm_dist/"+moduleName+"-dist.zip";
            String destDir = "/tmp/packages/"+moduleName+"/"+environment;
            //解压文件
            boolean isUnzip = ZipUtil.unzipFiles(packagePath,destDir);

            if(isUnzip){//解压成功
                //冲突的类
                String[] conflicts = JarConflictAnalysis.analysis(destDir+"/lib");
                //删除解压目录
                FileUtils.forceDelete(new File(destDir));
                StringBuilder sb = new StringBuilder(message);
                //jar包冲突
                if(conflicts.length>0){
                    sb.append("\r\n");
                    sb.append("---------------------------------------------------------------------------------------------------------");sb.append("\r\n");
                    sb.append("jar包冲突警告\r\n");
                    for(String jarName:conflicts){
                        sb.append(jarName);
                        sb.append("\r\n");
                    }
                }
                message = sb.toString();
            }

            if("online".equals(environment)&&tag!=null&&!"".equals(tag.trim())){//更新tag信息
            	projectDao.updateAllTag(projectId, tag);
            }

            if("develop".equals(environment)){
                projectDao.updateDevelopBranch(projectId,branch);
            }

            if("test".equals(environment)){
                projectDao.updateTestBranch(projectId,branch);
            }

        } catch (Exception e) {
            message = "部署时出现错误,请查看日志";
            e.printStackTrace();
        } finally {
            lock.remove(gitUrl);
            OperationResult.putByProjectName(moduleName, message);
        }
        OperationResult.endLog(moduleName);
        return message;
    }


    /**
     * 版本回退
     * @param projectName
     * @param moduleName
     * @param tag
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    @RequestMapping("tagBack.do")
    @ResponseBody
    public String tagBack(@RequestParam("projectName")String projectName
    					,@RequestParam("moduleName")String moduleName
    					,@RequestParam("tag")String tag) throws IOException, GitAPIException{
    	Preconditions.checkNotNull(moduleName);
    	Preconditions.checkNotNull(tag);
    	String projectHome=Constant.PROJECT_BASE_PATH+projectName;
    	boolean exist = GitUtil.isExistTag(projectHome, tag);
    	String message = null;
    	logger.info(this.getCurrentPassport()+" tag back project="+projectName+" module="+moduleName+" tag="+tag);
    	if(!exist){
    		message = "tag "+tag+"不存在";
    	}else{
            org.json.JSONObject json = ConsoleCommand.tagBack(Constant.bin_home,moduleName, tag);
    		if(json.getBoolean("result")){
                Project project = projectDao.getByName(projectName);
                projectDao.updateCurrentTag(project.getId()+"",tag);
    			message = projectName + " 回退成功，当前Tag："+ tag +",请部署服务以使回退生效";
    		}else{
    			message = json.getString("msg");
    		}
    	}
    	return message;
    }
    
    /**
     * 解析pom文件包含的模块列表
     * @param project
     * @return
     */
    @RequestMapping("queryProjectModules.do")
    @ResponseBody
    public String queryProjectModules(@RequestParam("project")String project){
        JSONObject json = new JSONObject();
        //1:操作成功 0:操作失败
        int status = 0;
    	String file = Constant.PROJECT_BASE_PATH+project+"/pom.xml";
    	Document doc = null;
		try {
			doc = XMLUtil.read(file);
            XPath x=doc.createXPath("/aa:project//aa:modules//aa:module");
            Map map = new HashMap();
            //添加命名空间
            map.put("aa", "http://maven.apache.org/POM/4.0.0");
            x.setNamespaceURIs(map);
            List<Node> nodes = x.selectNodes(doc);
            JSONArray array = new JSONArray();
            for(Node node:nodes){
                array.add(node.getText());
            }
            json.put("data",array);
		} catch (Exception e) {
            status = 1;
            json.put("msg","解析pom文件出错,pom文件路径: "+file);
			logger.error("pase pom ex",e);
		}
        json.put("status",status);
		return json.toString();
    }
    
    /**
     * 从git地址中解析其project名称
     * @param gitUrl
     * @return
     */
    private String getProjectNameFromGitUrl(String gitUrl) {
       // gitUrl = gitUrl.replaceAll("http://", "");
         gitUrl = gitUrl.replaceAll("\\.git", "");
        int p=gitUrl.lastIndexOf("/")+1;
        gitUrl=gitUrl.substring(p);

       // Preconditions.checkArgument(gitUrl.contains("admin@code.k.sohuno.com"), "git server must be 'deploy@code.k.sohuno.com'");
       // String projectName = gitUrl.replaceAll("deploy@code.k.sohuno.com/", "");
        return gitUrl;
    }
}
