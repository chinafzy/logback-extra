package me.in1978.third.logback.util;

import java.util.HashMap;

public class ChainedMap<K, V> extends HashMap<K, V> {

    public static <K, V> ChainedMap<K, V> ins() {
        return new ChainedMap<>();
    }

    public ChainedMap<K, V> set(K k, V v) {
        this.put(k, v);
        return this;
    }
}
