package com.huatu.ztk.search.observer;

import java.util.Observable;

/**
 * @author zhengyi
 * @date 2019-03-07 15:51
 **/
public class HotWordObservable extends Observable {
    @Override
    public synchronized void setChanged() {
        super.setChanged();
    }
}