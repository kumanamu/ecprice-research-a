interface Props {
  premium: boolean;
  setPremium: (p: boolean) => void;
}

export default function TogglePremium({ premium, setPremium }: Props) {
  return (
    <div className="flex gap-2 mb-6">
      <button
        className={`px-3 py-1 rounded ${!premium ? "bg-green-500 text-white" : "bg-gray-400"}`}
        onClick={() => setPremium(false)}
      >
        Basic
      </button>

      <button
        className={`px-3 py-1 rounded ${premium ? "bg-green-500 text-white" : "bg-gray-400"}`}
        onClick={() => setPremium(true)}
      >
        Premium
      </button>
    </div>
  );
}
