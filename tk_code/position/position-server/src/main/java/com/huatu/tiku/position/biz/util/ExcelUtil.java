package com.huatu.tiku.position.biz.util;

/**
 * Created by duanxiangchao on 2018/5/11
 */
public class ExcelUtil {

    /**
     * @描述：是否是2003的excel，返回true是2003
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */
    public static boolean isExcel2003(String filePath)
    {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    /**
     *
     * @描述：是否是2007的excel，返回true是2007
     * @参数：@param filePath　文件完整路径
     * @参数：@return
     * @返回值：boolean
     */
    public static boolean isExcel2007(String filePath)
    {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

}
