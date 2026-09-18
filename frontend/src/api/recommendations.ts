// Talks to POST /api/recommendations.

export interface RecommendationRequest {
  backend?: string | null;
  genreIds: number[];
  genreNames: string[];
  decades: string[];
  countries: string[];
  language?: string | null;
  includeWatched: boolean;
  mood: string;
}

export interface Recommendation {
  movie: {
    id: number;
    title: string;
    originalTitle: string;
    overview: string;
    year: number;
    genres: string[];
    countryCode: string | null;
    language: string;
    rating: number;
    voteCount: number;
    posterPath: string | null;
    directors: { id: number; name: string }[];
    actors: { id: number; name: string }[];
  };
  aiPitch: string;
  feedback: string;
}

export async function getRecommendations(
  req: RecommendationRequest,
): Promise<Recommendation[]> {
  const res = await fetch("/api/recommendations", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify(req),
  });
  if (!res.ok) {
    throw new Error(`Recommendation request failed (${res.status})`);
  }
  return res.json();
}