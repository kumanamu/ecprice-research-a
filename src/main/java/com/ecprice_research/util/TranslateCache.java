package com.ecprice_research.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 단순 Key-Value 번역 캐시
 * - KO_JP_문장
 * - JP_KO_文
 * - KO_EN_text
 * - JP_EN_text
 */
public class TranslateCache {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    /** 저장 */
    public static void put(String key, String value) {
        if (key == null || value == null) return;
        CACHE.put(key, value);
    }

    /** 조회 */
    public static String get(String key) {
        if (key == null) return null;
        return CACHE.get(key);
    }

    /** 전체 삭제 (테스트용) */
    public static void clear() {
        CACHE.clear();
    }
}
