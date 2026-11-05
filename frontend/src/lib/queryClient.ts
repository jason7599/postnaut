import { MutationCache, QueryCache, QueryClient } from "@tanstack/react-query";
import { ApiError } from "../api/client";

export const SESSION_KEY = ["session"];

// Sends the user to the login page and forgets everything loaded for them.
export function endSession() {
  queryClient.setQueryData(SESSION_KEY, false);
  queryClient.removeQueries({ predicate: (query) => query.queryKey[0] !== SESSION_KEY[0] });
}

// When any request finds that the session expired, end it here too.
function handleError(error: unknown) {
  if (error instanceof ApiError && error.code === "UNAUTHORIZED") endSession();
}

export const queryClient = new QueryClient({
  queryCache: new QueryCache({ onError: handleError }),
  mutationCache: new MutationCache({ onError: handleError }),
  defaultOptions: {
    queries: {
      // Retry dropped connections and server errors, but not a 400 or 401.
      retry: (failureCount, error) =>
        failureCount < 2 && (!(error instanceof ApiError) || error.status === 0 || error.status >= 500),
      refetchOnWindowFocus: false,
    },
  },
});
