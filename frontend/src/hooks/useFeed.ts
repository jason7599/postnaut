import { useQuery } from "@tanstack/react-query";
import { useRef, useState } from "react";
import { getRandomPost, recordView } from "../api/posts";

// The backend can return the same post until its view is recorded, so a repeat
// is skipped and another one requested a few times before giving up.
const MAX_ATTEMPTS = 5;

async function fetchUnseenPost(seen: Set<string>) {
  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
    const post = await getRandomPost();
    if (!post) return null;
    if (!seen.has(post.id)) return post;
  }
  return null;
}

export function useFeed() {
  const seen = useRef(new Set<string>());
  const [position, setPosition] = useState(0);

  const query = useQuery({
    queryKey: ["feed", position],
    queryFn: () => fetchUnseenPost(seen.current),
    staleTime: Infinity,
    gcTime: 0,
  });

  function next() {
    if (query.data) {
      seen.current.add(query.data.id);
      void recordView(query.data.id);
    }
    setPosition((current) => current + 1);
  }

  return { ...query, post: query.data, next };
}
