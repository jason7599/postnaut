package com.jason7599.postnaut.reply;

import java.util.UUID;

record ReplyChain(
    UUID id,
    UUID postId,
    UUID replierId,
    int lastSeq,
    boolean isPosterTurn,
    int posterLastReadSeq,
    int replierLastReadSeq
) {
}
