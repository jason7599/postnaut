import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRef, useState, type FormEvent } from "react";
import { useNavigate } from "react-router";
import { errorMessage } from "../api/client";
import { createPost, LIFESPANS, MAX_POST_LENGTH, type Lifespan } from "../api/posts";
import { CloudCard } from "../components/CloudCard";
import { newUuid } from "../lib/uuid";

function Toggle({ label, checked, onChange }: { label: string; checked: boolean; onChange: (value: boolean) => void }) {
  return (
    <label className="flex min-h-11 cursor-pointer items-center gap-3">
      <input type="checkbox" className="size-5 accent-lilac" checked={checked} onChange={(e) => onChange(e.target.checked)} />
      {label}
    </label>
  );
}

export function WritePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [content, setContent] = useState("");
  const [lifespan, setLifespan] = useState<Lifespan>("P1D");
  const [revealToFriends, setRevealToFriends] = useState(true);
  const [replyEnabled, setReplyEnabled] = useState(true);

  const mutation = useMutation({
    mutationFn: createPost,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["myPosts"] });
      navigate("/mine");
    },
  });

  // Retrying the same post reuses its clientId, so a retry can't post it twice.
  // The backend ignores new values on a reused id, so any edit needs a new id.
  const lastAttempt = useRef({ draft: "", clientId: "" });

  const trimmed = content.trim();
  const remaining = MAX_POST_LENGTH - trimmed.length;
  const canPost = trimmed.length > 0 && remaining >= 0 && !mutation.isPending;

  function submit(event: FormEvent) {
    event.preventDefault();
    if (!canPost) return;

    const fields = { content: trimmed, lifespan, revealToFriends, replyEnabled };
    const draft = JSON.stringify(fields);
    if (lastAttempt.current.draft !== draft) lastAttempt.current = { draft, clientId: newUuid() };

    mutation.mutate({ ...fields, clientId: lastAttempt.current.clientId });
  }

  return (
    <main className="mx-auto w-full max-w-xl py-8">
      <h1 className="sr-only">Write a post</h1>
      <form onSubmit={submit} className="grid gap-12">
        <CloudCard className="px-7 pt-8 pb-5 md:px-10">
          <label htmlFor="post-content" className="sr-only">Your post</label>
          <textarea
            id="post-content"
            rows={6}
            autoFocus
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="What's on your mind?"
            className="w-full resize-none bg-transparent font-post text-2xl outline-none placeholder:text-ink/60"
          />
          <p className={`text-right text-sm ${remaining < 0 ? "font-semibold text-rose" : "text-ink/70"}`}>
            {remaining < 0 ? `${-remaining} over the limit` : `${remaining} left`}
          </p>
        </CloudCard>

        <div className="grid gap-5 panel p-6">
          <fieldset>
            <legend className="mb-2 font-semibold">Stays up for</legend>
            <div className="flex flex-wrap gap-2">
              {LIFESPANS.map((option) => (
                <label
                  key={option.value}
                  className="cursor-pointer rounded-full border border-line px-3 py-1.5 text-sm font-medium text-haze has-checked:border-lilac has-checked:bg-lilac has-checked:text-ink has-focus-visible:outline-2 has-focus-visible:outline-offset-2 has-focus-visible:outline-lilac"
                >
                  <input
                    type="radio"
                    name="lifespan"
                    className="sr-only"
                    checked={lifespan === option.value}
                    onChange={() => setLifespan(option.value)}
                  />
                  {option.label}
                </label>
              ))}
            </div>
          </fieldset>

          <div>
            <Toggle label="Let friends see it's me" checked={revealToFriends} onChange={setRevealToFriends} />
            <Toggle label="Allow replies" checked={replyEnabled} onChange={setReplyEnabled} />
            <p className="mt-1 text-sm text-haze">You can't edit a post or change these settings after posting.</p>
          </div>

          {mutation.isError && <p role="alert" className="text-blush">{errorMessage(mutation.error)}</p>}

          <button className="button" disabled={!canPost}>
            {mutation.isPending ? "Posting…" : "Post"}
          </button>
        </div>
      </form>
    </main>
  );
}
