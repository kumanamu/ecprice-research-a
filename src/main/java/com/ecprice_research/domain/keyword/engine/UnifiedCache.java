package com.ecprice_research.domain.keyword.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🔥 Unified Cache Engine
 * - VariantCache + TranslateCache 완전 통합
 * - 검색 규칙/번역 규칙 기반 캐싱
 * - C-엔진 전체에서 전부 사용
 */
@Slf4j
@Component
public class UnifiedCache {

    private static final Map<String, Object> CACHE = new ConcurrentHashMap<>();

    /** 저장 */
    public void put(String key, Object value) {
        if (key == null || value == null) return;
        CACHE.put(key, value);
        log.info("💾 [UnifiedCache 저장] {} → {}", key, value);
    }

    /** 조회 (문자열) */
    public String getString(String key) {
        Object v = CACHE.get(key);
        return (v instanceof String) ? (String) v : null;
    }

    /** 조회 (문자열 리스트) */
    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        Object v = CACHE.get(key);
        return (v instanceof List) ? (List<String>) v : null;
    }

    /** 전체 삭제 (테스트용) */
    public void clear() {
        CACHE.clear();
        log.info("🧹 UnifiedCache 전체 삭제 완료");
    }

    /** 리스트 정리(중복 제거 + 빈값 제거) */
    public static List<String> cleanList(List<String> list) {
        if (list == null) return List.of();
        return list.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();
    }
}
