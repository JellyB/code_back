package com.huatu.ztk.scm;

import javax.annotation.Resource;

import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.dao.InstancePermissionsDao;
import com.huatu.ztk.scm.dao.UserMenuDao;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huatu.ztk.scm.dao.ProjectPermissionsDao;

/**
 * 权限管理Controller
 * @author shaojieyue
 * @date 2013-08-15 14:54:38
 */
@Controller
@RequestMapping("/permissions")
public class PermissionsManagerController extends BaseController {
	
	@Resource
	private ProjectPermissionsDao projectPermissionsDao;
	
	@Resource
	private InstancePermissionsDao instancePermissionsDao;
	
	@Resource
	private UserMenuDao userMenuDao;
	
	@RequestMapping("init.do")
	public String init(){
		return "permissions_init";
	}
	
	/**
	 * 更新用户的打包权限
	 * @param param
	 * @param userCode
	 * @return
	 */
	@RequestMapping("updatePackagePermissions.do")
	@ResponseBody
	public String updatePackagePermissions(@RequestParam("param")String param,@RequestParam("userCode")String userCode){
		//param结构 projectId=permissions&projectId=permissions&...
		String[] arr = param.split("&");
		String projectId = null;
		int permissions = 0;
		String createBy = this.getCurrentPassport();
		logger.info(createBy+" update user '"+userCode+"' package permissions.");
		JSONObject json = new JSONObject();
		String msg = "操作成功";
		for(String str:arr){
			String[] arr1 = str.split("=");
			if(arr1.length==2){
				projectId = arr1[0];
				permissions = Integer.valueOf(arr1[1]);
				try {
					projectPermissionsDao.insert(userCode, projectId, permissions, createBy);
				} catch (DuplicateKeyException e) {
					projectPermissionsDao.update(userCode, projectId, permissions);
				}
			}
		}
		try {
			json.put("msg", msg);
			json.put("type", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		logger.info(createBy+" update user '"+userCode+"' package "+json);
		return json.toString();
	}
	
	/**
	 * 更新用户部署权限
	 * @param param
	 * @param userCode
	 * @return
	 */
	@RequestMapping("updateInstancePermissions.do")
	@ResponseBody
	public String updateInstancePermissions(@RequestParam("param")String param,@RequestParam("userCode")String userCode){
		//param 结构 instanceId=permissions&instanceId=permissions...
		String[] arr = param.split("&");
		String instanceId = null;
		int permissions = 0;
		String createBy = this.getCurrentPassport();
		logger.info(createBy+" update user '"+userCode+"' instance permissions.");
		JSONObject json = new JSONObject();
		String msg = "操作成功";
		for(String str:arr){
			String[] arr1 = str.split("=");
			if(arr1.length==2){
				instanceId = arr1[0];
				permissions = Integer.valueOf(arr1[1]);
				try {
					instancePermissionsDao.insert(userCode, instanceId, permissions, createBy);
				} catch (DuplicateKeyException e) {
					instancePermissionsDao.update(userCode, instanceId, permissions);
				}
			}
		}
		try {
			json.put("msg", msg);
			json.put("type", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		logger.info(createBy+" update user '"+userCode+"' instance "+msg);
		return json.toString();
	}
	
	/**
	 * 更新用户的菜单权限
	 * @param param
	 * @param userCode
	 * @return
	 */
	@RequestMapping("updateMenuPermissions.do")
	@ResponseBody
	public String updateMenuPermissions(@RequestParam("param")String param,@RequestParam("userCode")String userCode){
		//param 结构 menuId=on&menuId=on&...
		String[] arr = param.split("&");
		String menuId = null;
		String createBy = this.getCurrentPassport();
		logger.info(createBy+" update user '"+userCode+"' menu permissions.");
		JSONObject json = new JSONObject();
		String msg = "操作成功";
		userMenuDao.deleteAllUserMenu(userCode);
		for(String str:arr){
			String[] arr1 = str.split("=");
			if(arr1.length==2){
				menuId = arr1[0];
				try {
					userMenuDao.insert(userCode, menuId, createBy);
				} catch (DuplicateKeyException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			json.put("msg", msg);
			json.put("type", "success");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		logger.info(createBy+" update user '"+userCode+"' menu "+msg);
		return json.toString();
	}
	
	
	
}
