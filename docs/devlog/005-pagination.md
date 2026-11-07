# Pagination

I hate the pagination boilerplate bullcrap. Look at how ugly this is getting

```java
@Query(value = MY_POST_SELECT + """
    WHERE p.poster_id = :userId
        AND id < :cursor
        AND (
            (:includeLive AND p.expires_at > NOW())
            OR (:includeExpired AND p.expires_at <= NOW())
        )
    ORDER BY id DESC
    LIMIT :limit
""", nativeQuery = true)
List<MyPostResponse> getUserPostsNewestFirst(UUID userId, UUID cursor, boolean includeLive, boolean includeExpired, int limit);

@Query(value = MY_POST_SELECT + """
    WHERE p.poster_id = :userId
        AND id > :cursor
        AND (
            (:includeLive AND p.expires_at > NOW())
            OR (:includeExpired AND p.expires_at <= NOW())
        )
    ORDER BY id ASC
    LIMIT :limit
""", nativeQuery = true)
List<MyPostResponse> getUserPostsOldestFirst(UUID userId, UUID cursor, boolean includeLive, boolean includeExpired, int limit);

default List<MyPostResponse> getUserPosts(UUID userId, UUID cursor, boolean includeLive, boolean includeExpired, boolean newestFirst, int limit) {
    return newestFirst
            ? getUserPostsNewestFirst(userId, cursor, includeLive, includeExpired, limit)
            : getUserPostsOldestFirst(userId, cursor, includeLive, includeExpired, limit);
}
```

And then in the service code:

```java
public PageResponse<MyPostResponse> getUserPosts(UUID userId, UUID cursor, PostStatusFilter status, boolean newestFirst) {
    // null cursor: initial fetch. Assign sentinel
    if (cursor == null) {
        cursor = newestFirst ? new UUID(-1L, -1L) : new UUID(0L, 0L);
    }

    List<MyPostResponse> rows = postRepository.getUserPosts(
            userId,
            cursor,
            status != PostStatusFilter.EXPIRED,
            status != PostStatusFilter.LIVE,
            newestFirst,
            MY_POSTS_PAGE_SIZE + 1
    );

    return PageResponse.toPage(
            rows,
            MY_POSTS_PAGE_SIZE,
            MyPostResponse::id
    );
}
```

Disgusting. And now that I'm gonna need something similar for listing reply chains, I set out to look for a better alternative.

I know Spring Data have some nice helpers for pagination, with a sort, direction, limit, etc all the nice stuff. But apparently that won't work for native queries. Bummer.

THEN I found out they do work for views! So getting on that. My code is about to get even sexier