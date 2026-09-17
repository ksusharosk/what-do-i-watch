import { Outlet } from "react-router-dom";
import { Sidebar } from "./Sidebar";
import { LanguageSwitch } from "./LanguageSwitch";
import { LoginButton } from "./LoginButton";

export function AppLayout() {
  return (
    <div className="min-h-screen flex">
      <Sidebar />

      {/* Main content area, positioned so we can float things in its corners */}
      <main className="flex-1 relative">
        {/* Top-right controls, floating above the page */}
        <div className="absolute top-4 right-4 z-20 flex items-start gap-3">
          <LoginButton />
          <LanguageSwitch />
        </div>

        {/* The actual page */}
        <Outlet />
      </main>
    </div>
  );
}