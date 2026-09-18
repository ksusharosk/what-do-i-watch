interface FilterGroupProps {
  label: string;
  options: string[];
  selected: string[];
  onToggle: (value: string) => void;
}

export function FilterGroup({ label, options, selected, onToggle }: FilterGroupProps) {
  return (
    <div>
      <p className="mb-2 text-xs font-medium uppercase text-muted-foreground">
        {label}
      </p>
      <div className="flex flex-wrap gap-2">
        {options.map((option) => {
          const isSelected = selected.includes(option);
          return (
            <button
              key={option}
              onClick={() => onToggle(option)}
              className={`rounded-full px-3 py-1 text-xs transition-colors ${
                isSelected
                  ? "bg-primary text-primary-foreground"
                  : "bg-background text-content hover:bg-muted-foreground/20"
              }`}
            >
              {option}
            </button>
          );
        })}
      </div>
    </div>
  );
}