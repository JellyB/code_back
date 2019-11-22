package com.huatu.tiku.position.biz.constant;

public class JetCacheConstant {

    public static final String POSITION_CONTROLLER_FIND_POSITION_NAME = "p.c.position";

    public static final String POSITION_CONTROLLER_FIND_POSITION_KEY = "'.type.' + #type + '.areas.' + #areas + '.education.' + #education + " +
            "'.degree.' + #degree + '.political.' + #political + '.exp.' + #exp + " +
            "'.baseExp.' + #baseExp + '.sex.' + #sex + '.year.' + #year + " +
            "'.status.' + #status + '.page.' + #page.getPageNumber() + '.size.' + #page.getPageSize() + " +
            "'.search.' + #search + '.searchType.' + #searchType + '.nature.' + #nature";

    public static final String POSITION_SERVICE_FIND_POSITION_1_NAME = "p.c.position1";

    public static final String POSITION_SERVICE_FIND_POSITION_1_KEY = "'.type.' + #type + '.areas.' + #areas + '.education.' + #education + " +
            "'.degree.' + #degree + '.political.' + #political + '.exp.' + #exp + " +
            "'.baseExp.' + #baseExp + '.sex.' + #sex + '.year.' + #year + " +
            "'.status.' + #status";

    public static final String PURGE_CACHE_CHANNEL = "jetCache/position";

    public static final String AREA_CONTROLLER_FIND_BY_PARENT_ID_NAME = "p.c.area";

    public static final String AREA_CONTROLLER_FIND_BY_PARENT_ID_KEY = "'.areaId.' + #areaId + '.noLimit.' + #noLimit";
}
