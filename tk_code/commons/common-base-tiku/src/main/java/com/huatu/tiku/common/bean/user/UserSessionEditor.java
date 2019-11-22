package com.huatu.tiku.common.bean.user;


import java.beans.PropertyEditorSupport;

/**
 * usersession不建议default
 * @author hanchao
 * @date 2017/8/25 19:08
 */
@Deprecated
public class UserSessionEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(null);
    }

    @Override
    public String getAsText() {
        return null;
    }
}
