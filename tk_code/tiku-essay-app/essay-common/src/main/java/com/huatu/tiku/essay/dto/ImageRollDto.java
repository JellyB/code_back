package com.huatu.tiku.essay.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-07-10 1:31 PM
 **/
@Getter
@Setter
@NoArgsConstructor
public class ImageRollDto implements Serializable{
    private Long imageId;
    private Integer roll;

    @Builder
    public ImageRollDto(Long imageId, Integer roll) {
        this.imageId = imageId;
        this.roll = roll;
    }
}
