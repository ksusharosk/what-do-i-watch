import type { ReactNode } from "react";

interface IconCircleProps {
  children: ReactNode;
  active?: boolean;
}

export function IconCircle({ children, active = false }: IconCircleProps) {
  return (
    <span
      className={`grid size-[40px] shrink-0 place-items-center rounded-full shadow-elevated [&>svg]:size-[27px] ${
        active ? "bg-primary text-[#e5e5e5]" : "bg-icon-circle text-sidebar-foreground"
      }`}
    >
      {children}
    </span>
  );
}