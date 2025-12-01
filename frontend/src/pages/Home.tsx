import { useState } from "react";
import SearchBar from "../components/SearchBar";
import ToggleLanguage from "../components/ToggleLanguage";
import TogglePremium from "../components/TogglePremium";
import PlatformCard from "../components/PlatformCard";
import ProductList from "../components/ProductList";
import AnalysisBox from "../components/AnalysisBox";

import { getMarginResult } from "../api/marginapi";
import type { MarginResponse } from "../types/marginTypes";

const emptyResult: MarginResponse = {
  platformPrices: {
    amazonJp: { priceJpy: null, priceKrw: null, items: [] },
    rakuten: { priceJpy: null, priceKrw: null, items: [] },
    naver: { priceJpy: null, priceKrw: null, items: [] },
    coupang: { priceJpy: null, priceKrw: null, items: [] }
  },
  basicAi: {
    buyPlatform: "",
    sellPlatform: "",
    profitKrw: 0,
    profitRate: 0,
    reason: "",
    text: ""
  },
  premiumAi: {
    buyPlatform: "",
    sellPlatform: "",
    profitKrw: 0,
    profitRate: 0,
    reason: "",
    text: ""
  },
  bestPlatform: ""
};

export default function Home() {
  const [result, setResult] = useState<MarginResponse>(emptyResult);
  const [loading, setLoading] = useState(false);
  const [lang, setLang] = useState<"ko" | "jp">("ko");
  const [premium, setPremium] = useState(false);

  const search = async (keyword: string) => {
    try {
      setLoading(true);
      const data = await getMarginResult(keyword, lang);
      setResult(data);
    } catch (e) {
      console.error(e);
      alert("검색 오류 발생!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-800 to-purple-700 p-6 text-white">
      <div className="max-w-5xl mx-auto">
        <h1 className="text-2xl font-bold mb-6">ECPriceResearch</h1>

        <SearchBar onSearch={search} lang={lang} />
        <ToggleLanguage lang={lang} setLang={setLang} />
        <TogglePremium premium={premium} setPremium={setPremium} />

        {loading && (
          <div className="mt-6 text-center">
            <label>{"검색중..."}</label>
          </div>
        )}

        {!loading && (
          <>
            <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
              <PlatformCard
                title="아마존 JP"
                info={result.platformPrices.amazonJp}
                highlight={result.bestPlatform === "amazonJp"}
              />

              <PlatformCard
                title="라쿠텐"
                info={result.platformPrices.rakuten}
                highlight={result.bestPlatform === "rakuten"}
              />

              <PlatformCard
                title="Naver"
                info={result.platformPrices.naver}
                highlight={result.bestPlatform === "naver"}
              />

              <PlatformCard
                title="Coupang"
                info={result.platformPrices.coupang}
                highlight={result.bestPlatform === "coupang"}
              />
            </div>

            {/* 상품 리스트 */}
            <ProductList info={result.platformPrices.amazonJp.items} />
            <ProductList info={result.platformPrices.rakuten.items} />
            <ProductList info={result.platformPrices.naver.items} />
            <ProductList info={result.platformPrices.coupang.items} />

            {/* AI 분석 */}
            <AnalysisBox
              title="Basic AI 분석"
              detail={result.basicAi.reason || result.basicAi.text}
            />

            <AnalysisBox
              title="Premium AI 분석"
              detail={premium ? (result.premiumAi.text || result.premiumAi.reason) : ""}
            />
          </>
        )}
      </div>
    </div>
  );
}
