### 2018-08-30 pandora 项目移植更改记录
#### 更改背景
+ `Pandora`项目重构题库管理平台，此模块中需要使用的`Knowledage`相关数据表迁移至`Pandora`数据库。
+ 原始数据库中的试题信息重新导入，部分试题信息被丢弃，知识点-试题 关联关系未被清理。
+ 原始的用户错题信息、试题收藏信息、已经完成试题信息不能丢弃，需要数据矫正。
+ 知识点-试题关联关系数据不能丢弃，需要在`redis`中重新矫正。

#### 更改操作记录
##### 项目集成更改记录
+ `knowledge-web-server`服务中新增模块信息集成`mytais`组件、`Pandora`实体模块。
+ `applicationContext`新增`Pandora`数据源。
+ `com.huatu.ztk.knowledge.common.config.PandoraDataSourceConfig` 新增`Pandora`数据源的`mybatis`配置信息。
+ `disconf`中`knowledge-web-server jdbc.properties`新增`mysql.pandora.xxx`数据库配置信息。
+ ***原始项目中在测试服务器中引入的是`qa`环境包，新项目中无此环境包，引入新环境的`dev`包***

#### 代码更改记录
##### `Dao`更改记录
+ `com.huatu.ztk.knowledge.dao.SubejctDao` 实现迁移至`com.huatu.ztk.knowledge.servicePandora.SubjectService`,
***受到影响文件`com.huatu.ztk.knowledge.service.SubjectDubboServiceImpl`***
+ `com.huatu.ztk.knowledge.dao.SubjectTreeDao` 实现迁移至`com.huatu.ztk.knowledge.servicePandora.SubjectService`,
***受到影响文件`com.huatu.ztk.knowledge.service.SubjectTreeService`***

##### `RedisKnowledgeKeys` 更改记录
+ 新增该类的适配器`RedisKnowledgeKeysAdapter`，使用单例，在获取该`Key`时判断是否需要数据补偿操作，收到影响的方法：`getWrongSetKey(long uid, int point)` 
、`getCollectSetKey(long uid, int point)`。

---
##### ADD BY 黄庆鹏
```
rocksDB+protoBuf 的修改
question字段修改
新版查询、批量查询实现
生成答题卡时替换试题ID实现
pandora试题到mongo
科目、知识点数据迁移
```