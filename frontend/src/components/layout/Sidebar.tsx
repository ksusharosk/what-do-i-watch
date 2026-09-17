import { useState } from "react";
import { NavItem } from "./NavItem";
import { IconCircle } from "./IconCircle";
import {
    PanelLeft,
    ArrowLeft,
    User,
    Search,
    MessageSquareText,
    Clock,
    Settings,
    FileText
} from "lucide-react";

/**
 * Sidebar element, has three states:
 * closed - idle state, not interacted with
 * hovered - opens on hover, but without the arrow
 * pinned - opened and pinned (on click on the tab icon), arrow to close appears
 */

export function Sidebar() {
    const [pinned, setPinned] = useState(false);
    const [hovered, setHovered] = useState(false);

    const expanded = pinned || hovered;

    return (
        <nav
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        className={`${expanded ? "w-[var(--rail-expanded)]" : "w-[var(--rail-collapsed)]"} shrink-0 transition-[width] duration-200 ease-out overflow-hidden bg-sidebar flex flex-col gap-2 p-3`}
        >
            <div>
                {expanded ? (
                // Expanded: cinematica on the left, toggle on the right
                <div className="flex items-center gap-3 p-1.5">
                    <span className="truncate font-logo text-[20px] text-black">
                    cinematica
                    </span>
                    <button
                        onClick={() => setPinned(!pinned)}
                        aria-label={pinned ? "Collapse sidebar" : "Pin sidebar open"}
                        className="shrink-0 ml-auto"
                    >
                        <span className="relative grid size-[40px] shrink-0 place-items-center rounded-full bg-[#d9d9d9] text-sidebar-foreground">
                            <PanelLeft
                            className={`col-start-1 row-start-1 size-[27px] transition-all duration-200 ${
                                pinned ? "opacity-0 scale-75 rotate-90" : "opacity-100 scale-100 rotate-0"
                            }`}
                            />
                            <ArrowLeft
                            className={`col-start-1 row-start-1 size-[27px] transition-all duration-200 ${
                                pinned ? "opacity-100 scale-100 rotate-0" : "opacity-0 scale-75 -rotate-90"
                            }`}
                            />
                        </span>
                    </button>
                </div>
                ) : (
                <button
                    onClick={() => setPinned(true)}
                    aria-label="Open sidebar"
                    className="flex w-full rounded-lg p-1.5 justify-center"
                >
                    <IconCircle><PanelLeft /></IconCircle>
                </button>
                )}
            </div>
            
            <ul className="flex flex-col gap-1">
                <NavItem icon={<User />} label="Profile" expanded={expanded} to="/profile" />
                <NavItem icon={<Search />} label="Search" expanded={expanded} to="/search" />
                <NavItem icon={<MessageSquareText />} label="New" expanded={expanded} to="/" keepActive={false} />
            </ul>
            
            <div className="h-0.5 bg-[#484848] mx-1.5" />

            <div>
                {expanded ? (
                <>
                    {/* Section header: clock + circle + "History" label (heading, not a button) */}
                    <div className="flex items-center gap-3 p-1.5">
                    <IconCircle><Clock /></IconCircle>
                    <h2 className="truncate text-base font-medium text-sidebar-foreground">
                        History
                    </h2>
                    </div>

                    {/* Entries — clickable, text only, indented under the header */}
                    <ul className="flex flex-col gap-1 pl-2">
                    <li>
                        <button className="w-full truncate rounded-lg px-2 py-1.5 text-left text-sm text-sidebar-foreground transition-colors hover:bg-primary hover:text-primary-foreground">
                        cozy sci-fi
                        </button>
                    </li>
                    <li>
                        <button className="w-full truncate rounded-lg px-2 py-1.5 text-left text-sm text-sidebar-foreground transition-colors hover:bg-primary hover:text-primary-foreground">
                        90s thrillers
                        </button>
                    </li>
                    </ul>
                </>
                ) : (
                // Collapsed: just the clock in a circle, centered like nav icons
                <div className="flex w-full justify-center p-1.5">
                    <IconCircle><Clock /></IconCircle>
                </div>
                )}
            </div>

            <ul className="flex flex-col gap-1 mt-auto">
                <NavItem icon={<Settings />} label="Settings" expanded={expanded} to="/settings" />
                <NavItem icon={<FileText />} label="Legal" expanded={expanded} to="/legal" />
            </ul>
        </nav>
    );
}