import { useTranslation } from "react-i18next";
import { ChevronDown, ChevronUp, SlidersHorizontal, X } from "lucide-react";
import { FilterGroup } from "./FilterGroup";
import { SearchableFilterGroup } from "./SearchableFilterGroup";
import {
  GENRE_NAMES,
  POPULAR_GENRE_NAMES,
  COUNTRY_NAMES,
  POPULAR_COUNTRY_NAMES,
} from "../../data/filters";

export interface FilterState {
  genres: string[];
  decades: string[];
  countries: string[];
}

interface FiltersProps {
  filters: FilterState;
  onChange: (filters: FilterState) => void;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  openUp?: boolean;
}

const DECADES = ["1960s", "1970s", "1980s", "1990s", "2000s", "2010s", "2020s"];

export function Filters({
  filters,
  onChange,
  open,
  onOpenChange,
  openUp = false,
}: FiltersProps) {
  const { t } = useTranslation();

  const toggleValue = (group: keyof FilterState, value: string) => {
    const current = filters[group];
    const isSelected = current.includes(value);
    const next = isSelected
      ? current.filter((v) => v !== value)
      : [...current, value];
    onChange({ ...filters, [group]: next });
  };

  const activeChips = [
    ...filters.genres.map((v) => ({ group: "genres" as const, value: v })),
    ...filters.decades.map((v) => ({ group: "decades" as const, value: v })),
    ...filters.countries.map((v) => ({ group: "countries" as const, value: v })),
  ];
  const activeCount = activeChips.length;

  const header = (
    <button
      onClick={() => onOpenChange(!open)}
      className="flex w-full items-center gap-2 px-5 py-2.5 text-sm font-medium text-content"
    >
      <SlidersHorizontal className="size-4" />
      {t("main.filters")}
      {activeCount > 0 && (
        <span className="grid size-5 place-items-center rounded-full bg-primary text-xs text-primary-foreground">
          {activeCount}
        </span>
      )}
      {open ? (
        <ChevronUp className="ml-auto size-4" />
      ) : (
        <ChevronDown className="ml-auto size-4" />
      )}
    </button>
  );

  const panel = open && (
    <div className="flex flex-col gap-3 px-4 pb-4">
      <SearchableFilterGroup
        label={t("main.genre")}
        allOptions={GENRE_NAMES}
        popular={POPULAR_GENRE_NAMES}
        selected={filters.genres}
        onToggle={(v) => toggleValue("genres", v)}
        searchPlaceholder={t("main.searchGenres")}
      />

      <div className="h-px bg-border" />

      <FilterGroup
        label={t("main.decade")}
        options={DECADES}
        selected={filters.decades}
        onToggle={(v) => toggleValue("decades", v)}
      />

      <div className="h-px bg-border" />

      <SearchableFilterGroup
        label={t("main.country")}
        allOptions={COUNTRY_NAMES}
        popular={POPULAR_COUNTRY_NAMES}
        selected={filters.countries}
        onToggle={(v) => toggleValue("countries", v)}
        searchPlaceholder={t("main.searchCountries")}
      />
    </div>
  );

  return (
    <div className="relative w-130">
      {/* CONNECTED SHAPE: header + panel. Anchors bottom (opens up) or top (opens down). */}
      <div
        className={`absolute left-0 w-full overflow-hidden bg-card shadow-y4 ${
          openUp ? "bottom-0" : "top-0"
        } ${open ? "rounded-2xl" : "rounded-full"}`}
      >
        {openUp ? (
          <>
            {panel}
            {header}
          </>
        ) : (
          <>
            {header}
            {panel}
          </>
        )}
      </div>

      {/* CHIPS — one line, horizontal scroll */}
      {activeCount > 0 && !open && (
        <div className="absolute left-0 top-14 flex w-full gap-1.5 overflow-x-auto pt-1">
          {activeChips.map(({ group, value }) => (
            <button
              key={`${group}-${value}`}
              onClick={() => toggleValue(group, value)}
              className="flex shrink-0 items-center gap-1 rounded-full bg-card px-2.5 py-1 text-xs text-content shadow-y2 hover:bg-muted-foreground/20"
            >
              {value}
              <X className="size-3" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}