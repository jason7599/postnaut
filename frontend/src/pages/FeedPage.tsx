import { ArrowRight } from "lucide-react";
import { AnimatePresence, motion, type PanInfo } from "motion/react";
import { useEffect, useState } from "react";
import { Link } from "react-router";
import { errorMessage } from "../api/client";
import type { FeedPost } from "../api/posts";
import { CloudCard } from "../components/CloudCard";
import { useFeed } from "../hooks/useFeed";
import { timeLeft } from "../lib/time";

const SWIPE_DISTANCE = 100;
const SWIPE_SPEED = 500;

// Short posts get big type. Long ones shrink so they still fit on a phone.
function textSize(content: string) {
  if (content.length <= 60) return "text-3xl md:text-4xl";
  if (content.length <= 180) return "text-xl md:text-2xl";
  return "text-base md:text-lg";
}

const cardMotion = {
  enter: { opacity: 0, scale: 0.92 },
  center: { opacity: 1, scale: 1, x: 0, rotate: 0 },
  exit: (direction: number) => ({ opacity: 0, x: direction * 600, rotate: direction * 10 }),
};

function PostCard({ post }: { post: FeedPost }) {
  return (
    <CloudCard className="flex min-h-72 flex-col justify-center px-7 py-10 md:min-h-80 md:px-12">
      <p className={`font-post leading-snug wrap-anywhere break-keep whitespace-pre-wrap ${textSize(post.content)}`}>{post.content}</p>
      <p className="mt-6 text-sm text-ink/70">{timeLeft(post.expiresAt)}</p>
    </CloudCard>
  );
}

export function FeedPage() {
  const feed = useFeed();
  const [direction, setDirection] = useState(1);
  const canAdvance = Boolean(feed.post);

  function advance(nextDirection: number) {
    if (!canAdvance) return;
    setDirection(nextDirection);
    feed.next();
  }

  function handleDragEnd(_event: unknown, info: PanInfo) {
    const swiped = Math.abs(info.offset.x) > SWIPE_DISTANCE || Math.abs(info.velocity.x) > SWIPE_SPEED;
    if (swiped) advance(info.offset.x > 0 ? 1 : -1);
  }

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "ArrowRight") advance(1);
      if (event.key === "ArrowLeft") advance(-1);
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  });

  return (
    <main className="mx-auto flex w-full max-w-xl flex-1 flex-col items-center justify-center gap-16 py-8">
      <h1 className="sr-only">Feed</h1>

      <div className="grid min-h-72 w-full place-items-center *:col-start-1 *:row-start-1">
        {feed.isPending && <p role="status" className="fade-in-late text-haze">Finding a post…</p>}

        {feed.isError && (
          <div className="panel grid justify-items-center gap-4 p-6 text-center">
            <p role="alert">{errorMessage(feed.error)}</p>
            <button className="button" onClick={() => feed.refetch()}>Try again</button>
          </div>
        )}

        {feed.data === null && (
          <CloudCard className="grid w-full justify-items-center gap-4 px-7 py-8 text-center">
            <img src="/mascot.png" alt="" width={320} height={321} className="h-24 w-auto" />
            <h2 className="text-xl font-bold text-balance">You've read every post up here</h2>
            <p>Check back later, or send one up yourself.</p>
            <Link to="/write" className="button">Write a post</Link>
          </CloudCard>
        )}

        <AnimatePresence mode="wait" custom={direction}>
          {feed.post && (
            <motion.div
              key={feed.post.id}
              className="w-full cursor-grab select-none active:cursor-grabbing"
              custom={direction}
              variants={cardMotion}
              initial="enter"
              animate="center"
              exit="exit"
              transition={{ duration: 0.22 }}
              drag="x"
              dragSnapToOrigin
              dragElastic={0.8}
              onDragEnd={handleDragEnd}
            >
              <PostCard post={feed.post} />
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {feed.data !== null && !feed.isError && (
        <div className="grid justify-items-center gap-2">
          <button className="button button-quiet" onClick={() => advance(1)} disabled={!canAdvance}>
            Next post <ArrowRight size={18} aria-hidden />
          </button>
          <p className="text-sm text-haze max-md:hidden">Or swipe, or press the arrow keys</p>
        </div>
      )}
    </main>
  );
}
