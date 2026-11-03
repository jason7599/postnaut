# Auth API

## Register

Creates an account and logs the user in.

```
POST /api/auth/register
```

### Request
| Field | Type | Rules |
| - | - | - |
| `username` | string | 3-32 chars, letters and digits only. Case-insensitive unique |
| `password` | string | 6-64 chars |
| `displayName` | string | 3-32 chars | 

### Response: `201 CREATED`
Sets the session cookie.

### Errors
| Code | Status | When |
| - | - | - |
| `VALIDATION_ERROR` | 400 | Any field breaks its rules |
| `USERNAME_TAKEN` | 409 | Username exists (any casing) |

## Login

```
POST /api/auth/login
```

### Request
| Field | Type | Rules |
| - | - | - |
| `username` | string | not blank |
| `password` | string | not blank |

### Response: `204 NO CONTENT`
Sets the session cookie

### Errors
| Code | Status | When |
| - | - | - |
| `VALIDATION_ERROR` | 400 | Any field breaks it rules |
| `BAD_CREDENTIALS` | 401 | Matching username password pair not found |

## Logout

```
POST /api/auth/logout
```

Removes this session only, doesn't log out other sessions of the user.

### Response: `204 NO CONTENT`