package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 保存mongo中的临时信息
 * Author:huangqingpeng
 * Time: 2017-05-18 15:27.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Document(collection = "paper_id_base")
public class PaperTemp {
    @Id
    private int id;//id
    private int paperId;
}
