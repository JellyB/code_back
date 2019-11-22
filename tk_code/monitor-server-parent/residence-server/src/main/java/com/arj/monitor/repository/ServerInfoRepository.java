package com.arj.monitor.repository;

import com.arj.monitor.entity.ServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2019-01-02 下午9:15
 **/
@RepositoryRestResource(collectionResourceRel = "serverInfo",path = "serverInfo")
public interface ServerInfoRepository extends JpaRepository<ServerInfo, Long>, JpaSpecificationExecutor<ServerInfo> {

}
