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
| `MAX_LIVE_POST_REACHED` | `409` | The user has reached the live post cap |