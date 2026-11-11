# Reply API

## Start Reply Chain

Starts a reply chain on a post by sending its first reply, or returns the original reply if `clientId` has already been used by this user on this post.

```
POST /api/posts/{postId}/reply-chains
```

Requires a valid session cookie.

Each user can have at most one reply chain per post. The replier always sends first, so the reply returned here always has `seq` `1`.

### Request

| Field | Type | Required | Rules |
| - | - | - | - |
| `content` | string | Yes | 1 ~ 300 characters after stripping surrounding whitespace |
| `clientId` | UUID | Yes | Generate a new one for each reply, and reuse it when retrying |

```json
{
  "content": "Love this!",
  "clientId": "0190f3b4-1a2b-7c3d-9e4f-5a6b7c8d9e01"
}
```

### Response

`201 Created`

| Field | Type | Description |
| - | - | - |
| `chainId` | UUID | The new reply chain |
| `seq` | number | Position of the reply in the chain, starting at `1` |
| `fromPoster` | boolean | Whether the poster sent the reply. Always `false` here |
| `content` | string | |
| `createdAt` | timestamp | ISO-8601, UTC |

```json
{
  "chainId": "0190f3b4-2c3d-7e4f-8a5b-6c7d8e9f0a12",
  "seq": 1,
  "fromPoster": false,
  "content": "Love this!",
  "createdAt": "2026-11-04T10:15:00Z"
}
```

Retrying with the same `clientId` and `content` returns the original reply with the same status. Retries stop working once the post expires.

### Errors

| Code | Status | When |
| - | - | - |
| `VALIDATION_FAILED` | `400` | `postId` is not a valid UUID, or a field is missing or invalid |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |
| `REPLIES_DISABLED` | `403` | The post has replies disabled |
| `CANNOT_INTERACT_WITH_OWN_POST` | `403` | The post belongs to the caller |
| `POST_NOT_FOUND` | `404` | The post doesn't exist or has expired |
| `REPLY_CHAIN_ALREADY_EXISTS` | `409` | The caller already has a reply chain on this post. Use [Append Reply](#append-reply) instead |
| `REPLY_CLIENT_ID_CONFLICT` | `409` | `clientId` was already used with different `content` |

---

## Append Reply

Sends the next reply in a reply chain, or returns the original reply if `clientId` has already been used in this chain.

```
POST /api/reply-chains/{chainId}/replies
```

Requires a valid session cookie.

Only the poster and the replier of the chain can send replies, and they must take turns: the poster answers odd-numbered replies, and the replier answers even-numbered ones.

### Request

| Field | Type | Required | Rules |
| - | - | - | - |
| `content` | string | Yes | 1 ~ 300 characters after stripping surrounding whitespace |
| `clientId` | UUID | Yes | Generate a new one for each reply, and reuse it when retrying |

```json
{
  "content": "Thank you!",
  "clientId": "0190f3b5-3d4e-7f5a-8b6c-7d8e9f0a1b23"
}
```

### Response

`201 Created`

| Field | Type | Description |
| - | - | - |
| `chainId` | UUID | |
| `seq` | number | Position of the reply in the chain, starting at `1` |
| `fromPoster` | boolean | Whether the poster sent the reply |
| `content` | string | |
| `createdAt` | timestamp | ISO-8601, UTC |

```json
{
  "chainId": "0190f3b4-2c3d-7e4f-8a5b-6c7d8e9f0a12",
  "seq": 2,
  "fromPoster": true,
  "content": "Thank you!",
  "createdAt": "2026-11-04T11:02:00Z"
}
```

Retrying with the same `clientId` and `content` returns the original reply with the same status, even if it is no longer the caller's turn. Retries stop working once the post expires.

### Errors

| Code | Status | When |
| - | - | - |
| `VALIDATION_FAILED` | `400` | `chainId` is not a valid UUID, or a field is missing or invalid |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |
| `POST_NOT_FOUND` | `404` | The post of the chain has expired |
| `REPLY_CHAIN_NOT_FOUND` | `404` | The chain doesn't exist, or the caller isn't part of it |
| `REPLY_OUT_OF_TURN` | `409` | It's the other side's turn to reply |
| `REPLY_CLIENT_ID_CONFLICT` | `409` | `clientId` was already used in this chain with different `content` |

---

## Get Reply Chains on My Post

Returns all reply chains on one of the current user's posts, including posts that have expired.

```
GET /api/posts/{postId}/reply-chains
```

Requires a valid session cookie.

### Response

`200 OK`

An array of reply chains, latest activity first. Empty if the post has no reply chains. At most 1000 chains are returned.

Each item has the following fields:

| Field | Type | Description |
| - | - | - |
| `id` | UUID | |
| `lastSeq` | number | `seq` of the latest reply in the chain |
| `hasUnread` | boolean | Whether the chain has a reply the user hasn't read |
| `isMyTurn` | boolean | Whether the user is the one to reply next |
| `replierName` | string or null | The user's nickname for the replier, or the replier's display name if no nickname is set. `null` unless the replier is the user's friend and the post has `revealToFriends` enabled |
| `lastReplyContent` | string | Content of the latest reply |
| `lastReplyAt` | timestamp | ISO-8601, UTC. When the latest reply was sent |

```json
[
  {
    "id": "0190f3b4-2c3d-7e4f-8a5b-6c7d8e9f0a12",
    "lastSeq": 3,
    "hasUnread": true,
    "isMyTurn": true,
    "replierName": null,
    "lastReplyContent": "Where was this taken?",
    "lastReplyAt": "2026-11-04T12:40:00Z"
  }
]
```

### Errors

| Code | Status | When |
| - | - | - |
| `VALIDATION_FAILED` | `400` | `postId` is not a valid UUID |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |
| `NOT_MY_POST` | `403` | The post belongs to another user |
| `POST_NOT_FOUND` | `404` | The post doesn't exist |

---

## Get Reply Inbox

Returns the reply chains the current user started on other users' posts where it's the user's turn to reply.

```
GET /api/me/reply-inbox
```

Requires a valid session cookie.

A chain appears here once the poster answers, and leaves after the user replies. Chains on expired posts never appear.

### Response

`200 OK`

An array of reply chains, latest activity first. Empty if nothing is waiting for the user. At most 1000 chains are returned.

Each item has the following fields:

| Field | Type | Description |
| - | - | - |
| `id` | UUID | |
| `lastSeq` | number | `seq` of the latest reply in the chain |
| `hasUnread` | boolean | Whether the chain has a reply the user hasn't read |
| `postContent` | string | Content of the post the chain is on |
| `posterName` | string or null | The user's nickname for the poster, or the poster's display name if no nickname is set. `null` unless the poster is the user's friend and the post has `revealToFriends` enabled |
| `postExpiresAt` | timestamp | ISO-8601, UTC. When the post expires and the chain closes |
| `lastReplyContent` | string | Content of the latest reply |
| `lastReplyAt` | timestamp | ISO-8601, UTC. When the latest reply was sent |

```json
[
  {
    "id": "0190f3b4-2c3d-7e4f-8a5b-6c7d8e9f0a12",
    "lastSeq": 2,
    "hasUnread": true,
    "postContent": "Hello!",
    "posterName": "Minji",
    "postExpiresAt": "2026-11-05T09:30:00Z",
    "lastReplyContent": "Thank you!",
    "lastReplyAt": "2026-11-04T11:02:00Z"
  }
]
```

### Errors

| Code | Status | When |
| - | - | - |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |