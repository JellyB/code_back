package com.huatu.tiku.position.biz.vo;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**分页
 * @author wangjian
 **/
@Data
public class PageVo<T> implements Serializable {

    private static final long serialVersionUID = 1524680315644972407L;

    private Collection<T>  content;//内容
    private Boolean isfirst;//第一页
    private Boolean islast;//最后一页
    private Integer number;//页数
    private Integer numberOfElements;//当页条数
    private Integer size;//每页条数
    private Long totalElements;//总条数
    private Integer totalPages;//总页数

    private PageVo(){
        this.isfirst=true;
        this.islast=false;
        this.number=0;
        this.numberOfElements=0;
        this.size=20;
        this.totalElements=0L;
        this.totalPages=0;
    }

    public PageVo(Pageable page,List<T> list){
        new PageVo();
        int contentSize = list.size();//数据数
        int size = contentSize % page.getPageSize() == 0 ? contentSize / page.getPageSize() : contentSize / page.getPageSize() + 1;
        int start = page.getOffset();
        int end = page.getPageSize() + start;
        if (start > list.size()) {
            start = list.size();
        }
        if (end > list.size()) {
            end = list.size();
        }
        this.content=list.subList(start, end);//截取内容
        this.isfirst=page.getPageNumber() == 0 ? true : false;
        this.islast=page.getPageNumber() == size - 1 ? true : false;
        this.number=page.getPageNumber();//页数
        this.numberOfElements=content.size();//本页条数
        this.size=page.getPageSize();//每页条数
        this.totalElements=Long.valueOf(contentSize);//总条数
        this.totalPages=size;//总页数
    }

    public PageVo(Page page,Collection<T> list){
        new PageVo();
        this.content=list;
        this.isfirst=page.isFirst();
        this.islast=page.isLast();
        this.number=page.getNumber();
        this.numberOfElements=page.getNumberOfElements();
        this.size=page.getSize();
        this.totalElements=page.getTotalElements();
        this.totalPages=page.getTotalPages();
    }

}
