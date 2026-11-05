package com.jason7599.postnaut.post;

import lombok.Getter;

@Getter
public enum PostReaction {
    DISLIKE(-1),
    NONE(0),
    LIKE(1),
    ;

    private final int reactionvalue;

    PostReaction(int value) { this.reactionvalue = value; }
}
