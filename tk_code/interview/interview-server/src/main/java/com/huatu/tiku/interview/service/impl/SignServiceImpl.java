package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.entity.po.SignIn;
import com.huatu.tiku.interview.entity.vo.response.SignInfoVO;
import com.huatu.tiku.interview.entity.vo.response.SignTimeVO;
import com.huatu.tiku.interview.entity.vo.response.excel.ExcelView;
import com.huatu.tiku.interview.entity.vo.response.excel.UserSignExcelView;
import com.huatu.tiku.interview.repository.SignInRepository;
import com.huatu.tiku.interview.repository.impl.UserRepositoryImpl;
import com.huatu.tiku.interview.service.LearningReportService;
import com.huatu.tiku.interview.service.SignService;
import com.huatu.tiku.interview.util.common.PageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by x6 on 2018/5/17.
 * 学员签到信息管理
 */
@Service
public class SignServiceImpl implements SignService {
    @Autowired
    SignInRepository signInRepository;
    @Autowired
    UserRepositoryImpl userRepositoryImpl;
    @Autowired
    private LearningReportService learningReportService;
    @Override
    public PageUtil findByConditions(int page, int pageSize, String uname, long classId) {
        //分页查询符合条件的openId
        List<Map<String, Object>> maps = userRepositoryImpl.listForLimit(page, pageSize, uname, classId, -1);
        long count = userRepositoryImpl.count(uname, classId, -1);
        LinkedList<String> openIdList = new LinkedList<>();
        for(Map map :maps){
            openIdList.add(map.get("openId").toString());
        }

        //查询所有符合条件的签到记录
        List<SignIn> allSign = signInRepository.findByOpenIdInAndStatus(openIdList, 1);
        LinkedList<SignInfoVO> signInfoVOS = new LinkedList<>();
        for(Map map :maps){
            String openId = map.get("openId").toString();
            SignInfoVO signInfoVO = SignInfoVO.builder()
                    .id(new Long(map.get("id").toString()))
                    .areaId(new Long(map.get("aid").toString()))
                    .areaName(map.get("aname").toString())
                    .classId(new Long(map.get("cid").toString()))
                    .className(map.get("cname").toString())
                    .uname(map.get("uname").toString())
                    .build();
            List<String> classDateList = learningReportService.date(openId);
            LinkedList<SignTimeVO> dateList = new LinkedList<>();
            for(String date :classDateList){
                LinkedList<String> signList = new LinkedList<>();
                for(SignIn signIn:allSign){
                    if(signIn.getGmtCreate().toString().contains(date) && signIn.getOpenId().equals(openId)){
                        signList.add(signIn.getGmtCreate().toString().substring(0,signIn.getGmtCreate().toString().lastIndexOf(".")));
                    }
                }
                SignTimeVO signTimeVO = SignTimeVO.builder()
                        .date(date)
                        .signList(signList)
                        .build();
                //判断status
                //获取当前时间
                Date now = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String nowStr = sdf.format(now);
                if(CollectionUtils.isNotEmpty(signList)){
                    //正常
                    signTimeVO.setStatus(1);
                }else if(nowStr.compareTo(date) > 0){
                    //异常
                    signTimeVO.setStatus(2);
                    signList.add("打卡异常");
                    signTimeVO.setSignList(signList);
                }else{
                    //未到签到时间
                    signTimeVO.setStatus(0);
                    signList.add("暂无打卡信息");
                    signTimeVO.setSignList(signList);
                }
                dateList.add(signTimeVO);
            }
            signInfoVO.setDateList(dateList);
            signInfoVOS.add(signInfoVO);
        }
        PageUtil resultPageUtil = PageUtil.builder()
                .result(signInfoVOS)
                .total(count)
                .totalPage(0 == count % pageSize ? count / pageSize : count / pageSize + 1)
                .next(count > pageSize * page ? 1 : 0)
                .build();
        return resultPageUtil;
    }

    @Override
    public ModelAndView export(String uname, long classId) {

        long count = userRepositoryImpl.count(uname, classId, -1);
        PageUtil page = findByConditions(1, (int) count, uname, classId);
        List<SignInfoVO> list = (List<SignInfoVO>)page.getResult();


        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", "学员打卡记录"+System.currentTimeMillis());
        ExcelView excelView = new UserSignExcelView();
        return new ModelAndView(excelView, map);

    }
}
