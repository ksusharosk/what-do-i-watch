import { useState } from "react";
import { useTranslation } from "react-i18next";

const LANGUAGES = ["en", "ru", "pl"] as const;

export function LanguageSwitch() {
  const { i18n } = useTranslation();
  const [open, setOpen] = useState(false);

  const current = i18n.language;

  const pick = (lang: string) => {
    i18n.changeLanguage(lang);
    setOpen(false);
  };

  return (
    <div className="flex flex-col items-end gap-2">
      <button
        onClick={() => setOpen(!open)}
        className="rounded-full bg-card px-5 py-2 text-sidebar-foreground font-medium uppercase shadow-y4 transition-colors hover:bg-primary hover:text-primary-foreground"
      >
        {current}
      </button>

      {open &&
        LANGUAGES.filter((lang) => lang !== current).map((lang) => (
          <button
            key={lang}
            onClick={() => pick(lang)}
            className="rounded-full bg-card px-5 py-2 text-sidebar-foreground font-medium uppercase shadow-elevated transition-colors hover:bg-primary hover:text-primary-foreground"
          >
            {lang}
          </button>
        ))}
    </div>
  );
}