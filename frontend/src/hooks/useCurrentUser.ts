import { useQuery } from "@tanstack/react-query";

export interface CurrentUser {
  id: string;
  email: string;
  displayName: string;
  // avatar, preferences, etc. — add as needed
}

async function fetchCurrentUser(): Promise<CurrentUser | null> {
  const res = await fetch("/api/me", { credentials: "include" });

  if (res.status === 401) {
    return null; 
  }
  if (!res.ok) {
    throw new Error(`Failed to load user (${res.status})`);
  }
  return res.json();
}

export function useCurrentUser() {
  const query = useQuery({
    queryKey: ["currentUser"],
    queryFn: fetchCurrentUser,
    retry: false, 
  });

  return {
    user: query.data ?? null,      // the user object, or null if guest
    isLoading: query.isLoading,    // still checking
    isLoggedIn: !!query.data,     
  };
}