import { useState } from "react";
import { useTranslation } from "react-i18next";

interface SearchableFilterGroupProps {
  label: string;
  allOptions: string[];       // the full list (e.g. all genres)
  popular: string[];          // the few shown by default
  selected: string[];
  onToggle: (value: string) => void;
  searchPlaceholder: string;
}

export function SearchableFilterGroup({
  label,
  allOptions,
  popular,
  selected,
  onToggle,
  searchPlaceholder,
}: SearchableFilterGroupProps) {
  const { t } = useTranslation();
  const [search, setSearch] = useState("");
  const [expanded, setExpanded] = useState(false); // did the user click "+N more"?

  const searching = search.trim().length > 0;

  // Decide which options to show, based on the three states:
  let shown: string[];
  if (searching) {
    // Search results replace everything
    shown = allOptions.filter((o) =>
      o.toLowerCase().includes(search.toLowerCase())
    );
  } else if (expanded) {
    // "+N more" was clicked → show all
    shown = allOptions;
  } else {
    // Idle → just the popular few
    shown = popular;
  }

  const hiddenCount = allOptions.length - popular.length;

  return (
    <div>
      <p className="mb-2 text-xs font-medium uppercase text-muted-foreground">
        {label}
      </p>

      {/* Search box */}
      <input
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder={searchPlaceholder}
        className="mb-2 w-full rounded-lg bg-background px-3 py-1 text-xs text-content outline-none placeholder:text-muted-foreground"
      />

      {/* Pills — one line; scrolls horizontally when expanded */}
      <div
        className={`flex gap-2 pb-2 ${
          expanded && !searching ? "overflow-x-auto scrollbar-pill" : "overflow-hidden"
        }`}
      >
        {shown.map((option) => {
          const isSelected = selected.includes(option);
          return (
            <button
              key={option}
              onClick={() => onToggle(option)}
              className={`shrink-0 rounded-full px-3 py-1 text-xs transition-colors ${
                isSelected
                  ? "bg-primary text-primary-foreground"
                  : "bg-background text-content hover:bg-muted-foreground/20"
              }`}
            >
              {option}
            </button>
          );
        })}

        {/* "+N more" — only when idle and there are hidden options */}
        {!searching && !expanded && hiddenCount > 0 && (
          <button
            onClick={() => setExpanded(true)}
            className="shrink-0 rounded-full px-3 py-1 text-xs text-muted-foreground hover:text-content"
          >
            {t("main.more", { count: hiddenCount })}
          </button>
        )}
      </div>
    </div>
  );
}