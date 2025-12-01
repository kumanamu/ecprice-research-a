interface Props {
  title: string;
  detail: string;
}

export default function AnalysisBox({ title, detail }: Props) {
  return (
    <div className="mt-4 p-4 rounded-xl bg-white/10">
      <div className="text-lg font-bold">{title}</div>
      <div className="text-sm whitespace-pre-line mt-2">
        {detail || "결과 없음"}
      </div>
    </div>
  );
}
