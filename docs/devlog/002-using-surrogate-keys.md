# Surrogate Keys

I've always been a pretty strong proponent in minimalism when it comes to PKs, but finally I found a usecase where it is very reasonable to use one.

```sql
CREATE TABLE reply_chain (
    id          UUID NOT NULL DEFAULT uuidv7(),
    post_id     UUID NOT NULL,
    replier_id  UUID NOT NULL,
    ...

    CONSTRAINT pk_reply_chain PRIMARY KEY (id),
    CONSTRAINT fk_reply_chain_post_id FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_reply_chain_replier_id FOREIGN KEY (replier_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_reply_chain_post_replier UNIQUE (post_id, replier_id),

    ...
);
```
Here, honestly (post_id, replier_id) is exactly enough. The pair can be the PK and everything will work just fine. 

But. A core part of my app is about anonymity. `post_id` being exposed is fine, but `replier_id` is not. The user shouldn't know who the replying user is. And while the user can't distinguish that information just by looking at the UUID, it still opens up the possibility of comparing 2 `replier_id`s and being able to tell if they are the same person or not. "Cross-post linkability".

So yes, I think this is a great place to use a surrogate key.