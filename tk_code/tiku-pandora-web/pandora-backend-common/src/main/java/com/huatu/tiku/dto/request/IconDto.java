package com.huatu.tiku.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-29 2:19 PM
 **/

@NoArgsConstructor
@Getter
@Setter
public class IconDto {


    private Long id;

    @JsonProperty(value = "icon")
    private IconType type;

    private String name;

    private String url;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String info;

    private Integer sort;

    private Integer bizStatus;


    public IconDto(Long id, String type, String name, String url, String info, Integer sort, Integer bizStatus) {
        this.id = id;
        this.type = IconType.create(type);
        this.name = name;
        this.url = url;
        this.info = info;
        this.sort = sort;
        this.bizStatus = bizStatus;
    }

    @Getter
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum IconType{
        ZTYL("ZTYL", "真题演练", ""),
        MKDS("MKDS", "模考大赛", ""),
        ZNST("ZNST", "智能刷题", ""),
        XMK("XMK", "小模考", ""),
        JZGF("JZGF", "精准估分", ""),
        PGBG("PGBG", "评估报告", ""),
        CTCL("CTCL", "错题重练", ""),
        DTJL("DTJL", "答题记录", ""),
        MRTX("MRTX", "每日特训", ""),
        ZTMK("ZXMK", "专项模考", ""),
        SC("SC", "收藏", ""),
        XZ("XZ", "下载", "");

        @JsonProperty(value = "code")
        private String type;

        @JsonProperty(value = "text")
        private String name;

        @JsonIgnore
        private String url;


        IconType(String type, String name, String url) {
            this.type = type;
            this.name = name;
            this.url = url;
        }

        public static IconType create(String type){
            for (IconType iconType : values()){
                if(iconType.type.equals(type)){
                    return iconType;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"type\":\"" + type +"\""+
                    "\"name\":\"" + name + "\""+
                    "}";

        }
    }

}


