package com.bestorigin.monolith.auth.domain;

import java.util.Optional;

public interface AuthSessionRepository {

    AuthSessionSnapshot save(AuthSessionSnapshot snapshot);

    Optional<AuthSessionSnapshot> findByToken(String token);
}
