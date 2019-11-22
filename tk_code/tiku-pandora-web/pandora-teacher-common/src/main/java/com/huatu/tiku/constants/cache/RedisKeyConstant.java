package com.huatu.tiku.constants.cache;


/**
 * redis相关缓存的key
 **/
public class RedisKeyConstant {

    private static Object pdfDownCacheInfo;

    /**
     * 查询所有知识点(过期时间：7天)
     *
     * @return
     */
    public static String getKnowledgeList() {
        return "pandora_knowledge_list";
    }

    /**
     * 查询地区列表(过期时间：7天)
     * pandora-server.area_tree
     * @return
     */
    public static String getAreaTreeKey() {
        return "area_tree";
    }

    /**
     * 试题ID,避免重复迁移(分布式锁)
     *
     * @return
     */
    public static String getCompositeSync2MysqlLock(int id) {
        return "composite_sync_mysql_set" + id;
    }

    public static String getPdfDownCacheInfo(Long paperId, int paperType, int exportType) {
        return String.format("pdfDownCacheInfo_%d_%d_%d",paperId.intValue(),paperType,exportType);
    }

    /**
     * 查询学院列表(过期时间：7天)
     * @return
     */
    public static String getSchoolListKey() {
        return "school_list";
    }
}
