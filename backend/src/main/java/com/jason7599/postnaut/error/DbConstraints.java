package com.jason7599.postnaut.error;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DbConstraints {

    public static Optional<String> getViolation(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                return Optional.ofNullable(cve.getConstraintName());
            }
        }
        return Optional.empty();
    }

    public static boolean isViolation(Throwable e, String constraintName) {
        return getViolation(e).filter(constraintName::equals).isPresent();
    }
}
