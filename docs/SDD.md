# Postnaut — Software Design Document

## Overview

Postnaut is an anonymous message-in-a-bottle app. Users post text or images that stay public for a lifespan they choose, and others swipe through them at random like flashcards. Strangers can like, dislike, or reply privately, and a reply can grow into a short anonymous back-and-forth with the poster.

Core loop: write a post → swipe through random posts → react or reply → come back to read and answer replies.

## Requirements

### Anonymity
No public usernames, profiles or post histories. Only the poster can access their full post history. Users still need an account for persistent data and moderation purposes, but which user uploaded which post is never shown to other users(with the exception of friends, see below).

### Posts
- **Content**: Either text and/or an image. No editing after posting.
- **Lifespan**: Users select a fixed lifespan between a minimum and maximum(exact values TBD) at post time, which is how long the post stays public. Once expired, other users will not see the post, nor be able to leave a reply or like/dislike the post. The poster however can access their post history.
- **Visibility**: By default, users can see the nickname of the poster if the poster is in their friend list. However, the poster can choose to be completely anonymous to override this behavior. 
- **Likes and Dislikes**: Users can leave likes and dislikes on a post. Like and dislike counts are public; who reacted is never shown.
- **Enable Replies**: Users can disable replies on a post, at creation time. No changing after posting. 

### Replies
Users can leave a text reply on a post. Each user has at most one reply thread per post. The replier's identity is never revealed to the poster, and only the poster sees the list of replies. The poster can remove replies.

### Reply Chain
The poster can answer a reply, and the replier can answer back, forming a private thread. Messages strictly alternate: neither side can send twice in a row. The replier always sends first. The thread closes when the post expires.

### Post Feed
Once the user scrolls pass a post, it counts as a view, and their feed will never show it again. Each post maintains a view count, which is public to everyone. The feed never shows a post by the viewing user. 

### Replied Posts
A separate menu lists the non-expired posts a user as replied to, with the reply chain.

### Friends
A user sends a friend request using the other user's friend code, plus an alias to recognize who is requesting. The receiver accepts or declines. After accepting, each side sets their own nickname for the other. Nicknames must be unique within that user's own friend list. The other person never sees them.

### Realtime Notifications
In-app, realtime notifications for:
- New replies on your posts
- New reply in reply threads you're part of (either as poster or replier)
- Friend requests