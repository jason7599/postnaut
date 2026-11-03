package com.jason7599.postnaut.user;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
class FriendCodeGenerator {
    /**
     * Crockford Base32 algo. Digits + Uppercase letters minus I, L, O, U
     * With length = 8 it still has around 1.1 trillion codes, so fine for now.
     */

    private static final char[] ALPHABETS;
    static {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(i);
        for (char c = 'A'; c <= 'Z'; c++) {
            switch (c) {
                case 'I', 'L', 'O', 'U': break;
                default: sb.append(c);
            }
        }
        ALPHABETS = sb.toString().toCharArray();
    }

    private static final int LENGTH = 8;

    private final SecureRandom RANDOM = new SecureRandom();

    String generate() {
        char[] code = new char[LENGTH];
        for (int i = 0; i < LENGTH; i++) {
            code[i] = ALPHABETS[RANDOM.nextInt(ALPHABETS.length)];
        }
        return new String(code);
    }
}
