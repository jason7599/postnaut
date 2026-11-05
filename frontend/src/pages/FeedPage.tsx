import { Link } from "react-router";
import { errorMessage } from "../api/client";
import type { FeedPost } from "../api/posts";
import { CloudCard } from "../components/CloudCard";
import { ReactionButtons } from "../components/ReactionButtons";
import { useFeed } from "../hooks/useFeed";
import { timeLeft } from "../lib/time";

// Short posts get big type. Long ones shrink so they still fit on a phone.
function textSize(content: string) {
  if (content.length <= 60) return "text-3xl md:text-4xl";
  if (content.length <= 180) return "text-xl md:text-2xl";
  return "text-base md:text-lg";
}

function PostCard({ post }: { post: FeedPost }) {
  return (
    <div className="post-in w-full">
      <CloudCard className="flex min-h-72 flex-col px-7 pt-8 pb-5 md:min-h-80 md:px-12">
        <div className="flex flex-1 items-center">
          <p className={`font-post leading-snug wrap-anywhere break-keep whitespace-pre-wrap ${textSize(post.content)}`}>{post.content}</p>
        </div>
        <div className="mt-4 flex items-center justify-between gap-4">
          <p className="text-sm text-ink/70">{timeLeft(post.expiresAt)}</p>
          <ReactionButtons postId={post.id} />
        </div>
      </CloudCard>
    </div>
  );
}

export function FeedPage() {
  const feed = useFeed();

  return (
    <main className={`mx-auto flex w-full max-w-xl flex-1 flex-col items-center gap-10 md:gap-12 ${feed.data === null ? "justify-center" : ""}`}>
      <h1 className="sr-only">Feed</h1>

      {feed.data !== null && !feed.isError && (
        <img src="/mascot.png" alt="" width={320} height={395} className="-mb-11 h-32 w-auto md:-mb-14 md:h-36" />
      )}

      <div className="grid min-h-72 w-full place-items-center md:min-h-80">
        {feed.isPending && <p role="status" className="fade-in-late text-haze">Finding a post…</p>}

        {feed.isError && (
          <div className="panel grid justify-items-center gap-4 p-6 text-center">
            <p role="alert">{errorMessage(feed.error)}</p>
            <button className="button" onClick={() => feed.refetch()}>Try again</button>
          </div>
        )}

        {feed.data === null && (
          <CloudCard className="grid w-full justify-items-center gap-4 px-7 py-8 text-center">
            <img src="/mascot.png" alt="" width={320} height={395} className="h-24 w-auto" />
            <h2 className="text-xl font-bold text-balance">You've read every post up here</h2>
            <p>Check back later, or send one up yourself.</p>
            <Link to="/write" className="button">Write a post</Link>
          </CloudCard>
        )}

        {feed.post && <PostCard key={feed.post.id} post={feed.post} />}
      </div>

      {feed.data !== null && !feed.isError && (
        <div className="grid justify-items-center gap-2">
          {feed.nextError && <p role="alert" className="text-blush">{errorMessage(feed.nextError)}</p>}
          <button className="button button-quiet" onClick={feed.next} disabled={!feed.post || feed.isMovingOn}>
            Next post
          </button>
        </div>
      )}
    </main>
  );
}
