package com.huatu.tiku.essay.test;

import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderListDto;
import com.huatu.tiku.essay.repository.EssayMockUserMetaRepository;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.service.EssayGoodsOrderService;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderDetailWrapperVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderListVO;

/**
 * 订单数据导出
 * 
 * @author zhangchong
 *
 */
public class OrderDataExport extends TikuBaseTest {

	@Autowired
	private EssayMockUserMetaRepository essayMockUserMetaRepository;

	@Autowired
	private EssayPaperAnswerRepository essayPaperAnswerRepository;
	
	@Autowired
	private EssayGoodsOrderService essayGoodsOrderService;

	@Autowired
	private RestTemplate template;

	final String userUrl = "https://ns.huatu.com/u/v1/users/batchUserInfo";
	
	final String orderList = "http://192.168.100.22:11122/e/end/v1/goodsOrder?pageSize=10000&bizStatus=1&gmtCreateBegin=2019-08-19+12:00:00&gmtCreateEnd=2019-10-22+12:00:00&orderNumStr=&page=1";

	final String orderDetail = "http://ns.huatu.com/e/end/v1/goodsOrder/61635";
	
	
	@Test
	public void detail() throws ParseException {
		Map orderRet = (Map) template.getForObject(orderDetail, Map.class);
		System.out.println(orderRet);
	}

	
	/**
	 * 导出报名以及批改时间数据
	 * @throws ParseException 
	 */
	@Test
	public void exportEnrollInfo() throws ParseException {

		List<AdminEssayGoodsOrderListVO> goodsOrderListVOS = Lists.newArrayList();

		AdminEssayGoodsOrderListDto dto = new AdminEssayGoodsOrderListDto();
		dto.setBizStatus(1);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = formatter.parse("2019-08-19 12:00:00");
		Date end = formatter.parse("2019-10-22 12:00:00");
		dto.setGmtCreateBegin(start);
		dto.setGmtCreateEnd(end);
		PageUtil<AdminEssayGoodsOrderListVO> list = essayGoodsOrderService.list(dto, 1, 10000);
		goodsOrderListVOS = (List<AdminEssayGoodsOrderListVO>) list.getResult();
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("申论订单信息");
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();

		HSSFCell cell = row.createCell((short) 0);
		cell.setCellValue("订单号");
		cell.setCellStyle(style);

		cell = row.createCell((short) 1);
		cell.setCellValue("用户名");
		cell.setCellStyle(style);

		cell = row.createCell((short) 2);
		cell.setCellValue("用户手机号");
		cell.setCellStyle(style);

		cell = row.createCell((short) 3);
		cell.setCellValue("商品名称");
		cell.setCellStyle(style);

		cell = row.createCell((short) 4);
		cell.setCellValue("批改类型");
		cell.setCellStyle(style);

		cell = row.createCell((short) 5);
		cell.setCellValue("购买量");
		cell.setCellStyle(style);

		cell = row.createCell((short) 6);
		cell.setCellValue("订单金额");
		cell.setCellStyle(style);

		cell = row.createCell((short) 7);
		cell.setCellValue("支付方式");
		cell.setCellStyle(style);

		cell = row.createCell((short) 8);
		cell.setCellValue("支付时间");
		cell.setCellStyle(style);

		cell = row.createCell((short) 9);
		cell.setCellValue("订单来源");
		cell.setCellStyle(style);

		for (int i = 0; i < goodsOrderListVOS.size(); i++) {
			row = sheet.createRow((int) i + 1);
			AdminEssayGoodsOrderListVO order = goodsOrderListVOS.get(i);
			row.createCell((short) 0).setCellValue(order.getOrderNumStr());
			row.createCell((short) 1).setCellValue(order.getName());
			row.createCell((short) 2).setCellValue(order.getMobile());

			AdminEssayGoodsOrderDetailWrapperVO detail = essayGoodsOrderService.detail(order.getId());
			row.createCell((short) 3).setCellValue(detail.getOrderDetails().get(0).getGoodsName());// 商品名称

			row.createCell((short) 4).setCellValue(detail.getOrderDetails().get(0).getGoodsTypeName());// 批改类型

			row.createCell((short) 5).setCellValue(detail.getTotalAmount());// 购买量
			row.createCell((short) 6).setCellValue(order.getRealMoney());// 金额
			row.createCell((short) 7).setCellValue(order.getPayType());// 付款方式
			String payTime = "";
			if (order.getPayTime() != null) {
				payTime = formatter.format(order.getPayTime());

			}
			row.createCell((short) 8).setCellValue(payTime);// 支付时间
			row.createCell((short) 9).setCellValue(order.getSource());// 订单来源

		}
		try {

			Date currentTime = new Date();
			String dateString = formatter.format(currentTime);

			String deskPath = "/Users/zhangchong/Desktop/申论订单数据" + dateString + ".xls";
			FileOutputStream fout = new FileOutputStream(deskPath);
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
