package com.ecprice_research.domain.keyword.engine;

import com.ecprice_research.domain.translate.service.TranslateService;
import com.ecprice_research.keyword.engine.KeywordDetect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordVariantBuilder {

    private final TranslateService translate;
    private final UnifiedCache cache;

    public VariantResult build(String keyword, KeywordDetect.LangType type) {

        // 캐시 우선
        List<String> amz = cache.getList("AMZ_" + keyword);
        List<String> rak = cache.getList("RAK_" + keyword);
        List<String> nav = cache.getList("NAV_" + keyword);
        List<String> cup = cache.getList("CUP_" + keyword);

        if (amz != null && rak != null && nav != null && cup != null) {
            log.info("⚡ 캐시 HIT → {}", keyword);
            return new VariantResult(amz, rak, nav, cup);
        }

        log.info("🔧 후보 생성 시작 → 입력='{}' / type={}", keyword, type);

        List<String> amazon = new ArrayList<>();
        List<String> rakuten = new ArrayList<>();
        List<String> naver = new ArrayList<>();
        List<String> coupang = new ArrayList<>();

        String ko = null;
        String jp = null;

        boolean hasKo = keyword.matches(".*[가-힣].*");
        boolean hasJp = keyword.matches(".*[一-龥ぁ-ゔァ-ヴー々〆〤].*");
        boolean hasEn = keyword.matches(".*[a-zA-Z].*");

        // ============================================================
        // 🔥 1) KO only
        // ============================================================
        if (hasKo && !hasJp && !hasEn) {
            jp = translate.koToJp(keyword);

            amazon.add(jp);
            rakuten.add(jp);

            naver.add(keyword);
            coupang.add(keyword);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // ============================================================
        // 🔥 2) JP only
        // ============================================================
        if (hasJp && !hasKo && !hasEn) {
            ko = translate.jpToKo(keyword);

            amazon.add(keyword);
            rakuten.add(keyword);

            naver.add(ko);
            coupang.add(ko);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // ============================================================
        // 🔥 3) EN only (번역 절대 금지)
        // ============================================================
        if (hasEn && !hasKo && !hasJp) {

            amazon.add(keyword);
            rakuten.add(keyword);
            naver.add(keyword);
            coupang.add(keyword);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // ============================================================
        // 🔥 4) KO + EN
        // ============================================================
        if (hasKo && hasEn && !hasJp) {
            ko = keyword.replaceAll("[^가-힣]", "");
            String en = keyword.replaceAll("[^a-zA-Z0-9\\s\\-_.]", "");

            jp = translate.koToJp(ko);

            amazon.add(jp + " " + en);
            rakuten.add(jp + " " + en);

            naver.add(ko + " " + en);
            coupang.add(ko + " " + en);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // ============================================================
        // 🔥 5) JP + EN
        // ============================================================
        if (hasJp && hasEn && !hasKo) {
            jp = keyword.replaceAll("[^一-龥ぁ-ゔァ-ヴー々〆〤]", "");
            String en = keyword.replaceAll("[^a-zA-Z0-9\\s\\-_.]", "");

            ko = translate.jpToKo(jp);

            amazon.add(jp + " " + en);
            rakuten.add(jp + " " + en);

            naver.add(ko + " " + en);
            coupang.add(ko + " " + en);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // ============================================================
        // 🔥 6) KO + JP (서로 번역 금지)
        // ============================================================
        if (hasKo && hasJp && !hasEn) {
            String koOnly = keyword.replaceAll("[^가-힣]", "");
            String jpOnly = keyword.replaceAll("[^一-龥ぁ-ゔァ-ヴー々〆〤]", "");

            amazon.add(jpOnly);
            rakuten.add(jpOnly);

            naver.add(koOnly);
            coupang.add(koOnly);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // ============================================================
        // 🔥 7) KO + JP + EN
        // ============================================================
        if (hasKo && hasJp && hasEn) {
            String koOnly = keyword.replaceAll("[^가-힣]", "");
            String jpOnly = keyword.replaceAll("[^一-龥ぁ-ゔァ-ヴー々〆〤]", "");
            String en = keyword.replaceAll("[^a-zA-Z0-9\\s\\-_.]", "");

            amazon.add(jpOnly + " " + en);
            rakuten.add(jpOnly + " " + en);

            naver.add(koOnly + " " + en);
            coupang.add(koOnly + " " + en);

            return finalize(keyword, amazon, rakuten, naver, coupang);
        }

        // fallback
        amazon.add(keyword);
        rakuten.add(keyword);
        naver.add(keyword);
        coupang.add(keyword);

        return finalize(keyword, amazon, rakuten, naver, coupang);
    }


    private VariantResult finalize(
            String key,
            List<String> amazon,
            List<String> rakuten,
            List<String> naver,
            List<String> coupang
    ) {
        amazon = UnifiedCache.cleanList(amazon);
        rakuten = UnifiedCache.cleanList(rakuten);
        naver = UnifiedCache.cleanList(naver);
        coupang = UnifiedCache.cleanList(coupang);

        cache.put("AMZ_" + key, amazon);
        cache.put("RAK_" + key, rakuten);
        cache.put("NAV_" + key, naver);
        cache.put("CUP_" + key, coupang);

        log.info("✅ 후보 생성 완료 → {}", key);
        log.info("   ▶ Amazon:  {}", amazon);
        log.info("   ▶ Rakuten: {}", rakuten);
        log.info("   ▶ Naver:   {}", naver);
        log.info("   ▶ Coupang: {}", coupang);

        return new VariantResult(amazon, rakuten, naver, coupang);
    }

    public record VariantResult(
            List<String> amazon,
            List<String> rakuten,
            List<String> naver,
            List<String> coupang
    ) {}
}
