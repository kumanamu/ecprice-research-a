package com.ecprice_research.domain.keyword.engine;

import com.ecprice_research.keyword.engine.KeywordDetect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordEngine {

    private final KeywordDetect detector;
    private final KeywordVariantBuilder variantBuilder;

    public KeywordVariantBuilder.VariantResult buildVariants(String keyword) {

        var lang = detector.detect(keyword);
        log.info("🧠 KeywordEngine 감지언어 = {}", lang);

        var result = variantBuilder.build(keyword, lang);

        log.info("📦 후보 생성 완료 → AMZ={}, RAK={}, NAV={}, CUP={}",
                result.amazon(), result.rakuten(), result.naver(), result.coupang());

        return result;
    }
}
