import axios from "axios";
import type { MarginResponse } from "../types/marginTypes";

const API_URL = "/api/margin";

export async function getMarginResult(
  keyword: string,
  lang: "ko" | "jp"
): Promise<MarginResponse> {
  const res = await axios.get(API_URL, {
    params: { keyword, lang },
  });
  return res.data;
}
