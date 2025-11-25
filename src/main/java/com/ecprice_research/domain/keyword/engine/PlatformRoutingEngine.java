package com.ecprice_research.domain.keyword.engine;

import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlatformRoutingEngine {

    private final Map<String, PlatformAdapter> adapters;

    public PriceInfo search(String platform, List<String> candidates) {

        log.info("🔍 플랫폼 검색 시작: {}  / 후보={}", platform, candidates);

        PlatformAdapter adapter = adapters.get(platform.toUpperCase());
        if (adapter == null) {
            log.warn("❌ 알 수 없는 플랫폼: {}", platform);
            return PriceInfo.notFound(platform, "Unknown platform");
        }

        if (candidates == null || candidates.isEmpty()) {
            return PriceInfo.notFound(platform, "No candidates");
        }

        PriceInfo best = null;

        for (String cand : candidates) {
            PriceInfo pi = adapter.searchSingle(cand);

            if (pi == null || !pi.isSuccess()) {
                log.info("   ❌ 후보 실패: {}", cand);
                continue;
            }

            log.info("   ✅ 후보 성공: {} / {} JPY(환산)", cand, pi.getPriceJpy());

            if (best == null ||
                    (pi.getPriceJpy() != null &&
                            pi.getPriceJpy() < best.getPriceJpy())) {
                best = pi;
            }
        }

        if (best == null) {
            return PriceInfo.notFound(platform, "Not found");
        }

        log.info("🏆 최종 선택: {} → {}", platform, best.getProductName());
        return best;
    }
}
