import type { ReactNode } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { IconCircle } from "./IconCircle";

interface NavItemProps {
  icon: ReactNode;
  label: string;
  expanded: boolean;
  to: string;
  keepActive?: boolean; // does this item show a persistent active highlight?
}

export function NavItem({ icon, label, expanded, to, keepActive = true }: NavItemProps) {
  const navigate = useNavigate();
  const location = useLocation();

  // Active only if this item keeps active state AND the URL matches.
  const active = keepActive && location.pathname === to;
  const activeRow = active && expanded;

  return (
    <li>
      <button
        onClick={() => navigate(to)}
        className={`group flex items-center gap-3 w-full rounded-lg p-1.5 transition-colors ${
          expanded ? "justify-start" : "justify-center"
        } ${activeRow ? "bg-primary shadow-elevated" : "hover:bg-primary"}`}
      >
        <IconCircle active={active && !expanded}>{icon}</IconCircle>
        {expanded && (
          <span
            className={`truncate text-base font-medium ${
              activeRow
                ? "text-[#e5e5e5]"
                : "text-sidebar-foreground group-hover:text-primary-foreground"
            }`}
          >
            {label}
          </span>
        )}
      </button>
    </li>
  );
}