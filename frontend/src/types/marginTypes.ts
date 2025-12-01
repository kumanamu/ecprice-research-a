// 상품 상세 정보
export interface ItemInfo {
  title: string;
  price: number;
  rawPrice: number;
  currency: string;
  image: string;
  url: string;
}

// 플랫폼 가격 정보
export interface PlatformPrice {
  priceJpy: number | null;
  priceKrw: number | null;
  items: ItemInfo[];
}

// AI 분석 정보 (백엔드 응답 그대로 반영)
export interface AiDetail {
  buyPlatform: string;
  sellPlatform: string;
  profitKrw: number;
  profitRate: number;
  reason: string; // basic ai summary
  text: string;   // premium ai markdown
}

// 전체 응답 스키마
export interface MarginResponse {
  platformPrices: {
    amazonJp: PlatformPrice;
    rakuten: PlatformPrice;
    naver: PlatformPrice;
    coupang: PlatformPrice;
  };
  basicAi: AiDetail;
  premiumAi: AiDetail;
  bestPlatform: string;
}
