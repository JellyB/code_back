package com.huatu.tiku.essay.service.v2.fileDownload;

import com.huatu.tiku.essay.vo.resp.FileResultVO;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/1
 * @描述
 */
public interface EssayFileServiceV2 {

    FileResultVO saveFile(long questionBaseId, long paperId, long questionAnswerId, long paperAnswerId);

}