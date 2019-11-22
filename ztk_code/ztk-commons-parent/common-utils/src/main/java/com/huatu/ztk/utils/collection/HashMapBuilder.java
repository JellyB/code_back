package com.huatu.ztk.utils.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/5/6 22:26
 */
public class HashMapBuilder<K,V> {

    private HashMap<K,V> map;

    public HashMapBuilder(){
        this.map = new HashMap<K,V>();
    }

    public HashMapBuilder(Map<? extends K, ? extends V> m){
        this.map = new HashMap<K,V>();
    }

    public HashMapBuilder(int initialCapacity){
        this.map = new HashMap<K,V>(initialCapacity);
    }

    public HashMapBuilder(int initialCapacity, float loadFactor){
        this.map = new HashMap(initialCapacity,loadFactor);
    }

    public HashMapBuilder<K,V> put(K k,V v){
        this.map.put(k,v);
        return this;
    }

    public HashMap<K,V> build(){
        return this.map;
    }
}
