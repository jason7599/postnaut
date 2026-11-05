# Post API

## Create Post

Creates a post, or returns the original post if `clientId` has already been used by this user.

```
POST /api/posts
```

Requires a valid session cookie.

### Request

| Field | Type | Required | Rules |
| - | - | - | - |
| `content` | string | Yes | 1 ~ 500 characters after stripping surrounding whitespace |
| `revealToFriends` | boolean | Yes | |
| `replyEnabled` | boolean | Yes | |
| `clientId` | UUID | Yes | Generate a new one for each post, and reuse it when retrying |
| `lifespan` | string | Yes | ISO-8601 duration: `PT1H`, `PT6H`, `PT12H`, `P1D`, `P3D`, `P5D`, or `P7D` |

```json
{
  "content": "Hello!",
  "revealToFriends": true,
  "replyEnabled": true,
  "clientId": "0190f3a2-7c41-7b1e-9d55-2f6c1a8e4b10",
  "lifespan": "P1D"
}
```

### Response

`201 Created`

| Field | Type | Description |
| - | - | - |
| `id` | UUID | |
| `content` | string | |
| `revealToFriends` | boolean | |
| `replyEnabled` | boolean | |
| `createdAt` | timestamp | ISO-8601, UTC |
| `expiresAt` | timestamp | ISO-8601, UTC |

```json
{
  "id": "0190f3a2-9e10-7c3d-8a21-6d4b0f7e2c55",
  "content": "Hello!",
  "revealToFriends": true,
  "replyEnabled": true,
  "createdAt": "2026-11-04T09:30:00Z",
  "expiresAt": "2026-11-05T09:30:00Z"
}
```

Retrying with the same `clientId` returns the original post with the same status, and any changes to the other fields are ignored. Retries still work when the user is at the live post cap.

### Errors

| Code | Status | When |
| - | - | - |
| `VALIDATION_FAILED` | `400` | A field is missing or invalid |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |
| `MAX_LIVE_POST_REACHED` | `429` | The user has reached the live post cap |

---

## Get My Posts

Returns a page of the current user's posts, newest first.

```
GET /api/posts/mine
```

Requires a valid session cookie.

### Query Parameters

| Parameter | Type | Required | Rules |
| - | - | - | - |
| `cursor` | UUID | No | `id` of the last post from the previous page. Omit to get the first page |
| `filter` | string | No | `ALL`, `LIVE`, or `EXPIRED`. Defaults to `ALL` |

```
GET /api/posts/mine?filter=LIVE&cursor=0190f3a2-9e10-7c3d-8a21-6d4b0f7e2c55
```

### Response

`200 OK`

| Field | Type | Description |
| - | - | - |
| `items` | array | Posts on this page, newest first. Empty if no posts match |
| `hasMore` | boolean | Whether more posts are available after this page |

Each item in `items` has the following fields:

| Field | Type | Description |
| - | - | - |
| `id` | UUID | |
| `content` | string | |
| `revealToFriends` | boolean | |
| `replyEnabled` | boolean | |
| `createdAt` | timestamp | ISO-8601, UTC |
| `expiresAt` | timestamp | ISO-8601, UTC |
| `viewCount` | number | Total views |
| `likeCount` | number | Total likes |
| `dislikeCount` | number | Total dislikes |
| `replyChainCount` | number | Total reply chains |
| `unreadChainCount` | number | Reply chains with messages the user hasn't read |
| `isExpired` | boolean | Whether the post has expired |

```json
{
  "items": [
    {
      "id": "0190f3a2-9e10-7c3d-8a21-6d4b0f7e2c55",
      "content": "Hello!",
      "revealToFriends": true,
      "replyEnabled": true,
      "createdAt": "2026-11-04T09:30:00Z",
      "expiresAt": "2026-11-05T09:30:00Z",
      "viewCount": 42,
      "likeCount": 12,
      "dislikeCount": 1,
      "replyChainCount": 3,
      "unreadChainCount": 1,
      "isExpired": false
    }
  ],
  "hasMore": false
}
```

When `hasMore` is `true`, get the next page by passing the `id` of the last item as `cursor`. Keep the same `filter` across pages.

### Errors

| Code | Status | When |
| - | - | - |
| `VALIDATION_FAILED` | `400` | `cursor` or `filter` is invalid |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |

---

## Get Random Post

Returns a random live post that the current user hasn't viewed yet.

```
GET /api/posts/random
```

Requires a valid session cookie.

The result never includes the user's own posts, or posts from friends that have `revealToFriends` enabled.

Fetching a post doesn't record a view, so the same post may be returned again until a view is recorded.

### Response

`200 OK`

| Field | Type | Description |
| - | - | - |
| `id` | UUID | |
| `content` | string | |
| `replyEnabled` | boolean | |
| `createdAt` | timestamp | ISO-8601, UTC |
| `expiresAt` | timestamp | ISO-8601, UTC |

```json
{
  "id": "0190f3a2-9e10-7c3d-8a21-6d4b0f7e2c55",
  "content": "Hello!",
  "replyEnabled": true,
  "createdAt": "2026-11-04T09:30:00Z",
  "expiresAt": "2026-11-05T09:30:00Z"
}
```

`204 No Content`

There are no posts left to show. The response has no body.

### Errors

| Code | Status | When |
| - | - | - |
| `UNAUTHORIZED` | `401` | The session is missing, invalid, or expired |

---

## Get My Posts

Returns the caller's posts, newest first, including expired posts.

```
GET /api/posts/mine
```

Requires a valid session cookie.

### Query Parameters

| Field | Type | Required | Rules |
| - | - | - | - |
| `cursor` | UUID | No | `nextCursor` from the previous page. Omit for the first page |
| `limit` | integer | No | 1 ~ 50. Defaults to 20 |
| `status` | string | No | `ALL`, `LIVE`, or `EXPIRED`. Defaults to `ALL` |

### Response

`200 OK`

| Field | Type | Description |
| - | - | - |
| `items` | array | Posts in this page |
| `nextCursor` | UUID or null | Cursor for the next page. `null` on the last page |

Each item contains:

| Field | Type | Description |
| - | - | - |
| `id` | UUID | |
| `content` | string | |
| `revealToFriends` | boolean | |
| `replyEnabled` | boolean | |
| `createdAt` | timestamp | ISO-8601, UTC |
| `expiresAt` | timestamp | ISO-8601, UTC |
| `viewCount` | integer | |
| `likeCount` | integer | |
| `dislikeCount` | integer | |
| `chainCount` | integer | Number of reply chains |
| `unreadChainCount` | integer | Chains with a reply the poster hasn't read |

```json
{
  "items": [
    {
      "id": "0190f3a2-9e10-7c3d-8a21-6d4b0f7e2c55",
      "content": "Hello!",
      "revealToFriends": true,
      "replyEnabled": true,
      "createdAt": "2026-11-04T09:30:00Z",
      "expiresAt": "2026-11-05T09:30:00Z",
      "viewCount": 12,
      "likeCount": 4,
      "dislikeCount": 1,
      "chainCount": 2,
      "unreadChainCount": 1
    }
  ],
  "nextCursor": "0190f3a2-9e10-7c3d-8a21-6d4b0f7e2c55"
}
```

### Errors

| Status | Code | When |
| - | - | - |
| `400` | `VALIDATION_FAILED` | `cursor`, `limit`, or `status` is invalid |
| `401` | `UNAUTHORIZED` | The session is missing, invalid, or expired |

## Record View

Records that the caller viewed a post. Repeating this request has no further effect.

```
PUT /api/posts/{postId}/view
```

Requires a valid session cookie.

### Response

`204 No Content`

### Errors

| Status | Code | When |
| - | - | - |
| `400` | `VALIDATION_FAILED` | `postId` is not a valid UUID |
| `401` | `UNAUTHORIZED` | The session is missing, invalid, or expired |
| `403` | `CANNOT_INTERACT_WITH_OWN_POST` | The post belongs to the caller |
| `404` | `POST_NOT_FOUND` | The post doesn't exist or has expired |

---

## Set Reaction

Sets the caller's reaction to a post, and records a view if there isn't one yet. Repeating this request has no further effect.

```
PUT /api/posts/{postId}/reaction
```

Requires a valid session cookie.

### Request

| Field | Type | Required | Rules |
| - | - | - | - |
| `reaction` | string | Yes | `LIKE`, `DISLIKE`, or `NONE`. `NONE` removes the reaction |

```json
{
  "reaction": "LIKE"
}
```

### Response

`204 No Content`

### Errors

| Status | Code | When |
| - | - | - |
| `400` | `VALIDATION_FAILED` | `postId` or `reaction` is missing or invalid |
| `401` | `UNAUTHORIZED` | The session is missing, invalid, or expired |
| `403` | `CANNOT_INTERACT_WITH_OWN_POST` | The post belongs to the caller |
| `404` | `POST_NOT_FOUND` | The post doesn't exist or has expired |