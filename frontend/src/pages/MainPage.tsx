import { useState } from "react";
import { useTranslation } from "react-i18next";
import { ArrowRight, Heart } from "lucide-react"; 
import { useMutation } from "@tanstack/react-query"; 
import logo from "../assets/logo.jpg";
import { NostalgiaToggle } from "@/components/main/NostalgiaToggle";
import { Filters } from "../components/main/Filters";
import { StarRating } from "../components/main/StarRating"; 
import { getRecommendations } from "../api/recommendations"; 
import type { Recommendation } from "../api/recommendations"; 
import { genreNamesToIds, countryNamesToCodes, countryCodeToName } from "../data/filters"; 

export default function MainPage() {
  const { t } = useTranslation();

  const [mood, setMood] = useState("");
  const [nostalgia, setNostalgia] = useState(false);
  const [filtersOpen, setFiltersOpen] = useState(false);

  const [filters, setFilters] = useState({
    genres: [] as string[],
    decades: [] as string[],
    countries: [] as string[],
  });

  const [lastMood, setLastMood] = useState("");

  const recommend = useMutation<Recommendation[]>({
    mutationFn: () =>
      getRecommendations({
        mood,
        genreIds: genreNamesToIds(filters.genres),
        genreNames: filters.genres,
        decades: filters.decades,
        countries: countryNamesToCodes(filters.countries),
        includeWatched: false,
      }),
  });

  const submit = () => {
    if (
      !mood.trim() &&
      filters.genres.length === 0 &&
      filters.decades.length === 0 &&
      filters.countries.length === 0
    ) {
      return; // nothing to search on
    }
    setLastMood(mood);
    recommend.mutate();
    setMood("");
  };

  const hasResults = recommend.data !== undefined || recommend.isPending;

  const controls = (
    <div className="flex w-full max-w-3xl flex-col items-center">
      {/* search bar */}
      <div className="flex w-full items-center gap-2 rounded-full bg-card py-2.5 pl-6 pr-2 shadow-y4">
        <input
          value={mood}
          onChange={(e) => setMood(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && submit()}
          placeholder={t("main.moodPlaceholder")}
          className="flex-1 bg-transparent text-base outline-none placeholder:text-muted-foreground"
        />
        <button
          onClick={submit}
          aria-label={t("main.submit")}
          className="grid size-10 shrink-0 place-items-center rounded-full bg-primary text-primary-foreground shadow-y4 transition-opacity hover:opacity-90"
        >
          <ArrowRight className="size-5" />
        </button>
      </div>

      {/* nostalgia toggle */}
      <div className="mt-4">
        <NostalgiaToggle checked={nostalgia} onChange={setNostalgia} disabled={false} />
      </div>

      {/* filters */}
      <div className="mt-3">
        <Filters
          filters={filters}
          onChange={setFilters}
          open={filtersOpen}
          onOpenChange={setFiltersOpen}
          openUp={hasResults}
        />
      </div>
    </div>
  );

  const resultsList = (
    <div className="mx-auto w-full max-w-3xl pb-8">
      {recommend.isPending && (
        <>
          {[0, 1, 2].map((i) => (
            <div key={i} className="mb-6">
              {/* pitch line skeleton */}
              <div className="mb-2 h-4 w-3/4 skeleton rounded" />
              {/* card skeleton */}
              <div className="flex gap-4 rounded-2xl bg-card p-4 shadow-y2">
                <div className="h-40 w-28 shrink-0 rounded-lg skeleton" />
                <div className="flex-1 space-y-2">
                  <div className="h-5 w-1/2 rounded skeleton" />
                  <div className="h-3 w-1/3 rounded skeleton" />
                  <div className="h-3 w-2/5 rounded skeleton" />
                  <div className="mt-3 h-3 w-full rounded skeleton" />
                  <div className="h-3 w-5/6 rounded skeleton" />
                  <div className="mt-3 flex gap-1.5">
                    <div className="h-5 w-14 rounded-full skeleton" />
                    <div className="h-5 w-14 rounded-full skeleton" />
                  </div>
                </div>
              </div>
            </div>
          ))}
        </>
      )}
      {recommend.isError && (
        <p className="text-center text-destructive">{t("main.searchError")}</p>
      )}
      {recommend.data && recommend.data.length === 0 && (
        <p className="text-center text-muted-foreground">{t("main.noResults")}</p>
      )}

      {recommend.data && recommend.data.length > 0 && (
        <>
          <p className="mb-4 text-sm text-muted-foreground">{t("main.resultsIntro")}</p>

          {recommend.data.map((rec) => (
            <div key={rec.movie.id} className="mb-6">
              {/* AI pitch — ABOVE the card */}
              <p className="mb-2 text-sm text-content">{rec.aiPitch}</p>

              {/* The card */}
              <div className="flex gap-4 rounded-2xl bg-card p-4 shadow-y2">
                {/* Poster */}
                <div className="flex shrink-0 flex-col items-center gap-2">
                  <div className="h-40 w-28 shrink-0 overflow-hidden rounded-lg bg-muted">
                    {rec.movie.posterPath && (
                      <img
                        src={`https://image.tmdb.org/t/p/w200${rec.movie.posterPath}`}
                        alt=""
                        className="h-full w-full object-cover"
                      />
                    )}
                  </div>
                  <StarRating value={rec.movie.rating / 2} />
                </div>

                {/* Details */}
                <div className="min-w-0 flex-1">
                  <div className="flex items-start justify-between gap-2">
                    <p className="font-medium text-content">
                      {rec.movie.title}{" "}
                      <span className="text-muted-foreground">{rec.movie.year}</span>
                    </p>
                    {/* Heart — void for now */}
                    <button className="shrink-0" aria-label={t("main.like")}>
                      <Heart className="size-5 text-muted-foreground" />
                    </button>
                  </div>

                  {rec.movie.countryCode && (
                    <p className="text-xs capitalize text-muted-foreground">
                      {countryCodeToName(rec.movie.countryCode)}
                    </p>
                  )}

                  {rec.movie.directors.length > 0 && (
                    <p className="mt-1 text-xs text-content">
                      <span className="text-muted-foreground">{t("main.director")}: </span>
                      {rec.movie.directors.map((d) => d.name).join(", ")}
                    </p>
                  )}

                  {rec.movie.actors.length > 0 && (
                    <p className="text-xs text-content">
                      <span className="text-muted-foreground">{t("main.cast")}: </span>
                      {rec.movie.actors.map((a) => a.name).join(", ")}
                    </p>
                  )}

                  {/* Overview — INSIDE the card */}
                  <p className="mt-2 text-sm text-content">{rec.movie.overview}</p>

                  {/* Genre tags */}
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {rec.movie.genres.map((g) => (
                      <span
                        key={g}
                        className="rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground"
                      >
                        {g}
                      </span>
                    ))}
                  </div>

                  {/* mark-watched */}
                  <div className="mt-2 flex items-center justify-between">
                    {/* Mark-watched — void toggle for now */}
                    <button
                      className="flex items-center gap-1.5 text-xs text-muted-foreground"
                      aria-label={t("main.markWatched")}
                    >
                      <span className="inline-block h-4 w-7 rounded-full bg-muted-foreground/40" />
                      {t("main.markWatched")}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </>
      )}
    </div>
  );

  if (!hasResults) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center p-8">
        <div className="mb-4 flex items-center gap-2">
          <img src={logo} alt="" className="h-8 w-auto shadow-y3" />
          <span className="font-logo text-3xl text-black text-shadow-y3">cinematica</span>
        </div>
        <h1 className="mb-12 text-4xl font-medium text-content text-shadow-y3">
          {t("main.greeting")}
        </h1>
        {controls}
      </div>
    );
  }
 
  return (
    <div className="flex h-screen flex-col">
      {/* Logo pinned top */}
      <div className="flex shrink-0 items-center justify-center gap-2 py-4">
        <img src={logo} alt="" className="h-7 w-auto shadow-y3" />
        <span className="font-logo text-2xl text-black text-shadow-y3">cinematica</span>
      </div>

      {/* Results — scroll independently in the middle */}
      <div className="flex flex-1 justify-center overflow-hidden px-8">
        <div className="w-full max-w-4xl overflow-y-auto scrollbar-pill">
          <div className="mx-auto max-w-3xl">
            {resultsList}
          </div>
        </div>
      </div>

      {/* Controls — grows when filters open, shrinking the results area above */}
      <div className="flex shrink-0 justify-center px-4 pb-8 pt-4">
        {controls}
      </div>
    </div>
  );
}