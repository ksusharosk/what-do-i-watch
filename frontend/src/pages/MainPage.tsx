import { useState } from "react";
import { useTranslation } from "react-i18next";
import { ArrowRight } from "lucide-react";
import logo from "../assets/logo.jpg";
import { NostalgiaToggle } from "@/components/main/NostalgiaToggle";
import { Filters } from "../components/main/Filters";

export default function MainPage() {
  const { t } = useTranslation();

  const [mood, setMood ] = useState("");
  const [nostalgia, setNostalgia] = useState(false);
  const [filtersOpen, setFiltersOpen] = useState(false);

  const submit = () => {
    console.log("Seatching for:", mood);
  };

  const [filters, setFilters] = useState({
    genres: [] as string[],
    decades: [] as string[],
    countries: [] as string[],
  });

  return (
    
    <div className={`flex min-h-screen flex-col items-center justify-center p-8`}>
      <div className={`flex w-full flex-col items-center transition-transform duration-500 ease-in-out ${
          filtersOpen ? "-translate-y-30" : "translate-y-0"
        }`}
      >
        <div className="mb flex items-center gap-2">
          <img src={logo} alt="" className="h-8 w-auto shadow-y3" />
          <span className="font-logo text-3xl text-black text-shadow-y3">cinematica</span>
        </div>
        <h1 className="mb-12 text-4xl pt-3 font-medium text-content text-shadow-y3">
          {t("main.greeting")}
        </h1>
      
          <div className="flex w-full max-w-3xl items-center gap-2 rounded-full pl-6 pr-2 py-2.5 bg-card shadow-y4">
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

        { /*nostalgia toggle*/ }
        <div className="mt-4">
          <NostalgiaToggle
            checked={nostalgia}
            onChange={setNostalgia}
            disabled={false}
          />
        </div>

        {/* filters */}
        <div className="mt-3">
          <Filters 
            filters={filters} 
            onChange={setFilters}
            open={filtersOpen}
            onOpenChange={setFiltersOpen}
          />
        </div>
      </div>
    </div>
  );
}