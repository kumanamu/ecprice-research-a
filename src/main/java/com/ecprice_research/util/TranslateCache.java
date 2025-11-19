package com.ecprice_research.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 번역 결과 캐싱
 * - 같은 문장을 반복 번역하는 비용을 절감하기 위한 캐시
 * - 운영 서비스에서는 반드시 필요한 구조
 */
public class TranslateCache {

    // 한국어 → 일본어
    private static final Map<String, String> KO_JP_CACHE = new ConcurrentHashMap<>();

    // 일본어 → 한국어
    private static final Map<String, String> JP_KO_CACHE = new ConcurrentHashMap<>();

    public static String getKoToJp(String input) {
        return KO_JP_CACHE.get(input);
    }

    public static void putKoToJp(String input, String output) {
        KO_JP_CACHE.put(input, output);
    }

    public static String getJpToKo(String input) {
        return JP_KO_CACHE.get(input);
    }

    public static void putJpToKo(String input, String output) {
        JP_KO_CACHE.put(input, output);
    }
}
