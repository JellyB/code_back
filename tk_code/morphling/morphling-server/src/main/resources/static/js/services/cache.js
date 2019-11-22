'use strict';

angular.module("app").service('CacheManageService', ['dbUtils', '$http', '$q', function (dbUtils, $http, $q) {
    return {
        preview:function(appId){
            return dbUtils.get("/cache/preview/"+appId);
        },
        getCache:function(appId,cacheId,type,clusterId,instanceId,data){
            return dbUtils.postBody("/cache/"+appId+"/"+cacheId+"?type="+type+"&clusterId="+clusterId+"&instanceId="+instanceId,data)
        },
        delCache:function(appId,cacheId,type,clusterId,instanceId,data){
           return dbUtils.postBody("/cache/del/"+appId+"/"+cacheId+"?type="+type+"&clusterId="+clusterId+"&instanceId="+instanceId,data)
        },
        dataSources:function(){
            return dbUtils.get("/cache/dataSources");
        },
        getRedis:function(data){
            return dbUtils.get("/cache/redis",data);
        },
        delRedis:function(data){
            return dbUtils.delete("/cache/redis",data);
        }
    }
}]);