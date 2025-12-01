interface Props {
  lang: "ko" | "jp";
  setLang: (l: "ko" | "jp") => void;
}

export default function ToggleLanguage({ lang, setLang }: Props) {
  return (
    <div className="flex gap-2 mb-4">
      <button
        className={`px-3 py-1 rounded ${lang === "ko" ? "bg-white text-black" : "bg-gray-400"}`}
        onClick={() => setLang("ko")}
      >
        한국어
      </button>

      <button
        className={`px-3 py-1 rounded ${lang === "jp" ? "bg-white text-black" : "bg-gray-400"}`}
        onClick={() => setLang("jp")}
      >
        日本語
      </button>
    </div>
  );
}
