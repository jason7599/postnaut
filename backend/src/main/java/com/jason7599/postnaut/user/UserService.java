package com.jason7599.postnaut.user;

import com.jason7599.postnaut.auth.UserLoginRequest;
import com.jason7599.postnaut.auth.UserRegisterRequest;
import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.error.ApiException;
import com.jason7599.postnaut.error.DbConstraints;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FriendCodeGenerator friendCodeGenerator;

    // NEVER: each save() must run in its own transaction so the friend code retry can work
    // In a caller's transaction, a failed save would doom the whole transaction, even if a
    // retry with a new friend code succeeds.
    @Transactional(propagation = Propagation.NEVER)
    public UUID register(UserRegisterRequest request) {
        String passwordHash = passwordEncoder.encode(request.password());

        final int MAX_TRIES = 5;

        for (int t = 0; t < MAX_TRIES; t++) {
            try {
                return userRepository.save(
                    new UserEntity(
                        request.username(),
                        request.displayName(),
                        passwordHash,
                        friendCodeGenerator.generate()
                    )
                ).getId();
            } catch (DataIntegrityViolationException e) {
                String violation = DbConstraints.getViolation(e).orElseThrow(() -> e);

                // Unlikely friend code collision, retry
                if (violation.equals(UserConstraints.UQ_FRIEND_CODE)) {
                    continue;
                }

                if (violation.equals(UserConstraints.UQ_USERNAME)) {
                    throw new ApiException(ApiErrorCode.USERNAME_TAKEN);
                }

                throw e;
            }
        }

        throw new IllegalStateException("Failed to generate unique friend code");
    }

    public UUID authenticate(UserLoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(ApiErrorCode.BAD_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(ApiErrorCode.BAD_CREDENTIALS);
        }

        return user.getId();
    }

    /** Lock by user id. Doesn't contend with FK calls. Must be called within a transaction */
    @Transactional(propagation = Propagation.MANDATORY)
    public void lock(UUID userId) {
        userRepository.lockById(userId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    }
}
