package com.ecprice_research.domain.keyword.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 플랫폼별 최종 검색 키워드를 결정하는 라우터
 * C-Engine의 출력(SearchKeywords)을 각 서비스에게 정확히 전달하는 역할
 */
@Slf4j
@Component
public class PlatformKeywordRouter {

    /**
     * @param platform  AMAZON / RAKUTEN / NAVER / COUPANG
     * @param keywords  C-엔진에서 만들어진 SearchKeywords (jp/kr 세트)
     * @return          해당 플랫폼이 사용할 검색어 문자열
     */
    public String pick(String platform, com.ecprice_research.domain.engine.KeywordConvert.SearchKeywords keywords) {

        return switch (platform) {

            case "AMAZON", "RAKUTEN" -> {
                log.info("🔗 [Router] {} ← JP: {}", platform, keywords.amazon());
                yield keywords.amazon();
            }

            case "NAVER", "COUPANG" -> {
                log.info("🔗 [Router] {} ← KR: {}", platform, keywords.naver());
                yield keywords.naver();
            }

            default -> {
                log.warn("⚠ [Router] Unknown platform: {}", platform);
                yield keywords.naver(); // 기본값: KR
            }
        };
    }
}
