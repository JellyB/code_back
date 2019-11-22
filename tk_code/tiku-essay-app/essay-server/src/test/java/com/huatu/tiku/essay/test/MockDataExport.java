package com.huatu.tiku.essay.test;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.essay.entity.EssayMockUserMeta;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.repository.EssayMockUserMetaRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import com.huatu.tiku.essay.vo.statistics.MockUserVO;

/**
 * 模考数据导出
 * 
 * @author zhangchong
 *
 */
public class MockDataExport extends TikuBaseTest {

	@Autowired
	private EssayMockUserMetaRepository essayMockUserMetaRepository;

	@Autowired
	private EssayPaperAnswerRepository essayPaperAnswerRepository;

	@Autowired
	private RestTemplate template;

	final String userUrl = "https://ns.huatu.com/u/v1/users/batchUserInfo";

	final long paperId = 710L;

	/**
	 * 导出报名以及批改时间数据
	 */
	@Test
	public void exportEnrollInfo() {
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		Map<Integer, EssayPaperAnswer> paperAnswerMap = Maps.newHashMap();
		List<EssayMockUserMeta> mockUserList = essayMockUserMetaRepository.findByPaperIdAndStatus(paperId, 1);
		List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository
				.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus(paperId, 1, EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType(),3);
		paperAnswerList.forEach(paperAnswer -> {
			paperAnswerMap.put(paperAnswer.getUserId(), paperAnswer);
		});
		List<MockUserVO> list = Lists.newArrayList();
		mockUserList.forEach(mockUser -> {

			List<String> idsList = new ArrayList<String>();
			idsList.add(mockUser.getUserId() + "");

			HttpEntity<String> request = new HttpEntity<String>(idsList.toString(), headers);
			Map postForObject = template.postForObject(userUrl, request, Map.class);
			List<Map> userList = (List<Map>) postForObject.get("data");
			String name = (String) userList.get(0).get("name");
			String nick = (String) userList.get(0).get("nick");
			String mobile = (String) userList.get(0).get("mobile");
			Integer uid = Integer.parseInt(String.valueOf(userList.get(0).get("id")));
			EssayPaperAnswer essayPaperAnswer = paperAnswerMap.get(uid);
			MockUserVO build = MockUserVO.builder().userName(name).nick(nick).mobile(mobile)
					.enrollTime(mockUser.getGmtCreate()).build();
			if (essayPaperAnswer != null) {
				build.setStartTime(essayPaperAnswer.getCorrectDate());
			}
			list.add(build);

		});
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("申论报名信息");
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();

		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("用户名");
		cell.setCellStyle(style);

		cell = row.createCell((short) 1);
		cell.setCellValue("昵称");
		cell.setCellStyle(style);

		cell = row.createCell((short) 2);
		cell.setCellValue("手机号");
		cell.setCellStyle(style);

		cell = row.createCell((short) 3);
		cell.setCellValue("申论报名时间");
		cell.setCellStyle(style);

		cell = row.createCell((short) 4);
		cell.setCellValue("申论批改时间");
		cell.setCellStyle(style);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (int i = 0; i < list.size(); i++) {
			row = sheet.createRow((int) i + 1);
			MockUserVO userinfo = list.get(i);
			row.createCell((short) 0).setCellValue(userinfo.getUserName());
			row.createCell((short) 1).setCellValue(userinfo.getNick());
			row.createCell((short) 2).setCellValue(userinfo.getMobile());
			String dateString = formatter.format(userinfo.getEnrollTime());
			row.createCell((short) 3).setCellValue(dateString);
			String correctDate = "";
			if (userinfo.getStartTime() != null) {
				correctDate = formatter.format(userinfo.getStartTime());
			}
			row.createCell((short) 4).setCellValue(correctDate);

		}
		try {

			Date currentTime = new Date();
			String dateString = formatter.format(currentTime);

			String deskPath = "/Users/zhangchong/Desktop/申论报名数据" + dateString + ".xls";
			FileOutputStream fout = new FileOutputStream(deskPath);
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
