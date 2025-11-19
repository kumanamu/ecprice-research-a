package com.ecprice_research.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KeywordVariantCache {

    private static final Map<String, String[]> CACHE = new ConcurrentHashMap<>();

    public static String[] get(String key) {
        return CACHE.get(key);
    }

    public static void put(String key, String[] variants) {
        CACHE.put(key, variants);
    }
}
