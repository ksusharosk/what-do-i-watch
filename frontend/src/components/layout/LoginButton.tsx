import { useTranslation } from "react-i18next";
import { useCurrentUser } from "../../hooks/useCurrentUser";

export function LoginButton() {
  const { t } = useTranslation();
  const { isLoggedIn, isLoading } = useCurrentUser();

  // Don't show anything while we're still checking, or if logged in.
  if (isLoading || isLoggedIn) {
    return null;
  }

  const login = () => {
    // Redirect the browser to the backend's Google OAuth entry point.
    window.location.href = "/oauth2/authorization/google";
  };

  return (
    <button
      onClick={login}
      className="rounded-full bg-card px-5 py-2 text-sidebar-foreground font-medium shadow-elevated transition-colors hover:bg-primary hover:text-primary-foreground"
    >
      {t("auth.login")}
    </button>
  );
}