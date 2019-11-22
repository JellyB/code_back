package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Created by linkang on 17-7-14.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private int parent;
    private List<Position> childrens;
}
