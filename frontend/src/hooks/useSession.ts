import { useMutation, useQuery } from "@tanstack/react-query";
import { logout } from "../api/auth";
import { ApiError } from "../api/client";
import { getMyPosts } from "../api/posts";
import { endSession, queryClient, SESSION_KEY } from "../lib/queryClient";

// There's no "who am I" endpoint yet, so this asks for the user's posts and
// checks for a 401. Swap in GET /api/auth/me here if you add one.
async function hasSession() {
  try {
    await getMyPosts("LIVE");
    return true;
  } catch (error) {
    if (error instanceof ApiError && error.code === "UNAUTHORIZED") return false;
    throw error;
  }
}

export function useSession() {
  return useQuery({ queryKey: SESSION_KEY, queryFn: hasSession, staleTime: Infinity });
}

export const markLoggedIn = () => queryClient.setQueryData(SESSION_KEY, true);

export function useLogout() {
  return useMutation({ mutationFn: logout, onSuccess: endSession });
}
