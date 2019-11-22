package com.huatu.ztk.scm;

import java.util.List;

import javax.annotation.Resource;

import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.dto.Menu;
import net.sf.json.JSONArray;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huatu.ztk.scm.dao.MenuDao;

/**
 * scm_menu Controller
 * @author shaojieyue
 * @date 2013-08-16 14:38:43
 */
@Controller
@RequestMapping("/menu")
public class MenuController extends BaseController {
	@Resource
	private MenuDao menuDao;
	
	/**
	 * 查询当前用户所拥有的菜单
	 * @return
	 */
	@RequestMapping("queryMenu.do")
	@ResponseBody
	public String queryMenu(){
		List<Menu> menus = menuDao.queryMenu(this.getCurrentPassport());
		JSONArray json = JSONArray.fromObject(menus);
		return json.toString();
	}
	/**
	 * 查询某用户对所有菜单的拥有情况
	 * @param userCode
	 * @return
	 */
	@RequestMapping("queryAllMenu.do")
	@ResponseBody
	public String queryAllMenu(@RequestParam("userCode")String userCode){
		List<Menu> menus = menuDao.queryAllMenu(userCode);
		JSONArray json = JSONArray.fromObject(menus);
		return json.toString();
	}
	
	
	
}
