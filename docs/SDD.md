# Postnaut — Software Design Document

## Overview

Postnaut is an anonymous message-in-a-bottle app. Users post text or images that stay public for a lifespan they choose, and others swipe through them at random like flashcards. Strangers can like, dislike, or reply privately, and a reply can grow into a short anonymous back-and-forth with the poster.

Core loop: write a post → swipe through random posts → react or reply → come back to read and answer replies.

## Requirements

### Anonymity
No public usernames, profiles or post histories. Only the poster can access their full post history. Users still need an account for persistent data and moderation purposes, but which user uploaded which post is never shown to other users (with the exception of friends, see below).

### Posts
- **Content**: Either text and/or an image. No editing after posting.
- **Lifespan**: Users select a fixed lifespan between a minimum and maximum (maximum currently 7 days; exact values TBD) at post time, which is how long the post stays public. Once expired, other users will not see the post, nor be able to leave a reply or like/dislike the post. The poster however can access their post history.
- **Visibility**: By default, users can see the nickname of the poster if the poster is in their friend list. However, the poster can choose to be completely anonymous to override this behavior.
- **Likes and Dislikes**: Users can leave likes and dislikes on a post.
- **Enable Replies**: Users can disable replies on a post, at creation time. No changing after posting.

### Replies
Users can leave a text reply on a post. Each user has at most one reply thread per post. The replier's identity is never revealed to the poster, and only the poster sees the count and the list of replies. The poster can remove replies.

### Reply Chain
The poster can answer a reply, and the replier can answer back, forming a private thread. Messages strictly alternate: neither side can send twice in a row. The replier always sends first.

When the post expires, the thread closes:
- **Replier**: the thread disappears entirely.
- **Poster**: the thread stays readable (read-only) in their post history.

### Reply Chains on My Post (poster view)
For each of their posts, the poster sees the full list of reply chains on it, live or expired.
- **Order**: Latest activity first (time of the last reply in the chain), with the chain ID as a tiebreaker.
- **Each item shows**: the last reply and its time, whether it's the poster's turn, unread state, and the replier's nickname where visibility rules allow (see Friends).
- **Not paginated**: The number of chains per post is naturally bounded (one per replier, limited lifespan), so the whole list is returned at once, with a hard server-side cap as a safety net.
- **Reordering**: A chain that gets new activity moves to the top on the next refresh, not while the list is on screen.
- The post itself is returned once alongside the list, not repeated on every item.

### Reply Inbox (replier view)
A separate menu shows the reply chains that are waiting on the user as replier: chains on non-expired posts where it is the replier's turn.
- After the replier sends a message, the chain leaves their inbox. It comes back, with the full thread history, once the poster answers.
- Chains on expired posts never appear.
- Not paginated, for the same reason as above (only live posts count); hard server-side cap.

### Post Feed
Once the user scrolls past a post, it counts as a view, and their feed will never show it again. The feed never shows a post by the viewing user.

### Friends
A user sends a friend request using the other user's friend code. The receiver accepts or declines. After accepting, each side can set their own nickname for the other. Nicknames must be unique within that user's own friend list. The other person never sees them. If no nickname is set, the friend's display name is shown instead.

### Realtime Notifications
In-app, realtime notifications for:
- New replies on your posts
- New reply in reply threads you're part of (either as poster or replier)
- Friend requests