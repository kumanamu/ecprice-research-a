import type { ItemInfo } from "../types/marginTypes";

interface Props {
  info: ItemInfo[];
}

export default function ProductList({ info }: Props) {
  if (!info || info.length === 0) {
    return (
      <div className="mt-4 bg-white text-black rounded p-4">
        <p>상품 없음</p>
      </div>
    );
  }

  return (
    <div className="mt-4 bg-white text-black rounded p-4">
      {info.map((item, idx) => (
        <div key={idx} className="mb-4 border-b pb-4">
          <p className="font-bold">{item.title}</p>
          <p>가격: {item.price}원 ({item.rawPrice} {item.currency})</p>
          <img src={item.image} alt={item.title} className="w-32 h-32 object-cover mt-2" />
          <a
            href={item.url}
            target="_blank"
            className="text-blue-500 underline mt-1 block"
          >
            상품 보기
          </a>
        </div>
      ))}
    </div>
  );
}
