package com.huatu.ztk.paper.controller;

import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhouwei
 * @Description: 临时测试
 * @create 2017-12-08 下午2:34
 **/
public class FilterTest {

    //测试uid列表
    private static final List<Long> testUids =
            Collections.unmodifiableList(Arrays.asList(233939122L, 233883562L, 2233997281L, 7741045L, 11563817L, 233098784L));


    public   static void filterPaper(List<EstimatePaper> papers,long userId){
        if(testUids.contains(userId)) {
          return;
        }else{
            if(papers!=null){
                Iterator<EstimatePaper> it = papers.iterator();
                while(it.hasNext()){
                    EstimatePaper estimatePaper = it.next();
                    if(estimatePaper!=null && (estimatePaper.getId()==2222 || estimatePaper.getId()==2223)){
                            it.remove();
                    }
                }


            }

        }


    }
    public   static void filterFromPaper(PageBean<Paper> papers, long userId){
        if(testUids.contains(userId)) {
          return;
        }else{
            if(papers!=null && papers.getResutls() !=null ){
                Iterator<Paper> it = papers.getResutls().iterator();
                while(it.hasNext()){
                    Paper estimatePaper = it.next();
                    if(estimatePaper!=null && (estimatePaper.getId()==2222 || estimatePaper.getId()==2223)){
                            it.remove();
                    }
                }


            }

        }


    }
}
