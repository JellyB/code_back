package com.huatu.tiku.essay.web.controller.admin.v1;

import java.util.Arrays;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.huatu.tiku.essay.essayEnum.ArticleLevelEnum;
import com.huatu.tiku.essay.essayEnum.ArticleTypeEnum;
import com.huatu.tiku.essay.util.LogPrint;

import lombok.extern.slf4j.Slf4j;

/**
 * 文章类别和批注类型相关（议论文综合阅卷获取文章类别&要点制 划档制）
 * 
 * @author zhangchong
 *
 */
@RestController
@Slf4j
@RequestMapping("/end/v1/hand")
public class EssayArticleLevelControllerV1 {

	/**
	 * 获取文章类别
	 * 
	 * @return
	 */
	@LogPrint
	@GetMapping("/article/level")
	public Object getArticleLevel() {
		Map<Integer, String> levelMap = Maps.newHashMap();
		Arrays.asList(ArticleLevelEnum.values()).stream().forEach(x -> levelMap.put(x.getType(), x.getName()));
		return levelMap;
	}

	/**
	 * 获取划档制 要点制 枚举类别
	 * 
	 * @return
	 */
	@LogPrint
	@GetMapping("/comprehensiveCorrectType")
	public Object getComprehensiveCorrectType() {
		Map<Integer, String> articleTypeMap = Maps.newHashMap();
		Arrays.asList(ArticleTypeEnum.values()).stream().forEach(x -> articleTypeMap.put(x.getType(), x.getName()));
		return articleTypeMap;
	}
}
