package com.huatu.naga.dao.es.api;

import com.huatu.naga.dao.es.entity.ExceptionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author hanchao
 * @date 2018/1/23 16:08
 */
public interface ExceptionDocumentDao extends ElasticsearchRepository<ExceptionDocument,String> {
}
