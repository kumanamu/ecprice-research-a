import { useState } from "react";

interface SearchBarProps {
  onSearch: (keyword: string) => void;
  lang: "ko" | "jp";
}

export default function SearchBar({ onSearch, lang }: SearchBarProps) {
  const [keyword, setKeyword] = useState("");

  const label = (ko: string, jp: string) => (lang === "ko" ? ko : jp);

  const submit = (e: any) => {
    e.preventDefault();
    if (!keyword.trim()) return;
    onSearch(keyword);
  };

  return (
    <form onSubmit={submit} className="flex gap-2 mb-4 w-full">
      <input
        type="text"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        placeholder={label("검색어를 입력...", "検索ワードを入力...")}
        className="w-full p-2 rounded-lg text-black"
      />
      <button type="submit" className="px-4 py-2 bg-blue-500 text-white rounded-lg">
        {label("검색", "検索")}
      </button>
    </form>
  );
}
