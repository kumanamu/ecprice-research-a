package com.ecprice_research.domain.margin.util;

import com.ecprice_research.domain.margin.dto.MarginCompareResult;

public class BasicPromptBuilder {

    public static String build(MarginCompareResult r) {

        StringBuilder sb = new StringBuilder();

        sb.append("상품 가격 비교와 수익 분석을 기반으로 국가별 판매 전략을 제시해 주세요.\n\n");

        sb.append("🔎 검색어: ").append(r.getKeyword()).append("\n");
        sb.append("💱 환율: 1 JPY = ").append(r.getJpyToKrw()).append(" KRW\n\n");

        sb.append("📦 플랫폼별 데이터:\n");

        r.getPlatformPrices().forEach((platform, info) -> {
            if (info == null) return;

            sb.append("- ").append(platform).append(":\n");
            sb.append("  • 가격(KRW): ").append(info.getPriceKrw()).append("\n");
            sb.append("  • 가격(JPY): ").append(info.getPriceJpy()).append("\n");
        });

        sb.append("\n📉 최저가 플랫폼: ").append(r.getBestPlatform()).append("\n");
        sb.append("📈 예상 이익: ").append(r.getProfitKrw()).append(" KRW | ")
                .append(r.getProfitJpy()).append(" JPY\n\n");

        sb.append("아래 형식으로 분석해 주세요:\n");
        sb.append("1) 플랫폼별 가격 및 수익 비교\n");
        sb.append("2) 국가별 판매의 장단점\n");
        sb.append("3) 시장 경쟁도 및 리스크\n");
        sb.append("4) 최종 추천 플랫폼 및 이유\n");

        return sb.toString();
    }
}
