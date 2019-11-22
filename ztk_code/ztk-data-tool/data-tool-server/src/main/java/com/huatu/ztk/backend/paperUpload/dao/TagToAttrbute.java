package com.huatu.ztk.backend.paperUpload.dao;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.ModuleBean;
import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.bean.PaperErrors;
import com.huatu.ztk.backend.paper.controller.PaperController;
import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.paperModule.dao.PaperModuleDao;
import com.huatu.ztk.backend.paperUpload.bean.WordElement;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.dao.CatgoryDao;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by lenovo on 2017/4/22.
 */
@Repository
public class TagToAttrbute {
    private static final Logger logger = LoggerFactory.getLogger(TagToAttrbute.class);
    private static final BiMap<String,String> relationMap = HashBiMap.create();
    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private CatgoryDao catgoryDao;
    @Autowired
    private PaperModuleDao paperModuleDao;
    static{
        /**
         * 第一位:判断属性所属：0、试卷1、模块2、试题
         * 如果第一位是0或1则第二位设置元素的必要性，后面6位为基数标识；
         * 如果第一位是2，设置后面6位分别代表单一主观题，单一客观题，复合主观题（主干），复合客观题（主干），单一主观子题，单一客观子题
         * 后六位的设置分0,2,3；0代表不属于，2代表属于但非必要3代表属于且必要
         * 最后一位是基数标识
         */
        relationMap.put("【试卷开始】","01000000");
        relationMap.put("【试卷名称】","01000001");
        relationMap.put("【考试类型】","00000002");
        relationMap.put("【科目】","01000003");
        relationMap.put("【试卷类型】","01000004");
        relationMap.put("【区域】","01000005");
        relationMap.put("【年份】","01000006");
        relationMap.put("【总分】","01000007");
        relationMap.put("【答题时间】","01000008");
        relationMap.put("【试卷结束】","01000009");

        relationMap.put("【模块开始】","11000000");
        relationMap.put("【模块种类】","11000001");
        relationMap.put("【模块结束】","11000002");

        relationMap.put("【子试题开始】","20000330");
        relationMap.put("【子试题结束】","20000331");
        relationMap.put("【注意事项（题干要求）】","20030000");
        relationMap.put("【题源】","20202000");
        relationMap.put("【审核人】","20200020");
        relationMap.put("【知识点】","20300030");
        relationMap.put("【选项】","20300031");
        relationMap.put("【答案】","20300032");
        relationMap.put("【难度系数】","20303030");
        relationMap.put("【答案要求】","22000200");
        relationMap.put("【审题要求】","22000201");
        relationMap.put("【赋分说明】","22000202");
        relationMap.put("【解题思路】","22000203");
        relationMap.put("【扩展】","22200220");
        relationMap.put("【作者】","22200221");
        relationMap.put("【题型】","23300330");
        relationMap.put("【参考解析】","23300331");
        relationMap.put("【题干内容】","23300332");
        relationMap.put("【分数】","23300333");
        relationMap.put("【材料信息】","23033300");
        relationMap.put("【试题开始】","23333001");
        relationMap.put("【试题结束】","23333002");
        relationMap.put("【试题类型】","23333330");
        relationMap.put("【图片】","24444440");
    }


    /**
     * 得到映射关系
     * @return
     */
    public Map creatMapTree(XWPFDocument xwpf) throws Exception{
        XWPFWordExtractor extractor = new XWPFWordExtractor(xwpf);
        String text = extractor.getText();

        Map map = new HashMap();
        ArrayList<WordElement> arrayList = getWordElementList(text);
        PaperBean paper = resolvePaperElement(arrayList);        //modules字段还未赋值，应该赋予模块种类id
        List<PaperModuleBean> moduleList = paperModuleDao.findAll();
        moduleList = moduleList.stream()
                .filter(i->i.getSubject()==paper.getCatgory())
                .collect(Collectors.toList());
        Map moduleMap = new HashMap();
        for(PaperModuleBean moduleBean:moduleList){
            moduleMap.put(moduleBean.getName(),moduleBean.getId());
        }
        System.out.println();
        for(WordElement tmp:arrayList){
            int paperOrModuleFlag = Integer.parseInt(tmp.getId().substring(0,2));
            int paperNo = Integer.parseInt(tmp.getId().substring(2,tmp.getId().length()));
            if(paperOrModuleFlag<20&&paperOrModuleFlag>=10&&paperNo==1){
                String moduleName = tmp.getContent();
                if(moduleMap.get(moduleName)==null){
                    String str = "模块名称为"+moduleName+"的标签不存在";
                    logger.error(str);
                    throw new Exception(str);
                }else{
                    addModule((int)moduleMap.get(moduleName),moduleName,paper);
                }
            }



        }
        map.put("paperInfo",paper);




        return map;
    }

    /**
     *
     * @param mid  modulId
     * @param name  moduleName
     * @param paper
     * @throws BizException
     */
    public void addModule(int mid, String name,PaperBean paper) throws BizException {
        if (paper == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        List<ModuleBean> modules = paper.getModules();
        //modules为空的情况
        if (CollectionUtils.isEmpty(modules)) {
            modules = Lists.newArrayList();
        }
        //如果已经存在同一个模块id，或模块名称
        if (modules.stream().anyMatch(m -> m.getId() == mid || m.getName().equals(name))) {
            throw new BizException(PaperErrors.EXISTS_MODULE);
        }
        ModuleBean module = ModuleBean.builder()
                .id(mid)
                .name(name)
                .build();
        modules.add(module);
        paper.setModules(modules);
    }
    private PaperBean resolvePaperElement(ArrayList<WordElement> arrayList) throws Exception{
        Map mapData = new HashMap();
        //        遍历处理所有需要处理的试卷标签，并将对应关系存在mapData中
        for(WordElement tmp:arrayList){
            int paperOrModuleFlag = Integer.parseInt(tmp.getId().substring(0,2));
            if(paperOrModuleFlag<10){
                int paperNo = Integer.parseInt(tmp.getId().substring(2,tmp.getId().length()));
                if(paperNo>0&&paperNo<9){
                    mapData.put(tmp.getName(),tmp.getContent());
                }
            }else if(paperOrModuleFlag<20){
                int paperNo = Integer.parseInt(tmp.getId().substring(2,tmp.getId().length()));
                if(paperNo==1){
                    mapData.put(tmp.getName(),tmp.getContent());
                }
            }


        }
//        转化格式
        int type ;
        if (mapData.get(relationMap.inverse().get("01000004")).toString().equals("真题")) type =1;
        else type =0;
        int subjectType = getSubjectType(mapData);
        String areaName = mapData.get(relationMap.inverse().get("01000005")).toString();
        String[] areaNames = areaName.split(",|，");

        String areas = "";
        for(int i = 0;i<areaNames.length;i++){
            int areaId =getFullAreaNmae(areaNames[i]);
            if(areaId==-1){
                String str = "文档中的区域‘"+areaNames[i]+"’不存在";
                logger.error(str);
                throw new Exception(str);
            }
            areas += areaId + ",";
        }
        areas = areas.substring(0,areas.length()-1);
        return PaperBean.builder().name(mapData.get(relationMap.inverse().get("01000001")).toString())
                .year(Integer.parseInt(mapData.get(relationMap.inverse().get("01000006")).toString()))
                .catgory(subjectType).areas(areas)
                .score(Integer.parseInt(mapData.get(relationMap.inverse().get("01000007")).toString()))
                .type(type)
                .time(Integer.parseInt(mapData.get(relationMap.inverse().get("01000008")).toString()))
                .build();
    }

    private ArrayList<WordElement> getWordElementList(String text) throws Exception{
//        System.out.println(text);
        ArrayList<WordElement> weList = new ArrayList<WordElement>();
        Pattern pattern = Pattern.compile("【[\u4e00-\u9fa5]+】");
        Matcher matcher = pattern.matcher(text);
        if(!matcher.find()){
            String str = "没有与录题相关的标签";
            logger.error(str);
            throw new Exception(str);
        }
//        System.out.println("count"+matcher.);
        int i=0;
        while(matcher.find(i)){
            String name = text.substring(matcher.start(),matcher.end());
            if(relationMap.get(name)==null){
                String str = "标签‘"+name+"’不合法";
                logger.error(str);
                throw new Exception(str);
            }else{
                weList.add(WordElement.builder().name(name).id(relationMap.get(name))
                        .start(matcher.start()).end(matcher.end()).content("").build());
                i=matcher.end();
            }
            int size = weList.size();
            if(size>1){
                weList.get(size-2).setContent(text.substring(weList.get(size-2).getEnd(),weList.get(size-1).getStart()).trim());
            }

        }
        weList.get(weList.size()-1).setContent("");
        return weList;
    }


    public static final int getFullAreaNmae(String areaName) {
        Iterator var1 = AreaConstants.getAreas(1).iterator();
        Area area;
        while(var1.hasNext()) {
            area = (Area)var1.next();
            if(areaName.equals(area.getName())) {
                return area.getId();

            }
        }

        var1 = AreaConstants.getAreas(2).iterator();

        while(var1.hasNext()) {
            area = (Area)var1.next();
            List<Area> children = area.getChildren();
            Iterator var4 = children.iterator();

            while(var4.hasNext()) {
                Area child = (Area)var4.next();
                if(areaName.equals(area.getName())) {
                    return area.getId();
                }
            }
        }
        return -1;
    }
    private int getSubjectType(Map mapData) throws Exception{
        int type = -1;
        List<SubjectBean> allSubject = subjectDao.findAll();
        int catgory = -1;
        for(SubjectBean sub:allSubject){
            System.out.println(sub.getName());
            System.out.println(mapData.get(relationMap.inverse().get("01000003")).toString());
            if(sub.getName().equals(mapData.get(relationMap.inverse().get("01000003")).toString())){
                type = sub.getId();
                catgory = sub.getCatgory();
                break;
            }
        }
        if(type==-1){
            String str = "文档中的考试科目不存在";
            logger.error(str);
            throw new Exception(str);
        }
        List<SubjectBean> allCatgory = catgoryDao.findAll();
        for(SubjectBean sub:allCatgory){
            if(sub.getId()==catgory){
                if(!sub.getName().equals(mapData.get(relationMap.inverse().get("00000002")).toString())){
                    String str = "文档中的考试类型和考试科目不匹配";
                    logger.error(str);
                    throw new Exception(str);
                }
                break;
            }
        }
        return type;
    }

    /**
     * 处理试卷标签
     * @param paperTagStart
     * @param paperTagEnd
     * @param text
     * @return
     */
    private WordElement getWordElement(String paperTagStart, String paperTagEnd, String text)throws Exception {
        int start = 0;
        int end = 0;
        if(!paperTagStart.equals(paperTagEnd)){
            Pattern pattern = Pattern.compile(paperTagStart);
            Pattern pattern1 = Pattern.compile(paperTagEnd);
            Matcher matcher = pattern.matcher(text);
            start = matcher.end()+1;
            matcher = pattern1.matcher(text);
            end = matcher.start();
        }else{
            Pattern pattern = Pattern.compile(paperTagStart);
            Matcher matcher = pattern.matcher(text);
            int i = 0;
            while(matcher.find()){
                if(i==0){
                    start = matcher.end()+1;
                }
                end = matcher.start();
                i++;
            }
            if(i>2){
                String error = "标签 "+ paperTagStart +" 匹配数量不正确";
                logger.error(error);
                throw new Exception(error);
            }
        }
        String content = text.substring(start,end);
//        System.out.println("getWordElement方法得到的content="+content);
        return WordElement.builder().start(start).end(end).content(content).build();
    }




    /**
     * 得到每个段落的字符串值
     * @param paras
     * @return
     * @throws Exception
     */
    public String getXwpfText(XWPFParagraph paras) throws Exception{
        String text="";
        text = paras.getText();
        //去掉word文档中的多个换行
        text = text.replaceAll("(\\r\\n){2,}", "\r\n");
        text = text.replaceAll("(\\n){2,}", "\n");
        return text;
    }
}
