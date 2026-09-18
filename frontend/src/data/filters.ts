export interface Genre {
  name: string; // lowercased TMDB genre name, shown in the UI
  id: number;   // TMDB genre id, sent to the backend (never displayed)
}

export interface Country {
  name: string; // shown in the UI
  code: string; // ISO 3166-1 alpha-2, uppercase, sent to the backend
}

export const GENRES: Genre[] = [
  { name: "action", id: 28 },
  { name: "adventure", id: 12 },
  { name: "animation", id: 16 },
  { name: "comedy", id: 35 },
  { name: "crime", id: 80 },
  { name: "documentary", id: 99 },
  { name: "drama", id: 18 },
  { name: "family", id: 10751 },
  { name: "fantasy", id: 14 },
  { name: "history", id: 36 },
  { name: "horror", id: 27 },
  { name: "music", id: 10402 },
  { name: "mystery", id: 9648 },
  { name: "romance", id: 10749 },
  { name: "science fiction", id: 878 },
  { name: "thriller", id: 53 },
  { name: "war", id: 10752 },
  { name: "western", id: 37 },
];

export const POPULAR_GENRE_NAMES = [
  "comedy", "drama", "thriller", "science fiction", "horror",
];

export const COUNTRIES: Country[] = [
  { name: "usa", code: "US" },
  { name: "japan", code: "JP" },
  { name: "france", code: "FR" },
  { name: "south korea", code: "KR" },
  { name: "italy", code: "IT" },
  { name: "uk", code: "GB" },
  { name: "india", code: "IN" },
  { name: "germany", code: "DE" },
  { name: "china", code: "CN" },
  { name: "spain", code: "ES" },
  { name: "mexico", code: "MX" },
  { name: "brazil", code: "BR" },
  { name: "canada", code: "CA" },
  { name: "sweden", code: "SE" },
];

export const POPULAR_COUNTRY_NAMES = ["usa", "japan", "france", "south korea"];

// Just the names, for the filter UI (pills work in names)
export const GENRE_NAMES = GENRES.map((g) => g.name);
export const COUNTRY_NAMES = COUNTRIES.map((c) => c.name);

// Translate selected names → the IDs/codes the backend wants
export function genreNamesToIds(names: string[]): number[] {
  return names
    .map((name) => GENRES.find((g) => g.name === name)?.id)
    .filter((id): id is number => id !== undefined);
}

export function countryNamesToCodes(names: string[]): string[] {
  return names
    .map((name) => COUNTRIES.find((c) => c.name === name)?.code)
    .filter((code): code is string => code !== undefined);
}

export function countryCodeToName(code: string | null): string {
  if (!code) return "";
  const match = COUNTRIES.find((c) => c.code === code);
  return match ? match.name : code;
}