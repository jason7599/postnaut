import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { ApiError } from "../api/client";
import { getRandomPost, recordView } from "../api/posts";

// These mean the post can't be viewed, so the feed should just move on.
function canSkip(error: unknown) {
  return error instanceof ApiError && (error.code === "POST_NOT_FOUND" || error.code === "CANNOT_INTERACT_WITH_OWN_POST");
}

export function useFeed() {
  const [position, setPosition] = useState(0);

  const query = useQuery({
    queryKey: ["feed", position],
    queryFn: async () => (await getRandomPost()) ?? null,
    staleTime: Infinity,
    gcTime: 0,
  });

  const showNextPost = () => setPosition((current) => current + 1);

  // The view is saved before the next post is requested, otherwise the backend
  // could send the same post again.
  const view = useMutation({
    mutationFn: recordView,
    onSuccess: showNextPost,
    onError: (error) => {
      if (canSkip(error)) showNextPost();
    },
  });

  return {
    ...query,
    post: query.data,
    next: () => query.data && view.mutate(query.data.id),
    isMovingOn: view.isPending,
    nextError: view.isError && !canSkip(view.error) ? view.error : null,
  };
}
