package com.huatu.tiku.essay.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-11 2:35 PM
 **/

@Getter
@Setter
@NoArgsConstructor
public class ImageSortDto implements Serializable{

    private Long answerId;
    private List<ImageSortDetailDto> imageList;

    @Builder
    public ImageSortDto(Long answerId, List<ImageSortDetailDto> imageList) {
        this.answerId = answerId;
        this.imageList = imageList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ImageSortDetailDto implements Serializable{
        private Long imageId;
        private Integer sort;

        @Builder
        public ImageSortDetailDto(Long imageId, Integer sort) {
            this.imageId = imageId;
            this.sort = sort;
        }
    }
}

