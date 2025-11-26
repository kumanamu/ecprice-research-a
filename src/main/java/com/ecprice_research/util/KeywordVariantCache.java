package com.ecprice_research.util;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 검색 후보(variants)를 캐싱하는 유틸
 * - 모든 서비스(Amazon/Rakuten/Naver/Coupang/Margin)에서 공통으로 사용
 * - 문자열 배열/리스트 자동 처리
 * - 중복 제거 & 공백 제거
 */
@Slf4j
public class KeywordVariantCache {

    private static final Map<String, List<String>> CACHE = new ConcurrentHashMap<>();


    // ======================================================================
    // 🔥 리스트 기반 저장
    // ======================================================================
    public static void put(String key, List<String> variants) {

        if (variants == null || variants.isEmpty()) return;

        // 정리: 공백 제거 + 중복 제거
        List<String> cleaned = variants.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        CACHE.put(key, cleaned);
        log.info("💾 [VariantCache 저장] {} → {}", key, cleaned);
    }


    // ======================================================================
    // 🔥 배열 기반 저장 (불러올 때 서비스에서 배열 사용하는 경우 대비)
    // ======================================================================
    public static void put(String key, String[] arr) {

        if (arr == null || arr.length == 0) return;

        List<String> cleaned = Arrays.stream(arr)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        CACHE.put(key, cleaned);
        log.info("💾 [VariantCache 저장] {} → {}", key, cleaned);
    }


    // ======================================================================
    // 🔥 캐시 조회 (List 형태로 반환)
    // ======================================================================
    public static List<String> get(String key) {
        return CACHE.get(key);
    }


    // ======================================================================
    // 🔥 캐시 조회 (배열 형태로 반환)
    // ======================================================================
    public static String[] getArray(String key) {
        List<String> list = CACHE.get(key);
        return (list == null) ? null : list.toArray(new String[0]);
    }


    // ======================================================================
    // 🔥 후보 필터링 (여기서 영어 규칙/공백 제거 적용)
    // ======================================================================
    public static List<String> filter(List<String> raw) {

        if (raw == null) return List.of();

        return raw.stream()
                .filter(s -> s != null && !s.isBlank()) // 공백 제거
                .distinct() // 중복 제거
                .toList();
    }
}
