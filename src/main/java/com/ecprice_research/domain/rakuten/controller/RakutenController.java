package com.ecprice_research.domain.rakuten.controller;

import com.ecprice_research.domain.keyword.engine.KeywordEngine;
import com.ecprice_research.domain.keyword.engine.KeywordVariantBuilder;
import com.ecprice_research.domain.keyword.engine.PlatformRoutingEngine;
import com.ecprice_research.domain.margin.dto.PriceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rakuten")
@RequiredArgsConstructor
public class RakutenController {

    private final KeywordEngine keywordEngine;
    private final KeywordVariantBuilder variantBuilder;
    private final PlatformRoutingEngine routing;

    @GetMapping("/search")
    public PriceInfo search(@RequestParam String keyword) {

        var detected = keywordEngine.detect(keyword);
        var variants = variantBuilder.build(keyword, detected);

        log.info("🔎 [RakutenController] 최종 후보 = {}", variants.rakuten());

        return routing.search("RAKUTEN", variants.rakuten());
    }
}
