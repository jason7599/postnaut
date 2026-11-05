import { useInfiniteQuery } from "@tanstack/react-query";
import { Eye, LogOut, MessageCircle, ThumbsDown, ThumbsUp, type LucideIcon } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router";
import { errorMessage } from "../api/client";
import { getMyPosts, type MyPost, type PostFilter } from "../api/posts";
import { useLogout } from "../hooks/useSession";
import { timeAgo, timeLeft } from "../lib/time";

const FILTERS: { value: PostFilter; label: string }[] = [
  { value: "ALL", label: "All" },
  { value: "LIVE", label: "Live" },
  { value: "EXPIRED", label: "Expired" },
];

const EMPTY_TEXT: Record<PostFilter, string> = {
  ALL: "You haven't posted anything yet.",
  LIVE: "You don't have any live posts.",
  EXPIRED: "None of your posts have expired yet.",
};

function Stat({ icon: Icon, label, value }: { icon: LucideIcon; label: string; value: number }) {
  return (
    <span className="flex items-center gap-1" title={label}>
      <Icon size={16} aria-hidden />
      <span className="sr-only">{label}:</span>
      {value}
    </span>
  );
}

function PostRow({ post }: { post: MyPost }) {
  const settings = [!post.revealToFriends && "Hidden from friends", !post.replyEnabled && "Replies off"].filter(Boolean);

  return (
    <li
      className={`rounded-3xl border p-5 md:p-6 ${
        post.isExpired ? "border-dashed border-line text-haze" : "border-line bg-panel"
      }`}
    >
      <p className="font-post text-lg leading-snug wrap-anywhere break-keep whitespace-pre-wrap">{post.content}</p>
      <div className="mt-4 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-haze">
        <Stat icon={Eye} label="Views" value={post.viewCount} />
        <Stat icon={ThumbsUp} label="Likes" value={post.likeCount} />
        <Stat icon={ThumbsDown} label="Dislikes" value={post.dislikeCount} />
        <Stat icon={MessageCircle} label="Replies" value={post.replyChainCount} />
        {post.unreadChainCount > 0 && (
          <span className="rounded-full bg-blush px-2 font-semibold text-ink">{post.unreadChainCount} unread</span>
        )}
        <span className="ml-auto">{post.isExpired ? `Posted ${timeAgo(post.createdAt)}` : timeLeft(post.expiresAt)}</span>
      </div>
      {settings.length > 0 && <p className="mt-1 text-sm text-haze">{settings.join(", ")}</p>}
    </li>
  );
}

export function MyPostsPage() {
  const [filter, setFilter] = useState<PostFilter>("ALL");
  const logout = useLogout();

  const query = useInfiniteQuery({
    queryKey: ["myPosts", filter],
    queryFn: ({ pageParam }) => getMyPosts(filter, pageParam),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) => (lastPage.hasMore ? lastPage.items.at(-1)?.id : undefined),
  });

  const posts = query.data?.pages.flatMap((page) => page.items) ?? [];

  return (
    <main className="mx-auto w-full max-w-2xl">
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-3xl font-bold">My posts</h1>
        <button
          onClick={() => logout.mutate()}
          disabled={logout.isPending}
          className="button button-quiet text-sm"
        >
          <LogOut size={16} aria-hidden /> Log out
        </button>
      </div>
      {logout.isError && <p role="alert" className="mt-3 panel p-3 text-blush">{errorMessage(logout.error)}</p>}

      <div role="group" aria-label="Filter posts" className="mt-5 flex gap-2">
        {FILTERS.map((option) => (
          <button
            key={option.value}
            aria-pressed={filter === option.value}
            onClick={() => setFilter(option.value)}
            className="min-h-11 cursor-pointer rounded-full border border-line px-4 text-sm font-medium text-haze aria-pressed:border-lilac aria-pressed:bg-lilac aria-pressed:text-ink"
          >
            {option.label}
          </button>
        ))}
      </div>

      <div className="mt-5">
        {query.isPending && <p role="status" className="fade-in-late text-haze">Loading your posts…</p>}

        {query.isError && (
          <div className="panel grid justify-items-start gap-3 p-6">
            <p role="alert">{errorMessage(query.error)}</p>
            <button className="button" onClick={() => query.refetch()}>Try again</button>
          </div>
        )}

        {query.isSuccess && posts.length === 0 && (
          <div className="panel grid justify-items-start gap-4 p-6">
            <p>{EMPTY_TEXT[filter]}</p>
            <Link to="/write" className="button">Write a post</Link>
          </div>
        )}

        <ul className="grid gap-5">
          {posts.map((post) => <PostRow key={post.id} post={post} />)}
        </ul>

        {query.hasNextPage && (
          <div className="mt-6 grid justify-items-center gap-2">
            {query.isFetchNextPageError && <p role="alert">{errorMessage(query.error)}</p>}
            <button className="button" onClick={() => query.fetchNextPage()} disabled={query.isFetchingNextPage}>
              {query.isFetchingNextPage ? "Loading…" : "Show more"}
            </button>
          </div>
        )}
      </div>
    </main>
  );
}
