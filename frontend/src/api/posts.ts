import { request } from "./client";

export const LIFESPANS = [
  { value: "PT1H", label: "1 hour" },
  { value: "PT6H", label: "6 hours" },
  { value: "PT12H", label: "12 hours" },
  { value: "P1D", label: "1 day" },
  { value: "P3D", label: "3 days" },
  { value: "P5D", label: "5 days" },
  { value: "P7D", label: "7 days" },
] as const;

export type Lifespan = (typeof LIFESPANS)[number]["value"];
export type PostFilter = "ALL" | "LIVE" | "EXPIRED";
export type Reaction = "LIKE" | "DISLIKE" | "NONE";

export const MAX_POST_LENGTH = 500;

export type CreatePostRequest = {
  content: string;
  revealToFriends: boolean;
  replyEnabled: boolean;
  clientId: string;
  lifespan: Lifespan;
};

export type Post = {
  id: string;
  content: string;
  revealToFriends: boolean;
  replyEnabled: boolean;
  createdAt: string;
  expiresAt: string;
};

export type MyPost = Post & {
  viewCount: number;
  likeCount: number;
  dislikeCount: number;
  replyChainCount: number;
  unreadChainCount: number;
  isExpired: boolean;
};

export type FeedPost = Omit<Post, "revealToFriends">;

export type Page<T> = {
  items: T[];
  hasMore: boolean;
};

export const createPost = (body: CreatePostRequest) =>
  request<Post>("/posts", { method: "POST", body });

export function getMyPosts(filter: PostFilter, cursor?: string) {
  const params = new URLSearchParams({ filter });
  if (cursor) params.set("cursor", cursor);
  return request<Page<MyPost>>(`/posts/mine?${params}`);
}

// Resolves to undefined when the backend answers 204, meaning no posts are left.
export const getRandomPost = () => request<FeedPost | undefined>("/posts/random");

export const recordView = (postId: string) =>
  request<void>(`/posts/${postId}/view`, { method: "PUT" });

export const setReaction = (postId: string, reaction: Reaction) =>
  request<void>(`/posts/${postId}/reaction`, { method: "PUT", body: { reaction } });
