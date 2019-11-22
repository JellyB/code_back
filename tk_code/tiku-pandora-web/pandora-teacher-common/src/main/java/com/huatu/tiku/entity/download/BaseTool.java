package com.huatu.tiku.entity.download;

import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.Serializable;

/**
 * Created by huangqingpeng on 2018/11/6.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseTool implements Serializable {
    private String dir;

    private String suffix;

    private File file;

    private int id;

    private int subject;

    private long answerId;
    /**
     * 下一个文字段落的缩进程度（字符位）
     */
    private int indentation;

    private String moduleName;

    private boolean duplicateFlag;

    PaperInfoEnum.TypeInfo typeInfo;

    QuestionElementEnum.QuestionFieldEnum exportType;

    private boolean footerFlag = true;

    public void setIndentation(int indentation) {
        this.indentation = indentation;
    }

    public int getIndentation() {
        return indentation;
    }
}
