import { useMutation } from "@tanstack/react-query";
import { ThumbsDown, ThumbsUp, type LucideIcon } from "lucide-react";
import { useState } from "react";
import { errorMessage } from "../api/client";
import { setReaction, type Reaction } from "../api/posts";

function ReactionButton({
  label,
  icon: Icon,
  pressed,
  onClick,
}: {
  label: string;
  icon: LucideIcon;
  pressed: boolean;
  onClick: () => void;
}) {
  return (
    <button type="button" className="reaction" aria-label={label} aria-pressed={pressed} title={label} onClick={onClick}>
      <Icon size={20} aria-hidden />
    </button>
  );
}

export function ReactionButtons({ postId }: { postId: string }) {
  const [reaction, setLocalReaction] = useState<Reaction>("NONE");

  const mutation = useMutation({
    mutationFn: (next: Reaction) => setReaction(postId, next),
    // Requests run one at a time, so quick taps reach the backend in order.
    scope: { id: "reaction" },
  });

  // The button changes straight away, and changes back if the request fails.
  function choose(option: Reaction) {
    const previous = reaction;
    const next = reaction === option ? "NONE" : option;
    setLocalReaction(next);
    mutation.mutate(next, { onError: () => setLocalReaction(previous) });
  }

  return (
    <div className="grid justify-items-end gap-1">
      {mutation.isError && (
        <p role="alert" className="text-sm text-rose">{errorMessage(mutation.error)}</p>
      )}
      <div className="flex gap-1">
        <ReactionButton label="Like" icon={ThumbsUp} pressed={reaction === "LIKE"} onClick={() => choose("LIKE")} />
        <ReactionButton label="Dislike" icon={ThumbsDown} pressed={reaction === "DISLIKE"} onClick={() => choose("DISLIKE")} />
      </div>
    </div>
  );
}
