import type { PlatformPrice } from "../types/marginTypes";

interface Props {
  title: string;
  info: PlatformPrice;
  highlight: boolean;
}

export default function PlatformCard({ title, info, highlight }: Props) {
  if (!info) return null;

  return (
    <div
      className={`p-4 rounded-xl bg-white/10 text-white ${
        highlight ? "border-2 border-yellow-400" : ""
      }`}
    >
      <div className="text-xl font-bold mb-2">{title}</div>

      {info.priceKrw != null ? (
        <>
          <div className="text-lg font-bold">
            {info.priceKrw.toLocaleString()} 원
          </div>
          {info.priceJpy != null && (
            <div className="text-sm opacity-70">
              {info.priceJpy.toLocaleString()} 円
            </div>
          )}
        </>
      ) : (
        <div className="opacity-60">데이터 없음</div>
      )}
    </div>
  );
}
