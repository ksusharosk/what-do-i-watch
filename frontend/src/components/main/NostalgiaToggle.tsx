import { useTranslation } from "react-i18next";

interface NostalgiaToggleProps {
  checked: boolean;
  onChange: (value: boolean) => void;
  disabled?: boolean; // gated off for guests / <5 watched
}

export function NostalgiaToggle({ checked, onChange, disabled = false }: NostalgiaToggleProps) {
  const { t } = useTranslation();

  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => onChange(!checked)}
      title={disabled ? t("main.nostalgiaLocked") : undefined}
      className="flex items-center gap-2 text-sm text-content disabled:opacity-50 disabled:cursor-not-allowed"
    >
      {/* The switch track + knob */}
        <span
        className={`relative inline-flex h-6 w-10 items-center rounded-full shadow-y2 transition-colors ${
          checked ? "bg-primary" : "bg-muted-foreground"
        }`}
      >
        <span
          className={`absolute left-1 size-4 rounded-full bg-card transition-transform duration-200 ease-out ${
            checked ? "translate-x-4" : "translate-x-0"
          }`}
        />
      </span >
      <span className=" font-medium text-shadow-y2">
        {t("main.nostalgia")}
      </span>
    </button>
  );
}